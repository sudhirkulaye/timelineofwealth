package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "composite_constituents")
public class CompositeConstituents  implements Serializable {

    @Embeddable
    public static class CompositeConstituentsKey implements Serializable {
        @Column(name = "compositeid")
        private long compositeid;
        @Column(name = "ticker")
        private String ticker;

        public long getCompositeid() {
            return compositeid;
        }
        public void setCompositeid(long compositeid) {
            this.compositeid = compositeid;
        }

        public String getTicker() {
            return ticker;
        }
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }
    }

    @EmbeddedId
    private CompositeConstituentsKey key;
    @Column(name = "short_name")
    private String shortName;
    @Column(name = "asset_classid")
    private long assetClassid;
    @Column(name = "subindustryid")
    private long subindustryid;
    @Column(name = "target_weight")
    private int targetWeight;
    @Column(name = "min_weight")
    private int minWeight;
    @Column(name = "max_weight")
    private int maxWeight;

    public CompositeConstituentsKey getKey() {
        return key;
    }
    public void setKey(CompositeConstituentsKey key) {
        this.key = key;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public long getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(long assetClassid) {
        this.assetClassid = assetClassid;
    }

    public long getSubindustryid() {
        return subindustryid;
    }
    public void setSubindustryid(long subindustryid) {
        this.subindustryid = subindustryid;
    }

    public int getTargetWeight() {
        return targetWeight;
    }
    public void setTargetWeight(int targetWeight) {
        this.targetWeight = targetWeight;
    }

    public int getMinWeight() {
        return minWeight;
    }
    public void setMinWeight(int minWeight) {
        this.minWeight = minWeight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }
    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }
}
