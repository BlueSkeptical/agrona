package org.agrona.concurrent.errors;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;

import org.agrona.BitUtil;
import org.agrona.concurrent.AtomicBuffer;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.agrona.concurrent.errors.DistinctErrorLog.*;

public class DistinctErrorLogTestTempusFugit
{
    private final UnsafeBuffer unsafeBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(64 * 1024));
    private final AtomicBuffer buffer = spy(unsafeBuffer);
    private final EpochClock clock = mock(EpochClock.class);
    private final DistinctErrorLog log = new DistinctErrorLog(buffer, clock);
    
    @Rule
	public ConcurrentRule concurrently = new ConcurrentRule();
	@Rule
	public RepeatingRule rule = new RepeatingRule();

    @Test
    @Concurrent(count = 2)
	@Repeating(repetition = 100)
    public void shouldRecordFirstObservation()
    {
    	//System.out.println(Thread.currentThread().getId());
        final long timestamp = 7;
        final int offset = 0;
        final RuntimeException error = new RuntimeException("Test Error");

        when(clock.time()).thenReturn(timestamp);

        assertTrue(log.record(error));

        final InOrder inOrder = inOrder(buffer);
        inOrder.verify(buffer).putBytes(eq(offset + ENCODED_ERROR_OFFSET), any(byte[].class));
        inOrder.verify(buffer).putLong(offset + FIRST_OBSERVATION_TIMESTAMP_OFFSET, timestamp);
        inOrder.verify(buffer).putIntOrdered(eq(offset + LENGTH_OFFSET), anyInt());
        inOrder.verify(buffer).getAndAddInt(offset + OBSERVATION_COUNT_OFFSET, 1);
        inOrder.verify(buffer).putLongOrdered(offset + LAST_OBSERVATION_TIMESTAMP_OFFSET, timestamp);
    }
}
