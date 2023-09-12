package com.ooimi.socket.proxy.config

import java.io.Serializable

/**
 * @author 尹帅
 * @Description Socket代理的配置
 * @createTime 2023年08月22日 18:49
 */
class SocksProxyConfig : Serializable {
    /**
     * 通知的标题
     */
    var notificationTitle: String? = "socket proxy"

    /**
     * 通知的描述
     */
    var notificationDesc: String? = "socket proxy running ..."

    /**
     * 通知的Icon
     */
    var notificationIcon: Int? = -1

    /**
     * socks服务器地址
     */
    var socksServiceAddress: String? = "127.0.0.1"

    /**
     * socks服务器端口
     */
    var socksServicePort: Int? = 1080

    /**
     * 用户名（可选）
     */
    var userName: String? = ""

    /**
     * 密码（可选）
     */
    var password: String? = ""

    /**
     * 路由 把目标ip的请求全部转到tun0网卡
     */
    var routers: ArrayList<String> = arrayListOf("0.0.0.0/0")

    /**
     * dns服务器地址
     */
    var dnsServerAddress: String? = "8.8.8.8"

    /**
     * dns服务器端口
     */
    var dnsServerPort: Int? = 53

    /**
     * 绕过模式 1:白名单模式（appList内的应用走VPN） 2:黑名单模式（appList内的应用不走VPN）
     */
    var passModel: Int = 1

    /**
     * 需要配置的VPN 为空则全局代理
     */
    var appList: ArrayList<String> = arrayListOf()

    /**
     * 是否支持ipv6
     */
    var supportIpV6: Boolean = false

    /**
     * udp网关
     */
    var udpgw: String? = "127.0.0.1:7300"
}