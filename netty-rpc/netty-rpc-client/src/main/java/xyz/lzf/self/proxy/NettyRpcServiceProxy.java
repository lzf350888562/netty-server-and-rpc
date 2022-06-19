package xyz.lzf.self.proxy;

import xyz.lzf.self.core.RegisterCenter;
import xyz.lzf.self.http.Request;
import xyz.lzf.self.http.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * rpc接口代理
 */
public class NettyRpcServiceProxy implements InvocationHandler {

    private RegisterCenter registerCenter;

    public NettyRpcServiceProxy() {
        this.registerCenter = new RegisterCenter();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParams(args);
        request.setParamsTypes(method.getParameterTypes());
        Response response = sendRequest(request);
        if (response != null){
            return response.getData();
        }else{
            return null;
        }
    }

    /**
     * 利用本身作为JDK代理的参数, 生成代理类的接口
     */
    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }

    private Response sendRequest(Request request) {
        InetSocketAddress address = registerCenter.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        try {
            Socket socket = new Socket(host, port);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

            return (Response) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // log todo
            return null;
        }
    }
}
