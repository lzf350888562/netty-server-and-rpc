package xyz.lzf.self.balance;

import java.util.List;

public class RoundLoadBalance implements LoadBalance {
    private int currentIndex = -1;
    @Override
    public String balance(List<String> addressList) {
        currentIndex++;
        currentIndex = currentIndex % addressList.size();
        return addressList.get(currentIndex);
    }
}
