package com.incountry;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(Enclosed.class)
public class StorageTest {

    @RunWith(Parameterized.class)
    public static class StorageParamTests {

        @Parameterized.Parameter(0)
        public String country;
        @Parameterized.Parameter(1)
        public String key;
        @Parameterized.Parameter(2)
        public String body;

        @Parameterized.Parameters(name = "{index}:withParams({0}, {1}, {2}")
        public static Iterable<Object[]> dataForTest() {
            return Arrays.asList(new Object[][]{
                    {"us", "123", "test"},
                    {"us", "456", "testBody"},
                    {"us", "456", null}
            });
        }

        @Test
        public void testHelloWorld() {
            // U can use country, key and body here
            String hello = "world";
            assertEquals("world", hello);
        }

    }

    public static class StorageSingleTests {

        @Test
        public void testH1() {
            // U can use country, key and body here
            String hello = "world";
            assertEquals("world", hello);
        }
    }
}