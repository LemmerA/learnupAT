package ru.learnup.javaqa.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learnup.javaqa.dto.Category;
import ru.learnup.javaqa.enums.CategoryType;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static ru.learnup.javaqa.Endpoints.CATEGORY_ID_ENDPOINT;
import static ru.learnup.javaqa.enums.CategoryType.*;
import static ru.learnup.javaqa.asserts.CommonAsserts.*;

public class CategoryTests extends BaseTest{

    private CategoryType cat;

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
    void getCategoryScan(int num) {
        Response res = getCategoryErr(num);

        assertNotFound(res);
    }

    @Test
    void getCategoryFood() {
        cat = FOOD;
        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Test
    void getCategoryElectronic() {
        cat = ELECTRONIC;
        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Test
    void getCategoryFurniture() {
        cat = FURNITURE;
        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Test
    void getCategoryNA() {
        cat = NOT_AVAILABLE;
        Response res = getCategoryErr(cat.getId());

        assertNotFound(res);
    }

    @Test
    void getCategoryNoProducts() {
        cat = F_35;
        Category res = getCategoryOK();

        assertCategoryEquals(cat, res);
    }

    @Test
    void getCategoryIdByTitle() {
        Response res = getCategoryErr("Food");

        assertBadRequest(res);
    }

    @Test
    void getCategoryNegative() {
        Response res = getCategoryErr(-1);

        assertBadRequest(res);
    }

    @Test
    void getCategoryPositiveFloat() {
        Response res = getCategoryErr(3.1);

        assertBadRequest(res);
    }

    @Test
    void getCategoryNegativeFloat() {
        Response res = getCategoryErr(-3.1);

        assertBadRequest(res);
    }

    @Test
    void getCategoryPositiveIntOverflow() {
        Response res = getCategoryErr(Integer.toUnsignedLong(Integer.MAX_VALUE) + 1);

        assertNotFound(res);
    }

    @Test
    void getCategoryMax() {
        Response res = getCategoryErr(Long.MAX_VALUE);

        assertNotFound(res);
    }

    @Test
    void getCategoryMin() {
        Response res = getCategoryErr(Long.MIN_VALUE);

        assertBadRequest(res);
    }

    @Test
    void getCategoryLongOverflow() {
        Response res = getCategoryErr(Long.MAX_VALUE + "0");

        assertBadRequest(res);
    }

    @Test
    void getCategory100LongStr() {
        Response res = getCategoryErr(latinCheck(100));

        assertBadRequest(res);
    }

    @Test
    void getCategory1000LongStr() {
        Response res = getCategoryErr(latinCheck(1000));

        assertBadRequest(res);
    }

    @Test
    void getCategory10000LongStr() {
        Response res = getCategoryErr(latinCheck(10000));

        assertBadRequest(res);
    }

    @Test
    void getCategoryZero() {
        Response res = getCategoryErr(0);

        assertBadRequest(res);
    }

    @Test
    void getCategoryEmpty() {
        Response res = getCategoryErr("");

        assertBadRequest(res);
    }

    @Test
    void getCategoryUndefined() {
        Response res = getCategoryErr("undefined");

        assertBadRequest(res);
    }

    @Test
    void getCategoryNull() {
        Response res = getCategoryErr("null");

        assertBadRequest(res);
    }

    @ParameterizedTest
    @MethodSource("whitespaceStream")
    void getCategoryWhitespaces(char space) {
        Response res = getCategoryErr(space);

        assertBadRequest(res);
    }

    @ParameterizedTest
    @MethodSource("asciiStream")
    void getCategoryAscii(char symbol) {
        Response res = getCategoryErr("2" + symbol + "2");

        assertBadRequest(res);
    }

    @Test
    void getCategoryAlphanumeric() {
        Response res = getCategoryErr(alphanumericCheck(10));

        assertBadRequest(res);
    }

    @Test
    void getCategoryHex() {
        Response res = getCategoryErr(hexCheck(10));

        assertBadRequest(res);
    }

    @Test
    void getCategoryCyrillic() {
        Response res = getCategoryErr(cyrillicCheck(1));

        assertBadRequest(res);
    }

    @Test
    void getCategoryHieroglyphs() {
        Response res = getCategoryErr(hieroglyphCheck(1));

        assertBadRequest(res);
    }

    @Test
    void getCategoryExtUnicode() {
        Response res = getCategoryErr(extUnicodeCheck(1));

        assertBadRequest(res);
    }

    @Test
    void getCategoryRoot() {
        Response res = given()
                .when()
                .get("categories")
                .prettyPeek();

        assertNotFound(res);
    }
}
