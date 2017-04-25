package wrm.libsass;

import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static wrm.libsass.Lookups.*;

class ClasspathAwareImporter implements Importer {
	private static final String SCSS_EXT = ".scss";
	private static final String CSS_EXT = ".css";

	private final WebJarTranslator webJarTranslator = new WebJarTranslator();

	@Override
	public Collection<Import> apply(String importStr, Import previous) {
		URI base = previous.getAbsoluteUri();
		URI uri = buildUri(importStr);
		Optional<URI> uriCss = tryBuildUriCss(importStr);

		Stream<Lookup> lookups = Stream.of(
				() -> findLocalFile(base, addUnderscore(uri)),
				() -> findLocalFile(base, uri),
				() -> uriCss.map(u -> findLocalFile(base, u)).orElse(Optional.empty()),
				() -> findResource(addUnderscore(uri)),
				() -> findResource(uri),
				() -> uriCss.map(Lookups::findResource).orElse(Optional.empty()),
				() -> findResource(addUnderscore(base.resolve(uri))),
				() -> findResource(base.resolve(uri)),
				() -> uriCss.map(u -> findResource(base.resolve(u))).orElse(Optional.empty()),
				() -> findWebJarResource(addUnderscore(uri), webJarTranslator),
				() -> findWebJarResource(uri, webJarTranslator),
				() -> uriCss.map(u -> findWebJarResource(u, webJarTranslator)).orElse(Optional.empty()));
		Optional<Lookup.Result> lookupResult = lookups
				.map(Lookup::run)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
		return lookupResult
				.map(r -> r.buildImport(importStr))
				.map(Collections::singletonList)
				.orElse(null);
	}

	private static URI buildUri(String importStr) {
		String withExtension = importStr.endsWith(SCSS_EXT) ? importStr : importStr + SCSS_EXT;
		return URI.create(withExtension);
	}

	private static Optional<URI> tryBuildUriCss(String importStr) {
		return importStr.endsWith(SCSS_EXT)
				? Optional.empty()
				: Optional.of(URI.create(importStr + CSS_EXT));
	}

	private static URI addUnderscore(URI source) {
		String sourceStr = source.toString();
		int afterLastSlash = sourceStr.lastIndexOf("/") + 1;
		String withUnderscore = new StringBuilder(sourceStr)
				.insert(afterLastSlash, "_")
				.toString();
		return URI.create(withUnderscore);
	}
}
