package org.agrona;

import org.junit.Test;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static org.agrona.BitUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Rule;

public class BitUtilTestTempusFugit
{
	@Rule
	public ConcurrentRule concurrently = new ConcurrentRule();
	@Rule
	public RepeatingRule rule = new RepeatingRule();
	
    @Test
    @Concurrent(count = 2)
	@Repeating(repetition = 100)
    public void shouldReturnNextPositivePowerOfTwo()
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

}
