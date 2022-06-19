package xyz.lzf.self.entity;

import java.io.Serializable;

public class Dept implements Serializable {
    private Integer id;
    private Integer deptName;
    private String desc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeptName() {
        return deptName;
    }

    public void setDeptName(Integer deptName) {
        this.deptName = deptName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
