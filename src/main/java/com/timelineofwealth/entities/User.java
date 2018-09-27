package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "user")
public class User implements Serializable {

    private String email; //email id
    private String password;
    private int active; //'1:Active Free, 2:Active Standard, 3:Active Premium, 4:Inactive, 5:Closed'
    private Date joiningDate;
    private String prefix;
    private String name;
    private String lastName;
    private String cellNo;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String state;
    private String pin;
    private Timestamp lastLoginTime;
    private String roleName; //ADMIN , END_USER, ADVISER

    public User(){}
    public User(User user){
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.active = user.getActive();
        this.joiningDate = user.getJoiningDate();
        this.prefix = user.getPrefix();
        this.name = user.getName();
        this.lastName = user.getLastName();
        this.cellNo = user.getCellNo();
        this.addressLine1 = user.getAddressLine1();
        this.addressLine2 = user.getAddressLine2();
        this.addressLine3 = user.getAddressLine3();
        this.city = user.getCity();
        this.state = user.getState();
        this.pin = user.getPin();
        this.lastLoginTime = user.getLastLoginTime();
        this.roleName = user.getRoleName();
    }

    @Id
    @Column(name = "email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "active")
    public int getActive() {
        return active;
    }
    public void setActive(int active) {
        this.active = active;
    }

    @Column(name = "joining_date")
    public Date getJoiningDate() {
        return joiningDate;
    }
    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }

    @Column(name = "prefix")
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "cellno")
    public String getCellNo() {
        return cellNo;
    }
    public void setCellNo(String cellNo) {
        this.cellNo = cellNo;
    }

    @Column(name = "add_line1")
    public String getAddressLine1() {
        return addressLine1;
    }
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    @Column(name = "add_line2")
    public String getAddressLine2() {
        return addressLine2;
    }
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    @Column(name = "add_line3")
    public String getAddressLine3() {
        return addressLine3;
    }
    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    @Column(name = "city")
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    @Column(name = "state")
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "pin")
    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }

    @Column(name = "last_login_time")
    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }
    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Column(name = "role_name")
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
