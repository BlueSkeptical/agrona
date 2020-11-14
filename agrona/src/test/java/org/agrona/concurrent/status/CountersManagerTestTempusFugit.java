package org.agrona.concurrent.status;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.IntObjConsumer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.agrona.concurrent.status.CountersReader.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CountersManagerTestTempusFugit
{
    private static final int NUMBER_OF_COUNTERS = 4;
    private static final long FREE_TO_REUSE_TIMEOUT = 1000;

    private long currentTimestamp = 0;

    private final UnsafeBuffer metadataBuffer = new UnsafeBuffer(allocate(NUMBER_OF_COUNTERS * METADATA_LENGTH));
    private final UnsafeBuffer valuesBuffer = new UnsafeBuffer(allocate(NUMBER_OF_COUNTERS * COUNTER_LENGTH));
    private final CountersManager manager = new CountersManager(metadataBuffer, valuesBuffer, US_ASCII);
    private final CountersReader reader = new CountersManager(metadataBuffer, valuesBuffer, US_ASCII);
    private final CountersManager managerWithCooldown = new CountersManager(
        metadataBuffer, valuesBuffer, US_ASCII, () -> currentTimestamp, FREE_TO_REUSE_TIMEOUT);

    @SuppressWarnings("unchecked")
    private final IntObjConsumer<String> consumer = mock(IntObjConsumer.class);
    private final CountersReader.MetaData metaData = mock(CountersReader.MetaData.class);
    
    @Rule
	public ConcurrentRule concurrently = new ConcurrentRule();
	@Rule
	public RepeatingRule rule = new RepeatingRule();

    @Test
    @Concurrent(count = 2)
	@Repeating(repetition = 1)
    public void shouldTruncateLongLabel()
    {
    	System.out.println(Thread.currentThread().getId());
        final int labelLength = MAX_LABEL_LENGTH + 10;
        final StringBuilder sb = new StringBuilder(labelLength);

        for (int i = 0; i < labelLength; i++)
        {
            sb.append('x');
        }

        final String label = sb.toString();
        final int counterId = manager.allocate(label);

        reader.forEach(consumer);
        verify(consumer).accept(counterId, label.substring(0, MAX_LABEL_LENGTH));
    }
}
