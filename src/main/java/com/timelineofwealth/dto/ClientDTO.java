package com.timelineofwealth.dto;

import java.io.Serializable;

public class ClientDTO implements Serializable {
    private String userid;
    private long memberid;
    private String relationship;
    private String memberName;

    public String getUserid() {
        return userid;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }

    public long getMemberid() {
        return memberid;
    }
    public void setMemberid(long memberid) {
        this.memberid = memberid;
    }

    public String getRelationship() {
        return relationship;
    }
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getMemberName() {
        return memberName;
    }
    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

}
