package com.example.notification.vo;

import lombok.Data;

@Data
public class OrgVo {
    private String iam_org_id;
    private String org_id;
    private String chub_id;
    private String org_name;

    private String status= "active";
    private String member_types="org";
    private String is_parent_allowed = "T";
    private String is_child_allowed = "T";

    public OrgVo(String iam_org_id, String org_id, String chub_id, String org_name) {
        this.iam_org_id = iam_org_id;
        this.org_id = org_id;
        this.chub_id = chub_id;
        this.org_name = org_name;
    }
}
