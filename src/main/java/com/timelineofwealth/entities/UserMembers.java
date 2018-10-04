package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "user_members")
public class UserMembers implements Serializable {
    private String email;
    private long memberid;
    private String relationship;


    @Column(name = "email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Id
    @Column(name = "memberid")
    public long getMemberid() {
        return memberid;
    }
    public void setMemberid(long memberid) {
        this.memberid = memberid;
    }

    @Column(name = "relationship")
    public String getRelationship() {
        return relationship;
    }
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
