/**
 * 
 */
package org.springframework.integration.test.matcher;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;

/**
 * Matcher to make assertions about message equality easier.  Usage:
 * 
 * <pre>
 * &#064;Test
 * public void testSomething() {
 *   Message<String> expected = ...;
 *   Message<String> result = ...;
 *   assertThat(result, sameExceptImmutableHeaders(expected));
 * }
 * 
 * &#064;Factory
 * public static Matcher<Message<?>> sameExceptImmutableHeaders(Message<?> expected) {
 *   return new PayloadAndHeaderMatcher(expected);
 * }
 * </pre>
 *
 * @author Dave Syer
 *
 */
public class PayloadAndHeaderMatcher extends BaseMatcher<Message<?>> {

	private final Object payload;
	private final Map<String, Object> headers;

	@Factory
	public static Matcher<Message<?>> sameExceptImmutableHeaders(Message<?> expected) {
		return new PayloadAndHeaderMatcher(expected);
	}

	private PayloadAndHeaderMatcher(Message<?> expected) {
		this.payload = expected.getPayload();
		this.headers = getHeaders(expected);
	}

	private Map<String, Object> getHeaders(Message<?> operand) {
		HashMap<String, Object> headers = new HashMap<String, Object>(operand.getHeaders());
		headers.remove(MessageHeaders.ID);
		headers.remove(MessageHeaders.TIMESTAMP);
		return headers;
	}

	public boolean matches(Object arg) {
		Message<?> input = (Message<?>) arg;
		Map<String, Object> inputHeaders = getHeaders(input);
		return input.getPayload().equals(payload) && inputHeaders.equals(headers);
	}

	public void describeTo(Description description) {
		description.appendText("a Message with Headers that match except ID and timestamp for payload: ").appendValue(payload).appendText(" and headers: ").appendValue(headers);
	}
	
}