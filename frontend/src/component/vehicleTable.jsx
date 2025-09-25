'use client';
import React, {useCallback, useMemo, useState} from "react";
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


const EditBtnRender = ({onOpen, rowData}) => {
    return (
        <div>
            <button
                style={{
                    paddingInline: "15px",
                    border: "none",
                    backgroundColor: "#007bff",
                    color: "white",
                    borderRadius: "5px",
                    cursor: "pointer",
                }}
                onClick={() => onOpen(rowData)}
            >
                Edit
            </button>
        </div>
    );
};

export default function VehicleTable() {
    const defaultCol = useMemo(() => ({
        filter: true,
        // editable: true,
        // sortable: true,
        // floatingFilter: true,
    }));
    const defaultRow = {
        //mode: "multiRow",
        //headerCheckbox: true,
    };
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
        {
            headerName: "Edit",
            filter: false,
            width: 90,
            cellRenderer: (p) => (<EditBtnRender onOpen={handleOpenEditPostModal} rowData={p.data}/>),
        },
    ]);

    const handleOpenEditPostModal = useCallback((row) => {
        console.log("Edit:", row);
    }, []);

    const rows = [
        {
            id: 1,
            name: "Falcon X",
            coordinates: {x: 512.4, y: 820.0},
            creationDate: "2025-09-20T10:15:00Z",
            type: "CAR",
            enginePower: 180,
            numberOfWheels: 4,
            capacity: 5,
            distanceTravelled: 12000,
            fuelConsumption: 7.6,
            fuelType: "KEROSENE",
        },
        {
            id: 2,
            name: "Sky Hammer",
            coordinates: {x: 300.0, y: 640.5},
            creationDate: "2025-09-18T08:00:00Z",
            type: "HELICOPTER",
            enginePower: 900,
            numberOfWheels: 3,
            capacity: 2,
            distanceTravelled: 5400,
            fuelConsumption: 42.3,
            fuelType: "NUCLEAR",
        },
        {
            id: 3,
            name: "Street Bee",
            coordinates: {x: 120.7, y: 450.2},
            creationDate: "2025-09-10T12:30:00Z",
            type: "MOTORCYCLE",
            enginePower: 110,
            numberOfWheels: 2,
            capacity: 2,
            distanceTravelled: 2100,
            fuelConsumption: 3.9,
            fuelType: "MANPOWER",
        },
        {
            id: 4,
            name: "ChopMaster",
            coordinates: {x: 590.0, y: 900.0},
            creationDate: "2025-09-14T17:45:00Z",
            type: "CHOPPER",
            enginePower: 140,
            numberOfWheels: 2,
            capacity: 1,
            distanceTravelled: 980,
            fuelConsumption: 4.5,
            fuelType: "KEROSENE",
        },
        {
            id: 5,
            name: "Urban Rider",
            coordinates: {x: 410.2, y: 700.0},
            creationDate: "2025-09-12T09:20:00Z",
            type: "CAR",
            enginePower: 95,
            numberOfWheels: 4,
            capacity: 4,
            distanceTravelled: 35000,
            fuelConsumption: 6.2,
            fuelType: "MANPOWER",
        },
        {
            id: 6,
            name: "Cargo Ant",
            coordinates: {x: 50.0, y: 120.0},
            creationDate: "2025-09-05T06:00:00Z",
            type: "CAR",
            enginePower: 220,
            numberOfWheels: 6,
            capacity: 3,
            distanceTravelled: 76000,
            fuelConsumption: 12.4,
            fuelType: "KEROSENE",
        },
        {
            id: 7,
            name: "Sky Whisper",
            coordinates: {x: 333.3, y: 910.0},
            creationDate: "2025-09-22T13:05:00Z",
            type: "HELICOPTER",
            enginePower: 850,
            numberOfWheels: 3,
            capacity: null,               // допустимо null
            distanceTravelled: 12500,
            fuelConsumption: 39.8,
            fuelType: "NUCLEAR",
        },
        {
            id: 8,
            name: "Road Wolf",
            coordinates: {x: 600.9, y: 500.5}, // x ≤ 613
            creationDate: "2025-09-23T07:10:00Z",
            type: "CAR",
            enginePower: 150,
            numberOfWheels: 4,
            capacity: 5,
            distanceTravelled: null,      // допустимо null
            fuelConsumption: 7.1,
            fuelType: "KEROSENE",
        },
        {
            id: 9,
            name: "Blue Comet",
            coordinates: {x: 275.0, y: 960.0}, // y ≤ 962
            creationDate: "2025-09-24T15:30:00Z",
            type: "MOTORCYCLE",
            enginePower: 125,
            numberOfWheels: 2,
            capacity: 2,
            distanceTravelled: 4500,
            fuelConsumption: 4.1,
            fuelType: "MANPOWER",
        },
        {
            id: 10,
            name: "Steel Runner",
            coordinates: {x: 10.0, y: 50.0},
            creationDate: "2025-09-01T11:11:00Z",
            type: "CAR",
            enginePower: null,            // допустимо null
            numberOfWheels: 4,
            capacity: 5,
            distanceTravelled: 89000,
            fuelConsumption: 8.0,
            fuelType: "KEROSENE",
        },
        {
            id: 11,
            name: "Sky Lifter",
            coordinates: {x: 450.0, y: 888.8},
            creationDate: "2025-09-03T19:40:00Z",
            type: "HELICOPTER",
            enginePower: 1000,
            numberOfWheels: 3,
            capacity: 4,
            distanceTravelled: 6200,
            fuelConsumption: 45.5,
            fuelType: "NUCLEAR",
        },
        {
            id: 12,
            name: "City Hopper",
            coordinates: {x: 200.0, y: 600.0},
            creationDate: "2025-09-08T21:00:00Z",
            type: "CHOPPER",
            enginePower: 135,
            numberOfWheels: 2,
            capacity: 1,
            distanceTravelled: 3000,
            fuelConsumption: 4.3,
            fuelType: "KEROSENE",
        },
    ];

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
                        paginationPageSize={200}
                        paginationPageSizeSelector={[200, 500, 1000]}
                    />
                </div>
            </div>
        </div>
    );

};