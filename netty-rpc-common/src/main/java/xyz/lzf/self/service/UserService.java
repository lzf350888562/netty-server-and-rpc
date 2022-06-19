package xyz.lzf.self.service;

import xyz.lzf.self.entity.User;

/**
 * 公共rpc接口
 */
public interface UserService {
    User getUserByUserId(Integer id);
}
