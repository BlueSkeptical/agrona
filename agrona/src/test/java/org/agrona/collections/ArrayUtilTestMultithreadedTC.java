package org.agrona.collections;

import org.junit.jupiter.api.Test;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ArrayUtilTestMultithreadedTC extends MultithreadedTestCase
{
    // Reference Equality
    private static final Integer ONE = 1;
    private static final Integer TWO = 2;
    private static final Integer THREE = 3;

    private final Integer[] values = { ONE, TWO };

    public void thread1()
    {
    	//System.out.println(Thread.currentThread().getId());
        final Integer[] result = ArrayUtil.remove(values, THREE);

        assertArrayEquals(values, result);
    }
    
    public void thread2()
    {
        final Integer[] result = ArrayUtil.remove(values, THREE);

        assertArrayEquals(values, result);
    }

    @Test
    public void shouldNotRemoveMissingElement() throws Throwable {
    	TestFramework.runManyTimes(new ArrayUtilTestMultithreadedTC(), 100);
    }
}
