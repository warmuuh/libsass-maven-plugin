package wrm.libsass;

import org.webjars.WebJarAssetLocator;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

class WebJarTranslator {
	private static final Pattern WEBJAR_PATTERN = Pattern.compile(WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/([^/]+)/([^/]+)/(.*)");

	private final Map<String, String> index;

	WebJarTranslator() {
		index = new WebJarAssetLocator().getFullPathIndex()
				.values()
				.stream()
				.map(WEBJAR_PATTERN::matcher)
				.filter(Matcher::matches)
				.collect(toMap(WebJarTranslator::convertMatchedPath, m -> m.group(0)));
	}

	private static String convertMatchedPath(Matcher matcher) {
		String name = matcher.group(1);
		String path = matcher.group(3);
		return name + "/" + path;
	}

	Optional<URI> translate(URI uri) {
		String fullPath = index.get(uri.toString());
		return fullPath == null ? Optional.empty() : Optional.of(URI.create(fullPath));
	}
}