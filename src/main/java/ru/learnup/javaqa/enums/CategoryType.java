package ru.learnup.javaqa.enums;

import lombok.Getter;

@Getter
public enum CategoryType {
    FOOD(1L, "Food"),
    ELECTRONIC(2L, "Electronic"),
    FURNITURE(3L, "Furniture"),
    NOT_AVAILABLE(567L, "Not Available"),
    COOKIES(77L, "Cookies"),
    CLOTHES(100L, "Clothes"),
    NULL(515L, "null"),
    GIRAFFE(616L, "giraffe"),
    BEAR(617L, "bear"),
    SWAN(618L, "swan"),
    AN_70(619L, "An-70"),
    MIG_21(620L, "MiG-21"),
    ZLIN_Z_50(621L, "Zlín Z-50"),
    F_35(622L, "F-35");

    private Long id;
    private String name;

    CategoryType(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}