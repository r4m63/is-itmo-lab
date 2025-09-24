package ru.itmo.isitmolab.grid;

import java.util.List;

public class GridResponse<T> {
    public List<T> rows;
    public Integer lastRow;

    public GridResponse(List<T> rows, int lastRow) {
        this.rows = rows;
        this.lastRow = lastRow;
    }
}