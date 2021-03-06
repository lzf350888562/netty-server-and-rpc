package xyz.lzf.self.core;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.lzf.self.balance.LoadBalance;
import xyz.lzf.self.balance.RoundLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

public class RegisterCenter{
    public static final Logger logger = LoggerFactory.getLogger(RegisterCenter.class);

    private CuratorFramework client;
    private LoadBalance loadBalance = new RoundLoadBalance();

    private static final String ROOT_PATH = "NettyRPC";
    private static final String ZK_ADDR = "120.79.67.1:2181";   // todo need to hide when push


    public RegisterCenter(){
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.builder().connectString(ZK_ADDR)
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
    }

    public void register(String serviceName, InetSocketAddress serverAddress){
        try {
            if(client.checkExists().forPath("/" + serviceName) == null){
                logger.info("first meet service {}. add thr persistent path", serviceName);
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            String path = "/" + serviceName +"/"+ getServiceAddress(serverAddress);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            logger.info("add a node [{}] to service {}", path, serviceName);
        } catch (Exception e) {
            logger.info("register error", e);
        }
    }

    /**
     * 从注册中心获取服务列表, 并进行负载均衡
     * @param serviceName 服务名
     * @return 服务地址
     */
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            List<String> strings = client.getChildren().forPath("/" + serviceName);
            String balance = loadBalance.balance(strings);
            logger.info("current loadBalance choose node : {}", balance);
            return parseAddress(balance);
        } catch (Exception e) {
            logger.info("service discovery error : {} . return null", serviceName);
            return null;
        }
    }

    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }

    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
}