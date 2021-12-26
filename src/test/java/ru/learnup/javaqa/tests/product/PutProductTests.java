package ru.learnup.javaqa.tests.product;

import com.github.javafaker.Faker;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learnup.javaqa.dto.ProductDouble;
import ru.learnup.javaqa.dto.Product;
import ru.learnup.javaqa.tests.BaseTest;

import static io.restassured.RestAssured.given;
import static ru.learnup.javaqa.Endpoints.PRODUCT_ENDPOINT;
import static ru.learnup.javaqa.Endpoints.PRODUCT_ID_ENDPOINT;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;
import static ru.learnup.javaqa.enums.CategoryType.*;

public class PutProductTests extends BaseTest {
    Faker faker = new Faker();
    Product prod;
    ProductDouble prodDouble;
    ResponseSpecification postProductResponseSpec;
    ResponseSpecification putProductResponseSpec;

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

    private Product putProductOK() {
        return given()
                .body(prod)
                .contentType(ContentType.JSON)
                .when()
                .put(PRODUCT_ENDPOINT)
                .prettyPeek()
                .body()
                .as(Product.class);
    }

    //Отказ от использования спеков для адекватного логирования: при провале валидации не печатался ответ
    private Response putProductErr() {
        return given()
                .body(prod)
                .contentType(ContentType.JSON)
                .when()
                .put(PRODUCT_ENDPOINT)
                .prettyPeek();
    }

    private Response putProductDouble() {
        return given()
                .body(prodDouble)
                .contentType(ContentType.JSON)
                .when()
                .put(PRODUCT_ENDPOINT)
                .prettyPeek();
    }

    @BeforeEach
    void setUp() {
        prod = Product.builder()
                .price(randomInt())
                .title(faker.food().ingredient())
                .categoryTitle(FOOD.getName())
                .build();

        prodDouble = ProductDouble.builder()
                .price(prod.getPrice())
                .title(prod.getTitle())
                .categoryTitle(prod.getCategoryTitle())
                .build();

        postProductResponseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectStatusCode(201)
                .build();

        Product p = postProductOK();

        prod.setId(p.getId());
        prodDouble.setId(prod.getId());
    }

    @Test
    void putProductInit() {
        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductIntoNonexistent() {
        prodDouble.setId(Long.MAX_VALUE);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductAll() {
        prod.setPrice(1);
        prod.setCategoryTitle(ELECTRONIC.getName());
        prod.setTitle(faker.hacker().noun());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductAll2() {
        prod.setPrice(randomInt());
        prod.setCategoryTitle(MIG_21.getName());
        prod.setTitle(faker.aviation().aircraft());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductAll3() {
        prod.setPrice(Integer.MAX_VALUE);
        prod.setCategoryTitle(FURNITURE.getName());
        prod.setTitle(faker.commerce().productName());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductIntoEmptyProductList() {
        prod.setCategoryTitle(F_35.getName());
        prod.setTitle(faker.aviation().aircraft());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductEmptyJson() {
        ProductDouble empty = new ProductDouble();

        Response res = given()
                .body(empty)
                .contentType(ContentType.JSON)
                .when()
                .put(PRODUCT_ENDPOINT)
                .prettyPeek();

        assertProductBadRequest(prod, res);
    }

    //Id tests
    @Test
    void putProductIdMissing() {
        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdNegative() {
        prodDouble.setId(prod.getId() * -1L);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdPositiveIntOverflow() {
        prodDouble.setId(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdMax() {
        prodDouble.setId(Long.MAX_VALUE);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdMin() {
        prodDouble.setId(Long.MIN_VALUE);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdZero() {
        prodDouble.setId(0L);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________

    @Test
    void putProductIdPositiveFloat() {
        prodDouble.setId(prod.getId() + 0.1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdNegativeFloat() {
        prodDouble.setId(prod.getId() * -1L + 0.1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdLongOverflow() {
        prodDouble.setId(Long.MAX_VALUE + "0");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductId100LongStr() {
        prodDouble.setId(latinCheck(100));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductId1000LongStr() {
        prodDouble.setId(latinCheck(1000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductId10000LongStr() {
        prodDouble.setId(latinCheck(10000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdEmpty() {
        prodDouble.setId("");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdUndefined() {
        prodDouble.setId("undefined");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdNullString() {
        prodDouble.setId("null");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductIdWhitespaces(char space) {
        prodDouble.setId(space);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductIdAscii(char symbol) {
        prodDouble.setId(symbol);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdLatin() {
        prodDouble.setId(latinCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdAlphanumeric() {
        prodDouble.setId(alphanumericCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdHex() {
        prodDouble.setId(hexCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdCyrillic() {
        prodDouble.setId(cyrillicCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdHieroglyphs() {
        prodDouble.setId(hieroglyphCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdExtUnicode() {
        prodDouble.setId(extUnicodeCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductIdArray() {
        prodDouble.setId(new int[]{randomInt(), randomInt(), randomInt()});

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Title tests
    @Test
    void putProductTitleMissing() {
        prodDouble.setTitle(null);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductTitleNumeric() {
        prod.setTitle(numericCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitle100LongStr() {
        prod.setTitle(latinCheck(100));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitle1000LongStr() {
        prod.setTitle(latinCheck(1000));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitle10000LongStr() {
        prod.setTitle(latinCheck(10000));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleZero() {
        prod.setTitle("0");

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleEmpty() {
        prod.setTitle("");

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductTitleUndefined() {
        prod.setTitle("undefined");

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleNull() {
        prod.setTitle(null);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductTitleWhitespaces(String space) {
        prod.setTitle(space);

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductTitleAscii(String symbol) {
        prod.setTitle(symbol);

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleAlphanumeric() {
        prod.setTitle(alphanumericCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleHex() {
        prod.setTitle(hexCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleCyrillic() {
        prod.setTitle(cyrillicCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleHieroglyphs() {
        prod.setTitle(hieroglyphCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleExtUnicode() {
        prod.setTitle(extUnicodeCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductTitleArray() {
        prodDouble.setTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Price tests
    @Test
    void putProductPriceMissing() {
        prodDouble.setPrice(null);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceNegative() {
        prod.setPrice(-1);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceZero() {
        prod.setPrice(0);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceNull() {
        prod.setPrice(null);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceMax() {
        prod.setPrice(Integer.MAX_VALUE);

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    void putProductPriceMin() {
        prod.setPrice(Integer.MIN_VALUE);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________

    @Test
    void putProductPriceCurrency() {
        prodDouble.setPrice("$1,000.00");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPricePositiveFloat() {
        prodDouble.setPrice(3.01);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceNegativeFloat() {
        prodDouble.setPrice(-3.01);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceLongOverflow() {
        prodDouble.setPrice(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPrice100LongStr() {
        prodDouble.setPrice(latinCheck(100));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPrice1000LongStr() {
        prodDouble.setPrice(latinCheck(1000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPrice10000LongStr() {
        prodDouble.setPrice(latinCheck(10000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceEmpty() {
        prodDouble.setPrice("");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceUndefined() {
        prodDouble.setPrice("undefined");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceNullString() {
        prodDouble.setPrice("null");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductPriceWhitespaces(char space) {
        prodDouble.setPrice(space);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductPriceAscii(char symbol) {
        prodDouble.setPrice(symbol);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceLatin() {
        prodDouble.setPrice(latinCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceAlphanumeric() {
        prodDouble.setPrice(alphanumericCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceHex() {
        prodDouble.setPrice(hexCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceCyrillic() {
        prodDouble.setPrice(cyrillicCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceHieroglyphs() {
        prodDouble.setPrice(hieroglyphCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceExtUnicode() {
        prodDouble.setPrice(extUnicodeCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductPriceArray() {
        prodDouble.setPrice(new int[]{1, 2, 3});

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //CategoryTitle tests
    @Test
    void putProductCategoryMissing() {
        prodDouble.setCategoryTitle(null);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductCategoryIncorrect() {
        prod.setCategoryTitle(NOT_AVAILABLE.getName());

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategory100LongStr() {
        prod.setCategoryTitle(latinCheck(100));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategory1000LongStr() {
        prod.setCategoryTitle(latinCheck(1000));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategory10000LongStr() {
        prod.setCategoryTitle(latinCheck(10000));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryZero() {
        prod.setCategoryTitle("0");

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryNull() {
        prod.setCategoryTitle(null);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductCategoryEmpty() {
        prod.setCategoryTitle("");

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    void putProductCategoryUndefined() {
        prod.setCategoryTitle("undefined");

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void postProductCategoryWhitespaces(String space) {
        prod.setCategoryTitle(space);

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void postProductCategoryAscii(String symbol) {
        prod.setCategoryTitle(symbol);

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryAlphanumeric() {
        prod.setCategoryTitle(alphanumericCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryHex() {
        prod.setCategoryTitle(hexCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryCyrillic() {
        prod.setCategoryTitle(cyrillicCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryHieroglyphs() {
        prod.setCategoryTitle(hieroglyphCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryExtUnicode() {
        prod.setCategoryTitle(extUnicodeCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Test
    void putProductCategoryArray() {
        prodDouble.setCategoryTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }
    
    @AfterEach
    void tearDown() {
        if (prod.getId() != null) {
            given()
                    .when()
                    .delete(PRODUCT_ID_ENDPOINT, prod.getId())
                    .prettyPeek();
            given()
                    .when()
                    .get(PRODUCT_ID_ENDPOINT, prod.getId())
                    .prettyPeek();
        }
    }
}
