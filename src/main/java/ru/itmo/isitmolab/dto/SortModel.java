package ru.itmo.isitmolab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortModel {
    public String colId;
    public String sort; // "asc" | "desc"
}
