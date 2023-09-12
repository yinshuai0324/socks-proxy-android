package com.ooimi.socket.proxy.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ooimi.socket.proxy.SocketProxy

/**
 * @author 尹帅
 * @Description 因为代理服务运行在另一个进程，这里使用广播来同步代理的状态
 * @createTime 2023年08月23日 15:58
 */
class SocksProxyStatusBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("===>>>","收到广播")
        intent?.let {
            when (intent.getIntExtra("status", -1)) {

                1 -> {
                    //代理启动成功
                    SocketProxy.statusCallback?.onStart()
                }

                2 -> {
                    //代理启动异常
                    SocketProxy.statusCallback?.onException(
                        Exception(
                            intent.getStringExtra("msg") ?: ""
                        )
                    )
                }

                3 -> {
                    //代理已关闭
                    SocketProxy.statusCallback?.onStop()
                }

                else -> {}
            }
        }
    }
}