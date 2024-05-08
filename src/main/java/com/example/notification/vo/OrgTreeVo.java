package com.example.notification.vo;

import lombok.Data;

@Data
public class OrgTreeVo {
    private String org_id;

    public OrgTreeVo(String org_id) {
        this.org_id = org_id;
    }
}
