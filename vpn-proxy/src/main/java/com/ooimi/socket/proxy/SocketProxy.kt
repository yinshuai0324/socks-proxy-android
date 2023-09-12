package com.ooimi.socket.proxy

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.ooimi.socket.proxy.callback.InternalVPNPermissionCallback
import com.ooimi.socket.proxy.callback.SocketProxyStatusCallback
import com.ooimi.socket.proxy.config.SocksProxyConfig
import com.ooimi.socket.proxy.page.VPNPermissionRequestActivity
import com.ooimi.socket.proxy.service.SocksVpnService

/**
 * @author 尹帅
 * @Description Socket代理
 * @createTime 2023年08月23日 11:27
 */
object SocketProxy {
    /**
     * 状态回掉
     */
    internal var statusCallback: SocketProxyStatusCallback? = null

    /**
     * 内部VPN权限回掉
     */
    internal var vpnPermissionCallback: InternalVPNPermissionCallback? = null

    /**
     * 启动代理服务
     */
    @JvmStatic
    fun startProxy(activity: Activity, config: SocksProxyConfig) {
        vpnPermissionCallback = object : InternalVPNPermissionCallback {
            override fun onSucceed() {
                //启动代理服务
                activity.startService(Intent(activity, SocksVpnService::class.java).apply {
                    putExtra("config", config)
                })
            }

            override fun onFailure(resultCode: Int) {
                statusCallback?.onException(Exception("code:${resultCode}"))
            }
        }
        //请求VPN权限
        VPNPermissionRequestActivity.requestPermission(activity)
    }

    /**
     * 停止代理
     */
    @JvmStatic
    fun stopProxy(context: Context) {
        context.startService(Intent(context, SocksVpnService::class.java).apply {
            putExtra("close", "1")
        })
    }

    /**
     * 设置监听
     */
    @JvmStatic
    fun setProxyStatusCallback(callback: SocketProxyStatusCallback) {
        statusCallback = callback
    }

    /**
     * 发送状态
     */
    internal fun sendStatus(context: Context, status: Int, msg: String? = "") {
        context.sendBroadcast(Intent().apply {
            //和清单文件里配置的一样
            action = "com.ooimi.socket.proxy.status"
            component = ComponentName(
                context.packageName,
                "com.ooimi.socket.proxy.broadcast.SocksProxyStatusBroadcast"
            )
            putExtra("status", status)
            if (!TextUtils.isEmpty(msg)) {
                putExtra("msg", msg)
            }
        })
    }

}