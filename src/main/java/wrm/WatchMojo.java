package wrm;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Watch for changes in inputPath, then compile scss files to outputPath using
 * includePaths
 *
 * @goal watch
 */
public class WatchMojo extends AbstractSassMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		validateConfig();
		compiler = initCompiler();

		inputPath = inputPath.replaceAll("\\\\", "/");

		getLog().debug("Input Path=" + inputPath);
		getLog().debug("Output Path=" + outputPath);

		try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
			Path root = project.getBasedir().toPath().resolve(Paths.get(inputPath));
			registerAll(root, watcher);
			getLog().info("Watching [" + inputPath + "]...");
			try {
				long lastModified = 0;
				for (;;) {
					WatchKey key = watcher.take();
					for (WatchEvent<?> event : key.pollEvents()) {
						if (event.kind() == OVERFLOW) {
							continue;
						}
						Path modifiedFile = ((Path) key.watchable()).resolve((Path) event.context());
						if (modifiedFile.toFile().lastModified() - lastModified > 1000) {
							// Ignore multiple occurrences that are too close
							// Usually caused by editors (where content is changed
							// and then file attributes are changed)
							getLog().debug(String.format(
									"%s: %s", event.kind().name(), modifiedFile));
							try {
								compile();
							} catch (Exception e) {
								// ignore
							}
						}
						lastModified = modifiedFile.toFile().lastModified();
						break;
					}
					if (!key.reset()) {
						break;
					}
				}
			} catch (InterruptedException e) {
				getLog().warn("Watch service interrupted");
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Exception while watching", e);
		}
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(Path start, WatchService watcher) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}