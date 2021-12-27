package ru.learnup.javaqa.tests.product;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learnup.javaqa.dto.Product;
import ru.learnup.javaqa.dto.ProductList;
import ru.learnup.javaqa.tests.BaseTest;

import static io.qameta.allure.SeverityLevel.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.learnup.javaqa.Endpoints.*;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;
import static ru.learnup.javaqa.asserts.IsProductArray.isProductArray;

@Epic("Тесты для контроллера продуктов")
@Feature("GET Product")
@Severity(NORMAL)
public class GetProductTests extends BaseTest {
    private Product prod = new Product();

    @Step("Отправить GET-запрос на контроллер продуктов")
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
    @Step("Отправить GET-запрос на контроллер продуктов")
    private Response getProductErr(Object id) {
        return given()
                .when()
                .get(PRODUCT_ID_ENDPOINT, id)
                .prettyPeek();
    }

    @Step("Отправить GET-запрос в корень контроллера продуктов")
    private ProductList getProductAll() {
        return given()
                .response()
                .spec(productsResSpec)
                .when()
                .get(PRODUCT_ENDPOINT)
                .prettyPeek()
                .body()
                .as(ProductList.class);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить все продукты")
    void getAllProducts() {
        ProductList res = getProductAll();

        assertThat(res.getProducts(), isProductArray());
    }

    @Severity(BLOCKER)
    @Test
    @Story("Получить существующий продукт")
    void getProductFound() {
        prod.setId(20442L);

        Product res = getProductOK();

        assertThat(res.getId(), equalTo(prod.getId()));
    }

    @Test
    @Story("Получить продукт по его имени, а не идентификатору")
    void getProductIdByTitle() {
        Response res = getProductErr("Apples");

        assertBadRequest(res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Получить несуществующий продукт")
    void getProductNotFound() {
        prod.setId(1L);
        Response res = getProductErr(prod.getId());

        assertNotFound(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт c отрицательным идентификатором")
    void getProductNegative() {
        Response res = getProductErr(-1);

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт c положительным нецелым идентификатором")
    void getProductPositiveFloat() {
        Response res = getProductErr(3.1);

        assertBadRequest(res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Получить продукт c отрицательным нецелым идентификатором")
    void getProductNegativeFloat() {
        Response res = getProductErr(-3.1);

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт c идентификатором, превышающим 32 бита")
    void getProductPositiveIntOverflow() {
        Response res = getProductErr(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        assertNotFound(res);
    }

    @Test
    @Story("Получить продукт c идентификатором с максимально возможным значением")
    void getProductMax() {
        Response res = getProductErr(Long.MAX_VALUE);

        assertNotFound(res);
    }

    @Test
    @Story("Получить продукт c идентификатором с минимально возможным значением")
    void getProductMin() {
        Response res = getProductErr(Long.MIN_VALUE);

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить продукт c идентификатором, превышающим максимально возможное значение")
    void getProductLongOverflow() {
        Response res = getProductErr(Long.MAX_VALUE + "0");

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос длиной больше 100 символов")
    void getProduct100LongStr() {
        Response res = getProductErr(latinCheck(100));

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос длиной больше 1000 символов")
    void getProduct1000LongStr() {
        Response res = getProductErr(latinCheck(1000));

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос длиной больше 10000 символов")
    void getProduct10000LongStr() {
        Response res = getProductErr(latinCheck(10000));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт с нулевым идентификатором")
    void getProductZero() {
        Response res = getProductErr(0);

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить продукт с пустым идентификатором")
    void getProductEmpty() {
        Response res = getProductErr("");

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт с идентификатором \"undefined\"")
    void getProductUndefined() {
        Response res = getProductErr("undefined");

        assertBadRequest(res);
    }

    @Test
    @Story("Получить продукт с идентификатором \"null\"")
    void getProductNull() {
        Response res = getProductErr("null");

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Получить продукт из пробельного символа")
    void getProductWhitespaces(char space) {
        Response res = getProductErr(space);

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Получить продукт из символа ASCII")
    void getProductAscii(char symbol) {
        Response res = getProductErr("2" + symbol + "2");

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт из алфавитно-цифровых символов")
    void getProductAlphanumeric() {
        Response res = getProductErr(alphanumericCheck(10));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт из шестнадцатеричных символов")
    void getProductHex() {
        Response res = getProductErr(hexCheck(10));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт из кириллицы")
    void getProductCyrillic() {
        Response res = getProductErr(cyrillicCheck(1));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт из иероглифов")
    void getProductHieroglyphs() {
        Response res = getProductErr(hieroglyphCheck(1));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить продукт из расширенного Юникода")
    void getProductExtUnicode() {
        Response res = getProductErr(extUnicodeCheck(1));

        assertBadRequest(res);
    }
}
