package com.ooimi.socket.proxy.utils

import android.net.VpnService.Builder
import com.ooimi.socket.proxy.config.SocksProxyConfig

/**
 * @author 尹帅
 * @Description 路由配置工具类
 * @createTime 2023年08月22日 19:23
 */
class RouterUtils {
    companion object {

        /**
         * 路由配置
         */
        fun router(builder: Builder, config: SocksProxyConfig) {
            config.routers.forEach {
                val cidr = it.split("/")
                // Cannot handle 127.0.0.0/8
                if (cidr.size == 2 && !cidr[0].startsWith("127")) {
                    builder.addRoute(cidr[0], cidr[1].toInt())
                }
            }
        }
    }
}