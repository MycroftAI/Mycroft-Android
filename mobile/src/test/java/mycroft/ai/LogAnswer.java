/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
		String tag = invocation.getArgument(0, String.class);
		String msg = invocation.getArgument(1, String.class);

		String name = invocation.getMethod().getName();

		String format = String.format(Locale.US, "[Log.%s] %s: %s", name, tag, msg);
		stream.println(format);

		return format.getBytes(Charset.forName("UTF8")).length;
	}
}