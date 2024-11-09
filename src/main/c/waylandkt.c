#include <jni.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <fcntl.h>

JNIEXPORT jint JNICALL
Java_wayland_Native_openSocket(JNIEnv *env, jclass class, jstring path) {
    const char *socket_path = (*env)->GetStringUTFChars(env, path, NULL);
    if (!socket_path) {
        return -EINVAL;
    }

    int sock = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock < 0) {
        (*env)->ReleaseStringUTFChars(env, path, socket_path);
        return -errno;
    }

    struct sockaddr_un addr = {0};
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, socket_path, sizeof(addr.sun_path) - 1);

    (*env)->ReleaseStringUTFChars(env, path, socket_path);

    // Set non-blocking
    int flags = fcntl(sock, F_GETFL, 0);
    if (flags >= 0) {
        fcntl(sock, F_SETFL, flags | O_NONBLOCK);
    }

    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        if (errno != EINPROGRESS) {
            close(sock);
            return -errno;
        }
    }

    return sock;
}

JNIEXPORT jint JNICALL
Java_wayland_Native_closeSocket(JNIEnv *env, jclass class, jint fd) {
    return close(fd) == 0 ? 0 : -errno;
}

JNIEXPORT jint JNICALL
Java_wayland_Native_write(JNIEnv *env, jclass class, jint fd, jobject buffer, jint length) {
    void* buf = (*env)->GetDirectBufferAddress(env, buffer);
    if (!buf) {
        return -EINVAL;
    }

    ssize_t written = write(fd, buf, length);
    return written >= 0 ? written : -errno;
}

JNIEXPORT jint JNICALL
Java_wayland_Native_sendFd(JNIEnv *env, jclass class, jint sockFd, jobject buffer, jint length, jint fd) {
    struct msghdr msg = {0};
    struct iovec iov = {0};
    char ctrl_buf[CMSG_SPACE(sizeof(int))] = {0};

    // Get direct buffer address
    void* buf = (*env)->GetDirectBufferAddress(env, buffer);
    if (!buf) {
        return -EINVAL;
    }

    // Setup iovec with the message data
    iov.iov_base = buf;
    iov.iov_len = length;

    msg.msg_iov = &iov;
    msg.msg_iovlen = 1;
    msg.msg_control = ctrl_buf;
    msg.msg_controllen = sizeof(ctrl_buf);

    struct cmsghdr *cmsg = CMSG_FIRSTHDR(&msg);
    cmsg->cmsg_level = SOL_SOCKET;
    cmsg->cmsg_type = SCM_RIGHTS;
    cmsg->cmsg_len = CMSG_LEN(sizeof(int));
    *((int *)CMSG_DATA(cmsg)) = fd;
    msg.msg_controllen = CMSG_SPACE(sizeof(int));

    ssize_t result = sendmsg(sockFd, &msg, 0);
    return result >= 0 ? result : -errno;
}

JNIEXPORT jint JNICALL
Java_wayland_Native_getAvailableBytes(JNIEnv *env, jclass class, jint fd) {
    int bytes_available;
    int result = ioctl(fd, FIONREAD, &bytes_available);
    return result == 0 ? bytes_available : -errno;
}

JNIEXPORT jint JNICALL
Java_wayland_Native_readSocket(JNIEnv *env, jclass class, jint fd, jobject buffer, jint length) {
    void* buf = (*env)->GetDirectBufferAddress(env, buffer);
    if (!buf) {
        return -EINVAL;
    }

    ssize_t bytes_read = read(fd, buf, length);
    return bytes_read >= 0 ? bytes_read : -errno;
}