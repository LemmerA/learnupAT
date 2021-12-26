package ru.learnup.javaqa.tests.product;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learnup.javaqa.dto.Product;
import ru.learnup.javaqa.dto.Products;
import ru.learnup.javaqa.tests.BaseTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.learnup.javaqa.Endpoints.*;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;
import static ru.learnup.javaqa.asserts.IsProductArray.isProductArray;

public class GetProductTests extends BaseTest {
    private Product prod = new Product();

    private Product getProductOK() {
        return given()
                .response()
                .spec(productsResSpec)
                .when()
                .get(PRODUCT_ID_ENDPOINT, prod.getId())
                .prettyPeek()
                .body()
                .as(Product.class);
    }

    //Отказ от использования спеков для адекватного логирования: при провале валидации не печатался ответ
    private Response getProductErr(Object id) {
        return given()
                .when()
                .get(PRODUCT_ID_ENDPOINT, id)
                .prettyPeek();
    }

    @Test
    void getAllProducts() {
        Products res = given()
                .response()
                .spec(productsResSpec)
                .when()
                .get(PRODUCT_ENDPOINT)
                .prettyPeek()
                .body()
                .as(Products.class);

        assertThat(res.getProducts(), isProductArray());
    }

    @Test
    void getProductFound() {
        prod.setId(20442L);

        Product res = getProductOK();

        assertThat(res.getId(), equalTo(prod.getId()));
    }

    @Test
    void getProductIdByTitle() {
        Response res = getProductErr("Apples");

        assertBadRequest(res);
    }

    @Test
    void getProductNotFound() {
        prod.setId(1L);
        Response res = getProductErr(prod.getId());

        assertNotFound(res);
    }

    @Test
    void getProductNegative() {
        Response res = getProductErr(-1);

        assertBadRequest(res);
    }


    @Test
    void getProductPositiveFloat() {
        Response res = getProductErr(3.1);

        assertBadRequest(res);
    }

    @Test
    void getProductNegativeFloat() {
        Response res = getProductErr(-3.1);

        assertBadRequest(res);
    }

    @Test
    void getProductPositiveIntOverflow() {
        Response res = getProductErr(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        assertNotFound(res);
    }

    @Test
    void getProductMax() {
        Response res = getProductErr(Long.MAX_VALUE);

        assertNotFound(res);
    }

    @Test
    void getProductMin() {
        Response res = getProductErr(Long.MIN_VALUE);

        assertBadRequest(res);
    }

    @Test
    void getProductLongOverflow() {
        Response res = getProductErr(Long.MAX_VALUE + "0");

        assertBadRequest(res);
    }

    @Test
    void getProduct100LongStr() {
        Response res = getProductErr(latinCheck(100));

        assertBadRequest(res);
    }

    @Test
    void getProduct1000LongStr() {
        Response res = getProductErr(latinCheck(1000));

        assertBadRequest(res);
    }

    @Test
    void getProduct10000LongStr() {
        Response res = getProductErr(latinCheck(10000));

        assertBadRequest(res);
    }

    @Test
    void getProductZero() {
        Response res = getProductErr(0);

        assertBadRequest(res);
    }

    @Test
    void getProductEmpty() {
        Response res = getProductErr("");

        assertBadRequest(res);
    }

    @Test
    void getProductUndefined() {
        Response res = getProductErr("undefined");

        assertBadRequest(res);
    }

    @Test
    void getProductNull() {
        Response res = getProductErr("null");

        assertBadRequest(res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void getProductWhitespaces(char space) {
        Response res = getProductErr(space);

        assertBadRequest(res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void getProductAscii(char symbol) {
        Response res = getProductErr("2" + symbol + "2");

        assertBadRequest(res);
    }

    @Test
    void getProductAlphanumeric() {
        Response res = getProductErr(alphanumericCheck(10));

        assertBadRequest(res);
    }

    @Test
    void getProductHex() {
        Response res = getProductErr(hexCheck(10));

        assertBadRequest(res);
    }

    @Test
    void getProductCyrillic() {
        Response res = getProductErr(cyrillicCheck(1));

        assertBadRequest(res);
    }

    @Test
    void getProductHieroglyphs() {
        Response res = getProductErr(hieroglyphCheck(1));

        assertBadRequest(res);
    }

    @Test
    void getProductExtUnicode() {
        Response res = getProductErr(extUnicodeCheck(1));

        assertBadRequest(res);
    }
}
