package ru.learnup.javaqa.asserts;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import ru.learnup.javaqa.db.model.Categories;
import ru.learnup.javaqa.db.model.Products;
import ru.learnup.javaqa.db.model.ProductsExample;
import ru.learnup.javaqa.dto.Category;
import ru.learnup.javaqa.dto.Product;
import lombok.experimental.UtilityClass;
import ru.learnup.javaqa.enums.CategoryType;
import ru.learnup.javaqa.utils.DbUtils;

import java.util.List;

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

        //Ищем продукт по полученному ИД
        Products dbProduct = DbUtils.getProductsMapper().selectByPrimaryKey(id);
        //Ищем категорию по ее ИД из продукта
        Categories dbCategory = DbUtils.getCategoryMapper()
                .selectByPrimaryKey(dbProduct.getCategory_id());

        //Сравниваем название категории
        assertThat(actual.getCategoryTitle(), equalTo(dbCategory.getTitle()));
        assertThat(actual.getTitle(), equalTo(dbProduct.getTitle()));
        assertThat(actual.getPrice(), equalTo(dbProduct.getPrice()));
    }

    @Step("Убедиться, что категория из ответа совпадает с категорией из запроса")
    public void assertCategoryEquals(CategoryType expected, Category actual) {
        Categories dbCategory = DbUtils.getCategoryMapper()
                .selectByPrimaryKey(expected.getId());

        assertThat(actual.getId(), equalTo(dbCategory.getId()));
        assertThat(actual.getTitle(), equalTo(dbCategory.getTitle()));
        assertThat(actual.getProducts(), isProductArray());

        //Критерий поиска продуктов по ИД категории
        ProductsExample searchProducts = new ProductsExample();
        searchProducts
                .createCriteria()
                .andCategory_idEqualTo(dbCategory.getId());

        //Собираем лист из записей
        List<Products> dbProductList = DbUtils.getProductsMapper()
                .selectByExample(searchProducts);

        //Сравниваем категории из листа с ожидаемой
        dbProductList.forEach(p ->
                assertThat(p.getCategory_id(), equalTo(expected.getId())));
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
            Products dbProduct = DbUtils.getProductsMapper().selectByPrimaryKey(id);
            p.setId(dbProduct.getId());
        } catch (NullPointerException ignored) {}

        assertNotFound(res);
    }

    @Step("Убедиться, что ответ на запрос - 400 Bad Request")
    public void assertProductBadRequest(Product p, Response res) {
        //Для правильного отслеживания ид в случае успешного запроса
        Long id;
        try {
            id = res.body().jsonPath().getLong("id");
            Products dbProduct = DbUtils.getProductsMapper().selectByPrimaryKey(id);
            p.setId(dbProduct.getId());
        } catch (NullPointerException ignored) {}

        assertBadRequest(res);
    }
}