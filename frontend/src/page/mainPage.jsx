// src/page/MainPage.jsx

import {useState} from "react";
import {
    Button,
    DateInput,
    Input,
    Modal,
    ModalBody,
    ModalContent,
    ModalFooter,
    ModalHeader,
    Popover,
    PopoverContent,
    PopoverTrigger,
    Textarea,
    useDisclosure,
} from "@heroui/react";
import {CalendarDate} from "@internationalized/date";
import {toast} from "sonner";
import styles from "./mainPage.module.css";
import {useNavigate} from "react-router-dom";
import useAuthStore from "../store/auth.js";
import {API_BASE} from "../../cfg.js";
import VehicleTable from "../component/vehicleTable.jsx";


export default function MainPage() {
    const [activePost, setActivePost] = useState(null);

    const [title, setTitle] = useState("");
    const [desc, setDesc] = useState("");
    const [date, setDate] = useState(null);

    const {isOpen, onOpen, onOpenChange} = useDisclosure();

    const openAddModal = () => {
        setActivePost(null);
        setTitle("");
        setDesc("");
        setDate(null);
        onOpen();
    };

    const handleSave = async () => {
        // Validate
        if (!title.trim()) return toast.warning("Заполните заголовок.");
        if (!desc.trim()) return toast.warning("Заполните описание.");
        if (!date) return toast.warning("Укажите дату.");

        // FormData
        const fd = new FormData();
        fd.append("title", title);
        fd.append("content", desc);
        fd.append("post_date", date.toString());

        const url = activePost
            ? `${API_BASE}/api/posts/${activePost.id}`
            : `${API_BASE}/api/posts`;
        const method = activePost ? "PUT" : "POST";

        try {
            const res = await fetch(url, {
                method,
                credentials: "include",
                body: fd
            });
            if (res.ok) {
                //
                toast.success("Сохранено");
                return;
            }
            let errorData = {};
            try {
                errorData = await res.json();
            } catch (_) {
                /* если не JSON, оставляем пустой объект */
            }
            switch (res.status) {
                case 401:
                    setIsAuthed(false);
                    toast.error(errorData.detail || errorData.message || "Session error");
                    navigate("/login", {replace: true});
                    break;
                case 409:
                    toast.warning("Пост с таким Title уже существует, напишите другой.");
                    break;
                default:
                    setIsAuthed?.(false);
                    toast.error(`Error: ${res.status} - ${res.statusText}`);
                    break;
            }
            // throw new Error(`HTTP ${res.status}`);
        } catch (err) {
            console.error(err);
            toast.error("Ошибка сохранения");
        }
    };

    const navigate = useNavigate();
    const {setIsAuthed} = useAuthStore();

    const handleLogout = async () => {
        try {
            const res = await fetch(`${API_BASE}/api/auth/logout`, {
                method: "POST",
                credentials: "include",
            });

            if (!res.ok) {
                const txt = await res.text();
                throw new Error(`${res.status} ${txt}`);
            }

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
                            <Button color="primary" className={styles.control} onPress={openAddModal}>
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

            <VehicleTable/>

            <Modal isOpen={isOpen} onOpenChange={onOpenChange} isDismissable={false}>
                <ModalContent className={styles.postModalBody}>
                    {(close) => (
                        <>
                            <ModalHeader>
                                {activePost ? "Редактировать пост" : "Новый пост"}
                            </ModalHeader>

                            <ModalBody className={styles.postModalBody}>
                                <Input
                                    label="Заголовок"
                                    variant="bordered"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    required
                                />

                                <Textarea
                                    label="Описание"
                                    variant="bordered"
                                    value={desc}
                                    onChange={(e) => setDesc(e.target.value)}
                                    required
                                    minRows={4}
                                />

                                <DateInput
                                    label="Дата поста"
                                    variant="bordered"
                                    value={date}
                                    onChange={setDate}
                                    placeholderValue={new CalendarDate(2025, 7, 24)}
                                    required
                                    className="max-w-sm"
                                />

                                {activePost && (
                                    <div style={{marginTop: 12}}>
                                        <Button
                                            color="danger"
                                            variant="solid"
                                            onClick={handleDelete}
                                        >
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