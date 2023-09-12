package com.ooimi.socket.proxy.jni;

public class SocksHelper {
    static {
        java.lang.System.loadLibrary("socks_helper");
    }

    public static native int sendFd(int fd, String sock);

    public static native void jniClose(int fd);
}
