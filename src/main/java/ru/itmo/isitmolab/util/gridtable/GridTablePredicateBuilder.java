package ru.itmo.isitmolab.util.gridtable;

import jakarta.persistence.criteria.*;
import ru.itmo.isitmolab.model.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ru.itmo.isitmolab.util.gridtable.DateParsers.parseToLocalDate;


public final class GridTablePredicateBuilder {

    public static Path<?> resolvePath(Root<Vehicle> root, String colId) {
        String norm = ColumnMapper.normalize(colId);
        if (norm.contains(".")) {
            Path<?> p = root;
            for (String part : norm.split("\\.")) {
                p = p.get(part);
            }
            return p;
        }
        return root.get(norm);
    }

    public static List<Predicate> build(
            CriteriaBuilder cb,
            Root<Vehicle> root,
            Map<String, Object> filterModel
    ) {
        List<Predicate> out = new ArrayList<>();
        if (filterModel == null || filterModel.isEmpty()) return out;

        for (var entry : filterModel.entrySet()) {
            String col = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) entry.getValue();
            String filterType = (String) fm.get("filterType");
            Path<?> path = resolvePath(root, col);

            switch (String.valueOf(filterType)) {
                case "text" -> handleText(cb, out, path, fm);
                case "number" -> handleNumber(cb, out, path, fm);
                case "date" -> handleDate(cb, out, path, fm);
                case "set" -> handleSet(cb, out, path, fm);
                default -> { /* ignore */ }
            }
        }
        return out;
    }

    private static void handleText(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        String type = (String) fm.get("type");
        String val = (String) fm.get("filter");
        if (val == null || val.isBlank()) return;

        Expression<String> exp = cb.lower(path.as(String.class));
        String p = val.toLowerCase(Locale.ROOT);

        switch (type) {
            case "contains" -> out.add(cb.like(exp, "%" + p + "%"));
            case "equals" -> out.add(cb.equal(exp, p));
            case "startsWith" -> out.add(cb.like(exp, p + "%"));
            case "endsWith" -> out.add(cb.like(exp, "%" + p));
            case "notEqual" -> out.add(cb.notEqual(exp, p));
        }
    }

    private static void handleNumber(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        String type = (String) fm.get("type");
        Number f1 = toNumber(fm.get("filter"));
        Number f2 = toNumber(fm.get("filterTo"));

        Class<?> jt = path.getJavaType();

        if (jt == Integer.class) {
            addNumber(cb, out, type, path.as(Integer.class),
                    f1 != null ? f1.intValue() : null,
                    f2 != null ? f2.intValue() : null);
        } else if (jt == Long.class) {
            addNumber(cb, out, type, path.as(Long.class),
                    f1 != null ? f1.longValue() : null,
                    f2 != null ? f2.longValue() : null);
        } else if (jt == Float.class) {
            addNumber(cb, out, type, path.as(Float.class),
                    f1 != null ? f1.floatValue() : null,
                    f2 != null ? f2.floatValue() : null);
        } else if (jt == Double.class) {
            addNumber(cb, out, type, path.as(Double.class),
                    f1 != null ? f1.doubleValue() : null,
                    f2 != null ? f2.doubleValue() : null);
        } else if (jt == BigDecimal.class) {
            addNumber(cb, out, type, path.as(BigDecimal.class),
                    f1 != null ? new BigDecimal(f1.toString()) : null,
                    f2 != null ? new BigDecimal(f2.toString()) : null);
        }
    }

    private static <T extends Number & Comparable<T>> void addNumber(
            CriteriaBuilder cb, List<Predicate> out, String type,
            Expression<T> num, T v1, T v2
    ) {
        if (v1 == null && !"inRange".equals(type)) return;

        switch (type) {
            case "equals" -> out.add(cb.equal(num, v1));
            case "notEqual" -> out.add(cb.notEqual(num, v1));
            case "lessThan" -> out.add(cb.lessThan(num, v1));
            case "lessThanOrEqual" -> out.add(cb.lessThanOrEqualTo(num, v1));
            case "greaterThan" -> out.add(cb.greaterThan(num, v1));
            case "greaterThanOrEqual" -> out.add(cb.greaterThanOrEqualTo(num, v1));
            case "inRange" -> {
                if (v1 != null && v2 != null) {
                    out.add(cb.and(cb.greaterThanOrEqualTo(num, v1), cb.lessThanOrEqualTo(num, v2)));
                } else if (v1 != null) {
                    out.add(cb.greaterThanOrEqualTo(num, v1));
                } else if (v2 != null) {
                    out.add(cb.lessThanOrEqualTo(num, v2));
                }
            }
        }
    }

    private static void handleDate(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        if (!LocalDateTime.class.isAssignableFrom(path.getJavaType())) return;

        String type = (String) fm.get("type");
        String d1s = (String) fm.get("dateFrom");
        String d2s = (String) fm.get("dateTo");
        if (d1s == null || d1s.isBlank()) return;

        LocalDate d1 = parseToLocalDate(d1s);
        if (d1 == null) return;

        LocalDateTime start = d1.atStartOfDay();
        Expression<LocalDateTime> dt = path.as(LocalDateTime.class);

        switch (type) {
            case "equals" -> {
                LocalDateTime end = d1.plusDays(1).atStartOfDay();
                out.add(cb.between(dt, start, end));
            }
            case "lessThan" -> out.add(cb.lessThan(dt, start));
            case "greaterThan" -> {
                LocalDateTime end = d1.plusDays(1).atStartOfDay();
                out.add(cb.greaterThanOrEqualTo(dt, end));
            }
            case "inRange" -> {
                LocalDate d2 = parseToLocalDate(d2s);
                if (d2 == null) d2 = d1;
                LocalDateTime end = d2.plusDays(1).atStartOfDay();
                out.add(cb.between(dt, start, end));
            }
        }
    }

    private static void handleSet(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) fm.get("values");
        if (values == null || values.isEmpty()) return;

        CriteriaBuilder.In<Object> in = cb.in(path);
        for (String v : values) {
            in.value(castForPath(path, v));
        }
        out.add(in);
    }

    @SuppressWarnings("unchecked")
    private static Object castForPath(Path<?> path, String value) {
        Class<?> t = path.getJavaType();
        if (t.isEnum()) return Enum.valueOf((Class<Enum>) t, value);
        if (t.equals(Integer.class)) return Integer.valueOf(value);
        if (t.equals(Long.class)) return Long.valueOf(value);
        if (t.equals(Double.class)) return Double.valueOf(value);
        if (t.equals(Float.class)) return Float.valueOf(value);
        if (t.equals(BigDecimal.class)) return new BigDecimal(value);
        return value;
    }

    private static Number toNumber(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n;
        return new BigDecimal(o.toString());
    }
}
