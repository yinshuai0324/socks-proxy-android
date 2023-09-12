package com.ooimi.socket.proxy.page

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import com.ooimi.socket.proxy.SocketProxy

/**
 * @author 尹帅
 * @Description 处理Vpn权限请求
 * @createTime 2023年08月23日 11:30
 */
class VPNPermissionRequestActivity : Activity() {


    companion object {
        fun requestPermission(activity: Activity) {
            activity.startActivity(Intent(activity, VPNPermissionRequestActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startVPN()
    }


    private fun startVPN() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 1000)
        } else {
            onActivityResult(1000, RESULT_OK, null)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1000) {
                SocketProxy.vpnPermissionCallback?.onSucceed()
            }
        } else {
            SocketProxy.vpnPermissionCallback?.onFailure(resultCode)
        }
        finish()
    }

}