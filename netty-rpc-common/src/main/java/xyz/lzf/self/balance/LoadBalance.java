package xyz.lzf.self.balance;

import java.util.List;

public interface LoadBalance {
    String balance(List<String> addressList);
}
