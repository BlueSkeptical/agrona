package org.agrona.concurrent.ringbuffer;

import org.agrona.concurrent.AtomicBuffer;
import org.agrona.concurrent.MessageHandler;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import java.nio.ByteBuffer;
import java.util.concurrent.CyclicBarrier;

import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.concurrent.ringbuffer.RingBuffer.INSUFFICIENT_CAPACITY;
import static org.agrona.concurrent.ringbuffer.RingBufferDescriptor.TRAILER_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManyToOneRingBufferConcurrentTestMultithreadedTC extends MultithreadedTestCase
{
    private static final int MSG_TYPE_ID = 7;

    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect((16 * 1024) + TRAILER_LENGTH);
    private final UnsafeBuffer unsafeBuffer = new UnsafeBuffer(byteBuffer);
    private final RingBuffer ringBuffer = new ManyToOneRingBuffer(unsafeBuffer);

    public void thread1() throws Exception
    {
        final int reps = 10_000_000;
        final int numThreads = 2;
        final CyclicBarrier barrier = new CyclicBarrier(numThreads);
        final Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new Thread(
                () ->
                {
                    try
                    {
                        barrier.await();
                    }
                    catch (final Exception ignore)
                    {
                    }

                    for (int r = 0; r < reps; r++)
                    {
                        ringBuffer.nextCorrelationId();
                    }
                });

            threads[i].start();
        }

        for (final Thread t : threads)
        {
            t.join();
        }

        Assert.assertEquals(reps * numThreads, ringBuffer.nextCorrelationId());
    }
    
    public void thread2() throws Exception
    {
        final int reps = 10_000_000;
        final int numThreads = 2;
        final CyclicBarrier barrier = new CyclicBarrier(numThreads);
        final Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new Thread(
                () ->
                {
                    try
                    {
                        barrier.await();
                    }
                    catch (final Exception ignore)
                    {
                    }

                    for (int r = 0; r < reps; r++)
                    {
                        ringBuffer.nextCorrelationId();
                    }
                });

            threads[i].start();
        }

        for (final Thread t : threads)
        {
            t.join();
        }

        Assert.assertEquals(reps * numThreads, ringBuffer.nextCorrelationId());
    }

    @Test
    public void shouldProvideCorrelationIds() throws Throwable {
    	TestFramework.runManyTimes(new ManyToOneRingBufferConcurrentTestMultithreadedTC(), 100);
    }
    
    
    class Producer implements Runnable
    {
        private final int producerId;
        private final CyclicBarrier barrier;
        private final int reps;

        Producer(final int producerId, final CyclicBarrier barrier, final int reps)
        {
            this.producerId = producerId;
            this.barrier = barrier;
            this.reps = reps;
        }

        public void run()
        {
            try
            {
                barrier.await();
            }
            catch (final Exception ignore)
            {
            }

            final int length = SIZE_OF_INT * 2;
            final int repsValueOffset = SIZE_OF_INT;
            final UnsafeBuffer srcBuffer = new UnsafeBuffer(new byte[1024]);

            srcBuffer.putInt(0, producerId);

            for (int i = 0; i < reps; i++)
            {
                srcBuffer.putInt(repsValueOffset, i);

                while (!ringBuffer.write(MSG_TYPE_ID, srcBuffer, 0, length))
                {
                    Thread.yield();
                }
            }
        }
    }

    class ClaimCommit implements Runnable
    {
        private final int producerId;
        private final CyclicBarrier barrier;
        private final int reps;

        ClaimCommit(final int producerId, final CyclicBarrier barrier, final int reps)
        {
            this.producerId = producerId;
            this.barrier = barrier;
            this.reps = reps;
        }

        public void run()
        {
            try
            {
                barrier.await();
            }
            catch (final Exception ignore)
            {
            }

            final int length = SIZE_OF_INT * 2;
            for (int i = 0; i < reps; i++)
            {
                int index = -1;
                try
                {
                    while (INSUFFICIENT_CAPACITY == (index = ringBuffer.tryClaim(MSG_TYPE_ID, length)))
                    {
                        Thread.yield();
                    }

                    final AtomicBuffer buffer = ringBuffer.buffer();
                    buffer.putInt(index, producerId);
                    buffer.putInt(index + SIZE_OF_INT, i);
                }
                finally
                {
                    ringBuffer.commit(index);
                }
            }
        }
    }

    class ClaimAbort implements Runnable
    {
        private final int producerId;
        private final CyclicBarrier barrier;
        private final int reps;

        ClaimAbort(final int producerId, final CyclicBarrier barrier, final int reps)
        {
            this.producerId = producerId;
            this.barrier = barrier;
            this.reps = reps;
        }

        public void run()
        {
            try
            {
                barrier.await();
            }
            catch (final Exception ignore)
            {
            }

            final int length = SIZE_OF_INT * 2;
            final UnsafeBuffer srcBuffer = new UnsafeBuffer(new byte[1024]);
            srcBuffer.putInt(0, producerId);

            for (int i = 0; i < reps; i++)
            {
                int claimIndex = -1;
                try
                {
                    while (INSUFFICIENT_CAPACITY == (claimIndex = ringBuffer.tryClaim(MSG_TYPE_ID, SIZE_OF_INT)))
                    {
                        Thread.yield();
                    }
                    ringBuffer.buffer().putInt(claimIndex, -i); // should be skipped
                }
                finally
                {
                    ringBuffer.abort(claimIndex);
                }

                srcBuffer.putInt(SIZE_OF_INT, i);
                while (!ringBuffer.write(MSG_TYPE_ID, srcBuffer, 0, length))
                {
                    Thread.yield();
                }
            }
        }
    }
}
