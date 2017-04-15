package wrm.libsass;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Optional;

public class WebJarTranslatorTest {
	@Test
	public void testTranslate() throws Exception {
		WebJarTranslator translator = new WebJarTranslator();

		URI importUri = URI.create("susy/sass/susy/_math.scss");
		Optional<URI> fullUri = translator.translate(importUri);
		Optional<URI> expectedUri = Optional.of(URI.create("META-INF/resources/webjars/susy/2.1.1/sass/susy/_math.scss"));

		Assert.assertEquals(expectedUri, fullUri);
	}
}