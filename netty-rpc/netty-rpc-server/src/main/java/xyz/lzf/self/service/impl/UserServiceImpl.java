package xyz.lzf.self.service.impl;

import xyz.lzf.self.entity.User;
import xyz.lzf.self.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User getUserByUserId(Integer id) {
        User user = new User();
        user.setId(id);
        user.setUserName("testU");
        user.setSex(true);
        return user;
    }
}
