package com.pojosontheweb.selenium.formz;

import com.pojosontheweb.selenium.Findr;
import org.openqa.selenium.WebElement;

import java.util.function.Function;
import java.util.function.Predicate;

public class Select {

    private final Findr findr;

    public Select(Findr findr) {
        this.findr = findr;
    }

    public Findr getFindr() {
        return findr;
    }

    public Select selectByVisibleText(String text) {
        findr.eval(makeSelectByVisibleText(text));
        return this;
    }

    public Select assertSelectedText(String expected) {
        findr.where(selectedText(expected)).eval();
        return this;
    }

    public static Function<WebElement,?> makeSelectByVisibleText(final String text) {
        return new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement select) {
                org.openqa.selenium.support.ui.Select selSelect =
                    new org.openqa.selenium.support.ui.Select(select);
                selSelect.selectByVisibleText(text);
                return true;
            }

            @Override
            public String toString() {
                return "selectByVisibleText:" + text;
            }
        };
    }

    public static Predicate<WebElement> selectedText(final String expectedText) {
        return new Predicate<WebElement>() {
            @Override
            public boolean test(WebElement select) {
                org.openqa.selenium.support.ui.Select selSelect =
                        new org.openqa.selenium.support.ui.Select(select);
                WebElement selOption = selSelect.getFirstSelectedOption();
                if (selOption == null) {
                    return false;
                }
                String text = selOption.getText();
                return text != null && text.equals(expectedText);
            }

            @Override
            public String toString() {
                return "selectedText:" + expectedText;
            }

        };
    }

    /**
     * @deprecated use instance method(s) instead
     */
    @Deprecated
    public static void selectByVisibleText(Findr selectFindr, final String text) {
        selectFindr.eval(makeSelectByVisibleText(text));
    }

}
