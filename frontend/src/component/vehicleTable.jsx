'use client';

import React, {useCallback, useMemo, useRef} from "react";
import {AgGridReact} from "ag-grid-react";
import {AllCommunityModule, colorSchemeDark, iconSetMaterial, ModuleRegistry, themeQuartz} from "ag-grid-community";
import {API_BASE} from "../../cfg.js";

ModuleRegistry.registerModules([AllCommunityModule]);

export const tableTheme = themeQuartz
    .withPart(iconSetMaterial)
    .withPart(colorSchemeDark)
    .withParams({
        backgroundColor: "#0f172a",
        foregroundColor: "#e5e7eb",
        headerBackgroundColor: "#111827",
        headerTextColor: "#e5e7eb",
        oddRowBackgroundColor: "#0b1326",
        accentColor: "#60a5fa",
        headerColumnResizeHandleColor: "#60a5fa",
        borderColor: "#1e293b",
        rowHoverColor: "#1f2937",
        selectedRowBackgroundColor: "#1e3a8a",
    });

export default function VehicleTable({onOpenEditVehicleModal, onReadyRefresh, onReadyControls}) {
    const gridApiRef = useRef(null);

    const colDefs = useMemo(() => ([
        {
            headerName: "ID",
            field: "id",
            width: 100,
            sortable: true,
            sort: "desc",
            filter: "agNumberColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Edit", filter: false, width: 90,
            cellRenderer: (p) => (
                <button
                    style={{
                        paddingInline: 15,
                        border: "none",
                        background: "#007bff",
                        color: "#fff",
                        borderRadius: 5,
                        cursor: "pointer"
                    }}
                    onClick={() => onOpenEditVehicleModal?.(p.data)}
                >
                    Edit
                </button>
            ),
        },
        {headerName: "Name", field: "name", sortable: true, filter: "agTextColumnFilter", floatingFilter: true},
        {
            headerName: "Type",
            field: "type",
            width: 160,
            sortable: true,
            filter: "agTextColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Fuel Type",
            field: "fuelType",
            width: 160,
            sortable: true,
            filter: "agTextColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Creation Date",
            field: "creationDate",
            width: 190,
            sortable: true,
            filter: "agDateColumnFilter",
            floatingFilter: true,
            valueFormatter: (p) => p.value ? new Date(p.value).toLocaleString() : "",
        },
        {
            headerName: "Coordinates",
            children: [
                {
                    headerName: "X",
                    colId: "coordinates.x",
                    valueGetter: (p) => p.data?.coordinates?.x,
                    width: 110,
                    sortable: true,
                    filter: "agNumberColumnFilter",
                    floatingFilter: true
                },
                {
                    headerName: "Y",
                    colId: "coordinates.y",
                    valueGetter: (p) => p.data?.coordinates?.y,
                    width: 110,
                    sortable: true,
                    filter: "agNumberColumnFilter",
                    floatingFilter: true
                },
            ],
        },
        {
            headerName: "Engine Power",
            field: "enginePower",
            width: 150,
            sortable: true,
            filter: "agNumberColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Wheels",
            field: "numberOfWheels",
            width: 130,
            sortable: true,
            filter: "agNumberColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Capacity",
            field: "capacity",
            width: 130,
            sortable: true,
            filter: "agNumberColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Distance Travelled",
            field: "distanceTravelled",
            width: 180,
            sortable: true,
            filter: "agNumberColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Fuel Consumption",
            field: "fuelConsumption",
            width: 170,
            sortable: true,
            filter: "agNumberColumnFilter",
            floatingFilter: true
        },
    ]), [onOpenEditVehicleModal]);

    const mapSortModel = (sm = []) => sm.map(s => ({colId: s.colId, sort: s.sort}));

    const makeDatasource = useCallback(() => ({
        getRows: async (params) => {
            try {
                const body = {
                    startRow: params.startRow,
                    endRow: params.endRow,
                    sortModel: mapSortModel(params.sortModel),
                    filterModel: params.filterModel || {},
                };

                const res = await fetch(`${API_BASE}/api/vehicle/query`, {
                    method: "POST",
                    credentials: "include",
                    headers: {"Content-Type": "application/json", "Accept": "application/json"},
                    body: JSON.stringify(body),
                });

                if (!res.ok) {
                    params.failCallback();
                    return;
                }
                const data = await res.json();
                params.successCallback(data.rows || [], data.lastRow ?? 0);
            } catch (e) {
                console.error(e);
                params.failCallback();
            }
        }
    }), []);

    const setDatasource = useCallback(() => {
        if (!gridApiRef.current) return;
        const ds = makeDatasource();
        gridApiRef.current.setGridOption('datasource', ds);
        gridApiRef.current.purgeInfiniteCache(); // первая загрузка
    }, [makeDatasource]);

    const exposeRefresh = useCallback(() => {
        onReadyRefresh?.(() => {
            if (!gridApiRef.current) return;
            gridApiRef.current.refreshInfiniteCache();
        });
    }, [onReadyRefresh]);

    const exposeControls = useCallback(() => {
        if (!onReadyControls) return;
        const api = gridApiRef.current;
        if (!api) return;

        onReadyControls({
            refresh: () => {
                api.refreshInfiniteCache();
            },

            clearFilters: () => {
                api.setFilterModel(null);
                api.onFilterChanged();
                api.purgeInfiniteCache();
                api.ensureIndexVisible(0);
            },

            // fuelConsumption > X
            applyFilterFuelGt: (raw) => {
                const x = Number.parseFloat(raw);
                if (!Number.isFinite(x)) return;

                api.setFilterModel({
                    fuelConsumption: {
                        filterType: "number",
                        type: "greaterThan",
                        filter: x,
                    },
                });
                api.onFilterChanged();
                api.purgeInfiniteCache();
                api.ensureIndexVisible(0);
            },

            applyFilterByType: (type) => {
                if (!type) return;
                api.setFilterModel({
                    type: {
                        filterType: "text",
                        type: "equals",
                        filter: String(type),
                    },
                });
                api.onFilterChanged();
                api.purgeInfiniteCache();
                api.ensureIndexVisible(0);
            },

            applyFilterEnginePowerRange: (rawMin, rawMax) => {
                const min = Number.parseFloat(rawMin);
                const max = Number.parseFloat(rawMax);
                if (!Number.isFinite(min) || !Number.isFinite(max) || min > max) return;

                api.setFilterModel({
                    enginePower: {
                        filterType: "number",
                        type: "inRange",
                        filter: min,
                        filterTo: max,
                    },
                });
                api.onFilterChanged();
                api.purgeInfiniteCache();
                api.ensureIndexVisible(0);
            },
        });
    }, [onReadyControls]);

    const onGridReady = useCallback((e) => {
        gridApiRef.current = e.api;
        setDatasource();
        exposeRefresh();
        exposeControls();
    }, [setDatasource, exposeRefresh, exposeControls]);

    const onFilterChanged = useCallback(() => {
        gridApiRef.current?.purgeInfiniteCache();
        gridApiRef.current?.ensureIndexVisible(0);
    }, []);

    const onSortChanged = useCallback(() => {
        gridApiRef.current?.purgeInfiniteCache();
        gridApiRef.current?.ensureIndexVisible(0);
    }, []);

    return (
        <div style={{minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", padding: 16}}>
            <div style={{width: "95%"}}>
                <div style={{width: "100%", height: 800}}>
                    <AgGridReact
                        theme={tableTheme}
                        columnDefs={colDefs}
                        rowModelType="infinite"
                        cacheBlockSize={50}
                        maxBlocksInCache={2}
                        pagination
                        paginationPageSize={50}
                        onGridReady={onGridReady}
                        onFilterChanged={onFilterChanged}
                        onSortChanged={onSortChanged}
                        suppressMultiSort={false}
                        defaultColDef={{filter: true, sortable: true, floatingFilter: true}}
                    />
                </div>
            </div>
        </div>
    );
}