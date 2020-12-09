package org.agrona;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Rule;
import org.junit.Test;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AsciiSequenceViewTestTempusFugit
{
    private static final int INDEX = 2;
    private final MutableDirectBuffer buffer = new UnsafeBuffer(new byte[128]);
    private final AsciiSequenceView asciiSequenceView = new AsciiSequenceView();
    
    @Rule
	public ConcurrentRule concurrently = new ConcurrentRule();
	@Rule
	public RepeatingRule rule = new RepeatingRule();

    @Test
    @Concurrent(count = 2)
	@Repeating(repetition = 100)
    public void shouldBeAbleToGetChars()
    {
    	//System.out.println(Thread.currentThread().getId());
        final String data = "stringy";
        buffer.putStringWithoutLengthAscii(INDEX, data);

        asciiSequenceView.wrap(buffer, INDEX, data.length());

        assertThat(asciiSequenceView.charAt(0), is('s'));
        assertThat(asciiSequenceView.charAt(1), is('t'));
        assertThat(asciiSequenceView.charAt(2), is('r'));
        assertThat(asciiSequenceView.charAt(3), is('i'));
        assertThat(asciiSequenceView.charAt(4), is('n'));
        assertThat(asciiSequenceView.charAt(5), is('g'));
        assertThat(asciiSequenceView.charAt(6), is('y'));
    }

}
