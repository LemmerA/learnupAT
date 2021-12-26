package ru.learnup.javaqa.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

//Класс-двойник для более гибкой сериализации запросов
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
public class ProductDouble {
    @JsonProperty("id")
    private Object id;

    @JsonProperty("title")
    private Object title;

    @JsonProperty("price")
    private Object price;

    @JsonProperty("categoryTitle")
    private Object categoryTitle;
}
