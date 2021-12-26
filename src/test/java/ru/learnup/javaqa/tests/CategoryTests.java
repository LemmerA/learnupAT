package ru.learnup.javaqa.tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learnup.javaqa.dto.Category;
import ru.learnup.javaqa.enums.CategoryType;

import java.util.stream.Stream;

import static io.qameta.allure.SeverityLevel.*;
import static io.restassured.RestAssured.given;
import static ru.learnup.javaqa.Endpoints.CATEGORY_ID_ENDPOINT;
import static ru.learnup.javaqa.enums.CategoryType.*;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;

@Epic("Тесты для контроллера категорий")
@Feature("GET Category")
@Severity(NORMAL)
public class CategoryTests extends BaseTest{

    private CategoryType cat;

    @Step("Отправить GET-запрос на контроллер категорий")
    private Category getCategoryOK() {
        return given()
                .response()
                .spec(categoriesResSpec)
                .when()
                .get(CATEGORY_ID_ENDPOINT, cat.getId())
                .prettyPeek()
                .body()
                .as(Category.class);
    }

    //Отказ от использования спеков для адекватного логирования: при провале валидации не печатался ответ
    @Step("Отправить GET-запрос на контроллер категорий")
    private Response getCategoryErr(Object id) {
        return given()
                .when()
                .get(CATEGORY_ID_ENDPOINT, id)
                .prettyPeek();
    }

    public static Stream<Integer> scan() {
        Integer[] arr = new Integer[1000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        return Stream.of(arr);
    }

    //Для поиска категорий
    @Disabled
    @ParameterizedTest
    @MethodSource("scan")
    @Story("Сканер категорий")
    void getCategoryScan(int num) {
        Response res = getCategoryErr(num);

        assertNotFound(res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Получить категорию 1")
    void getCategoryFood() {
        cat = FOOD;

        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Получить категорию 2")
    void getCategoryElectronic() {
        cat = ELECTRONIC;

        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Severity(BLOCKER)
    @Test
    @Story("Получить категорию 3")
    void getCategoryFurniture() {
        cat = FURNITURE;

        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить несуществующую категорию")
    void getCategoryNA() {
        cat = NOT_AVAILABLE;
        Response res = getCategoryErr(cat.getId());

        assertNotFound(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить категорию, в которой нет продуктов")
    void getCategoryNoProducts() {
        cat = F_35;
        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Test
    @Story("Получить категорию по ее имени, а не идентификатору")
    void getCategoryIdByTitle() {
        Response res = getCategoryErr("Food");

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию c отрицательным идентификатором")
    void getCategoryNegative() {
        Response res = getCategoryErr(-1);

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию c положительным нецелым идентификатором")
    void getCategoryPositiveFloat() {
        Response res = getCategoryErr(3.1);

        assertBadRequest(res);
    }

    @Severity(TRIVIAL)
    @Test
    @Story("Получить категорию c отрицательным нецелым идентификатором")
    void getCategoryNegativeFloat() {
        Response res = getCategoryErr(-3.1);

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию c идентификатором, превышающим 32 бита")
    void getCategoryPositiveIntOverflow() {
        Response res = getCategoryErr(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        assertNotFound(res);
    }

    @Test
    @Story("Получить категорию c идентификатором с максимально возможным значением")
    void getCategoryMax() {
        Response res = getCategoryErr(Long.MAX_VALUE);

        assertNotFound(res);
    }

    @Test
    @Story("Получить категорию c идентификатором с минимально возможным значением")
    void getCategoryMin() {
        Response res = getCategoryErr(Long.MIN_VALUE);

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить категорию c идентификатором, превышающим максимально возможное значение")
    void getCategoryLongOverflow() {
        Response res = getCategoryErr(Long.MAX_VALUE + "0");

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос длиной больше 100 символов")
    void getCategory100LongStr() {
        Response res = getCategoryErr(latinCheck(100));

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос длиной больше 1000 символов")
    void getCategory1000LongStr() {
        Response res = getCategoryErr(latinCheck(1000));

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Отправить запрос длиной больше 10000 символов")
    void getCategory10000LongStr() {
        Response res = getCategoryErr(latinCheck(10000));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию с нулевым идентификатором")
    void getCategoryZero() {
        Response res = getCategoryErr(0);

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить категорию с пустым идентификатором")
    void getCategoryEmpty() {
        Response res = getCategoryErr("");

        assertNotFound(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию с идентификатором \"undefined\"")
    void getCategoryUndefined() {
        Response res = getCategoryErr("undefined");

        assertBadRequest(res);
    }

    @Test
    @Story("Получить категорию с идентификатором \"null\"")
    void getCategoryNull() {
        Response res = getCategoryErr("null");

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("whitespaceStream")
    @Story("Получить категорию из пробельного символа")
    void getCategoryWhitespaces(char space) {
        Response res = getCategoryErr(space);

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @ParameterizedTest
    @MethodSource("asciiStream")
    @Story("Получить категорию из символа ASCII")
    void getCategoryAscii(char symbol) {
        Response res = getCategoryErr("2" + symbol + "2");

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию из алфавитно-цифровых символов")
    void getCategoryAlphanumeric() {
        Response res = getCategoryErr(alphanumericCheck(10));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию из шестнадцатеричных символов")
    void getCategoryHex() {
        Response res = getCategoryErr(hexCheck(10));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию из кириллицы")
    void getCategoryCyrillic() {
        Response res = getCategoryErr(cyrillicCheck(1));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию из иероглифов")
    void getCategoryHieroglyphs() {
        Response res = getCategoryErr(hieroglyphCheck(1));

        assertBadRequest(res);
    }

    @Severity(MINOR)
    @Test
    @Story("Получить категорию из расширенного Юникода")
    void getCategoryExtUnicode() {
        Response res = getCategoryErr(extUnicodeCheck(1));

        assertBadRequest(res);
    }

    @Severity(CRITICAL)
    @Test
    @Story("Получить категории корневого адреса контроллера")
    void getCategoryRoot() {
        Response res = given()
                .when()
                .get("categories")
                .prettyPeek();

        assertNotFound(res);
    }
}
