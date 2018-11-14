package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "asset_classification")
public class AssetClassification implements Serializable {
    @Id
    @Column(name = "classid")
    private int classid;
    @Column(name = "class_name")
    private String className;
    @Column(name = "subclass_name")
    private String subclassName;
    @Column(name = "subclass_description")
    private String subclassDescription;

    public int getClassid() {
        return classid;
    }
    public void setClassid(int classid) {
        this.classid = classid;
    }

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubclassName() {
        return subclassName;
    }
    public void setSubclassName(String subclassName) {
        this.subclassName = subclassName;
    }

    public String getSubclassDescription() {
        return subclassDescription;
    }
    public void setSubclassDescription(String subclassDescription) {
        this.subclassDescription = subclassDescription;
    }


}
