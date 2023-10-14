package com.timelineofwealth.dto;

import java.util.List;

public class BSEIndexDataResponse {
    private List<BSEIndexDataEntry> Table;

    public List<BSEIndexDataEntry> getTable() {
        return Table;
    }

    public void setTable(List<BSEIndexDataEntry> table) {
        Table = table;
    }
}
