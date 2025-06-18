package com.timelineofwealth.dto;

import java.util.List;

public class CustomChartData {
    private String title;
    private List<String> labels;  // X-axis (dates)
    private List<Double> values;  // Y-axis (values)
    private String fieldName;  // <-- New field
    private Integer combinationId;


    // Getter and Setter for title
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter for labels
    public List<String> getLabels() {
        return labels;
    }
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    // Getter and Setter for values
    public List<Double> getValues() {
        return values;
    }
    public void setValues(List<Double> values) {
        this.values = values;
    }

    // Getter and Setter for fieldName
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    // Getter and Setter for combinationId
    public Integer getCombinationId() {
        return combinationId;
    }
    public void setCombinationId(Integer combinationId) {
        this.combinationId = combinationId;
    }
}
