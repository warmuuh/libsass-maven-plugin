package wrm;

import wrm.libsass.SassCompilationException;
import wrm.libsass.SassCompiler;
import wrm.libsass.SassCompilerOutput;

import java.io.File;
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
	 * The directory in which the compiled CSS files will be placed. The default value is
	 * <tt>${project.build.directory}</tt>
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputPath;

	/**
	 * The directory from which the source .scss files will be read. This directory will be
	 * traversed recursively, and all .scss files found in this directory or subdirectories
	 * will be compiled. The default value is <tt>src/main/sass</tt>
	 *
	 * @parameter expression="src/main/sass"
	 */
	private String inputPath;

	/**
	 * Location of images to for use by the image-url Sass function. The default value is
	 * <tt>null</tt>.
	 *
	 * @parameter
	 */
	private String imagePath;

	/**
	 * Additional include path, ';'-separated. The default value is <tt>null</tt>
	 *
	 * @parameter
	 */
	private String includePath;

	/**
	 * Output style for the generated css code. One of <tt>nested</tt>, <tt>expanded</tt>,
	 * <tt>compact</tt>, <tt>compressed</tt>. Note that as of libsass 3.1, <tt>expanded</tt>
	 * and <tt>compact</tt> are the same as <tt>nested</tt>. The default value is
	 * <tt>expanded</tt>.
	 *
	 * @parameter expression="expanded"
	 */
	private SassCompiler.OutputStyle outputStyle;

	/**
	 * Emit comments in the compiled CSS indicating the corresponding source line. The default
	 * value is <tt>false</tt>
	 *
	 * @parameter expression="false"
	 */
	private boolean generateSourceComments;

	/**
	 * Generate source map files. The generated source map files will be placed in the directory
	 * specified by <tt>sourceMapOutputPath</tt>. The default value is <tt>true</tt>.
	 *
	 * @parameter expression="true"
	 */
	private boolean generateSourceMap;

	/**
	 * The directory in which the source map files that correspond to the compiled CSS will be
	 * placed. The default value is <tt>${project.build.directory}</tt>
	 *
	 * @parameter expression="${project.build.directory}"
	 */
	private String sourceMapOutputPath;

	/**
	 * Prevents the generation of the <tt>sourceMappingURL</tt> special comment as the last
	 * line of the compiled CSS. The default value is <tt>false</tt>.
	 *
	 * @parameter expression="false"
	 */
	private boolean omitSourceMapingURL;

	/**
	 * Embeds the whole source map data directly into the compiled CSS file by transforming
	 * <tt>sourceMappingURL</tt> into a data URI. The default value is <tt>false</tt>.
	 *
	 * @parameter expression="false"
	 */
	private boolean embedSourceMapInCSS;

	/**
	 * Embeds the contents of the source .scss files in the source map file instead of the
	 * paths to those files. The default value is <tt>false</tt>
	 *
	 * @parameter expression="false"
	 */
	private boolean embedSourceContentsInSourceMap;

	/**
	 * Switches the input syntax used by the files to either <tt>sass</tt> or <tt>scss</tt>.
	 * The default value is <tt>scss</tt>.
	 *
	 * @parameter expression="scss"
	 */
	private SassCompiler.InputSyntax inputSyntax;

	/**
	 * Precision for fractional numbers. The default value is <tt>5</tt>.
	 *
	 * @parameter expression="5"
	 */
	private int precision;

	private SassCompiler compiler;

	public void execute() throws MojoExecutionException, MojoFailureException {
		validateConfig();
		compiler = initCompiler();

		inputPath = inputPath.replaceAll("\\\\", "/");

		getLog().debug("Input Path=" + inputPath);
		getLog().debug("Output Path=" + outputPath);

		final Path root = Paths.get(inputPath);
		String globPattern = "glob:" + inputPath + "{**/,}*.scss";
		getLog().debug("Glob = " + globPattern);

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

	private void validateConfig() {
		if(!generateSourceMap){
			if(embedSourceMapInCSS){
				getLog().warn("embedSourceMapInCSS=true is ignored. Cause: generateSourceMap=false");
			}
			if(embedSourceContentsInSourceMap){
				getLog().warn("embedSourceContentsInSourceMap=true is ignored. Cause: generateSourceMap=false");
			}
		}
		if(outputStyle != SassCompiler.OutputStyle.compressed && outputStyle != SassCompiler.OutputStyle.nested){
			getLog().warn("outputStyle=" + outputStyle + " is ignored. Cause: libsass 3.1 only supports compressed and nested");
		}
	}

	private SassCompiler initCompiler() {
		SassCompiler compiler = new SassCompiler();
		compiler.setEmbedSourceMapInCSS(this.embedSourceMapInCSS);
		compiler.setEmbedSourceContentsInSourceMap(this.embedSourceContentsInSourceMap);
		compiler.setGenerateSourceComments(this.generateSourceComments);
		compiler.setGenerateSourceMap(this.generateSourceMap);
		compiler.setImagePath(this.imagePath);
		compiler.setIncludePaths(this.includePath);
		compiler.setInputSyntax(this.inputSyntax);
		compiler.setOmitSourceMappingURL(this.omitSourceMapingURL);
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
			SassCompilerOutput out = compiler.compileFile(inputFile);
			content = out.getCssOutput();
			// FIXME: sourcemap is not generated
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