package com.pojosontheweb.selenium;

import org.junit.Test;

public class GoogleManagedTest extends ManagedDriverJunit4TestBase {

    @Test
    public void testWithInjectedWebDriver() {
        GoogleRawTest.performTest(getWebDriver());
    }


}
