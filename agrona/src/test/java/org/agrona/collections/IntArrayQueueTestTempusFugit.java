package org.agrona.collections;

import org.junit.Test;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Rule;

public class IntArrayQueueTestTempusFugit
{
	@Rule
	public ConcurrentRule concurrently = new ConcurrentRule();
	@Rule
	public RepeatingRule rule = new RepeatingRule();

    @Test
    @Concurrent(count = 2)
	@Repeating(repetition = 100)
    public void shouldDefaultInitialise()
    {
    	//System.out.println(Thread.currentThread().getId());
        final IntArrayQueue queue = new IntArrayQueue();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertEquals(IntArrayQueue.MIN_CAPACITY, queue.capacity());
    }
}
