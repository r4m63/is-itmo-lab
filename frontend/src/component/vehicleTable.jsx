'use client';
import React, {useMemo, useState} from "react";
import {AllCommunityModule, colorSchemeDark, iconSetMaterial, ModuleRegistry, themeQuartz} from "ag-grid-community";
import {AgGridReact} from "ag-grid-react";


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


export default function VehicleTable({rows, onOpenEditVehicleModal}) {
    const defaultCol = useMemo(() => ({
        filter: true,
    }));
    const [colDefs, setColDefs] = useState([
        {
            headerName: "ID",
            field: "id",
            width: 100,
            sortable: true,
            filter: "agNumberColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Edit",
            filter: false,
            width: 90,
            cellRenderer: (p) => (
                <button
                    style={{
                        paddingInline: "15px",
                        border: "none",
                        backgroundColor: "#007bff",
                        color: "white",
                        borderRadius: "5px",
                        cursor: "pointer",
                    }}
                    onClick={() => onOpenEditVehicleModal(p.data)}
                >
                    Edit
                </button>
            ),
        },
        {
            headerName: "Name",
            field: "name",
            sortable: true,
            filter: "agTextColumnFilter",
            floatingFilter: true
        },
        {
            headerName: "Type",
            field: "type",
            width: 160,
            sortable: true,
            filter: "agTextColumnFilter",
            floatingFilter: true,
            filterParams: {
                values: ["CAR", "HELICOPTER", "MOTORCYCLE", "CHOPPER"],
                defaultToNothingSelected: false,
                suppressMiniFilter: true,
                debounceMs: 0,
            },
        },
        {
            headerName: "Fuel Type",
            field: "fuelType",
            width: 160,
            sortable: true,
            filter: "agTextColumnFilter",
            floatingFilter: true,
            filterParams: {
                values: ["KEROSENE", "MANPOWER", "NUCLEAR"],
                defaultToNothingSelected: false,
                suppressMiniFilter: true,
                debounceMs: 0,
            },
        },
        {
            headerName: "Creation Date",
            field: "creationDate",
            width: 190,
            sortable: true,
            filter: "agDateColumnFilter",
            floatingFilter: true,
            sort: "desc",
            valueFormatter: (p) => (p.value ? new Date(p.value).toLocaleString() : ""),
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
                    floatingFilter: true,
                },
                {
                    headerName: "Y",
                    colId: "coordinates.y",
                    valueGetter: (p) => p.data?.coordinates?.y,
                    width: 110,
                    sortable: true,
                    filter: "agNumberColumnFilter",
                    floatingFilter: true,
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
    ]);

    return (
        <div
            style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: "16px",
                boxSizing: "border-box",
            }}
        >
            <div style={{width: "95%"}}>
                <div style={{width: "100%", height: 800}}>
                    <AgGridReact
                        theme={tableTheme}
                        rowData={rows}
                        columnDefs={colDefs}
                        defaultColDef={defaultCol}
                        pagination
                        paginationPageSize={50}
                        paginationPageSizeSelector={[50, 100]}
                    />
                </div>
            </div>
        </div>
    );

};