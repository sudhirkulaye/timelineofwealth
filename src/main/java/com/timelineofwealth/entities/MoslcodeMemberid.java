package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "moslcode_memberid")
public class MoslcodeMemberid implements Serializable {
    @Id
    @Column(name = "memberid")
    private long memberid;
    @Column(name = "moslcode")
    private String moslcode;

    public long getMemberid() {
        return memberid;
    }
    public void setMemberid(long memberid) {
        this.memberid = memberid;
    }

    public String getMoslcode() {
        return moslcode;
    }
    public void setMoslcode(String moslcode) {
        this.moslcode = moslcode;
    }
}
