package ru.learnup.javaqa.asserts;

import lombok.NoArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import ru.learnup.javaqa.enums.CategoryType;

import java.util.Arrays;
import java.util.Objects;

@NoArgsConstructor
public class IsCategoryExists extends TypeSafeMatcher<String> {

    public static Matcher<String> isCategoryExists() {
        return new IsCategoryExists();
    }

    @Override
    protected boolean matchesSafely(String actual) {
        try {
            //Пофиксил поиск нумерованного списка: теперь ищет по полю с именем, а не идентификатору
            return Arrays.stream(CategoryType.values())
                    .anyMatch(p -> Objects.equals(p.getName(), actual));
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("No such category in our dictionary");
    }

}
