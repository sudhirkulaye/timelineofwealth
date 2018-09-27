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
    private long memberId;
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
    public long getMemberId() {
        return memberId;
    }
    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    @Column(name = "relationship")
    public String getRelationship() {
        return relationship;
    }
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
