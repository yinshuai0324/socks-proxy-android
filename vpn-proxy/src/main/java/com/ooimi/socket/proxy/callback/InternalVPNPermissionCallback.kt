package com.ooimi.socket.proxy.callback

/**
 * @author 尹帅
 * @Description 内部VPN权限回掉
 * @createTime 2023年08月23日 15:39
 */
internal interface InternalVPNPermissionCallback {
    fun onSucceed()
    fun onFailure(resultCode: Int)
}