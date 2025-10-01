'use client';

import {useEffect, useMemo, useRef, useState} from "react";
import {Input} from "@heroui/react";
import {API_BASE} from "../../cfg.js";

export default function OwnerPicker({
                                        value,
                                        onChange,
                                        required = true,
                                        errorText = "Выберите владельца из списка"
                                    }) {
    const [query, setQuery] = useState(value?.fullName || "");
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedId, setSelectedId] = useState(value?.id || null);
    const [touched, setTouched] = useState(false);
    const debounceRef = useRef(null);
    const listId = useMemo(() => "person-list-" + Math.random().toString(36).slice(2), []);

    useEffect(() => {
        setQuery(value?.fullName || "");
        setSelectedId(value?.id || null);
    }, [value?.id, value?.fullName]);

    const fetchPersons = async (q) => {
        setLoading(true);
        try {
            const url = new URL(`${API_BASE}/api/person/search`);
            if (q) url.searchParams.set("q", q);
            url.searchParams.set("limit", "20");
            const res = await fetch(url, {credentials: "include", headers: {"Accept": "application/json"}});
            const data = await res.json();
            setItems(Array.isArray(data) ? data : []);
        } catch {
            setItems([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchPersons(""); }, []);

    useEffect(() => {
        clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => fetchPersons(query.trim()), 300);
        return () => clearTimeout(debounceRef.current);
    }, [query]);

    const commitByName = (name) => {
        const hit = items.find(i => (i.fullName || "") === name);
        setTouched(true);
        if (hit) {
            setSelectedId(hit.id);
            onChange?.({id: hit.id, fullName: hit.fullName});
        } else {
            setSelectedId(null);
            onChange?.({id: null, fullName: name});
        }
    };

    const isInvalid = required && touched && !selectedId;

    return (
        <div className="flex flex-col gap-1">
            <Input
                list={listId}
                label="Владелец (начните вводить имя)"
                variant="bordered"
                value={query}
                isRequired={required}
                isInvalid={isInvalid}
                errorMessage={isInvalid ? errorText : undefined}
                description={selectedId
                    ? `Выбран ID: ${selectedId}`
                    : (loading ? "Загрузка..." : "Выберите из подсказок")}
                onChange={(e) => {
                    setQuery(e.target.value);
                    // пока печатает — сбрасываем выбранного
                    setSelectedId(null);
                }}
                onBlur={(e) => commitByName(e.target.value)}
            />
            <datalist id={listId}>
                {items.map(p => (
                    <option key={p.id} value={p.fullName}>{`id ${p.id}`}</option>
                ))}
            </datalist>

            {/* скрытое поле с ID, чтобы формально было обязательное значение при сабмите <form> */}
            <input type="hidden" name="ownerId" value={selectedId || ""} required={required} />
        </div>
    );
}
