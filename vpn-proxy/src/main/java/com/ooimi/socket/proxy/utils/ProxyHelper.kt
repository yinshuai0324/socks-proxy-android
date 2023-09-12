package com.ooimi.socket.proxy.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.ooimi.socket.proxy.R
import com.ooimi.socket.proxy.SocketProxy
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @author 尹帅
 * @Description 代理帮助类
 * @createTime 2023年08月22日 19:35
 */
class ProxyHelper {

    companion object {
        /**
         * 执行cmd
         */
        fun execCmd(cmd: String?): Int {
            return try {
                Runtime.getRuntime().exec(cmd).waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
                SocketProxy.statusCallback?.onException(e)
                -1
            }
        }

        /**
         * 杀死进程
         */
        fun killPidFile(path: String?) {
            val pidFile = File(path ?: "")
            if (!pidFile.exists()) return
            val inputStream = try {
                FileInputStream(pidFile)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            val buf = ByteArray(512)
            val str = StringBuilder()
            var len: Int

            try {
                while (inputStream.read(buf, 0, 512).also { len = it } > 0) {
                    str.append(String(buf, 0, len))
                }
                inputStream.close()
            } catch (e: Exception) {
                inputStream.close()
                e.printStackTrace()
                return
            }

            try {
                val pidStr = str.toString().trim().replace("\n", "")
                if (!TextUtils.isEmpty(pidStr)) {
                    Runtime.getRuntime().exec("kill ${pidStr.toInt()}").waitFor()
                    if (!pidFile.delete()) {
                        Log.w("ProxyHelper", "failed to delete pid file")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 加入数据
         */
        fun join(list: List<String>?, separator: String?): String {
            return try {
                if (list.isNullOrEmpty()) return ""
                val ret = StringBuilder()
                list.forEach {
                    ret.append(it).append(separator)
                }
                ret.substring(0, ret.length - (separator?.length ?: 0))
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }


        /**
         * 创建pdnsd配置文件
         */
        fun makePdnsdConf(context: Context, dns: String?, port: Int?) {
            try {
                val conf = context.getString(R.string.pdnsd_conf)
                    .replace("{DIR}", context.filesDir.toString())
                    .replace("{IP}", dns ?: "")
                    .replace("{PORT}", "$port")

                val confFile = File("${context.filesDir}/pdnsd.conf")
                if (confFile.exists()) {
                    //文件如果存在 先删除
                    if (!confFile.delete()) {
                        Log.w("ProxyHelper", "failed to delete pdnsd.conf")
                    }
                }

                //写入配置
                val outputStream = FileOutputStream(confFile)
                outputStream.write(conf.toByteArray())
                outputStream.flush()
                outputStream.close()

                val cache = File("${context.filesDir}/pdnsd.cache")
                if (!cache.exists()) {
                    //创建缓存文件
                    if (!cache.createNewFile()) {
                        Log.w("ProxyHelper", "failed to create pdnsd.cache");
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}