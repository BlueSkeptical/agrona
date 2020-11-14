package org.agrona.collections;

import org.junit.jupiter.api.Test;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Assert;

public class IntArrayQueueTestMultithreadedTC extends MultithreadedTestCase
{
    public void thread1()
    {
    	//System.out.println(Thread.currentThread().getId());
        final IntArrayQueue queue = new IntArrayQueue();

        Assert.assertTrue(queue.isEmpty());
        Assert.assertEquals(0, queue.size());
        Assert.assertEquals(IntArrayQueue.MIN_CAPACITY, queue.capacity());
    }
    
    public void thread2()
    {
        final IntArrayQueue queue = new IntArrayQueue();

        Assert.assertTrue(queue.isEmpty());
        Assert.assertEquals(0, queue.size());
        Assert.assertEquals(IntArrayQueue.MIN_CAPACITY, queue.capacity());
    }
    
    @Test
    public void shouldDefaultInitialise() throws Throwable {
    	TestFramework.runManyTimes(new IntArrayQueueTestMultithreadedTC(), 100);
    }
}
