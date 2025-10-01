'use client';

import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {
    Button, Input, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader,
    Popover, PopoverContent, PopoverTrigger, Select, SelectItem, useDisclosure
} from "@heroui/react";
import {toast} from "sonner";
import styles from "./mainPage.module.css";
import {useNavigate} from "react-router-dom";
import useAuthStore from "../store/auth.js";
import {API_BASE} from "../../cfg.js";
import VehicleTable from "../component/vehicleTable.jsx";
import OwnerPicker from "../component/ownerPicker.jsx";

const VEHICLE_TYPES = ["CAR", "HELICOPTER", "MOTORCYCLE", "CHOPPER"];
const FUEL_TYPES = ["KEROSENE", "MANPOWER", "NUCLEAR"];

export default function MainPage() {
    const navigate = useNavigate();
    const {setIsAuthed} = useAuthStore();

    const [activeVehicle, setActiveVehicle] = useState(null);

    const [name, setName] = useState("");
    const [coordX, setCoordX] = useState("");
    const [coordY, setCoordY] = useState("");
    const [type, setType] = useState("");
    const [enginePower, setEnginePower] = useState("");
    const [numberOfWheels, setNumberOfWheels] = useState("");
    const [capacity, setCapacity] = useState("");
    const [distanceTravelled, setDistanceTravelled] = useState("");
    const [fuelConsumption, setFuelConsumption] = useState("");
    const [fuelType, setFuelType] = useState("");

    // выбор владельца теперь через OwnerPicker
    const [ownerId, setOwnerId] = useState("");
    const [ownerName, setOwnerName] = useState("");

    const {isOpen, onOpen, onOpenChange} = useDisclosure();

    const [tableControls, setTableControls] = useState(null);
    const [refreshGrid, setRefreshGrid] = useState(() => () => {});

    const wsRef = useRef(null);
    const reconnectTimerRef = useRef(null);

    const WS_URL = useMemo(() => {
        try {
            const u = new URL(API_BASE);
            const wsProto = u.protocol === "https:" ? "wss:" : "ws:";
            return `${wsProto}//${u.host}${u.pathname.replace(/\/+$/, '')}/ws/vehicles`;
        } catch {
            const loc = window.location;
            const wsProto = loc.protocol === "https:" ? "wss:" : "ws:";
            const base = API_BASE?.startsWith("/") ? API_BASE : `/${API_BASE || ""}`;
            return `${wsProto}//${loc.host}${base.replace(/\/+$/, '')}/ws/vehicles`;
        }
    }, []);

    const connectWs = useCallback(() => {
        if (wsRef.current?.readyState === WebSocket.OPEN ||
            wsRef.current?.readyState === WebSocket.CONNECTING) return;

        let retry = 1000;
        const openSocket = () => {
            const ws = new WebSocket(WS_URL);
            wsRef.current = ws;

            ws.onopen = () => { retry = 1000; };
            ws.onmessage = (evt) => {
                const msg = (evt.data || "").toString().trim();
                if (msg === "refresh") refreshGrid?.();
            };
            ws.onclose = () => {
                reconnectTimerRef.current = setTimeout(() => {
                    retry = Math.min(retry * 2, 10000);
                    openSocket();
                }, retry);
            };
            ws.onerror = () => {
                try { ws.close(); } catch {}
            };
        };

        openSocket();
    }, [WS_URL, refreshGrid]);

    useEffect(() => {
        connectWs();
        return () => {
            clearTimeout(reconnectTimerRef.current);
            try { wsRef.current?.close(); } catch {}
        };
    }, [connectWs]);

    const openNewVehicleModal = () => {
        setActiveVehicle(null);
        setName("");
        setCoordX("");
        setCoordY("");
        setType("");
        setEnginePower("");
        setNumberOfWheels("");
        setCapacity("");
        setDistanceTravelled("");
        setFuelConsumption("");
        setFuelType("");
        setOwnerId("");
        setOwnerName("");
        onOpen();
    };

    const openEditVehicleModal = async (vehicle) => {
        setActiveVehicle(vehicle);
        setName(vehicle.name);
        setCoordX(vehicle.coordinates?.x ?? "");
        setCoordY(vehicle.coordinates?.y ?? "");
        setType(vehicle.type);
        setEnginePower(vehicle.enginePower ?? "");
        setNumberOfWheels(vehicle.numberOfWheels ?? "");
        setCapacity(vehicle.capacity ?? "");
        setDistanceTravelled(vehicle.distanceTravelled ?? "");
        setFuelConsumption(vehicle.fuelConsumption ?? "");
        setFuelType(vehicle.fuelType);
        setOwnerId(vehicle.ownerId ? String(vehicle.ownerId) : "");
        setOwnerName(vehicle.ownerName ?? "");
        onOpen();
    };

    function validate() {
        if (!name.trim()) return "Заполните name.";
        if (coordX === "" || isNaN(Number(coordX))) return "Координата X должна быть числом.";
        if (coordY === "" || isNaN(Number(coordY))) return "Координата Y должна быть числом.";
        if (!type) return "Выберите type.";
        if (!numberOfWheels || Number(numberOfWheels) <= 0) return "numberOfWheels должно быть > 0.";
        if (!fuelConsumption || Number(fuelConsumption) <= 0) return "fuelConsumption должно быть > 0.";
        if (!fuelType) return "Выберите fuelType.";
        if (enginePower !== "" && Number(enginePower) <= 0) return "enginePower должно быть > 0.";
        if (capacity !== "" && Number(capacity) <= 0) return "capacity должно быть > 0.";
        if (distanceTravelled !== "" && Number(distanceTravelled) <= 0) return "distanceTravelled должно быть > 0.";

        const oid = Number(ownerId);
        if (!Number.isFinite(oid) || oid <= 0) return "Выберите владельца (owner).";
        return null;
    }

    const handleSave = async () => {
        const err = validate();
        if (err) return toast.warning(err);

        const payload = {
            id: activeVehicle?.id ?? null,
            name: name.trim(),
            coordinates: {
                x: coordX === "" ? null : Number(coordX),
                y: coordY === "" ? null : Number(coordY),
            },
            type: type,
            enginePower: enginePower === "" ? null : Number(enginePower),
            numberOfWheels: Number(numberOfWheels),
            capacity: capacity === "" ? null : Number(capacity),
            distanceTravelled: distanceTravelled === "" ? null : Number(distanceTravelled),
            fuelConsumption: Number(fuelConsumption),
            fuelType: fuelType,

            ownerId: Number(ownerId),
        };

        const isEdit = Boolean(activeVehicle?.id);
        const url = isEdit
            ? `${API_BASE}/api/vehicle/${activeVehicle.id}`
            : `${API_BASE}/api/vehicle`;

        try {
            const res = await fetch(url, {
                method: isEdit ? "PUT" : "POST",
                credentials: "include",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload),
            });

            if (res.ok) {
                refreshGrid();
                onOpenChange(false);
                toast.success("Сохранено");
            } else {
                const errorData = await res.json().catch(() => ({}));
                switch (res.status) {
                    case 401:
                        setIsAuthed(false);
                        toast.error(errorData.message || 'Not correct credentials');
                        break;
                    default:
                        toast.error(errorData.message || `Error: ${res.status} - ${res.statusText}`);
                        break;
                }
            }
        } catch (e) {
            console.error(e);
            toast.error("Ошибка сохранения");
        }
    };

    const handleDelete = async () => {
        if (!activeVehicle?.id) return;
        try {
            const res = await fetch(`${API_BASE}/api/vehicle/${activeVehicle.id}`, {
                method: "DELETE",
                credentials: "include",
            });
            if (res.ok) {
                refreshGrid();
                onOpenChange(false);
                toast.success("Удалено");
                return;
            }
            let errorData = {};
            try { errorData = await res.json(); } catch {}
            toast.error(errorData.message || `Error: ${res.status}`);
        } catch (e) {
            console.error(e);
            toast.error("Ошибка удаления");
        }
    };

    const handleLogout = async () => {
        try {
            const res = await fetch(`${API_BASE}/api/auth/logout`, {
                method: "POST",
                credentials: "include",
            });
            if (!res.ok) throw new Error(`${res.status} ${await res.text()}`);
            setIsAuthed(false);
            navigate("/login", {replace: true});
            toast.success("Вы вышли из аккаунта");
        } catch (err) {
            toast.error("Не удалось выйти: " + (err.message || ""));
        }
    };

    const {isOpen: isPresetOpen, onOpen: onPresetOpen, onOpenChange: onPresetOpenChange} = useDisclosure();
    const [presetFuelGt, setPresetFuelGt] = useState("");
    const [presetType, setPresetType] = useState("");
    const [presetEngMin, setPresetEngMin] = useState("");
    const [presetEngMax, setPresetEngMax] = useState("");

    const presetMinDistance = async () => {
        try {
            const res = await fetch(`${API_BASE}/api/vehicle/special/min-distance`, {
                credentials: "include",
                headers: {"Accept": "application/json"},
            });
            if (res.status === 204) {
                toast.info("Нет объектов с заполненным distanceTravelled");
                return;
            }
            if (!res.ok) throw new Error(`${res.status}`);
            const dto = await res.json();
            onPresetOpenChange(false);
            await openEditVehicleModal(dto);
            toast.success("Загружен объект с минимальным пробегом");
        } catch (e) {
            console.error(e);
            toast.error("Не удалось получить объект");
        }
    };

    const presetCountFuelGt = async () => {
        const v = Number(presetFuelGt);
        if (isNaN(v) || v <= 0) return toast.warning("Введите корректное значение топлива > 0");
        try {
            const res = await fetch(`${API_BASE}/api/vehicle/special/count-fuel-gt?v=${encodeURIComponent(v)}`, {
                credentials: "include",
                headers: {"Accept": "application/json"},
            });
            if (!res.ok) throw new Error(`${res.status}`);
            const data = await res.json();
            toast.info(`Найдено: ${data.count}`);
        } catch (e) {
            console.error(e);
            toast.error("Не удалось выполнить подсчет");
        }
    };

    const presetListFuelGt = async () => {
        const v = Number(presetFuelGt);
        if (!Number.isFinite(v) || v <= 0) return toast.warning("Введите корректное значение топлива > 0");
        try {
            const res = await fetch(`${API_BASE}/api/vehicle/special/list-fuel-gt?v=${encodeURIComponent(v)}`, {
                credentials: "include",
                headers: {"Accept": "application/json"},
            });
            if (!res.ok) throw new Error(`${res.status}`);
            tableControls?.applyFilterFuelGt(v);
            onPresetOpenChange(false);
            toast.success("Фильтр применён");
        } catch (e) {
            console.error(e);
            toast.error("Не удалось получить список");
        }
    };

    const presetListByType = async () => {
        if (!presetType) return toast.warning("Выберите тип");
        try {
            const res = await fetch(`${API_BASE}/api/vehicle/special/by-type?type=${encodeURIComponent(presetType)}`, {
                credentials: "include",
                headers: {"Accept": "application/json"},
            });
            if (!res.ok) throw new Error(`${res.status}`);
            tableControls?.applyFilterByType(presetType);
            onPresetOpenChange(false);
            toast.success("Фильтр применён");
        } catch (e) {
            console.error(e);
            toast.error("Не удалось получить по типу");
        }
    };

    const presetListByEngineRange = async () => {
        const min = Number(presetEngMin);
        const max = Number(presetEngMax);
        if (!Number.isFinite(min) || !Number.isFinite(max) || min > max || !min || !max) {
            return toast.warning("Введите корректный диапазон мощности");
        }
        try {
            const res = await fetch(`${API_BASE}/api/vehicle/special/by-engine-range?min=${min}&max=${max}`, {
                credentials: "include",
                headers: {"Accept": "application/json"},
            });
            if (!res.ok) throw new Error(`${res.status}`);
            tableControls?.applyFilterEnginePowerRange(min, max);
            onPresetOpenChange(false);
            toast.success("Фильтр применён");
        } catch (e) {
            console.error(e);
            toast.error("Не удалось получить по диапазону");
        }
    };

    const handleResetFilters = () => { tableControls?.clearFilters(); };

    return (
        <>
            <div className={styles.totalwrapp}>
                <div className={styles.top}>
                    <div className={styles.left}>
                        <h1 className={styles.title}>Таблица элементов</h1>
                        <div className={styles.btnWrapper}>
                            <Button color="primary" className={styles.control} onPress={openNewVehicleModal}>
                                Добавить
                            </Button>
                            <Button color="primary" className={styles.control} onPress={onPresetOpen}>
                                Пресеты
                            </Button>
                            <Button color="warning" className={styles.control} onPress={handleResetFilters}>
                                Сбросить фильтры
                            </Button>
                            <Button color="primary" className={styles.control} onPress={() => navigate("/person")}>
                                Person
                            </Button>
                        </div>
                    </div>
                    <div className={styles.right}>
                        <Popover placement="bottom-end" showArrow>
                            <PopoverTrigger>
                                <div className={styles.profileWrapp}>
                                    <img src="/user.png" alt="User avatar" width={40} height={40}
                                         className="rounded-full object-cover"/>
                                    <h1 className="ml-3 font-medium">Мой профиль</h1>
                                </div>
                            </PopoverTrigger>
                            <PopoverContent className="p-4 flex flex-col items-stretch gap-2">
                                <Button color="danger" onPress={handleLogout}>Выход</Button>
                            </PopoverContent>
                        </Popover>
                    </div>
                </div>
            </div>

            <VehicleTable
                onOpenEditVehicleModal={openEditVehicleModal}
                onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                onReadyControls={(controls) => setTableControls(controls)}
            />

            <Modal isOpen={isOpen} onOpenChange={onOpenChange} isDismissable={false}>
                <ModalContent className={styles.postModalBody}>
                    {(close) => (
                        <>
                            <ModalHeader>
                                {activeVehicle ? "Редактировать транспорт" : "Новый транспорт"}
                            </ModalHeader>

                            <ModalBody className={styles.postModalBody}>
                                <Input label="Название" variant="bordered" value={name}
                                       onChange={(e) => setName(e.target.value)} isRequired/>

                                {/* новый выбор владельца */}
                                <OwnerPicker
                                    value={{ id: ownerId ? Number(ownerId) : null, fullName: ownerName || "" }}
                                    onChange={(sel) => {
                                        if (sel?.id) {
                                            setOwnerId(String(sel.id));
                                            setOwnerName(sel.fullName || "");
                                        } else {
                                            // пользователь не выбрал из подсказки
                                            setOwnerId("");
                                            setOwnerName(sel?.fullName || "");
                                        }
                                    }}
                                />

                                <div className="grid grid-cols-2 gap-3">
                                    <Input type="number" label="Координата X" variant="bordered" value={coordX}
                                           onChange={(e) => setCoordX(e.target.value)} isRequired/>
                                    <Input type="number" label="Координата Y" variant="bordered" value={coordY}
                                           onChange={(e) => setCoordY(e.target.value)} isRequired/>
                                </div>

                                <Select label="Тип" variant="bordered"
                                        selectedKeys={type ? [type] : []}
                                        onChange={(e) => setType(e.target.value)} isRequired>
                                    {VEHICLE_TYPES.map((t) => (
                                        <SelectItem key={t} value={t}>{t}</SelectItem>
                                    ))}
                                </Select>

                                <div className="grid grid-cols-2 gap-3">
                                    <Input type="number" label="Мощность двигателя" variant="bordered"
                                           value={enginePower} onChange={(e) => setEnginePower(e.target.value)}
                                           min={0}/>
                                    <Input type="number" label="Кол-во колёс" variant="bordered" value={numberOfWheels}
                                           onChange={(e) => setNumberOfWheels(e.target.value)} isRequired min={1}/>
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <Input type="number" label="Вместимость" variant="bordered" value={capacity}
                                           onChange={(e) => setCapacity(e.target.value)} min={0}/>
                                    <Input type="number" label="Пробег" variant="bordered" value={distanceTravelled}
                                           onChange={(e) => setDistanceTravelled(e.target.value)} min={0}/>
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <Input type="number" label="Расход топлива" variant="bordered"
                                           value={fuelConsumption} onChange={(e) => setFuelConsumption(e.target.value)}
                                           isRequired/>
                                    <Select label="Тип топлива" variant="bordered"
                                            selectedKeys={fuelType ? [fuelType] : []}
                                            onChange={(e) => setFuelType(e.target.value)} isRequired>
                                        {FUEL_TYPES.map((f) => (
                                            <SelectItem key={f} value={f}>{f}</SelectItem>
                                        ))}
                                    </Select>
                                </div>

                                {activeVehicle && (
                                    <div style={{marginTop: 12}}>
                                        <Button color="danger" variant="solid" onPress={handleDelete}>
                                            Удалить
                                        </Button>
                                    </div>
                                )}
                            </ModalBody>

                            <ModalFooter>
                                <Button variant="light" onPress={close}>Отмена</Button>
                                <Button color="primary" onPress={handleSave}>Сохранить</Button>
                            </ModalFooter>
                        </>
                    )}
                </ModalContent>
            </Modal>

            <Modal isOpen={isPresetOpen} onOpenChange={onPresetOpenChange} isDismissable>
                <ModalContent>
                    {(close) => (
                        <>
                            <ModalHeader>Пресеты</ModalHeader>
                            <ModalBody className="flex flex-col gap-5">
                                <div className="space-y-2">
                                    <div className="text-sm font-medium opacity-80">
                                        Вернуть один объект с минимальным distanceTravelled
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <Button onPress={presetMinDistance}>Выполнить</Button>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <div className="text-sm font-medium opacity-80">
                                        Вернуть количество объектов, у которых fuelConsumption &gt; X
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <Input label="X (fuelConsumption)" type="number" value={presetFuelGt}
                                               onChange={(e) => setPresetFuelGt(e.target.value)}/>
                                        <Button onPress={presetCountFuelGt}>Выполнить</Button>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <div className="text-sm font-medium opacity-80">
                                        Вернуть массив объектов, у которых fuelConsumption &gt; X
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <Input label="X (fuelConsumption)" type="number" value={presetFuelGt}
                                               onChange={(e) => setPresetFuelGt(e.target.value)}/>
                                        <Button onPress={presetListFuelGt}>Выполнить</Button>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <div className="text-sm font-medium opacity-80">Найти все транспортные средства заданного типа</div>
                                    <div className="flex items-center gap-3">
                                        <Select label="Тип ТС" selectedKeys={presetType ? [presetType] : []}
                                                onChange={(e) => setPresetType(e.target.value)}
                                                className="min-w-[180px]">
                                            {VEHICLE_TYPES.map(t => <SelectItem key={t} value={t}>{t}</SelectItem>)}
                                        </Select>
                                        <Button onPress={presetListByType}>Выполнить</Button>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <div className="text-sm font-medium opacity-80">
                                        Найти все ТС с мощностью двигателя в диапазоне [min, max]
                                    </div>
                                    <div className="grid grid-cols-3 gap-3 items-end">
                                        <Input label="Engine min" type="number" value={presetEngMin}
                                               onChange={(e) => setPresetEngMin(e.target.value)}/>
                                        <Input label="Engine max" type="number" value={presetEngMax}
                                               onChange={(e) => setPresetEngMax(e.target.value)}/>
                                        <Button onPress={presetListByEngineRange}>Выполнить</Button>
                                    </div>
                                </div>
                            </ModalBody>

                            <ModalFooter>
                                <Button variant="light" onPress={close}>Закрыть</Button>
                            </ModalFooter>
                        </>
                    )}
                </ModalContent>
            </Modal>
        </>
    );
}
