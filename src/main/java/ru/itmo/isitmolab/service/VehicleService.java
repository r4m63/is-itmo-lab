package ru.itmo.isitmolab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import ru.itmo.isitmolab.dao.AdminDao;
import ru.itmo.isitmolab.dao.VehicleDao;
import ru.itmo.isitmolab.dto.GridRequest;
import ru.itmo.isitmolab.dto.GridResponse;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Admin;
import ru.itmo.isitmolab.model.Vehicle;
import ru.itmo.isitmolab.ws.VehicleWsHub;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Stateless
public class VehicleService {

    @Inject
    VehicleDao dao;
    @Inject
    AdminDao adminDao;
    @Inject
    SessionService sessionService;
    @Inject
    VehicleWsHub wsHub;

    @PersistenceContext(unitName = "studsPU")
    private EntityManager em;

    private static final DateTimeFormatter DT_SPACE_SEC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_SPACE_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static LocalDate parseToLocalDate(String s) {
        if (s == null || s.isBlank()) return null;

        // 1) чистая дата
        try { return LocalDate.parse(s); } catch (DateTimeParseException ignored) {}

        // 2) ISO datetime "yyyy-MM-ddTHH:mm:ss[.SSS]"
        try { return LocalDateTime.parse(s).toLocalDate(); } catch (DateTimeParseException ignored) {}

        // 3) "yyyy-MM-dd HH:mm:ss.SSS"
        try { return LocalDateTime.parse(s, DT_SPACE_MILLIS).toLocalDate(); } catch (DateTimeParseException ignored) {}

        // 4) "yyyy-MM-dd HH:mm:ss"
        try { return LocalDateTime.parse(s, DT_SPACE_SEC).toLocalDate(); } catch (DateTimeParseException ignored) {}

        // если ничего не зашло — пробрасываем понятное исключение
        throw new DateTimeParseException("Unsupported date format", s, 0);
    }

    private static LocalDate safeParseOrNull(String s) {
        try { return parseToLocalDate(s); } catch (Exception e) { return null; }
    }

    // ========= CRUD =========

    public Long createNewVehicle(VehicleDto dto, HttpServletRequest req) {
        Long adminId = sessionService.getCurrentUserId(req);
        Admin admin = adminDao.findById(adminId)
                .orElseThrow(() -> new WebApplicationException(
                        "Admin not found: " + adminId, Response.Status.UNAUTHORIZED));

        Vehicle v = VehicleDto.toEntity(dto, null);
        v.setCreatedBy(admin);
        dao.save(v);
        wsHub.broadcastText("refresh");
        return v.getId();
    }

    public void updateVehicle(Long id, VehicleDto dto) {
        Vehicle current = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));
        VehicleDto.toEntity(dto, current);
        dao.save(current);
        wsHub.broadcastText("refresh");
    }

    public VehicleDto getVehicleById(Long id) {
        Vehicle v = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));
        return VehicleDto.fromEntity(v);
    }

    public void deleteVehicleById(Long id) {
        if (!dao.existsById(id)) {
            throw new WebApplicationException(
                    "Vehicle not found: " + id, Response.Status.NOT_FOUND);
        }
        dao.deleteById(id);
        wsHub.broadcastText("refresh");
    }

    public List<VehicleDto> getAllVehicles() {
        return dao.findAll().stream().map(VehicleDto::fromEntity).toList();
    }

    public List<VehicleDto> getAllVehicles(int offset, int limit) {
        return dao.findAll(offset, limit).stream().map(VehicleDto::fromEntity).toList();
    }

    // ========= AG Grid server-side =========

    public GridResponse<VehicleDto> queryVehicles(GridRequest req) {
        int pageSize = Math.max(1, req.endRow - req.startRow);
        int first = Math.max(0, req.startRow);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // SELECT
        CriteriaQuery<Vehicle> cq = cb.createQuery(Vehicle.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        List<Predicate> predicates = buildPredicates(cb, root, req.filterModel);
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // sorting
        if (req.sortModel != null && !req.sortModel.isEmpty()) {
            List<Order> orders = new ArrayList<>();
            req.sortModel.forEach(s -> {
                Path<?> path = resolvePath(root, s.getColId());
                orders.add("desc".equalsIgnoreCase(s.getSort()) ? cb.desc(path) : cb.asc(path));
            });
            cq.orderBy(orders);
        } else {
            // дефолт по дате создания
            cq.orderBy(cb.desc(resolvePath(root, "creationDateTime")));
        }

        List<Vehicle> pageRows = em.createQuery(cq)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();

        // COUNT
        CriteriaQuery<Long> cntq = cb.createQuery(Long.class);
        Root<Vehicle> cntRoot = cntq.from(Vehicle.class);
        List<Predicate> cntPreds = buildPredicates(cb, cntRoot, req.filterModel);
        cntq.select(cb.count(cntRoot));
        if (!cntPreds.isEmpty()) {
            cntq.where(cntPreds.toArray(new Predicate[0]));
        }
        long total = em.createQuery(cntq).getSingleResult();

        List<VehicleDto> dtos = pageRows.stream().map(VehicleDto::fromEntity).toList();
        return new GridResponse<>(dtos, (int) total);
    }

    // ========= Helpers =========

    // фронт => реальные поля сущности
    private static final Map<String, String> COL_MAP = new HashMap<>();

    static {
        COL_MAP.put("creationDate", "creationDateTime");
        COL_MAP.put("coordinates.x", "coordinates.x");
        COL_MAP.put("coordinates.y", "coordinates.y");
    }

    private String normalizeColId(String colId) {
        if (colId == null || colId.isBlank()) return "id";
        return COL_MAP.getOrDefault(colId, colId);
    }

    private Path<?> resolvePath(Root<Vehicle> root, String colId) {
        String norm = normalizeColId(colId);
        if (norm.contains(".")) {
            String[] parts = norm.split("\\.");
            Path<?> p = root;
            for (String part : parts) p = p.get(part);
            return p;
        }
        return root.get(norm);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb,
                                            Root<Vehicle> root,
                                            Map<String, Object> filterModel) {
        List<Predicate> out = new ArrayList<>();
        if (filterModel == null || filterModel.isEmpty()) return out;

        for (var entry : filterModel.entrySet()) {
            String col = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) entry.getValue();
            String filterType = (String) fm.get("filterType"); // 'text' | 'number' | 'date' | 'set'
            Path<?> path = resolvePath(root, col);

            switch (filterType) {
                case "text" -> {
                    String type = (String) fm.get("type");  // contains, equals, startsWith, endsWith, notEqual
                    String val = (String) fm.get("filter");
                    if (val == null || val.isBlank()) break;

                    Expression<String> exp = cb.lower(path.as(String.class));
                    String p = val.toLowerCase(Locale.ROOT);

                    switch (type) {
                        case "contains" -> out.add(cb.like(exp, "%" + p + "%"));
                        case "equals" -> out.add(cb.equal(exp, p));
                        case "startsWith" -> out.add(cb.like(exp, p + "%"));
                        case "endsWith" -> out.add(cb.like(exp, "%" + p));
                        case "notEqual" -> out.add(cb.notEqual(exp, p));
                        default -> {
                        }
                    }
                }
                case "number" -> {
                    String type = (String) fm.get("type");  // equals, notEqual, lessThan, greaterThan, lessThanOrEqual, greaterThanOrEqual, inRange
                    Number f1 = toNumber(fm.get("filter"));
                    Number f2 = toNumber(fm.get("filterTo"));

                    Class<?> jt = path.getJavaType();

                    if (jt == Integer.class) {
                        Expression<Integer> num = path.as(Integer.class);
                        Integer v1 = f1 != null ? f1.intValue() : null;
                        Integer v2 = f2 != null ? f2.intValue() : null;
                        addNumberPredicates(cb, out, type, num, v1, v2);
                    } else if (jt == Long.class) {
                        Expression<Long> num = path.as(Long.class);
                        Long v1 = f1 != null ? f1.longValue() : null;
                        Long v2 = f2 != null ? f2.longValue() : null;
                        addNumberPredicates(cb, out, type, num, v1, v2);
                    } else if (jt == Float.class) {
                        Expression<Float> num = path.as(Float.class);
                        Float v1 = f1 != null ? f1.floatValue() : null;
                        Float v2 = f2 != null ? f2.floatValue() : null;
                        addNumberPredicates(cb, out, type, num, v1, v2);
                    } else if (jt == Double.class) {
                        Expression<Double> num = path.as(Double.class);
                        Double v1 = f1 != null ? f1.doubleValue() : null;
                        Double v2 = f2 != null ? f2.doubleValue() : null;
                        addNumberPredicates(cb, out, type, num, v1, v2);
                    } else if (jt == BigDecimal.class) {
                        Expression<BigDecimal> num = path.as(BigDecimal.class);
                        BigDecimal v1 = f1 != null ? new BigDecimal(f1.toString()) : null;
                        BigDecimal v2 = f2 != null ? new BigDecimal(f2.toString()) : null;
                        addNumberPredicates(cb, out, type, num, v1, v2);
                    } else {
                        // неизвестный числовой тип — пропустим фильтр
                    }
                }
                case "date" -> {
                    String type = (String) fm.get("type"); // equals, lessThan, greaterThan, inRange
                    String d1s = (String) fm.get("dateFrom");
                    String d2s = (String) fm.get("dateTo");
                    if (d1s == null || d1s.isBlank()) break;

                    // фильтруем только если поле LocalDateTime
                    if (!LocalDateTime.class.isAssignableFrom(path.getJavaType())) break;

                    LocalDate d1 = safeParseOrNull(d1s);
                    if (d1 == null) break;

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
                            LocalDate d2 = safeParseOrNull(d2s);
                            if (d2 == null) d2 = d1;
                            LocalDateTime end = d2.plusDays(1).atStartOfDay();
                            out.add(cb.between(dt, start, end));
                        }
                        default -> { /* ignore */ }
                    }
                }
                case "set" -> {
                    @SuppressWarnings("unchecked")
                    List<String> values = (List<String>) fm.get("values");
                    if (values == null || values.isEmpty()) break;

                    CriteriaBuilder.In<Object> in = cb.in(path);
                    for (String v : values) {
                        in.value(castForPath(path, v));
                    }
                    out.add(in);
                }
                default -> {
                }
            }
        }
        return out;
    }

    /**
     * Универсальная сборка предикатов для числового поля конкретного типа.
     */
    private static <T extends Number & Comparable<T>> void addNumberPredicates(
            CriteriaBuilder cb,
            List<Predicate> out,
            String type,
            Expression<T> num,
            T v1,
            T v2
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
            default -> {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object castForPath(Path<?> path, String value) {
        Class<?> t = path.getJavaType();
        if (t.isEnum()) return Enum.valueOf((Class<Enum>) t, value);
        if (t.equals(Integer.class)) return Integer.valueOf(value);
        if (t.equals(Long.class)) return Long.valueOf(value);
        if (t.equals(Double.class)) return Double.valueOf(value);
        if (t.equals(Float.class)) return Float.valueOf(value);
        if (t.equals(BigDecimal.class)) return new BigDecimal(value);
        return value;
    }

    private Number toNumber(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n;
        return new BigDecimal(o.toString());
    }
}