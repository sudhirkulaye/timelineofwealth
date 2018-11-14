package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "subindustry")
public class Subindustry implements Serializable {
    @Id
    @Column(name = "subindustryid")
    private long subindustryid;
    @Column(name = "sector_name_display")
    private String sectorNameDisplay;
    @Column(name = "industry_name_display")
    private String industryNameDisplay;
    @Column(name = "sub_industry_name_display")
    private String subIndustryNameDisplay;

    public long getSubindustryid() {
        return subindustryid;
    }
    public void setSubindustryid(long subindustryid) {
        this.subindustryid = subindustryid;
    }

    public String getSectorNameDisplay() {
        return sectorNameDisplay;
    }
    public void setSectorNameDisplay(String sectorNameDisplay) {
        this.sectorNameDisplay = sectorNameDisplay;
    }

    public String getIndustryNameDisplay() {
        return industryNameDisplay;
    }
    public void setIndustryNameDisplay(String industryNameDisplay) {
        this.industryNameDisplay = industryNameDisplay;
    }

    public String getSubIndustryNameDisplay() {
        return subIndustryNameDisplay;
    }
    public void setSubIndustryNameDisplay(String subIndustryNameDisplay) {
        this.subIndustryNameDisplay = subIndustryNameDisplay;
    }
}
