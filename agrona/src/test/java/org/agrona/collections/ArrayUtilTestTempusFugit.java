package org.agrona.collections;

import org.junit.Test;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.Rule;

public class ArrayUtilTestTempusFugit
{
    // Reference Equality
    private static final Integer ONE = 1;
    private static final Integer TWO = 2;
    private static final Integer THREE = 3;

    private final Integer[] values = { ONE, TWO };
    
    @Rule
	public ConcurrentRule concurrently = new ConcurrentRule();
	@Rule
	public RepeatingRule rule = new RepeatingRule();

    @Test
    @Concurrent(count = 2)
	@Repeating(repetition = 100)
    public void shouldNotRemoveMissingElement()
    {
    	//System.out.println(Thread.currentThread().getId());
        final Integer[] result = ArrayUtil.remove(values, THREE);

        assertArrayEquals(values, result);
    }

}
