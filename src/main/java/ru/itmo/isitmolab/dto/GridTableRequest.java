package ru.itmo.isitmolab.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridTableRequest {
    @NotNull
    public Integer startRow;
    @NotNull
    public Integer endRow;
    public List<GridTableSortModel> sortModel;
    public Map<String, Object> filterModel;
}
