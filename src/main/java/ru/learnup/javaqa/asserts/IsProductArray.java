package ru.learnup.javaqa.asserts;

import lombok.NoArgsConstructor;
import org.hamcrest.Matcher;
import ru.learnup.javaqa.dto.Product;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@NoArgsConstructor
public class IsProductArray extends TypeSafeMatcher<List<Product>> {

    public static Matcher<List<Product>> isProductArray() {
        return new IsProductArray();
    }

    @Override
    protected boolean matchesSafely(List<Product> products) {
        try {
            //На случай категории без продуктов
            if (!products.isEmpty()){
                products.get(0).getId();
            }
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Not a list of products");
    }
}