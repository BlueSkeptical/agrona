package org.agrona;

import org.junit.jupiter.api.Test;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import static org.agrona.AsciiEncoding.parseIntAscii;
import static org.agrona.AsciiEncoding.parseLongAscii;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Assert;

public class AsciiEncodingTestMultithreadedTC extends MultithreadedTestCase
{

    public void thread1()
    {
    	//System.out.println(Thread.currentThread().getId());
        Assert.assertEquals(0, parseIntAscii("0", 0, 1));
        Assert.assertEquals(0, parseIntAscii("-0", 0, 2));
        Assert.assertEquals(7, parseIntAscii("7", 0, 1));
        Assert.assertEquals(-7, parseIntAscii("-7", 0, 2));
        Assert.assertEquals(33, parseIntAscii("3333", 1, 2));
        Assert.assertEquals(-123456789, parseIntAscii("-123456789", 0, 10));

        final String maxValueMinusOne = String.valueOf(Integer.MAX_VALUE - 1);
        Assert.assertEquals(Integer.MAX_VALUE - 1, parseIntAscii(maxValueMinusOne, 0, maxValueMinusOne.length()));

        final String maxValue = String.valueOf(Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, parseIntAscii(maxValue, 0, maxValue.length()));

        final String minValuePlusOne = String.valueOf(Integer.MIN_VALUE + 1);
        Assert.assertEquals(Integer.MIN_VALUE + 1, parseIntAscii(minValuePlusOne, 0, minValuePlusOne.length()));

        final String minValue = String.valueOf(Integer.MIN_VALUE);
        Assert.assertEquals(Integer.MIN_VALUE, parseIntAscii(minValue, 0, minValue.length()));
    }
    
    public void thread2()
    {
        Assert.assertEquals(0, parseIntAscii("0", 0, 1));
        Assert.assertEquals(0, parseIntAscii("-0", 0, 2));
        Assert.assertEquals(7, parseIntAscii("7", 0, 1));
        Assert.assertEquals(-7, parseIntAscii("-7", 0, 2));
        Assert.assertEquals(33, parseIntAscii("3333", 1, 2));
        Assert.assertEquals(-123456789, parseIntAscii("-123456789", 0, 10));

        final String maxValueMinusOne = String.valueOf(Integer.MAX_VALUE - 1);
        Assert.assertEquals(Integer.MAX_VALUE - 1, parseIntAscii(maxValueMinusOne, 0, maxValueMinusOne.length()));

        final String maxValue = String.valueOf(Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, parseIntAscii(maxValue, 0, maxValue.length()));

        final String minValuePlusOne = String.valueOf(Integer.MIN_VALUE + 1);
        Assert.assertEquals(Integer.MIN_VALUE + 1, parseIntAscii(minValuePlusOne, 0, minValuePlusOne.length()));

        final String minValue = String.valueOf(Integer.MIN_VALUE);
        Assert.assertEquals(Integer.MIN_VALUE, parseIntAscii(minValue, 0, minValue.length()));
    }
    
    @Test
    public void shouldParseInt() throws Throwable {
    	TestFramework.runManyTimes(new AsciiEncodingTestMultithreadedTC(), 100);
    }

}
