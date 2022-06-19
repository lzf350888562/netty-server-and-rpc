package xyz.lzf.self.route;

import xyz.lzf.self.core.RegisterCenter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServerRouter {

    private Map<String, Object> interfaceProvider;

    private RegisterCenter registerCenter;
    private String host;
    private int port;

    public ServerRouter(String host, int port){
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
        this.registerCenter = new RegisterCenter();
    }

    public void provideServiceInterface(Object service){
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for(Class clazz : interfaces){
            interfaceProvider.put(clazz.getName(),service);
            // 在注册中心注册服务
            registerCenter.register(clazz.getName(),new InetSocketAddress(host,port));
        }

    }

    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }
}
