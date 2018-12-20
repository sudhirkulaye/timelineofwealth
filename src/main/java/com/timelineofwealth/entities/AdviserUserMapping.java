package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "adviser_user_mapping")
public class AdviserUserMapping implements Serializable {
    @Embeddable
    public static class AdviserUserMappingKey implements Serializable {
        @Column(name = "adviserid")
        private String adviserid;
        @Column(name = "userid")
        private String userid;

        public String getAdviserid() {
            return adviserid;
        }
        public void setAdviserid(String adviserid) {
            this.adviserid = adviserid;
        }

        public String getUserid() {
            return userid;
        }
        public void setUserid(String userid) {
            this.userid = userid;
        }
    }

    @EmbeddedId
    private AdviserUserMapping.AdviserUserMappingKey key;
    @Column(name = "is_adviser_manager")
    private String isAdviserManager;

    public AdviserUserMappingKey getKey() {
        return key;
    }
    public void setKey(AdviserUserMappingKey key) {
        this.key = key;
    }

    public String getIsAdviserManager() {
        return isAdviserManager;
    }
    public void setIsAdviserManager(String isAdviserManager) {
        this.isAdviserManager = isAdviserManager;
    }

}
