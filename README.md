#### Android端的 Socks5代理 使用tun2socks实现



#### 使用方法 Kotlin

    ```
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
    ```

#### 使用方法 Java
    ```
        SocksProxyConfig proxyConfig = new SocksProxyConfig();
        //通知栏的标题
        proxyConfig.setNotificationTitle("socket代理");
        //通知栏的描述
        proxyConfig.setNotificationDesc("socket代理服务正在运行...");
        //通知栏的Icon
        proxyConfig.setNotificationIcon(R.drawable.ic_launcher_foreground);
        //socks服务器地址
        proxyConfig.setSocksServiceAddress("127.0.0.1");
        //socks服务器端口
        proxyConfig.setSocksServicePort(5200);
        //用户名（可选）
        if (!TextUtils.isEmpty(userName)) {
            proxyConfig.setUserName(userName);
        }
        //密码（可选）
        if (!TextUtils.isEmpty(password)) {
            proxyConfig.setPassword(password);
        }
        //路由 把目标ip的请求全部转到tun0网卡
        proxyConfig.setRouters(new ArrayList<>());
        //dns服务器地址
        proxyConfig.setDnsServerAddress("8.8.8.8");
        //dns服务器端口
        proxyConfig.setDnsServerPort(53);
        //绕过模式 1:白名单模式（appList内的应用走VPN） 2:黑名单模式（appList内的应用不走VPN）
        proxyConfig.setPassModel(1);
        ArrayList appList = new ArrayList();
        appList.add("com.socket.proxy");
        //需要配置的VPN的应用 为空则全局代理
        proxyConfig.setAppList(appList);
        //是否支持ipv6
        proxyConfig.setSupportIpV6(true);
        //启动代理
        SocketProxy.startProxy(this, proxyConfig);
    ```