package com.finance.api.common.util;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    public static <T> Specification<T> equalIfNotNull(String property, Object value) {
        return value == null ? null : (root, q, cb) -> cb.equal(root.get(property), value);
    }

    public static <T> Specification<T> likeInsensitiveIfNotBlank(String property, String term) {
        if (term == null || term.isBlank()) {
            return null;
        }
        return (root, q, cb) -> cb.like(cb.lower(root.get(property)), "%" + term.toLowerCase() + "%");
    }

    public static <T> Specification<T> inIfNotEmpty(String property, Collection<?> values) {
        return (values == null || values.isEmpty())
                ? null
                : (root, q, cb) -> root.get(property).in(values);
    }

    public static <T> Specification<T> ownedBy(String property, UUID userId) {
        return userId == null ? null : (root, q, cb) -> cb.equal(root.get(property), userId);
    }

    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specs) {
        Specification<T> result = Specification.where(null);
        if (specs != null) {
            for (Specification<T> s : specs) {
                if (s != null) {
                    result = result.and(s);
                }
            }
        }
        return result;
    }
}
