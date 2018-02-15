package com.pojosontheweb.selenium;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class Issue23RetryTest {

    @Test
    public void testRetriesOk() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicInteger i3 = new AtomicInteger(0);
        final List<String> l = new ArrayList<String>();
        Retry.retry()
                .add(() -> {
                    i1.incrementAndGet();
                    l.add("A");
                })
                .add(() -> {
                    i2.incrementAndGet();
                    l.add("B");
                })
                .add(() -> {
                    i3.incrementAndGet();
                    l.add("C");
                })
                .eval();
        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals(1, i3.get());
        assertEquals(Arrays.asList("A", "B", "C"), l);

    }

    @Test
    public void testRetriesResultOk() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicInteger i3 = new AtomicInteger(0);
        String s = Retry.retry()
                .add(() -> {
                i1.incrementAndGet();
                return "A";
            })
                .add(s1 -> {
                    i2.incrementAndGet();
                    return s1 +"B";
                })
                .add(s12 -> {
                    i3.incrementAndGet();
                    return s12 +"C";
                })
                .eval();
        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals(1, i3.get());
        assertEquals("ABC", s);
    }


    @Test
    public void testRetriesFindrs() {
        final WebDriver d = DriverBuildr.fromSysProps().build();
        final List<String> l = new ArrayList<String>();
        final AtomicInteger i = new AtomicInteger(0);
        boolean failed = false;
        try {
            final Findr f = new Findr(d);
            d.get("http://www.google.com");
            Retry.retry(5)
                    .add(f.$("#viewport"))
                    .add(() -> {
                        System.out.println("I am useless");
                        l.add("A");
                        i.incrementAndGet();
                    })
                    .add(f.$$("#viewport").count(1))
                    .add(f.$$("#viewport").count(1).at(0))
                    .add(() -> {
                        l.add("B");
                        f.$$("#viewport").count(1).at(0).eval();
                    })
                    .add(f.setTimeout(2).$("#i-dont-exist")) // just to make it fail
                    .eval();
        } catch (TimeoutException e) {
            failed = true;
        } finally {
            d.quit();
        }
        assertTrue(failed);
        assertEquals(5, i.get());
        assertEquals(Arrays.asList("A", "B", "A", "B", "A", "B", "A", "B", "A", "B"), l);
    }

    @Test
    public void testRetriesNoResultToResult() {
        final AtomicInteger i = new AtomicInteger(0);
        String s = Retry.retry(5)
                .add((Runnable) i::incrementAndGet)
                .add(() -> "ABC")
                .eval();
        assertEquals(1, i.get());
        assertEquals("ABC", s);
    }

    @Test
    public void testRetriesResultToNoResult() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicInteger i3 = new AtomicInteger(0);
        Retry.retry()
                .add(() -> {
                    i1.incrementAndGet();
                    return "ABC";
                })
                .add(s -> {
                    i2.incrementAndGet();
                    return "DEF";
                })
                .add(s -> {
                    i3.incrementAndGet();
                })
                .eval();
        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals(1, i3.get());
    }


    @Test
    public void testRetriesResultFindrs() {
        final WebDriver d = DriverBuildr.fromSysProps().build();
        final List<String> l = new ArrayList<String>();
        final AtomicInteger i = new AtomicInteger(0);
        try {
            final Findr f = new Findr(d);
            d.get("http://www.google.com");
            String s = Retry.retry()
                    .add(() -> "A")
                    .add(f.$("#viewport"))
                    .add(
                            f.$("#viewport"),
                            s1 -> s1 + "B"
                    )
                    .add(
                            s12 -> {
                                i.incrementAndGet();
                                return s12 + "C";
                            }
                    )
                    .eval();
            assertEquals("ABC", s);
        } finally {
            d.quit();
        }
        assertEquals(1, i.get());
    }

}
