package ru.itmo.isitmolab.grid;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.*;
import java.util.function.BiFunction;

public final class AgGridCriteriaBuilder<T> {

    private final EntityManager em;
    private final Class<T> entityClass;

    public AgGridCriteriaBuilder(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    /** Построить TypedQuery и COUNT по filterModel/sortModel/start/limit */
    public Result<T> buildAndExecute(Map<String, Object> filterModel,
                                     List<Map<String, Object>> sortModel,
                                     int startRow, int limit) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // SELECT
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        List<Predicate> predicates = parseFilterModel(cb, root, filterModel);

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(Predicate[]::new)));
        }

        // сортировки
        if (sortModel != null) {
            List<Order> orders = new ArrayList<>();
            for (Map<String, Object> s : sortModel) {
                String col = (String) s.get("colId");
                String dir = (String) s.get("sort"); // "asc"/"desc"
                if (col != null && dir != null) {
                    Path<?> p = root.get(col);
                    orders.add("asc".equalsIgnoreCase(dir) ? cb.asc(p) : cb.desc(p));
                }
            }
            if (!orders.isEmpty()) cq.orderBy(orders);
        }

        TypedQuery<T> dataQuery = em.createQuery(cq);
        dataQuery.setFirstResult(startRow);
        dataQuery.setMaxResults(limit);
        List<T> data = dataQuery.getResultList();

        // COUNT
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        List<Predicate> countPredicates = parseFilterModel(cb, countRoot, filterModel);
        countQuery.select(cb.count(countRoot));
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(Predicate[]::new)));
        }
        Long total = em.createQuery(countQuery).getSingleResult();

        return new Result<>(data, total.intValue());
    }

    /** Разбор filterModel ag-Grid -> Predicates (поддержка compound filters) */
    @SuppressWarnings("unchecked")
    private List<Predicate> parseFilterModel(CriteriaBuilder cb, Root<T> root,
                                             Map<String, Object> filterModel) {
        List<Predicate> predicates = new ArrayList<>();
        if (filterModel == null) return predicates;

        for (Map.Entry<String, Object> e : filterModel.entrySet()) {
            String field = e.getKey();
            Object spec = e.getValue();
            Path<?> path = root.get(field);

            if (!(spec instanceof Map)) continue;
            Map<String, Object> fm = (Map<String, Object>) spec;

            // compound filter?
            String operator = (String) fm.get("operator"); // "AND"/"OR" or null
            if (operator != null) {
                Map<String, Object> c1 = (Map<String, Object>) fm.get("condition1");
                Map<String, Object> c2 = (Map<String, Object>) fm.get("condition2");
                Predicate p1 = oneCondition(cb, path, c1);
                Predicate p2 = oneCondition(cb, path, c2);
                if (p1 != null && p2 != null) {
                    predicates.add("AND".equalsIgnoreCase(operator) ? cb.and(p1, p2) : cb.or(p1, p2));
                } else if (p1 != null) predicates.add(p1);
                else if (p2 != null) predicates.add(p2);
            } else {
                Predicate p = oneCondition(cb, path, fm);
                if (p != null) predicates.add(p);
            }
        }
        return predicates;
    }

    /** Один элементарный фильтр AG Grid */
    private Predicate oneCondition(CriteriaBuilder cb, Path<?> path, Map<String, Object> cond) {
        if (cond == null) return null;
        String type = (String) cond.get("filterType"); // "text" | "number" | "date" ...
        String op   = (String) cond.get("type");
        Object filter = cond.get("filter");
        Object filterTo = cond.get("filterTo"); // inRange

        if ("text".equals(type)) {
            if (!(path.getJavaType() == String.class)) return null;
            String val = filter != null ? filter.toString() : "";
            String like = "%" + val.toLowerCase() + "%";
            Expression<String> expr = cb.lower((Path<String>) path);
            return switch (op) {
                case "contains"       -> cb.like(expr, like);
                case "notContains"    -> cb.notLike(expr, like);
                case "equals"         -> cb.equal(expr, val.toLowerCase());
                case "notEqual"       -> cb.notEqual(expr, val.toLowerCase());
                case "startsWith"     -> cb.like(expr, val.toLowerCase() + "%");
                case "endsWith"       -> cb.like(expr, "%" + val.toLowerCase());
                default -> null;
            };
        }

        if ("number".equals(type)) {
            Class<?> jt = path.getJavaType();
            Number a = castNumber(filter, jt);
            Number b = castNumber(filterTo, jt);

            if (Number.class.isAssignableFrom(jt)) {
                Expression<Number> expr = (Expression<Number>) path;
                return switch (op) {
                    case "equals"                -> cb.equal(expr, a);
                    case "notEqual"              -> cb.notEqual(expr, a);
                    case "lessThan"              -> cb.lt(expr, a);
                    case "lessThanOrEqual"       -> cb.le(expr, a);
                    case "greaterThan"           -> cb.gt(expr, a);
                    case "greaterThanOrEqual"    -> cb.ge(expr, a);
                    case "inRange"               -> (a != null && b != null) ? cb.and(cb.ge(expr, a), cb.le(expr, b)) : null;
                    default -> null;
                };
            }
        }

        return null;
    }

    private Number castNumber(Object v, Class<?> jt) {
        if (v == null) return null;
        if (jt == Integer.class) return Integer.valueOf(v.toString());
        if (jt == Long.class)    return Long.valueOf(v.toString());
        if (jt == Double.class)  return Double.valueOf(v.toString());
        if (jt == Float.class)   return Float.valueOf(v.toString());
        if (jt == Short.class)   return Short.valueOf(v.toString());
        return Double.valueOf(v.toString());
    }

    public static final class Result<T> {
        public final List<T> data;
        public final int total;
        public Result(List<T> data, int total) { this.data = data; this.total = total; }
    }
}