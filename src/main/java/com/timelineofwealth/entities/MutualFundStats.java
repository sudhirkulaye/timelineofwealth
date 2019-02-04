package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "mutual_fund_stats")
public class MutualFundStats implements Serializable {
    @Id
    @Column(name = "scheme_code")
    private long schemeCode;
    @Column(name = "scheme_name_part")
    private String schemeNamePart;
    @Column(name = "scheme_type")
    private String schemeType;
    @Column(name = "scheme_index")
    private String schemeIndex;
    @Column(name = "scheme_investment_style")
    private String schemeInvestmentStyle;
    @Column(name = "total_returns_y0")
    private BigDecimal totalReturnsY0;
    @Column(name = "total_returns_y1")
    private BigDecimal totalReturnsY1;
    @Column(name = "total_returns_y2")
    private BigDecimal totalReturnsY2;
    @Column(name = "total_returns_y3")
    private BigDecimal totalReturnsY3;
    @Column(name = "total_returns_y4")
    private BigDecimal totalReturnsY4;
    @Column(name = "total_returns_y5")
    private BigDecimal totalReturnsY5;
    @Column(name = "total_returns_y6")
    private BigDecimal totalReturnsY6;
    @Column(name = "total_returns_y7")
    private BigDecimal totalReturnsY7;
    @Column(name = "total_returns_y8")
    private BigDecimal totalReturnsY8;
    @Column(name = "total_returns_y9")
    private BigDecimal totalReturnsY9;
    @Column(name = "total_returns_y10")
    private BigDecimal totalReturnsY10;
    @Column(name = "trailing_return_1yr")
    private BigDecimal trailingReturn1yr;
    @Column(name = "trailing_return_3yr")
    private BigDecimal trailingReturn3yr;
    @Column(name = "trailing_return_5yr")
    private BigDecimal trailingReturn5yr;
    @Column(name = "trailing_return_10yr")
    private BigDecimal trailingReturn10yr;
    @Column(name = "quartile_rank_y1")
    private int quartileRanky1;
    @Column(name = "quartile_rank_y2")
    private int quartileRanky2;
    @Column(name = "quartile_rank_y3")
    private int quartileRanky3;
    @Column(name = "quartile_rank_y4")
    private int quartileRanky4;
    @Column(name = "quartile_rank_y5")
    private int quartileRanky5;
    @Column(name = "quartile_rank_y6")
    private int quartileRanky6;
    @Column(name = "quartile_rank_y7")
    private int quartileRanky7;
    @Column(name = "quartile_rank_y8")
    private int quartileRanky8;
    @Column(name = "quartile_rank_y9")
    private int quartileRanky9;
    @Column(name = "quartile_rank_y10")
    private int quartileRanky10;
    @Column(name = "quartile_rank_1yr")
    private int quartileRank1yr;
    @Column(name = "quartile_rank_3yr")
    private int quartileRank3yr;
    @Column(name = "quartile_rank_5yr")
    private int quartileRank5yr;
    @Column(name = "quartile_rank_10yr")
    private int quartileRank10yr;
    @Column(name = "sector_basic_materials")
    private BigDecimal sectorBasicMaterials;
    @Column(name = "sector_consumer_cyclical")
    private BigDecimal sectorConsumerCyclical;
    @Column(name = "sector_finacial_services")
    private BigDecimal sectorFinacialServices;
    @Column(name = "sector_industrial")
    private BigDecimal sectorIndustrial;
    @Column(name = "sector_technology")
    private BigDecimal sectorTechnology;
    @Column(name = "sector_consumer_defensive")
    private BigDecimal sectorConsumerDefensive;
    @Column(name = "sector_healthcare")
    private BigDecimal sectorHealthcare;
    @Column(name = "stock_1")
    private String stock1;
    @Column(name = "stock_2")
    private String stock2;
    @Column(name = "stock_3")
    private String stock3;
    @Column(name = "stock_4")
    private String stock4;

    public long getSchemeCode() {
        return schemeCode;
    }
    public void setSchemeCode(long schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeNamePart() {
        return schemeNamePart;
    }
    public void setSchemeNamePart(String schemeNamePart) {
        this.schemeNamePart = schemeNamePart;
    }

    public String getSchemeType() {
        return schemeType;
    }
    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }

    public String getSchemeIndex() {
        return schemeIndex;
    }
    public void setSchemeIndex(String schemeIndex) {
        this.schemeIndex = schemeIndex;
    }

    public String getSchemeInvestmentStyle() {
        return schemeInvestmentStyle;
    }
    public void setSchemeInvestmentStyle(String schemeInvestmentStyle) {
        this.schemeInvestmentStyle = schemeInvestmentStyle;
    }

    public BigDecimal getTotalReturnsY0() {
        return totalReturnsY0;
    }
    public void setTotalReturnsY0(BigDecimal totalReturnsY0) {
        this.totalReturnsY0 = totalReturnsY0;
    }

    public BigDecimal getTotalReturnsY1() {
        return totalReturnsY1;
    }
    public void setTotalReturnsY1(BigDecimal totalReturnsY1) {
        this.totalReturnsY1 = totalReturnsY1;
    }

    public BigDecimal getTotalReturnsY2() {
        return totalReturnsY2;
    }
    public void setTotalReturnsY2(BigDecimal totalReturnsY2) {
        this.totalReturnsY2 = totalReturnsY2;
    }

    public BigDecimal getTotalReturnsY3() {
        return totalReturnsY3;
    }
    public void setTotalReturnsY3(BigDecimal totalReturnsY3) {
        this.totalReturnsY3 = totalReturnsY3;
    }

    public BigDecimal getTotalReturnsY4() {
        return totalReturnsY4;
    }
    public void setTotalReturnsY4(BigDecimal totalReturnsY4) {
        this.totalReturnsY4 = totalReturnsY4;
    }

    public BigDecimal getTotalReturnsY5() {
        return totalReturnsY5;
    }
    public void setTotalReturnsY5(BigDecimal totalReturnsY5) {
        this.totalReturnsY5 = totalReturnsY5;
    }

    public BigDecimal getTotalReturnsY6() {
        return totalReturnsY6;
    }
    public void setTotalReturnsY6(BigDecimal totalReturnsY6) {
        this.totalReturnsY6 = totalReturnsY6;
    }

    public BigDecimal getTotalReturnsY7() {
        return totalReturnsY7;
    }
    public void setTotalReturnsY7(BigDecimal totalReturnsY7) {
        this.totalReturnsY7 = totalReturnsY7;
    }

    public BigDecimal getTotalReturnsY8() {
        return totalReturnsY8;
    }
    public void setTotalReturnsY8(BigDecimal totalReturnsY8) {
        this.totalReturnsY8 = totalReturnsY8;
    }

    public BigDecimal getTotalReturnsY9() {
        return totalReturnsY9;
    }
    public void setTotalReturnsY9(BigDecimal totalReturnsY9) {
        this.totalReturnsY9 = totalReturnsY9;
    }

    public BigDecimal getTotalReturnsY10() {
        return totalReturnsY10;
    }
    public void setTotalReturnsY10(BigDecimal totalReturnsY10) {
        this.totalReturnsY10 = totalReturnsY10;
    }

    public BigDecimal getTrailingReturn1yr() {
        return trailingReturn1yr;
    }
    public void setTrailingReturn1yr(BigDecimal trailingReturn1yr) {
        this.trailingReturn1yr = trailingReturn1yr;
    }

    public BigDecimal getTrailingReturn3yr() {
        return trailingReturn3yr;
    }
    public void setTrailingReturn3yr(BigDecimal trailingReturn3yr) {
        this.trailingReturn3yr = trailingReturn3yr;
    }

    public BigDecimal getTrailingReturn5yr() {
        return trailingReturn5yr;
    }
    public void setTrailingReturn5yr(BigDecimal trailingReturn5yr) {
        this.trailingReturn5yr = trailingReturn5yr;
    }

    public BigDecimal getTrailingReturn10yr() {
        return trailingReturn10yr;
    }
    public void setTrailingReturn10yr(BigDecimal trailingReturn10yr) {
        this.trailingReturn10yr = trailingReturn10yr;
    }

    public int getQuartileRanky1() {
        return quartileRanky1;
    }
    public void setQuartileRanky1(int quartileRanky1) {
        this.quartileRanky1 = quartileRanky1;
    }

    public int getQuartileRanky2() {
        return quartileRanky2;
    }
    public void setQuartileRanky2(int quartileRanky2) {
        this.quartileRanky2 = quartileRanky2;
    }

    public int getQuartileRanky3() {
        return quartileRanky3;
    }
    public void setQuartileRanky3(int quartileRanky3) {
        this.quartileRanky3 = quartileRanky3;
    }

    public int getQuartileRanky4() {
        return quartileRanky4;
    }
    public void setQuartileRanky4(int quartileRanky4) {
        this.quartileRanky4 = quartileRanky4;
    }

    public int getQuartileRanky5() {
        return quartileRanky5;
    }
    public void setQuartileRanky5(int quartileRanky5) {
        this.quartileRanky5 = quartileRanky5;
    }

    public int getQuartileRanky6() {
        return quartileRanky6;
    }
    public void setQuartileRanky6(int quartileRanky6) {
        this.quartileRanky6 = quartileRanky6;
    }

    public int getQuartileRanky7() {
        return quartileRanky7;
    }
    public void setQuartileRanky7(int quartileRanky7) {
        this.quartileRanky7 = quartileRanky7;
    }

    public int getQuartileRanky8() {
        return quartileRanky8;
    }
    public void setQuartileRanky8(int quartileRanky8) {
        this.quartileRanky8 = quartileRanky8;
    }

    public int getQuartileRanky9() {
        return quartileRanky9;
    }
    public void setQuartileRanky9(int quartileRanky9) {
        this.quartileRanky9 = quartileRanky9;
    }

    public int getQuartileRanky10() {
        return quartileRanky10;
    }
    public void setQuartileRanky10(int quartileRanky10) {
        this.quartileRanky10 = quartileRanky10;
    }

    public int getQuartileRank1yr() {
        return quartileRank1yr;
    }
    public void setQuartileRank1yr(int quartileRank1yr) {
        this.quartileRank1yr = quartileRank1yr;
    }

    public int getQuartileRank3yr() {
        return quartileRank3yr;
    }
    public void setQuartileRank3yr(int quartileRank3yr) {
        this.quartileRank3yr = quartileRank3yr;
    }

    public int getQuartileRank5yr() {
        return quartileRank5yr;
    }
    public void setQuartileRank5yr(int quartileRank5yr) {
        this.quartileRank5yr = quartileRank5yr;
    }

    public int getQuartileRank10yr() {
        return quartileRank10yr;
    }
    public void setQuartileRank10yr(int quartileRank10yr) {
        this.quartileRank10yr = quartileRank10yr;
    }

    public BigDecimal getSectorBasicMaterials() {
        return sectorBasicMaterials;
    }
    public void setSectorBasicMaterials(BigDecimal sectorBasicMaterials) {
        this.sectorBasicMaterials = sectorBasicMaterials;
    }

    public BigDecimal getSectorConsumerCyclical() {
        return sectorConsumerCyclical;
    }
    public void setSectorConsumerCyclical(BigDecimal sectorConsumerCyclical) {
        this.sectorConsumerCyclical = sectorConsumerCyclical;
    }

    public BigDecimal getSectorFinacialServices() {
        return sectorFinacialServices;
    }
    public void setSectorFinacialServices(BigDecimal sectorFinacialServices) {
        this.sectorFinacialServices = sectorFinacialServices;
    }

    public BigDecimal getSectorIndustrial() {
        return sectorIndustrial;
    }
    public void setSectorIndustrial(BigDecimal sectorIndustrial) {
        this.sectorIndustrial = sectorIndustrial;
    }

    public BigDecimal getSectorTechnology() {
        return sectorTechnology;
    }
    public void setSectorTechnology(BigDecimal sectorTechnology) {
        this.sectorTechnology = sectorTechnology;
    }

    public BigDecimal getSectorConsumerDefensive() {
        return sectorConsumerDefensive;
    }
    public void setSectorConsumerDefensive(BigDecimal sectorConsumerDefensive) {
        this.sectorConsumerDefensive = sectorConsumerDefensive;
    }

    public BigDecimal getSectorHealthcare() {
        return sectorHealthcare;
    }
    public void setSectorHealthcare(BigDecimal sectorHealthcare) {
        this.sectorHealthcare = sectorHealthcare;
    }

    public String getStock1() {
        return stock1;
    }
    public void setStock1(String stock1) {
        this.stock1 = stock1;
    }

    public String getStock2() {
        return stock2;
    }
    public void setStock2(String stock2) {
        this.stock2 = stock2;
    }

    public String getStock3() {
        return stock3;
    }
    public void setStock3(String stock3) {
        this.stock3 = stock3;
    }

    public String getStock4() {
        return stock4;
    }
    public void setStock4(String stock4) {
        this.stock4 = stock4;
    }
}
