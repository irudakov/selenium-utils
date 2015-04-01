<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="taste" value="${renderPropertyValueEdit.owningObject}"/>
<s:textarea name="object.taste" id="tasteText" style="display: none;"/>
 <pre class="editor"
      data-editor-lang="js"
      data-editor-show-annotation-ruler="false"
      data-editor-show-overview-ruler="false"
      data-editor-show-folding-ruler="false"></pre>
<link rel="stylesheet" type="text/css" href="http://eclipse.org/orion/editor/releases/current/built-editor.css"/>
<script src="http://eclipse.org/orion/editor/releases/current/built-editor.min.js"></script>
<script type="text/taste" id="initTaste">/*

Taste Examples on google.com.

Basic Search/Images tests, written in two
different styles (plain findr or "dsl").

 */

import com.google.common.base.Predicate
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement

import static com.pojosontheweb.taste.Taste.*

suite('Google Tests') {

    add test('searchF') {

        webDriver.get 'http://www.google.com'

        findr()
            .elem(By.id('gbqfq'))
            .sendKeys('pojos on the web')

        findr()
            .elem(By.cssSelector('button.gbqfb'))
            .click()

        findr()
            .elem(By.id('search'))
            .elemList(By.cssSelector('h3.r'))
            .at(0)
            .elem(By.tagName('a'))
            .where(textContains('POJOs on the Web'))
            .eval()

    }

    add test('search') {

        webDriver.get 'http://www.google.com'

        $('#gbqfq') >> sendKeys('pojos on the web')

        $('button.gbqfb') >> click()

        $$('#search h3.r') +
            textContains('POJOs on the Web') +
            at(0) +
            $('a') +
            textContains('POJOs on the Web') >> eval()

    }

    add test('imagesF') {

        webDriver.get 'http://www.google.com'

        findr()
            .elemList(By.tagName('a'))
            .where(textEquals('Images'))
            .at(0)
            .click()

        findr()
            .elem(By.id('lst-ib'))
            .sendKeys('pojos on the web', Keys.ENTER)

        findr()
            .elemList(By.cssSelector('a.rg_l'))
            .where({ WebElement e ->
            e.getAttribute('href').contains 'www.pojosontheweb.com'
        } as Predicate)
            .at(0)
            .click()

        findr()
            .elemList(By.cssSelector('a'))
            .where(textEquals('Website for this image'))
            .at(0)
            .click()

        findr()
            .elem(By.cssSelector('h1.page-header.index'))
            .where(textEquals('Woko: POJOs on the Web!'))
            .eval()
    }

    add test('images') {

        webDriver.get 'http://www.google.com'
        $$('a.gb_f') + textEquals('Images') + at(0) >> click()

        $('#lst-ib') >> sendKeys('pojos on the web', Keys.ENTER)

        $$('a.rg_l') + { WebElement e ->
            e.getAttribute('href').contains 'www.pojosontheweb.com'
        } + at(0) >> click()

        $$('a') +
            textEquals('Website for this image') +
            at(0) >> click()

        $('h1.page-header.index') +
            textEquals('Woko: POJOs on the Web!') >> eval()

    }

}
</script>
<script>
  require(["orion/editor/edit"], function(edit) {
    var editor = edit({className: "editor"})[0];
    $('.w-edit.Taste form').submit(function() {
      $('#tasteText').val(editor.getText());
    });

    <c:choose>
      <c:when test="${taste.id==null}">
        editor.setText($('#initTaste').text());
      </c:when>
      <c:otherwise>
        editor.setText($('#tasteText').text());
      </c:otherwise>
    </c:choose>
  });
</script>

<style type="text/css">
  .editor .textview {
    min-height: 400px;
  }
</style>

