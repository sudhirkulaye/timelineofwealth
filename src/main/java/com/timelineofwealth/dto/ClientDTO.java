package com.timelineofwealth.dto;

import java.io.Serializable;

public class ClientDTO implements Serializable {
    private String userid;
    private long memberid;
    private String relationship;
    private String memberName;
    private String firstName;
    private String lastName;
    private String moslCode;

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

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMoslCode() {
        return moslCode;
    }
    public void setMoslCode(String moslCode) {
        this.moslCode = moslCode;
    }
}
