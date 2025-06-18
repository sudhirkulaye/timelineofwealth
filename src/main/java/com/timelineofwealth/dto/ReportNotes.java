package com.timelineofwealth.dto;

import java.io.Serializable;

public class ReportNotes implements Serializable {
    private String ticker;
    private String date;
    private String documentSource;
    private String documentSection;
    private String infoCategory;
    private String infoSubCategory;
    private String information;

    // Getters and Setters
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDocumentSource() { return documentSource; }
    public void setDocumentSource(String documentSource) { this.documentSource = documentSource; }

    public String getDocumentSection() { return documentSection; }
    public void setDocumentSection(String documentSection) { this.documentSection = documentSection; }

    public String getInfoCategory() { return infoCategory; }
    public void setInfoCategory(String infoCategory) { this.infoCategory = infoCategory; }

    public String getInfoSubCategory() { return infoSubCategory; }
    public void setInfoSubCategory(String infoSubCategory) { this.infoSubCategory = infoSubCategory; }

    public String getInformation() { return information; }
    public void setInformation(String information) { this.information = information; }
}
