package ru.learnup.javaqa.tests.product;

import com.github.javafaker.Faker;
import io.qameta.allure.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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

import static io.qameta.allure.SeverityLevel.*;
import static ru.learnup.javaqa.Endpoints.PRODUCT_ENDPOINT;
import static ru.learnup.javaqa.Endpoints.PRODUCT_ID_ENDPOINT;
import static io.restassured.RestAssured.given;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;
import static ru.learnup.javaqa.enums.CategoryType.*;

@Epic("Тесты для контроллера продуктов")
@Feature("POST Product")
@Severity(NORMAL)
public class PostProductTests extends BaseTest {
    Faker faker = new Faker();
    Product prod;
    ProductDouble prodDouble;
    ResponseSpecification postProductResponseSpec;

    @Step("Отправить POST-запрос на контроллер продуктов")
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
    @Step("Отправить POST-запрос на контроллер продуктов")
    private Response postProductErr() {
        return given()
                .body(prod)
                .contentType(ContentType.JSON)
                .when()
                .post(PRODUCT_ENDPOINT)
                .prettyPeek();
    }

    @Step("Отправить POST-запрос на контроллер продуктов")
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

    @Severity(BLOCKER)
    @Test
    @Story("Создать продукт в категории 1")
    void postProductFood() {
        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Создать продукт в категории 2")
    void postProductElectronic() {
        prod.setPrice(1);
        prod.setCategoryTitle(ELECTRONIC.getName());
        prod.setTitle(faker.hacker().noun());

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Создать продукт в категории 3")
    void postProductFurniture() {
        prod.setPrice(Integer.MAX_VALUE);
        prod.setCategoryTitle(FURNITURE.getName());
        prod.setTitle(faker.commerce().productName());

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Создать продукт в пустой категории")
    void postProductIntoEmptyProductList() {
        prod.setCategoryTitle(F_35.getName());
        prod.setTitle(faker.aviation().aircraft());

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить пустой JSON-запрос (без полей)")
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
    @Story("Отправить JSON-запрос без поля идентификатора")
    void postProductIdMissing() {
        Response res = postProductDouble();

        assertProductEquals(prod, res.body().as(Product.class));
    }

    @Severity(BLOCKER)
    @Test
    @Story("Отправить запрос с заполненным идентификатором")
    void postProductIdNonNull() {
        prodDouble.setId(1L);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с отрицательным идентификатором")
    void postProductIdNegative() {
        prodDouble.setId(-1L);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором, превышающим 32 бита")
    void postProductIdPositiveIntOverflow() {
        prodDouble.setId(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором с максимально возможным значением")
    void postProductIdMax() {
        prodDouble.setId(Long.MAX_VALUE);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором с минимально возможным значением")
    void postProductIdMin() {
        prodDouble.setId(Long.MIN_VALUE);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c нулевым идентификатором")
    void postProductIdZero() {
        prodDouble.setId(0L);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c положительным нецелым идентификатором")
    void postProductIdPositiveFloat() {
        prodDouble.setId(3.1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c отрицательным нецелым идентификатором")
    void postProductIdNegativeFloat() {
        prodDouble.setId(-3.1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором, превышающим максимально возможное значение")
    void postProductIdLongOverflow() {
        prodDouble.setId(Long.MAX_VALUE + "0");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором длиной 100")
    void postProductId100LongStr() {
        prodDouble.setId(latinCheck(100));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором длиной 1000")
    void postProductId1000LongStr() {
        prodDouble.setId(latinCheck(1000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором длиной 10000")
    void postProductId10000LongStr() {
        prodDouble.setId(latinCheck(10000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c пустым идентификатором")
    void postProductIdEmpty() {
        prodDouble.setId("");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором \"undefined\"")
    void postProductIdUndefined() {
        prodDouble.setId("undefined");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором \"null\"")
    void postProductIdNullString() {
        prodDouble.setId("null");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Отправить запрос с идентификатором из пробельного символа")
    void postProductIdWhitespaces(char space) {
        prodDouble.setId(space);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Отправить запрос с идентификатором из символа ASCII")
    void postProductIdAscii(char symbol) {
        prodDouble.setId(symbol);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором из латиницы")
    void postProductIdLatin() {
        prodDouble.setId(latinCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором из алфавитно-цифровых символов")
    void postProductIdAlphanumeric() {
        prodDouble.setId(alphanumericCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором из шестнадцатеричных символов")
    void postProductIdHex() {
        prodDouble.setId(hexCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором из кириллицы")
    void postProductIdCyrillic() {
        prodDouble.setId(cyrillicCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором из иероглифов")
    void postProductIdHieroglyphs() {
        prodDouble.setId(hieroglyphCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором из расширенного Юникода")
    void postProductIdExtUnicode() {
        prodDouble.setId(extUnicodeCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос с идентификатором в виде массива")
    void postProductIdArray() {
        prodDouble.setId(new int[]{randomInt(), randomInt(), randomInt()});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Title tests
    @Severity(CRITICAL)
    @Test
    @Story("Отправить JSON-запрос без поля названия товара")
    void postProductTitleMissing() {
        prodDouble.setTitle(null);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с числовым названием")
    void postProductTitleNumeric() {
        prod.setTitle(numericCheck(10));

        Product res = postProductOK();
        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с названием в 100 символов")
    void postProductTitle100LongStr() {
        prod.setTitle(latinCheck(100));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с названием в 1000 символов")
    void postProductTitle1000LongStr() {
        prod.setTitle(latinCheck(1000));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с названием в 10000 символов")
    void postProductTitle10000LongStr() {
        prod.setTitle(latinCheck(10000));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием \"0\"")
    void postProductTitleZero() {
        prod.setTitle("0");

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с пустым названием")
    void postProductTitleEmpty() {
        prod.setTitle("");

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Создать продукт с названием \"undefined\"")
    void postProductTitleUndefined() {
        prod.setTitle("undefined");

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с отсутствующим названием")
    void postProductTitleNull() {
        prod.setTitle(null);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Создать продукт с названием из пробельного символа")
    void postProductTitleWhitespaces(String space) {
        prod.setTitle(space);

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Создать продукт с названием из символа ASCII")
    void postProductTitleAscii(String symbol) {
        prod.setTitle(symbol);

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием из алфавитно-цифровых символов")
    void postProductTitleAlphanumeric() {
        prod.setTitle(alphanumericCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием из шестнадцатеричных символов")
    void postProductTitleHex() {
        prod.setTitle(hexCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием из кириллицы")
    void postProductTitleCyrillic() {
        prod.setTitle(cyrillicCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием из иероглифов")
    void postProductTitleHieroglyphs() {
        prod.setTitle(hieroglyphCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием из расширенного Юникода")
    void postProductTitleExtUnicode() {
        prod.setTitle(extUnicodeCheck(10));

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием из массива")
    void postProductTitleArray() {
        prodDouble.setTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Price tests
    @Severity(CRITICAL)
    @Test
    @Story("Отправить JSON-запрос без поля цены товара")
    void postProductPriceMissing() {
        prodDouble.setPrice(null);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с отрицательной ценой")
    void postProductPriceNegative() {
        prod.setPrice(-1);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с нулевой ценой")
    void postProductPriceZero() {
        prod.setPrice(0);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с отсутствующей ценой")
    void postProductPriceNull() {
        prod.setPrice(null);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с максимальной ценой")
    void postProductPriceMax() {
        prod.setPrice(Integer.MAX_VALUE);

        Product res = postProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    @Story("Создать продукт с минимальной ценой")
    void postProductPriceMin() {
        prod.setPrice(Integer.MIN_VALUE);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________

    @Test
    @Story("Создать продукт с ценой в долларовом формате")
    void postProductPriceCurrency() {
        prodDouble.setPrice("$1,000.00");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с положительной нецелой ценой")
    void postProductPricePositiveFloat() {
        prodDouble.setPrice(3.01);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с отрицательной нецелой ценой")
    void postProductPriceNegativeFloat() {
        prodDouble.setPrice(-3.01);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой, превышающей допустимую")
    void postProductPriceLongOverflow() {
        prodDouble.setPrice(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой в 100 символов")
    void postProductPrice100LongStr() {
        prodDouble.setPrice(latinCheck(100));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой в 1000 символов")
    void postProductPrice1000LongStr() {
        prodDouble.setPrice(latinCheck(1000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой в 10000 символов")
    void postProductPrice10000LongStr() {
        prodDouble.setPrice(latinCheck(10000));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с пустой ценой")
    void postProductPriceEmpty() {
        prodDouble.setPrice("");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с ценой \"undefined\"")
    void postProductPriceUndefined() {
        prodDouble.setPrice("undefined");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой \"null\"")
    void postProductPriceNullString() {
        prodDouble.setPrice("null");

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Создать продукт с ценой из пробельного символа")
    void postProductPriceWhitespaces(char space) {
        prodDouble.setPrice(space);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Создать продукт с ценой из ASCII-символа")
    void postProductPriceAscii(char symbol) {
        prodDouble.setPrice(symbol);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой из латиницы")
    void postProductPriceLatin() {
        prodDouble.setPrice(latinCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой из алфавитно-числовых символов")
    void postProductPriceAlphanumeric() {
        prodDouble.setPrice(alphanumericCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с ценой из шестнадцатерничных символов")
    void postProductPriceHex() {
        prodDouble.setPrice(hexCheck(10));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с ценой из кириллицы")
    void postProductPriceCyrillic() {
        prodDouble.setPrice(cyrillicCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с ценой из иероглифов")
    void postProductPriceHieroglyphs() {
        prodDouble.setPrice(hieroglyphCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с ценой из расширенного Юникода")
    void postProductPriceExtUnicode() {
        prodDouble.setPrice(extUnicodeCheck(1));

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Создать продукт с ценой из массива")
    void postProductPriceArray() {
        prodDouble.setPrice(new int[]{1, 2, 3});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    //CategoryTitle tests
    @Severity(CRITICAL)
    @Test
    @Story("Отправить JSON-запрос без поля названия категории")
    void postProductCategoryMissing() {
        prodDouble.setCategoryTitle(null);

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Создать продукт с несуществующей категорией")
    void postProductCategoryIncorrect() {
        prod.setCategoryTitle(NOT_AVAILABLE.getName());

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с названием категории в 100 символов")
    void postProductCategory100LongStr() {
        prod.setCategoryTitle(latinCheck(100));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с названием категории в 1000 символов")
    void postProductCategory1000LongStr() {
        prod.setCategoryTitle(latinCheck(1000));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с названием категории в 10000 символов")
    void postProductCategory10000LongStr() {
        prod.setCategoryTitle(latinCheck(10000));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием категории \"0\"")
    void postProductCategoryZero() {
        prod.setCategoryTitle("0");

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с отсутствующим названием категории")
    void postProductCategoryNull() {
        prod.setCategoryTitle(null);

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Создать продукт с пустым названием категории")
    void postProductCategoryEmpty() {
        prod.setCategoryTitle("");

        Response res = postProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Создать продукт с названием категории \"undefined\"")
    void postProductCategoryUndefined() {
        prod.setCategoryTitle("undefined");

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Создать продукт с названием категории из пробельного символа")
    void postProductCategoryWhitespaces(String space) {
        prod.setCategoryTitle(space);

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Создать продукт с названием категории из символа ASCII")
    void postProductCategoryAscii(String symbol) {
        prod.setCategoryTitle(symbol);

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием категории из алфавитно-цифровых символов")
    void postProductCategoryAlphanumeric() {
        prod.setCategoryTitle(alphanumericCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием категории из шестнадцатеричных символов")
    void postProductCategoryHex() {
        prod.setCategoryTitle(hexCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием категории из кириллицы")
    void postProductCategoryCyrillic() {
        prod.setCategoryTitle(cyrillicCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием категории из иероглифов")
    void postProductCategoryHieroglyphs() {
        prod.setCategoryTitle(hieroglyphCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием категории из расширенного Юникода")
    void postProductCategoryExtUnicode() {
        prod.setCategoryTitle(extUnicodeCheck(10));

        Response res = postProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Создать продукт с названием категории из массива")
    void postProductCategoryArray() {
        prodDouble.setCategoryTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = postProductDouble();

        assertProductBadRequest(prod, res);
    }

    @AfterEach
    void tearDown() {
        clearTestData(prod.getId());
    }
}
