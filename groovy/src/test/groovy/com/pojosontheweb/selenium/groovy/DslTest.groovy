package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import groovy.json.JsonBuilder
import org.junit.Assert
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.DollrCategory
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory

import static com.pojosontheweb.selenium.Findrs.*
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import org.junit.Test
import org.openqa.selenium.Dimension
import org.pojosontheweb.selenium.groovy.WebDriverCategory

class DslTest extends ManagedDriverJunit4TestBase {

    @Test
    void testFindrs() {
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            assert $('#foo .bar') instanceof Findr.ListFindr
            assert $('#foo') + isDisplayed() instanceof Findr.ListFindr
            assert $('#foo') + isDisplayed() + whereElemCount(5) instanceof Findr.ListFindr
            assert $('#foo') + isDisplayed() + whereElemCount(5) + at(0) instanceof Findr
            assert findr() + isDisplayed() instanceof Findr
            assert $(findr(), '.blah') instanceof Findr.ListFindr
        }
    }

    @Test
    void dsl() {

        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {

            def d = webDriver
            d.get 'http://www.leboncoin.fr'
            d.windowSize = new Dimension(1400, 1000)

            // some examples...
            findr().elem(By.id('TableContentBottom')) + isDisplayed() >> eval()
            $('#TableContentBottom') + isDisplayed() >> eval()

            // click
            $('#TableContentBottom a') + attrEquals('title', 'Alsace') + at(0) >> click()

            // selects
            $('#search_category') at 0 asSelect() selectByVisibleText('Motos')
            $('#searcharea')[0].asSelect().selectByVisibleText('Toute la France')

            // search
            $('#searchbutton')[0].click()

            // build result
            def res = [],
                nbPages = 2

            (1..nbPages).each { page ->
                println "page $page"

                $('#ContainerMain .detail') + { WebElement e ->
                    e.text != null
                } >> { List<WebElement> elems ->
                    elems.each { WebElement e ->
                        res << [
                            text: e.text
                        ]
                        true
                    }
                }

                d.executeJavaScript("window.scrollTo(0, document.body.scrollHeight);")

                if (page>1) {
                    $('#paging a') +
                        textEquals("$page") +
                        isDisplayed() +
                        at(0) >> click()
                }
            }


            println "Found : ${res.size()} ads"
            println new JsonBuilder(res).toPrettyString()

            Assert.assertEquals(70, res.size())
        }
    }

}