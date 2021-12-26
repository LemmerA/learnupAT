package ru.learnup.javaqa.tests.product;

import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learnup.javaqa.dto.ProductDouble;
import ru.learnup.javaqa.dto.Product;
import ru.learnup.javaqa.tests.BaseTest;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ru.learnup.javaqa.Endpoints.PRODUCT_ENDPOINT;
import static ru.learnup.javaqa.Endpoints.PRODUCT_ID_ENDPOINT;
import static io.restassured.RestAssured.given;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;
import static ru.learnup.javaqa.enums.CategoryType.*;

public class PostProductTests extends BaseTest {
    Faker faker = new Faker();
    Product prod;
    ProductDouble prodDouble;
    ResponseSpecification postProductResponseSpec;

    private Product postProductOK() {
        return given()
                .body(prod)
                .contentType(ContentType.JSON)
                .response()
                .spec(postProductResponseSpec)
                .when()
                .post(PRODUCT_ENDPOINT)
                .prettyPeek()
                .body()
                .as(Product.class);
    }

    //Отказ от использования спеков для адекватного логирования: при провале валидации не печатался ответ
    private Response postProductErr() {
        return given()
                .body(prod)
                .contentType(ContentType.JSON)
                .when()
                .post(PRODUCT_ENDPOINT)
                .prettyPeek();
    }

    private Response postProductDouble() {
        return given()
                .body(prodDouble)
                .contentType(ContentType.JSON)
                .when()
                .post(PRODUCT_ENDPOINT)
                .prettyPeek();
    }

    @BeforeEach
    void setUp() {
        prod = Product.builder()
                .price(randomInt())
                .title(faker.food().ingredient())
                .categoryTitle(FOOD.getName())
                .build();

        //Создание дублера с теми же данными
        prodDouble = ProductDouble.builder()
                .price(prod.getPrice())
                .title(prod.getTitle())
                .categoryTitle(prod.getCategoryTitle())
                .build();

        postProductResponseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectStatusCode(201)
                .build();
    }

    @Test
    void postProductFood() {
        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductElectronic() {
        prod.setPrice(1);
        prod.setCategoryTitle(ELECTRONIC.getName());
        prod.setTitle(faker.hacker().noun());

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductFurniture() {
        prod.setPrice(Integer.MAX_VALUE);
        prod.setCategoryTitle(FURNITURE.getName());
        prod.setTitle(faker.commerce().productName());

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductIntoEmptyProductList() {
        prod.setCategoryTitle(F_35.getName());
        prod.setTitle(faker.aviation().aircraft());

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductEmptyJson() {
        ProductDouble empty = new ProductDouble();

        Response res = given()
                .body(empty)
                .contentType(ContentType.JSON)
                .when()
                .post(PRODUCT_ENDPOINT)
                .prettyPeek();

        assertProductBadRequest(prod, res);
    }

    //Id tests
    @Test
    void postProductIdMissing() {
        Response res = postProductDouble();

        assertProductEquals(prod, res.body().as(Product.class));
    }

    @Test
    void postProductIdNonNull() {
        prodDouble.setId(1L);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdNegative() {
        prodDouble.setId(-1L);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdPositiveIntOverflow() {
        prodDouble.setId(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdMax() {
        prodDouble.setId(Long.MAX_VALUE);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdMin() {
        prodDouble.setId(Long.MIN_VALUE);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdZero() {
        prodDouble.setId(0L);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________

    @Test
    void postProductIdPositiveFloat() {
        prodDouble.setId(3.1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdNegativeFloat() {
        prodDouble.setId(-3.1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdLongOverflow() {
        prodDouble.setId(Long.MAX_VALUE + "0");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductId100LongStr() {
        prodDouble.setId(latinCheck(100));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductId1000LongStr() {
        prodDouble.setId(latinCheck(1000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductId10000LongStr() {
        prodDouble.setId(latinCheck(10000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdEmpty() {
        prodDouble.setId("");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdUndefined() {
        prodDouble.setId("undefined");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdNullString() {
        prodDouble.setId("null");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductIdWhitespaces(char space) {
        prodDouble.setId(space);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductIdAscii(char symbol) {
        prodDouble.setId(symbol);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdLatin() {
        prodDouble.setId(latinCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdAlphanumeric() {
        prodDouble.setId(alphanumericCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdHex() {
        prodDouble.setId(hexCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdCyrillic() {
        prodDouble.setId(cyrillicCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdHieroglyphs() {
        prodDouble.setId(hieroglyphCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdExtUnicode() {
        prodDouble.setId(extUnicodeCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductIdArray() {
        prodDouble.setId(new int[]{randomInt(), randomInt(), randomInt()});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Title tests
    @Test
    void postProductTitleMissing() {
        prodDouble.setTitle(null);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductTitleNumeric() {
        prod.setTitle(numericCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitle100LongStr() {
        prod.setTitle(latinCheck(100));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitle1000LongStr() {
        prod.setTitle(latinCheck(1000));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitle10000LongStr() {
        prod.setTitle(latinCheck(10000));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleZero() {
        prod.setTitle("0");

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleEmpty() {
        prod.setTitle("");

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductTitleUndefined() {
        prod.setTitle("undefined");

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleNull() {
        prod.setTitle(null);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductTitleWhitespaces(String space) {
        prod.setTitle(space);

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductTitleAscii(String symbol) {
        prod.setTitle(symbol);

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleAlphanumeric() {
        prod.setTitle(alphanumericCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleHex() {
        prod.setTitle(hexCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleCyrillic() {
        prod.setTitle(cyrillicCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleHieroglyphs() {
        prod.setTitle(hieroglyphCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleExtUnicode() {
        prod.setTitle(extUnicodeCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductTitleArray() {
        prodDouble.setTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Price tests
    @Test
    void postProductPriceMissing() {
        prodDouble.setPrice(null);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceNegative() {
        prod.setPrice(-1);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceZero() {
        prod.setPrice(0);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceNull() {
        prod.setPrice(null);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceMax() {
        prod.setPrice(Integer.MAX_VALUE);

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void postProductPriceMin() {
        prod.setPrice(Integer.MIN_VALUE);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________
    @Test
    void postProductPriceCurrency() {
        prodDouble.setPrice("$1,000.00");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPricePositiveFloat() {
        prodDouble.setPrice(3.01);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceNegativeFloat() {
        prodDouble.setPrice(-3.01);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceLongOverflow() {
        prodDouble.setPrice(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPrice100LongStr() {
        prodDouble.setPrice(latinCheck(100));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPrice1000LongStr() {
        prodDouble.setPrice(latinCheck(1000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPrice10000LongStr() {
        prodDouble.setPrice(latinCheck(10000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceEmpty() {
        prodDouble.setPrice("");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceUndefined() {
        prodDouble.setPrice("undefined");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceNullString() {
        prodDouble.setPrice("null");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductPriceWhitespaces(char space) {
        prodDouble.setPrice(space);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductPriceAscii(char symbol) {
        prodDouble.setPrice(symbol);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceLatin() {
        prodDouble.setPrice(latinCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceAlphanumeric() {
        prodDouble.setPrice(alphanumericCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceHex() {
        prodDouble.setPrice(hexCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceCyrillic() {
        prodDouble.setPrice(cyrillicCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceHieroglyphs() {
        prodDouble.setPrice(hieroglyphCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceExtUnicode() {
        prodDouble.setPrice(extUnicodeCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductPriceArray() {
        prodDouble.setPrice(new int[]{1, 2, 3});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //CategoryTitle tests
    @Test
    void postProductCategoryMissing() {
        prodDouble.setCategoryTitle(null);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductCategoryIncorrect() {
        prod.setCategoryTitle(NOT_AVAILABLE.getName());

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategory100LongStr() {
        prod.setCategoryTitle(latinCheck(100));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategory1000LongStr() {
        prod.setCategoryTitle(latinCheck(1000));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategory10000LongStr() {
        prod.setCategoryTitle(latinCheck(10000));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryZero() {
        prod.setCategoryTitle("0");

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryNull() {
        prod.setCategoryTitle(null);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductCategoryEmpty() {
        prod.setCategoryTitle("");

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void postProductCategoryUndefined() {
        prod.setCategoryTitle("undefined");

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductCategoryWhitespaces(String space) {
        prod.setCategoryTitle(space);

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductCategoryAscii(String symbol) {
        prod.setCategoryTitle(symbol);

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryAlphanumeric() {
        prod.setCategoryTitle(alphanumericCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryHex() {
        prod.setCategoryTitle(hexCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryCyrillic() {
        prod.setCategoryTitle(cyrillicCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryHieroglyphs() {
        prod.setCategoryTitle(hieroglyphCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryExtUnicode() {
        prod.setCategoryTitle(extUnicodeCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void postProductCategoryArray() {
        prodDouble.setCategoryTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @AfterEach
    void tearDown() {
        //Если ассерт не вернул ид продукта
        if (prod.getId() != null) {
            //Удаление
            given()
                    .when()
                    .delete(PRODUCT_ID_ENDPOINT, prod.getId())
                    .prettyPeek();
            //Проверка удаления
            given()
                    .when()
                    .get(PRODUCT_ID_ENDPOINT, prod.getId())
                    .prettyPeek();
        }
    }
}
