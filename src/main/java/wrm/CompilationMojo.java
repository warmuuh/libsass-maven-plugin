package wrm;

import wrm.libsass.SassCompilationException;
import wrm.libsass.SassCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Compilation of all scss files from inputpath to outputpath using includePaths
 *
 * @goal compile
 * @phase generate-resources
 */
public class CompilationMojo extends AbstractMojo {
	/**
	 * Location of the generated CSS files.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputPath;

	/**
	 * Location of source files.
	 *
	 * @parameter expression="src/main/sass"
	 */
	private String inputPath;

	/**
	 * Location of images to for use by the image-url Sass function.
	 *
	 * @parameter
	 */
	private String imagePath;

	/**
	 * additional include path, ';'-separated
	 *
	 * @parameter
	 */
	private String includePath;

	/**
	 * Output style for the generated css code. One of nested, expanded, compact, compressed
	 *
	 * @parameter expression="expanded"
	 */
	private SassCompiler.OutputStyle outputStyle;

	/**
	 * Emit comments in the generated CSS indicating the corresponding source line.
	 *
	 * @parameter expression="false"
	 */
	private boolean generateSourceComments;

	/**
	 * Generate source map files.
	 *
	 * @parameter expression="true"
	 */
	private boolean generateSourceMap;

	/**
	 *
	 * @parameter expression="${project.build.directory}"
	 */
	private String sourceMapOutputPath;

	/**
	 *
	 * @parameter expression="false"
	 */
	private boolean omitSourceMapUrl;

	/**
	 * TODO: not sure what this does
	 * @parameter expression="false"
	 */
	private boolean embedSourceMap;

	/**
	 * TODO: not sure what this does
	 * @parameter expression="false"
	 */
	private boolean embedSourceMapContents;

	/**
	 *
	 * @parameter expression="scss"
	 */
	private SassCompiler.InputSyntax inputSyntax;

	/**
	 * @parameter expression="5"
	 */
	private int precision;

	private SassCompiler compiler;

	public void execute() throws MojoExecutionException, MojoFailureException {
		inputPath = inputPath.replaceAll("\\\\", "/");

		getLog().debug("Input Path=" + inputPath);
		getLog().debug("Output Path=" + outputPath);

		final Path root = Paths.get(inputPath);
		String globPattern = "glob:" + inputPath + "{**/,}*.scss";
		getLog().debug("Glob = " + globPattern);

		compiler = initCompiler();

		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (matcher.matches(file) && !file.getFileName().toString().startsWith("_")) {
						processFile(root, file);
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException e) {
			throw new MojoExecutionException("Failed", e);
		}
	}

	private SassCompiler initCompiler() {
		SassCompiler compiler = new SassCompiler();
		compiler.setEmbedSourceMap(this.embedSourceMap);
		compiler.setEmbedSourceMapContents(this.embedSourceMapContents);
		compiler.setGenerateSourceComments(this.generateSourceComments);
		compiler.setGenerateSourceMap(this.generateSourceMap);
		compiler.setImagePath(this.imagePath);
		compiler.setIncludePaths(this.includePath);
		compiler.setInputSyntax(this.inputSyntax);
		compiler.setOmitSourceMapUrl(this.omitSourceMapUrl);
		compiler.setOutputStyle(this.outputStyle);
		compiler.setPrecision(this.precision);
		// FIXME: this is probably incorrect
		compiler.setSourceMapPathPrefix(this.sourceMapOutputPath);
		return compiler;
	}

	private void processFile(final Path root, Path file) throws IOException {
		getLog().debug("Processing File " + file);
		Path relPath = root.relativize(file);
		String outputFile = outputPath + File.separator + relPath.toString();
		outputFile = outputFile.substring(0, outputFile.lastIndexOf(".")) + ".css";
		convertFile(file.toString(), outputFile);
	}

	private void convertFile(String inputFile, String outputFile) throws IOException {
		String content;
		try {
			content = compiler.compileFile(inputFile);
		}
		catch (SassCompilationException e) {
			getLog().error(e.getMessage());
			getLog().debug(e);
			return;
		}

		getLog().debug("Compilation finished.");

		writeContentToFile(outputFile, content);
	}

	private void writeContentToFile(String outputFile, String content) throws IOException {
		File f = new File(outputFile);
		f.getParentFile().mkdirs();
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(content.getBytes());
		fos.flush();
		fos.close();
		getLog().debug("Written to: " + f);
	}
}