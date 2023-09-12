package com.socket.proxy

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.blankj.utilcode.util.ToastUtils
import com.ooimi.socket.proxy.SocketProxy
import com.ooimi.socket.proxy.callback.SocketProxyStatusCallback
import com.ooimi.socket.proxy.config.SocksProxyConfig

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var serviceAddress: EditText? = null
    private var servicePort: EditText? = null
    private var userNameView: EditText? = null
    private var passwordView: EditText? = null
    private var startVpn: Button? = null
    private var stopVpn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        serviceAddress = findViewById(R.id.serviceAddress)
        servicePort = findViewById(R.id.servicePort)
        userNameView = findViewById(R.id.userName)
        passwordView = findViewById(R.id.password)

        startVpn = findViewById(R.id.startVpn)
        startVpn?.setOnClickListener(this)
        stopVpn = findViewById(R.id.stopVpn)
        stopVpn?.setOnClickListener(this)

    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.startVpn -> {
                if (TextUtils.isEmpty(serviceAddress?.text)) {
                    ToastUtils.showShort("请输入socks服务器地址")
                    return
                }
                if (TextUtils.isEmpty(servicePort?.text)) {
                    ToastUtils.showShort("请输入socks服务器端口")
                    return
                }

                SocketProxy.startProxy(this, SocksProxyConfig().apply {
                    //通知栏的标题
                    notificationTitle = "socket代理"
                    //通知栏的描述
                    notificationDesc = "socket代理服务正在运行..."
                    //通知栏的Icon
                    notificationIcon = R.drawable.ic_launcher_foreground
                    //socks服务器地址
                    socksServiceAddress = serviceAddress?.text?.toString() ?: ""
                    //socks服务器端口
                    socksServicePort = servicePort?.text?.toString()?.toInt() ?: 0
                    //用户名（可选）
                    if (!TextUtils.isEmpty(userNameView?.text)) {
                        userName = userNameView?.text?.toString()
                    }
                    //密码（可选）
                    if (!TextUtils.isEmpty(passwordView?.text)) {
                        password = passwordView?.text?.toString()
                    }
                    //路由 把目标ip的请求全部转到tun0网卡
                    routers = arrayListOf("0.0.0.0/0")
                    //dns服务器地址
                    dnsServerAddress = "8.8.8.8"
                    //dns服务器端口
                    dnsServerPort = 53
                    //绕过模式 1:白名单模式（appList内的应用走VPN） 2:黑名单模式（appList内的应用不走VPN）
                    passModel = 2
                    //需要配置的VPN的应用 为空则全局代理
                    appList = arrayListOf("com.socket.proxy")
                    //是否支持ipv6
                    supportIpV6 = true
                })

                SocketProxy.setProxyStatusCallback(object : SocketProxyStatusCallback() {
                    override fun onStart() {
                        Log.i("===>>>", "代理已启动...")
                        startVpn?.isEnabled = false
                        stopVpn?.isEnabled = true
                        ToastUtils.showShort("代理连接成功")
                    }

                    override fun onStop() {
                        Log.i("===>>>", "代理已停止...")
                        startVpn?.isEnabled = true
                        stopVpn?.isEnabled = false
                        ToastUtils.showShort("代理已关闭")
                    }
                })

            }

            R.id.stopVpn -> {
                SocketProxy.stopProxy(this)
            }
        }
    }
}