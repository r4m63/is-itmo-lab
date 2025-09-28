package ru.itmo.isitmolab.util.gridtable;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class ColumnMapper {

    private static final Map<String, String> COL_MAP = new HashMap<>();

    static {
        COL_MAP.put("creationDate", "creationDateTime");
        COL_MAP.put("coordinates.x", "coordinates.x");
        COL_MAP.put("coordinates.y", "coordinates.y");
    }

    public static String normalize(String colId) {
        if (colId == null || colId.isBlank()) return "id";
        return COL_MAP.getOrDefault(colId, colId);
    }
}
