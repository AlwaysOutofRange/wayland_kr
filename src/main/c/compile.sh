#!/bin/bash

 # Get JDK include path
 JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")
 echo "Using JAVA_HOME: $JAVA_HOME"

 # Compile the shared library
 gcc -shared -fPIC \
     -I"$JAVA_HOME/include" \
     -I"$JAVA_HOME/include/linux" \
     waylandkt.c \
     -o libwaylandkt.so

 # Move the library to a common location
 rm ../resources/native/libwaylandkt.so
 mv libwaylandkt.so ../resources/native/