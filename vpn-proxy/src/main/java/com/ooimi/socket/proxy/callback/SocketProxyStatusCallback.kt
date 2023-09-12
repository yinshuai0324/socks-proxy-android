package com.ooimi.socket.proxy.callback

import java.lang.Exception

/**
 * @author 尹帅
 * @Description Socket代理状态监听
 * @createTime 2023年08月23日 11:44
 */
abstract class SocketProxyStatusCallback {
    abstract fun onStart()
    abstract fun onStop()
    fun onException(exception: Exception) {

    }
}