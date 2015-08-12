package com.mycompany.mytestapp;

import android.test.InstrumentationTestCase;

/**
 * Created by Tomek on 2015-08-11.
 */
public class ExampleTest extends InstrumentationTestCase{
    public void testAdd() throws Exception {
        final int expected = 2;
        System.out.println("\n\n\n");
        assertEquals(expected, 5);
        System.out.println("\n\n\n");
    }
}
