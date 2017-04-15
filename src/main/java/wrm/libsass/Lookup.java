package wrm.libsass;

import io.bit3.jsass.importer.Import;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

@FunctionalInterface
interface Lookup {
	Optional<Result> run();

	class Result {
		private final String absoluteUri;
		private final URL url;

		private Result(String absoluteUri, URL url) {
			this.absoluteUri = absoluteUri;
			this.url = url;
		}

		static Result of(String absoluteUri, URL url) {
			return new Result(absoluteUri, url);
		}

		static Result of(File file) {
			try {
				String absoluteUri = file.getPath();
				URL url = file.toURI().toURL();
				return new Result(absoluteUri, url);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		Import buildImport(String importUri) {
			try {
				String contents = read(url);
				return new Import(importUri, absoluteUri, contents);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}

		private static String read(URL url) {
			try (Scanner scanner = new Scanner(url.openStream(), StandardCharsets.UTF_8.name())) {
				return scanner.useDelimiter("\\A").next();
			} catch (IOException e) {
				throw new RuntimeException("Cannot read the url: " + url, e);
			}
		}
	}
}
