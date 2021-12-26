package ru.learnup.javaqa.asserts;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import ru.learnup.javaqa.dto.Category;
import ru.learnup.javaqa.dto.Product;
import lombok.experimental.UtilityClass;
import ru.learnup.javaqa.enums.CategoryType;

import static org.hamcrest.Matchers.notNullValue;
import static ru.learnup.javaqa.asserts.IsCategoryExists.isCategoryExists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static ru.learnup.javaqa.asserts.IsProductArray.isProductArray;

@UtilityClass
public class CommonAsserts {

    @Step("Убедиться, что продукт из ответа совпадает с продуктом из запроса")
    public void assertProductEquals(Product expected, Product actual) {
        //Для правильного отслеживания ид для удаления
        Long id = actual.getId();
        expected.setId(id);
        assertThat(id, notNullValue());
        assertThat(actual.getCategoryTitle(), isCategoryExists());
        assertThat(actual.getTitle(), equalTo(expected.getTitle()));
        assertThat(actual.getPrice(), equalTo(expected.getPrice()));
    }

    @Step("Убедиться, что категория из ответа совпадает с категорией из запроса")
    public void assertCategoryEquals(CategoryType expected, Category actual) {
        assertThat(actual.getId(), equalTo(expected.getId()));
        assertThat(actual.getTitle(), equalTo(expected.getName()));
        assertThat(actual.getProducts(), isProductArray());
        actual.getProducts().forEach(p -> assertThat(p.getCategoryTitle(), equalTo(expected.getName())));
    }

    //Проверка на статус 404 после получения запроса
    @Step("Убедиться, что ответ на запрос - 404 Not Found")
    public void assertNotFound(Response res) {
        assertThat(res.statusCode(), equalTo(404));
        assertThat(res.contentType(), equalTo(ContentType.JSON.toString()));
    }

    //Проверка на статус 400 после получения запроса
    @Step("Убедиться, что ответ на запрос - 400 Bad Request")
    public void assertBadRequest(Response res) {
        assertThat(res.statusCode(), equalTo(400));
        assertThat(res.contentType(), equalTo(ContentType.JSON.toString()));
    }

    @Step("Убедиться, что ответ на запрос - 404 Not Found")
    public void assertProductNotFound(Product p, Response res) {
        //Для правильного отслеживания ид в случае успешного запроса
        Long id;
        try {
            id = res.body().jsonPath().getLong("id");
            p.setId(id);
        } catch (NullPointerException ignored) {}

        assertNotFound(res);
    }

    @Step("Убедиться, что ответ на запрос - 400 Bad Request")
    public void assertProductBadRequest(Product p, Response res) {
        //Для правильного отслеживания ид в случае успешного запроса
        Long id;
        try {
            id = res.body().jsonPath().getLong("id");
            p.setId(id);
        } catch (NullPointerException ignored) {}

        assertBadRequest(res);
    }
}