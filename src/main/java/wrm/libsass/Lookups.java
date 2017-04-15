package wrm.libsass;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

class Lookups {
	static Optional<Lookup.Result> findLocalFile(URI base, URI uri) {
		String pathname = base.resolve(uri).toString();
		File file = new File(pathname);
		return file.exists()
				? Optional.of(Lookup.Result.of(file))
				: Optional.empty();
	}

	static Optional<Lookup.Result> findResource(URI uri) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL url = classLoader.getResource(uri.toString());
		return Optional.ofNullable(url).flatMap(Lookups::toResult);
	}

	private static Optional<Lookup.Result> toResult(URL url) {
		switch (url.getProtocol()) {
			case "file":
				return Optional.of(Lookup.Result.of(url.toString(), url));
			case "jar":
				try {
					JarURLConnection jarUrlConnection = (JarURLConnection) url.openConnection();
					String name = jarUrlConnection.getEntryName();
					return Optional.of(Lookup.Result.of(name, url));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			default:
				return Optional.empty();
		}
	}

	static Optional<Lookup.Result> findWebJarResource(URI uri, WebJarTranslator translator) {
		return translator.translate(uri).flatMap(Lookups::findResource);
	}
}
