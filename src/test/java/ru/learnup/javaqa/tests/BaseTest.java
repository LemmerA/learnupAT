package ru.learnup.javaqa.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.filter.log.LogDetail.BODY;
import static io.restassured.filter.log.LogDetail.HEADERS;
import static io.restassured.filter.log.LogDetail.METHOD;
import static io.restassured.filter.log.LogDetail.URI;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;

public abstract class BaseTest {

    static Properties properties = new Properties();
    static RequestSpecification logReqSpec;
    static ResponseSpecification resSpec;

    public static ResponseSpecification confirmDeleteResSpec;
    public static ResponseSpecification deleteResSpec;
    public static ResponseSpecification categoriesResSpec;
    public static ResponseSpecification productsResSpec;

    //Генератор случайного инт32
    public Integer randomInt(){
        return (int)(Math.random() * Integer.MAX_VALUE + 1);
    }

    //Набор аскии-символов для проверок
    public static final String asciiCheck = "!;()\"\'.,=+|&#$%*-/:<>?@[]\\^_~`}{";

    //Поток символов для параметризованных тестов
    public static Stream<String> asciiStream() {
        return Stream.of(asciiCheck.split(""));
    }

    //Набор пробельных символов для проверок
    public static final String whitespaceCheck = new String (new char[]{12, 13, 14,
            '\t', '\n', '\f', '\r',
            '\u000b', '\u001c', '\u001d', '\u001e', '\u001f'});

    //Поток пробелов для параметризованных тестов
    public static Stream<String> whitespaceStream() {
        return Stream.of(whitespaceCheck.split(""));
    }

    //Генератор числовой строки заданной длины
    public String numericCheck(int length) {
        char[] charArr = new char[length];
        for (int i = 0; i < charArr.length; i++) {
            char ch = (char) (Math.random() * (57 - 48 + 1) + 48);
            charArr[i] = ch;
        }
        return new String(charArr);
    }

    //Генератор шестнадцатеричной числовой строки заданной длины
    public String hexCheck(int length) {
        char[] charArr = new char[length];
        for (int i = 0; i < charArr.length; i++) {
            char ch = (char) (Math.random() * (70 - 48 + 1) + 48);
            if (ch >= 58 && ch <= 64) {
                i -= 1;
                continue;
            }
            charArr[i] = ch;
        }
        return new String(charArr);
    }

    //Генератор алфавитно-числовой строки заданной длины
    public String alphanumericCheck(int length) {
        char[] charArr = new char[length];
        for (int i = 0; i < charArr.length; i++) {
            char ch = (char) (Math.random() * (122 - 48 + 1) + 48);
            if ((ch >= 90 && ch <= 97) || (ch >= 58 && ch <= 64)) {
                i -= 1;
                continue;
            }
            charArr[i] = ch;
        }
        return new String(charArr);
    }

    //Генератор строки на латинице заданной длины
    public String latinCheck(int length) {
        char[] charArr = new char[length];
        for (int i = 0; i < charArr.length; i++) {
            char ch = (char) (Math.random() * (122 - 65 + 1) + 65);
            if (ch >= 90 && ch <= 97) {
                i -= 1;
                continue;
            }
            charArr[i] = ch;
        }
        return new String(charArr);
    }

    //Генератор строки на кириллице заданной длины
    public String cyrillicCheck(int length) {
        char[] charArr = new char[length];
        for (int i = 0; i < charArr.length; i++) {
            charArr[i] = (char)(Math.random() * (1103 - 1040 + 1) + 1040);
        }
        return new String(charArr);
    }

    //Генератор строки их иероглифов заданной длины
    public String hieroglyphCheck(int length) {
        char[] charArr = new char[length];
        for (int i = 0; i < charArr.length; i++) {
            charArr[i] = (char)(Math.random() * (19000 - 18500 + 1) + 18500);
        }
        return new String(charArr);
    }

    //Генератор строки из расширенного Юникода заданной длины
    public String extUnicodeCheck(int length) {
        char[] charArr = new char[length];
        for (int i = 0; i < charArr.length; i++) {
            charArr[i] = (char)(Math.random() * (130000 - 125000 + 1) + 125000);
        }
        return new String(charArr);
    }

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        properties.load(new FileInputStream("src/test/resources/application.properties"));
        RestAssured.baseURI = properties.getProperty("baseURL");

        logReqSpec = new RequestSpecBuilder()
                .log(METHOD)
                .log(URI)
                .log(BODY)
                .log(HEADERS)
                .build();

        resSpec = new ResponseSpecBuilder()
                .expectStatusLine(containsStringIgnoringCase("HTTP/1.1"))
                .build();

        categoriesResSpec =  new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectStatusLine(containsStringIgnoringCase("HTTP/1.1"))
                .expectStatusCode(200)
                .build();

        productsResSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectStatusLine(containsStringIgnoringCase("HTTP/1.1"))
                .expectStatusCode(200)
                .build();

        //Спеки для метода-уборщика
        deleteResSpec = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();

        confirmDeleteResSpec = new ResponseSpecBuilder()
                .expectStatusCode(404)
                .build();

        RestAssured.requestSpecification = logReqSpec;
        RestAssured.responseSpecification = resSpec;
    }
}