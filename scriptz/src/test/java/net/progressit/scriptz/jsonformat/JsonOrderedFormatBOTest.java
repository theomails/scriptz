package net.progressit.scriptz.jsonformat;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class JsonOrderedFormatBOTest {
	
	private JsonOrderedFormatBO bo = new JsonOrderedFormatBO();
	
	@Test
	public void shouldDoBasicOperations() {
		String input = "{\"test\":{\"a\":1, \"b\": \"Text\"}, \"test2\":[{\"a1\":11, \"b1\":\"Text1\"}, {\"a1\":12, \"b1\":\"Text2\"}], \"test3\":null}";
		
		String result = bo.orderAndFormatJson(input, false, true);
		
		String expected = "{\"test\":{\"a\":1.0,\"b\":\"Text\"},\"test2\":[{\"a1\":11.0,\"b1\":\"Text1\"},{\"a1\":12.0,\"b1\":\"Text2\"}],\"test3\":null}";
		
		assertThat(result, CoreMatchers.is(expected));
	}
}
