package com.finance.api.common.mapper;

import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;

public final class MappingUtils {

    private MappingUtils() {
    }

    public static <S, T> T mapTo(S source, Supplier<T> targetFactory) {
        T target = targetFactory.get();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <S, T> T copyTo(S source, T target) {
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
