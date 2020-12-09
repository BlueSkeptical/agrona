package org.agrona;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AsciiSequenceViewTestMultithreadedTC extends MultithreadedTestCase
{
    private static final int INDEX = 2;
    private final MutableDirectBuffer buffer = new UnsafeBuffer(new byte[128]);
    private final AsciiSequenceView asciiSequenceView = new AsciiSequenceView();

    public void thread1()
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
    
    public void thread2()
    {
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
    
    @Test
    public void shouldBeAbleToGetChars() throws Throwable {
    	TestFramework.runManyTimes(new AsciiSequenceViewTestMultithreadedTC(), 100);
    }

}
