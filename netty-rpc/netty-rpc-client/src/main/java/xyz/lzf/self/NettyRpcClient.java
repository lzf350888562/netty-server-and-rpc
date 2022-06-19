package xyz.lzf.self;

import xyz.lzf.self.entity.User;
import xyz.lzf.self.proxy.NettyRpcServiceProxy;
import xyz.lzf.self.service.UserService;

public class NettyRpcClient {
    public static void main(String[] args) {
        NettyRpcServiceProxy nettyRpcServiceProxy = new NettyRpcServiceProxy();
        UserService userService = nettyRpcServiceProxy.getProxy(UserService.class);

        //---测试负载均衡
        User userByUserId1 = userService.getUserByUserId(10);
        System.out.println("从服务端得到的user为：" + userByUserId1);
        System.out.println(userByUserId1.getUserName());
        System.out.println(userByUserId1.getSex());
        System.out.println(userByUserId1.getId());


        User userByUserId2 = userService.getUserByUserId(10);
        System.out.println("从服务端得到的user为：" + userByUserId2);
        System.out.println(userByUserId2.getUserName());
        System.out.println(userByUserId2.getSex());
        System.out.println(userByUserId2.getId());


        User userByUserId3 = userService.getUserByUserId(10);
        System.out.println("从服务端得到的user为：" + userByUserId3);
        System.out.println(userByUserId3.getUserName());
        System.out.println(userByUserId3.getSex());
        System.out.println(userByUserId3.getId());

    }
}
