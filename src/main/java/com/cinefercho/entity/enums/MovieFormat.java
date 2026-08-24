package com.cinefercho.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum MovieFormat {
    TWO_D("2D"),
    THREE_D("3D"),
    XD("XD");

    private final String label;

    MovieFormat(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static MovieFormat from(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(format -> format.name().equalsIgnoreCase(value) || format.label.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Formato de película inválido: " + value));
    }
}
