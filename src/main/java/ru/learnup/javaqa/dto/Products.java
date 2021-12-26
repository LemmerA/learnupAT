package ru.learnup.javaqa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//Класс для сериализации гет-запроса на все продукты
@Data
@NoArgsConstructor
public class Products {
    private List<Product> products;
}
