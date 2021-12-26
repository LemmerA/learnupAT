package ru.learnup.javaqa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Category {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("products")
    private List<Product> products;

    @JsonProperty("title")
    private String title;
}