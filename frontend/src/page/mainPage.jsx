'use client';

import {useState} from "react";
import {
    Button,
    Input,
    Modal,
    ModalBody,
    ModalContent,
    ModalFooter,
    ModalHeader,
    Popover,
    PopoverContent,
    PopoverTrigger,
    Select,
    SelectItem,
    useDisclosure,
} from "@heroui/react";
import {toast} from "sonner";
import styles from "./mainPage.module.css";
import {useNavigate} from "react-router-dom";
import useAuthStore from "../store/auth.js";
import {API_BASE} from "../../cfg.js";
import VehicleTable from "../component/vehicleTable.jsx";

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

    const {isOpen, onOpen, onOpenChange} = useDisclosure();

    const [refreshGrid, setRefreshGrid] = useState(() => () => {
    });

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

        onOpen();
    };

    const openEditVehicleModal = async (vehicle) => {
        setActiveVehicle(vehicle);

        setName(vehicle.name);
        setCoordX(vehicle.coordinates.x);
        setCoordY(vehicle.coordinates.y);
        setType(vehicle.type);
        setEnginePower(vehicle.enginePower);
        setNumberOfWheels(vehicle.numberOfWheels);
        setCapacity(vehicle.capacity);
        setDistanceTravelled(vehicle.distanceTravelled);
        setFuelConsumption(vehicle.fuelConsumption);
        setFuelType(vehicle.fuelType);

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
                const errorData = await res.json();
                switch (res.status) {
                    case 401:
                        setIsAuthed(false);
                        toast.error(errorData.message || 'Not correct credentials');
                        break;
                    default:
                        setIsAuthed(false);
                        toast.error(`Error: ${res.status} - ${res.statusText}`);
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
            try {
                errorData = await res.json();
            } catch (_) {
            }
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
                        </div>
                    </div>
                    <div className={styles.right}>
                        <Popover placement="bottom-end" showArrow>
                            <PopoverTrigger>
                                <div className={styles.profileWrapp}>
                                    <img
                                        src="/user.png"
                                        alt="User avatar"
                                        width={40}
                                        height={40}
                                        className="rounded-full object-cover"
                                    />
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
            />

            <Modal isOpen={isOpen} onOpenChange={onOpenChange} isDismissable={false}>
                <ModalContent className={styles.postModalBody}>
                    {(close) => (
                        <>
                            <ModalHeader>
                                {activeVehicle ? "Редактировать транспорт" : "Новый транспорт"}
                            </ModalHeader>

                            <ModalBody className={styles.postModalBody}>
                                <Input
                                    label="Название"
                                    variant="bordered"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    isRequired
                                />

                                <div className="grid grid-cols-2 gap-3">
                                    <Input
                                        type="number"
                                        label="Координата X"
                                        variant="bordered"
                                        value={coordX}
                                        onChange={(e) => setCoordX(e.target.value)}
                                        isRequired
                                    />
                                    <Input
                                        type="number"
                                        label="Координата Y"
                                        variant="bordered"
                                        value={coordY}
                                        onChange={(e) => setCoordY(e.target.value)}
                                        isRequired
                                    />
                                </div>

                                <Select
                                    label="Тип"
                                    variant="bordered"
                                    selectedKeys={type ? [type] : []}
                                    onChange={(e) => setType(e.target.value)}
                                    isRequired
                                >
                                    {VEHICLE_TYPES.map((t) => (
                                        <SelectItem key={t} value={t}>{t}</SelectItem>
                                    ))}
                                </Select>

                                <div className="grid grid-cols-2 gap-3">
                                    <Input
                                        type="number"
                                        label="Мощность двигателя"
                                        variant="bordered"
                                        value={enginePower}
                                        onChange={(e) => setEnginePower(e.target.value)}
                                        min={0}
                                    />
                                    <Input
                                        type="number"
                                        label="Кол-во колёс"
                                        variant="bordered"
                                        value={numberOfWheels}
                                        onChange={(e) => setNumberOfWheels(e.target.value)}
                                        isRequired
                                        min={1}
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <Input
                                        type="number"
                                        label="Вместимость"
                                        variant="bordered"
                                        value={capacity}
                                        onChange={(e) => setCapacity(e.target.value)}
                                        min={0}
                                    />
                                    <Input
                                        type="number"
                                        label="Пробег"
                                        variant="bordered"
                                        value={distanceTravelled}
                                        onChange={(e) => setDistanceTravelled(e.target.value)}
                                        min={0}
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-3">
                                    <Input
                                        type="number"
                                        label="Расход топлива"
                                        variant="bordered"
                                        value={fuelConsumption}
                                        onChange={(e) => setFuelConsumption(e.target.value)}
                                        isRequired
                                    />
                                    <Select
                                        label="Тип топлива"
                                        variant="bordered"
                                        selectedKeys={fuelType ? [fuelType] : []}
                                        onChange={(e) => setFuelType(e.target.value)}
                                        isRequired
                                    >
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
                                <Button variant="light" onPress={close}>
                                    Отмена
                                </Button>
                                <Button color="primary" onPress={handleSave}>
                                    Сохранить
                                </Button>
                            </ModalFooter>
                        </>
                    )}
                </ModalContent>
            </Modal>
        </>
    );
}
