package ru.learnup.javaqa.tests.product;

import com.github.javafaker.Faker;
import io.qameta.allure.*;
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

import static io.qameta.allure.SeverityLevel.*;
import static io.restassured.RestAssured.given;
import static ru.learnup.javaqa.Endpoints.PRODUCT_ENDPOINT;
import static ru.learnup.javaqa.Endpoints.PRODUCT_ID_ENDPOINT;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;
import static ru.learnup.javaqa.enums.CategoryType.*;

@Epic("Тесты для контроллера продуктов")
@Feature("PUT Product")
@Severity(NORMAL)
public class PutProductTests extends BaseTest {
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

    @Step("Отправить PUT-запрос на контроллер продуктов")
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
    @Step("Отправить PUT-запрос на контроллер продуктов")
    private Response putProductErr() {
        return given()
                .body(prod)
                .contentType(ContentType.JSON)
                .when()
                .put(PRODUCT_ENDPOINT)
                .prettyPeek();
    }

    @Step("Отправить PUT-запрос на контроллер продуктов")
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

    @Severity(BLOCKER)
    @Test
    @Story("Обновить продукт теми же данными")
    void putProductInit() {
        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить несуществующий продукт")
    void putProductIntoNonexistent() {
        prodDouble.setId(Long.MAX_VALUE);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Обновить продукт новыми данными (1)")
    void putProductAll() {
        prod.setPrice(1);
        prod.setCategoryTitle(ELECTRONIC.getName());
        prod.setTitle(faker.hacker().noun());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Обновить продукт новыми данными (2)")
    void putProductAll2() {
        prod.setPrice(randomInt());
        prod.setCategoryTitle(MIG_21.getName());
        prod.setTitle(faker.aviation().aircraft());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Обновить продукт новыми данными (3)")
    void putProductAll3() {
        prod.setPrice(Integer.MAX_VALUE);
        prod.setCategoryTitle(FURNITURE.getName());
        prod.setTitle(faker.commerce().productName());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт категорией, не имеющей товаров")
    void putProductIntoEmptyProductList() {
        prod.setCategoryTitle(F_35.getName());
        prod.setTitle(faker.aviation().aircraft());

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить пустой JSON-запрос (без полей)")
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
    @Severity(CRITICAL)
    @Test
    @Story("Отправить JSON-запрос без поля идентификатора")
    void putProductIdMissing() {
        prodDouble.setId(null);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }
    
    @Test
    @Story("Отправить запрос с отрицательным идентификатором")
    void putProductIdNegative() {
        prodDouble.setId(prod.getId() * -1L);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }
    
    @Test
    @Story("Отправить запрос c идентификатором, превышающим 32 бита")
    void putProductIdPositiveIntOverflow() {
        prodDouble.setId(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Отправить запрос c идентификатором с максимально возможным значением")
    void putProductIdMax() {
        prodDouble.setId(Long.MAX_VALUE);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Отправить запрос c идентификатором с минимально возможным значением")
    void putProductIdMin() {
        prodDouble.setId(Long.MIN_VALUE);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Отправить запрос c нулевым идентификатором")
    void putProductIdZero() {
        prodDouble.setId(0L);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________

    @Test
    @Story("Отправить запрос c положительным нецелым идентификатором")
    void putProductIdPositiveFloat() {
        prodDouble.setId(prod.getId() + 0.1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }
    
    @Severity(MINOR)
    @Test
    void putProductIdNegativeFloat() {
        prodDouble.setId(prod.getId() * -1L + 0.1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором, превышающим максимально возможное значение")
    void putProductIdLongOverflow() {
        prodDouble.setId(Long.MAX_VALUE + "0");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором длиной 100")
    void putProductId100LongStr() {
        prodDouble.setId(latinCheck(100));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором длиной 1000")
    void putProductId1000LongStr() {
        prodDouble.setId(latinCheck(1000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c идентификатором длиной 10000")
    void putProductId10000LongStr() {
        prodDouble.setId(latinCheck(10000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос c пустым идентификатором")
    void putProductIdEmpty() {
        prodDouble.setId("");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Отправить запрос с идентификатором \"undefined\"")
    void putProductIdUndefined() {
        prodDouble.setId("undefined");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Отправить запрос с идентификатором \"null\"")
    void putProductIdNullString() {
        prodDouble.setId("null");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Отправить запрос с идентификатором из пробельного символа")
    void postProductIdWhitespaces(char space) {
        prodDouble.setId(space);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Отправить запрос с идентификатором из символа ASCII")
    void postProductIdAscii(char symbol) {
        prodDouble.setId(symbol);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Отправить запрос с идентификатором из латиницы")
    void putProductIdLatin() {
        prodDouble.setId(latinCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }
    
    @Severity(MINOR)
    @Test
    @Story("Отправить запрос с идентификатором из алфавитно-цифровых символов")
    void putProductIdAlphanumeric() {
        prodDouble.setId(alphanumericCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Отправить запрос с идентификатором из шестнадцатеричных символов")
    void putProductIdHex() {
        prodDouble.setId(hexCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Отправить запрос с идентификатором из кириллицы")
    void putProductIdCyrillic() {
        prodDouble.setId(cyrillicCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Отправить запрос с идентификатором из иероглифов")
    void putProductIdHieroglyphs() {
        prodDouble.setId(hieroglyphCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Отправить запрос с идентификатором из расширенного Юникода")
    void putProductIdExtUnicode() {
        prodDouble.setId(extUnicodeCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Отправить запрос с идентификатором в виде массива")
    void putProductIdArray() {
        prodDouble.setId(new int[]{randomInt(), randomInt(), randomInt()});

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Title tests
    @Severity(CRITICAL)
    @Test
    @Story("Отправить JSON-запрос без поля названия товара")
    void putProductTitleMissing() {
        prodDouble.setTitle(null);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с числовым названием")
    void putProductTitleNumeric() {
        prod.setTitle(numericCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с названием в 100 символов")
    void putProductTitle100LongStr() {
        prod.setTitle(latinCheck(100));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с названием в 1000 символов")
    void putProductTitle1000LongStr() {
        prod.setTitle(latinCheck(1000));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с названием в 10000 символов")
    void putProductTitle10000LongStr() {
        prod.setTitle(latinCheck(10000));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием \"0\"")
    void putProductTitleZero() {
        prod.setTitle("0");

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с пустым названием")
    void putProductTitleEmpty() {
        prod.setTitle("");

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Обновить продукт с названием \"undefined\"")
    void putProductTitleUndefined() {
        prod.setTitle("undefined");

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с отсутствующим названием")
    void putProductTitleNull() {
        prod.setTitle(null);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Обновить продукт с названием из пробельного символа")
    void postProductTitleWhitespaces(String space) {
        prod.setTitle(space);

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Обновить продукт с названием из символа ASCII")
    void postProductTitleAscii(String symbol) {
        prod.setTitle(symbol);

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием из алфавитно-цифровых символов")
    void putProductTitleAlphanumeric() {
        prod.setTitle(alphanumericCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием из шестнадцатеричных символов")
    void putProductTitleHex() {
        prod.setTitle(hexCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием из кириллицы")
    void putProductTitleCyrillic() {
        prod.setTitle(cyrillicCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием из иероглифов")
    void putProductTitleHieroglyphs() {
        prod.setTitle(hieroglyphCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием из расширенного Юникода")
    void putProductTitleExtUnicode() {
        prod.setTitle(extUnicodeCheck(10));

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием из массива")
    void putProductTitleArray() {
        prodDouble.setTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //Price tests
    @Severity(CRITICAL)
    @Test
    @Story("Отправить JSON-запрос без поля цены товара")
    void putProductPriceMissing() {
        prodDouble.setPrice(null);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с отрицательной ценой")
    void putProductPriceNegative() {
        prod.setPrice(-1);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с нулевой ценой")
    void putProductPriceZero() {
        prod.setPrice(0);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с отсутствующей ценой")
    void putProductPriceNull() {
        prod.setPrice(null);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с максимальной ценой")
    void putProductPriceMax() {
        prod.setPrice(Integer.MAX_VALUE);

        Product res = putProductOK();

        assertProductEquals(prod, res);
    }

    @Test
    @Story("Обновить продукт с минимальной ценой")
    void putProductPriceMin() {
        prod.setPrice(Integer.MIN_VALUE);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    //!!!Тесты с дублером_________________________________________________________________________________________

    @Test
    @Story("Обновить продукт с ценой в долларовом формате")
    void putProductPriceCurrency() {
        prodDouble.setPrice("$1,000.00");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с положительной нецелой ценой")
    void putProductPricePositiveFloat() {
        prodDouble.setPrice(3.01);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с отрицательной нецелой ценой")
    void putProductPriceNegativeFloat() {
        prodDouble.setPrice(-3.01);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой, превышающей допустимую")
    void putProductPriceLongOverflow() {
        prodDouble.setPrice(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой в 100 символов")
    void putProductPrice100LongStr() {
        prodDouble.setPrice(latinCheck(100));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой в 1000 символов")
    void putProductPrice1000LongStr() {
        prodDouble.setPrice(latinCheck(1000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой в 10000 символов")
    void putProductPrice10000LongStr() {
        prodDouble.setPrice(latinCheck(10000));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с пустой ценой")
    void putProductPriceEmpty() {
        prodDouble.setPrice("");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с ценой \"undefined\"")
    void putProductPriceUndefined() {
        prodDouble.setPrice("undefined");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой \"null\"")
    void putProductPriceNullString() {
        prodDouble.setPrice("null");

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }
    
    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Обновить продукт с ценой из пробельного символа")
    void postProductPriceWhitespaces(char space) {
        prodDouble.setPrice(space);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Обновить продукт с ценой из ASCII-символа")
    void postProductPriceAscii(char symbol) {
        prodDouble.setPrice(symbol);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой из латиницы")
    void putProductPriceLatin() {
        prodDouble.setPrice(latinCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой из алфавитно-числовых символов")
    void putProductPriceAlphanumeric() {
        prodDouble.setPrice(alphanumericCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с ценой из шестнадцатерничных символов")
    void putProductPriceHex() {
        prodDouble.setPrice(hexCheck(10));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с ценой из кириллицы")
    void putProductPriceCyrillic() {
        prodDouble.setPrice(cyrillicCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с ценой из иероглифов")
    void putProductPriceHieroglyphs() {
        prodDouble.setPrice(hieroglyphCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с ценой из расширенного Юникода")
    void putProductPriceExtUnicode() {
        prodDouble.setPrice(extUnicodeCheck(1));

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Test
    @Story("Обновить продукт с ценой из массива")
    void putProductPriceArray() {
        prodDouble.setPrice(new int[]{1, 2, 3});

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    //CategoryTitle tests
    @Severity(CRITICAL)
    @Test
    @Story("Отправить JSON-запрос без поля названия категории")
    void putProductCategoryMissing() {
        prodDouble.setCategoryTitle(null);

        Response res = putProductDouble();

        assertProductBadRequest(prod, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Обновить продукт с несуществующей категорией")
    void putProductCategoryIncorrect() {
        prod.setCategoryTitle(NOT_AVAILABLE.getName());

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с названием категории в 100 символов")
    void putProductCategory100LongStr() {
        prod.setCategoryTitle(latinCheck(100));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с названием категории в 1000 символов")
    void putProductCategory1000LongStr() {
        prod.setCategoryTitle(latinCheck(1000));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с названием категории в 10000 символов")
    void putProductCategory10000LongStr() {
        prod.setCategoryTitle(latinCheck(10000));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием категории \"0\"")
    void putProductCategoryZero() {
        prod.setCategoryTitle("0");

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с отсутствующим названием категории")
    void putProductCategoryNull() {
        prod.setCategoryTitle(null);

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Обновить продукт с пустым названием категории")
    void putProductCategoryEmpty() {
        prod.setCategoryTitle("");

        Response res = putProductErr();

        assertProductBadRequest(prod, res);
    }

    @Severity(MINOR)
    @Test
    @Story("Обновить продукт с названием категории \"undefined\"")
    void putProductCategoryUndefined() {
        prod.setCategoryTitle("undefined");

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Обновить продукт с названием категории из пробельного символа")
    void postProductCategoryWhitespaces(String space) {
        prod.setCategoryTitle(space);

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Обновить продукт с названием категории из символа ASCII")
    void postProductCategoryAscii(String symbol) {
        prod.setCategoryTitle(symbol);

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием категории из алфавитно-цифровых символов")
    void putProductCategoryAlphanumeric() {
        prod.setCategoryTitle(alphanumericCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием категории из шестнадцатеричных символов")
    void putProductCategoryHex() {
        prod.setCategoryTitle(hexCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием категории из кириллицы")
    void putProductCategoryCyrillic() {
        prod.setCategoryTitle(cyrillicCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием категории из иероглифов")
    void putProductCategoryHieroglyphs() {
        prod.setCategoryTitle(hieroglyphCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием категории из расширенного Юникода")
    void putProductCategoryExtUnicode() {
        prod.setCategoryTitle(extUnicodeCheck(10));

        Response res = putProductErr();

        assertProductNotFound(prod, res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Обновить продукт с названием категории из массива")
    void putProductCategoryArray() {
        prodDouble.setCategoryTitle(new String[]{latinCheck(10), latinCheck(10), latinCheck(10)});

        Response res = putProductDouble();

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
