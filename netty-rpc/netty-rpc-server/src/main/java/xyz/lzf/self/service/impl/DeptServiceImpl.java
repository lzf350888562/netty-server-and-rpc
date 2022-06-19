package xyz.lzf.self.service.impl;

import xyz.lzf.self.entity.Dept;
import xyz.lzf.self.service.DeptService;

public class DeptServiceImpl implements DeptService {
    @Override
    public Dept getDeptById(Integer id) {
        Dept dept = new Dept();
        dept.setDeptName("测试");
        dept.setId(id);
        dept.setDesc("测试部门描述");
        return dept;
    }
}
