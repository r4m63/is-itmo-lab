package ru.itmo.isitmolab.grid;

import java.util.List;
import java.util.Map;

public class GridQueryRequest {
    public Map<String, Object> filterModel; // как прислал ag-Grid
    public List<Map<String, Object>> sortModel; // [{colId:"enginePower", sort:"asc"}]
}
