package org.agrona;

import org.junit.jupiter.api.Test;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static org.agrona.BitUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BitUtilTestMultithreadedTC extends MultithreadedTestCase
{

    public void thread1()
    {
    	//System.out.println(Thread.currentThread().getId());
        assertThat(findNextPositivePowerOfTwo(MIN_VALUE), is(MIN_VALUE));
        assertThat(findNextPositivePowerOfTwo(MIN_VALUE + 1), is(1));
        assertThat(findNextPositivePowerOfTwo(-1), is(1));
        assertThat(findNextPositivePowerOfTwo(0), is(1));
        assertThat(findNextPositivePowerOfTwo(1), is(1));
        assertThat(findNextPositivePowerOfTwo(2), is(2));
        assertThat(findNextPositivePowerOfTwo(3), is(4));
        assertThat(findNextPositivePowerOfTwo(4), is(4));
        assertThat(findNextPositivePowerOfTwo(31), is(32));
        assertThat(findNextPositivePowerOfTwo(32), is(32));
        assertThat(findNextPositivePowerOfTwo(1 << 30), is(1 << 30));
        assertThat(findNextPositivePowerOfTwo((1 << 30) + 1), is(MIN_VALUE));
    }
    
    public void thread2()
    {
        assertThat(findNextPositivePowerOfTwo(MIN_VALUE), is(MIN_VALUE));
        assertThat(findNextPositivePowerOfTwo(MIN_VALUE + 1), is(1));
        assertThat(findNextPositivePowerOfTwo(-1), is(1));
        assertThat(findNextPositivePowerOfTwo(0), is(1));
        assertThat(findNextPositivePowerOfTwo(1), is(1));
        assertThat(findNextPositivePowerOfTwo(2), is(2));
        assertThat(findNextPositivePowerOfTwo(3), is(4));
        assertThat(findNextPositivePowerOfTwo(4), is(4));
        assertThat(findNextPositivePowerOfTwo(31), is(32));
        assertThat(findNextPositivePowerOfTwo(32), is(32));
        assertThat(findNextPositivePowerOfTwo(1 << 30), is(1 << 30));
        assertThat(findNextPositivePowerOfTwo((1 << 30) + 1), is(MIN_VALUE));
    }

    @Test
    public void shouldReturnNextPositivePowerOfTwo() throws Throwable {
    	TestFramework.runManyTimes(new BitUtilTestMultithreadedTC(), 100);
    }
}
