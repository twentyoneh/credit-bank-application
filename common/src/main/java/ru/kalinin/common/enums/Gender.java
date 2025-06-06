package ru.kalinin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum Gender {
    MALE("мужчина", 1),
    FEMALE("женщина", 2),
    NON_BINARY("не бинарный", 3);
    private final String title;
    private final int code;
}