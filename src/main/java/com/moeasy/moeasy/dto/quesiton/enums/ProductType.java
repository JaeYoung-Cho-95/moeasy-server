package com.moeasy.moeasy.dto.quesiton.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductType {
    PRODUCT,
    DIGITAL,
    EVENT,
    SHOP,
    BRAND,
    CONTENT;

    @JsonCreator
    public static ProductType from(String s) {
        return ProductType.valueOf(s.toUpperCase());
    }
}
