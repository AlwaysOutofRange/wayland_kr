#include <jni.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>

JNIEXPORT jint JNICALL
Java_wayland_Native_sendFd(JNIEnv *env, jclass class, jint sockFd, jobject buffer, jint length, jint fd) {
    struct msghdr msg = {0};
    struct iovec iov[1];
    struct cmsghdr *cmsg;
    char ctrl_buf[CMSG_SPACE(sizeof(int))];

    // Get direct buffer address
    void* buf = (*env)->GetDirectBufferAddress(env, buffer);
    if (!buf) {
        return -1;
    }

    // Setup iovec with the message data
    iov[0].iov_base = buf;
    iov[0].iov_len = length;

    msg.msg_iov = iov;
    msg.msg_iovlen = 1;
    msg.msg_control = ctrl_buf;
    msg.msg_controllen = CMSG_SPACE(sizeof(int));

    // Setup control message for fd passing
    cmsg = CMSG_FIRSTHDR(&msg);
    cmsg->cmsg_level = SOL_SOCKET;
    cmsg->cmsg_type = SCM_RIGHTS;
    cmsg->cmsg_len = CMSG_LEN(sizeof(int));
    *(int *)CMSG_DATA(cmsg) = fd;

    // Send the message
    ssize_t result = sendmsg(sockFd, &msg, 0);
    if (result < 0) {
        return -errno;
    }

    return (jint)result;
}