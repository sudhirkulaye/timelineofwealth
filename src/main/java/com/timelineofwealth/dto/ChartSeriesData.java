package com.timelineofwealth.dto;

import java.util.List;

public class ChartSeriesData {
    private String label;
    private List<Double> values;
    private String chartType;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }
}