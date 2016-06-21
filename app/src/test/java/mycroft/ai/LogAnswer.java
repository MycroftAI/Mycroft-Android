package mycroft.ai;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Useful for mocking {@link android.util.Log} static methods.
 * <p>
 *     Use it like so:
 *     {@code
 *     Mockito.when(Log.v(anyString(), anyString())).then(new LogAnswer(System.out));
 *     }
 * </p>
 *
 * @author Philip Cohn-Cort
 */
public class LogAnswer implements Answer<Integer> {

	protected final PrintStream stream;

	public LogAnswer(PrintStream stream) {
		this.stream = stream;
	}

	@Override
	public Integer answer(InvocationOnMock invocation) throws Throwable {
		String tag = invocation.getArgumentAt(0, String.class);
		String msg = invocation.getArgumentAt(1, String.class);

		String name = invocation.getMethod().getName();

		String format = String.format(Locale.US, "[Log.%s] %s: %s", name, tag, msg);
		stream.println(format);

		return format.getBytes(Charset.forName("UTF8")).length;
	}
}