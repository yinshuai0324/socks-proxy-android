package com.ooimi.socket.proxy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import com.ooimi.socket.proxy.SocketProxy
import com.ooimi.socket.proxy.config.SocksProxyConfig
import com.ooimi.socket.proxy.jni.SocksHelper
import com.ooimi.socket.proxy.utils.ProxyHelper
import com.ooimi.socket.proxy.utils.RouterUtils
import java.util.Locale
import kotlin.random.Random

/**
 * @author 尹帅
 * @Description Vpn服务
 * @createTime 2023年08月22日 18:45
 */
internal class SocksVpnService : VpnService() {

    companion object {
        /**
         * tun0 ipv4地址
         */
        private const val TUN_IPV4_IP = "26.26.26.1"

        /**
         * tun0 ipv6地址
         */
        private const val TUN_IPV6_IP = "fdfe:dcba:9876::1"
    }

    private var mRunning: Boolean = false
    private var proxyConfig: SocksProxyConfig? = null
    private var mInterface: ParcelFileDescriptor? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        //获取配置文件
        proxyConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("config", SocksProxyConfig::class.java)
        } else {
            intent.getSerializableExtra("config") as SocksProxyConfig
        }
        if (intent.hasExtra("close")) {
            stopProxyService()
            return START_NOT_STICKY
        }
        if (mRunning) {
            return START_STICKY
        }
        //显示前台通知
        showNotification()
        //创建tun0网卡
        createTun0Dev()
        if (mInterface != null) {
            //启动代理服务
            startProxyService()
        }
        return START_STICKY
    }


    /**
     * 创建tun0设备
     */
    private fun createTun0Dev() {
        val builder = Builder()
        builder.setMtu(1500)
        //设置服务名称
        builder.setSession(proxyConfig?.notificationTitle ?: "")
        //设置tun0的ip地址
        builder.addAddress(TUN_IPV4_IP, 24)
        //设置dns服务器
        builder.addDnsServer("8.8.8.8")
        builder.addDnsServer("114.114.114.114")
        //如果支持ipv6 设置tun0 ipv6的ip地址
        if (proxyConfig?.supportIpV6 == true) {
            builder.addAddress(TUN_IPV6_IP, 126)
            //路由全部的流量
            builder.addRoute("::", 0)
        }
        //添加ipv4的路由
        proxyConfig?.let { RouterUtils.router(builder, it) }
        //添加默认DNS
        //注意，这个DNS只是一个存根
        //实际的DNS请求将通过pdsd重定向。
        builder.addRoute("8.8.8.8", 32)
        //配置绕过的App
        if ((proxyConfig?.appList?.size ?: 0) > 0) {
            //白名单模式还是黑名单模式
            if (proxyConfig?.passModel == 2) {
                //黑名单模式 appList里面的应用不会走代理
                proxyConfig?.appList?.forEach {
                    if (!TextUtils.isEmpty(it)) {
                        builder.addDisallowedApplication(it)
                    }
                }
            } else {
                //白名单模式 appList的应用才会走代理
                proxyConfig?.appList?.forEach {
                    if (!TextUtils.isEmpty(it)) {
                        builder.addAllowedApplication(it)
                    }
                }
            }
        }
        //开始创建tun0设备
        mInterface = builder.establish()
    }


    /**
     * 显示通知
     */
    private fun showNotification() {
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannelId = "SocksVpnService"
            val notificationChannelName = "ProxyService"
            val channel = NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                NotificationManager.IMPORTANCE_NONE
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            Notification.Builder(this, notificationChannelId)
        } else {
            Notification.Builder(this)
        }
        //创建通知
        val notificationId = Random.nextInt()
//        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        } else {
//            PendingIntent.FLAG_UPDATE_CURRENT
//        }
        //点击通知栏需要跳转的页面 这里暂时不需要
//        val contentIntent =
//            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), intentFlags)

        //配置通知参数
        builder.apply {
            setContentTitle(proxyConfig?.notificationTitle ?: "")
            setContentText(proxyConfig?.notificationDesc ?: "")
            setPriority(Notification.PRIORITY_DEFAULT)
            setSmallIcon(proxyConfig?.notificationIcon ?: 0)
//            setContentIntent(contentIntent)
        }

        startForeground(notificationId, builder.build())
    }

    /**
     * 启动代理服务
     */
    private fun startProxyService() {
        //先启动DNS守护进程
        ProxyHelper.makePdnsdConf(this, proxyConfig?.dnsServerAddress, proxyConfig?.dnsServerPort)
        val cmd = String.format(
            Locale.US,
            "%s/libpdnsd.so -c %s/pdnsd.conf",
            applicationInfo.nativeLibraryDir,
            filesDir
        )
        //执行启动Pdnsd服务的命令
        ProxyHelper.execCmd(cmd)
        //启动tun2socks
        val tun2socksCmd = StringBuilder()
        tun2socksCmd.append("${applicationInfo.nativeLibraryDir}/libtun2socks.so")
        tun2socksCmd.append(" --netif-ipaddr 26.26.26.2")
        tun2socksCmd.append(" --netif-netmask 255.255.255.0")
        tun2socksCmd.append(" --socks-server-addr ${proxyConfig?.socksServiceAddress}:${proxyConfig?.socksServicePort}")
        tun2socksCmd.append(" --tunfd ${mInterface?.fd}")
        tun2socksCmd.append(" --tunmtu 1500")
        tun2socksCmd.append(" --loglevel 3")
        tun2socksCmd.append(" --pid ${filesDir}/tun2socks.pid")
        tun2socksCmd.append(" --sock ${applicationInfo.dataDir}/sock_path")

        //用户名和密码（可选）
        if (!TextUtils.isEmpty(proxyConfig?.userName)) {
            tun2socksCmd.append(" --username ${proxyConfig?.userName}")
        }
        if (!TextUtils.isEmpty(proxyConfig?.password)) {
            tun2socksCmd.append(" --password ${proxyConfig?.password}")
        }

        //ipv6地址
        if (proxyConfig?.supportIpV6 == true) {
            tun2socksCmd.append(" --netif-ip6addr fdfe:dcba:9876::2")
        }

        //dns
        tun2socksCmd.append(" --dnsgw 26.26.26.1:8091")

        //udp代理服务器
        if (!TextUtils.isEmpty(proxyConfig?.udpgw)) {
            tun2socksCmd.append(" --udpgw-remote-server-addr ${proxyConfig?.udpgw}")
        }
        Log.i("===>>>", "cmd:${tun2socksCmd}")
        //启动tun2socks服务
        val result = ProxyHelper.execCmd(tun2socksCmd.toString())
        Log.i("===>>>", "启动tun2socks result:${result}")
        if (result != 0) {
            //启动失败
            SocketProxy.sendStatus(this, 2, "launch tun2socks service failure code:${result}")
            stopProxyService()
            return
        }
        //尝试通过套接字发送Fd。
        mInterface?.let {
            var i = 0
            while (i < 5) {
                val sendFdResult = SocksHelper.sendFd(it.fd, applicationInfo.dataDir + "/sock_path")
                if (sendFdResult != -1) {
                    mRunning = true
                    SocketProxy.sendStatus(this, 1)
                    return
                }
                i++
                try {
                    Thread.sleep(1000L * i)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
        //如果到了这里 一定是失败了
        SocketProxy.sendStatus(this, 2, "launch proxy failure")
        stopProxyService()
    }

    /**
     * 停止服务
     */
    private fun stopProxyService() {
        SocketProxy.sendStatus(this, 3)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        try {
            ProxyHelper.killPidFile("${filesDir}/tun2socks.pid")
            ProxyHelper.killPidFile("${filesDir}/pdnsd.pid")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mInterface?.let { SocksHelper.jniClose(it.fd) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopSelf()
    }

    override fun onRevoke() {
        super.onRevoke()
        stopProxyService()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProxyService()
    }
}