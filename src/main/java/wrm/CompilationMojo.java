package wrm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import wrm.libsass.SassCompilationException;
import wrm.libsass.SassCompiler;
import wrm.libsass.SassCompilerOutput;

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
	 * @parameter default-value="src/main/sass"
	 */
	private String inputPath;

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
	 * @parameter default-value="expanded"
	 */
	private SassCompiler.OutputStyle outputStyle;

	/**
	 * Emit comments in the compiled CSS indicating the corresponding source line. The default
	 * value is <tt>false</tt>
	 *
	 * @parameter default-value="false"
	 */
	private boolean generateSourceComments;

	/**
	 * Generate source map files. The generated source map files will be placed in the directory
	 * specified by <tt>sourceMapOutputPath</tt>. The default value is <tt>true</tt>.
	 *
	 * @parameter default-value="true"
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
	 * @parameter default-value="false"
	 */
	private boolean omitSourceMapingURL;

	/**
	 * Embeds the whole source map data directly into the compiled CSS file by transforming
	 * <tt>sourceMappingURL</tt> into a data URI. The default value is <tt>false</tt>.
	 *
	 * @parameter default-value="false"
	 */
	private boolean embedSourceMapInCSS;

	/**
	 * Embeds the contents of the source .scss files in the source map file instead of the
	 * paths to those files. The default value is <tt>false</tt>
	 *
	 * @parameter default-value="false"
	 */
	private boolean embedSourceContentsInSourceMap;

	/**
	 * Switches the input syntax used by the files to either <tt>sass</tt> or <tt>scss</tt>.
	 * The default value is <tt>scss</tt>.
	 *
	 * @parameter default-value="scss"
	 */
	private SassCompiler.InputSyntax inputSyntax;

	/**
	 * Precision for fractional numbers. The default value is <tt>5</tt>.
	 *
	 * @parameter default-value="5"
	 */
	private int precision;
	
	/**
	 * should fail the build in case of compilation errors.
	 * 
	 * @parameter default-value="true"
	 */
	private boolean failOnError;

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	private SassCompiler compiler;

	public void execute() throws MojoExecutionException, MojoFailureException {
		validateConfig();
		compiler = initCompiler();

		inputPath = inputPath.replaceAll("\\\\", "/");

		getLog().debug("Input Path=" + inputPath);
		getLog().debug("Output Path=" + outputPath);

		final Path root = project.getBasedir().toPath().resolve(Paths.get(inputPath));
		String fileExt = inputSyntax.toString();
		String globPattern = "glob:{**/,}*."+fileExt;
		getLog().debug("Glob = " + globPattern);

		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
		final AtomicInteger errorCount = new AtomicInteger(0);
		final AtomicInteger fileCount = new AtomicInteger(0);
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (matcher.matches(file) && !file.getFileName().toString().startsWith("_")) {
						fileCount.incrementAndGet();
						if(!processFile(root, file)){
							errorCount.incrementAndGet();
						}
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

		getLog().info("Compiled " + fileCount + " files");
		if(errorCount.get() > 0){
			if (failOnError){
				throw new MojoExecutionException("Failed with " + errorCount.get() + " errors");
			} else {
				getLog().error("Failed with " + errorCount.get() + " errors. Continuing due to failOnError=false.");
			}
		}
	}

	private void validateConfig() {
		if (!generateSourceMap) {
			if (embedSourceMapInCSS) {
				getLog().warn("embedSourceMapInCSS=true is ignored. Cause: generateSourceMap=false");
			}
			if (embedSourceContentsInSourceMap) {
				getLog().warn("embedSourceContentsInSourceMap=true is ignored. Cause: generateSourceMap=false");
			}
		}
		if (outputStyle != SassCompiler.OutputStyle.compressed && outputStyle != SassCompiler.OutputStyle.nested) {
			getLog().warn("outputStyle=" + outputStyle + " is replaced by nested. Cause: libsass 3.1 only supports compressed and nested");
		}
	}

	private SassCompiler initCompiler() {
		SassCompiler compiler = new SassCompiler();
		compiler.setEmbedSourceMapInCSS(this.embedSourceMapInCSS);
		compiler.setEmbedSourceContentsInSourceMap(this.embedSourceContentsInSourceMap);
		compiler.setGenerateSourceComments(this.generateSourceComments);
		compiler.setGenerateSourceMap(this.generateSourceMap);
		compiler.setIncludePaths(this.includePath);
		compiler.setInputSyntax(this.inputSyntax);
		compiler.setOmitSourceMappingURL(this.omitSourceMapingURL);
		compiler.setOutputStyle(this.outputStyle);
		compiler.setPrecision(this.precision);
		return compiler;
	}

	private boolean processFile(Path inputRootPath, Path inputFilePath) throws IOException {
		getLog().debug("Processing File " + inputFilePath);

		Path relativeInputPath = inputRootPath.relativize(inputFilePath);
		
		Path outputRootPath = this.outputPath.toPath();
		Path outputFilePath = outputRootPath.resolve(relativeInputPath);
		outputFilePath = Paths.get(outputFilePath.toAbsolutePath().toString().replaceFirst("\\.scss$", ".css"));
		
		Path sourceMapRootPath = Paths.get(this.sourceMapOutputPath);
		Path sourceMapOutputPath = sourceMapRootPath.resolve(relativeInputPath);
		sourceMapOutputPath = Paths.get(sourceMapOutputPath.toAbsolutePath().toString().replaceFirst("\\.scss$", ".css.map"));


		SassCompilerOutput out;
		try {
			out = compiler.compileFile(
					inputFilePath.toAbsolutePath().toString(),
					outputFilePath.toAbsolutePath().toString(),
					sourceMapOutputPath.toAbsolutePath().toString()
			);
		}
		catch (SassCompilationException e) {
			getLog().error(e.getMessage());
			getLog().debug(e);
			return false;
		}

		getLog().debug("Compilation finished.");

		writeContentToFile(outputFilePath, out.getCssOutput());
		if (out.getSourceMapOutput() != null) {
			writeContentToFile(sourceMapOutputPath, out.getSourceMapOutput());
		}
		return true;
	}

	private void writeContentToFile(Path outputFilePath, String content) throws IOException {
		File f = outputFilePath.toFile();
		f.getParentFile().mkdirs();
		f.createNewFile();
		OutputStreamWriter os = null;
		try{
			os = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
			os.write(content); 
			os.flush();
		} finally {
			if (os != null)
				os.close();
		}
		getLog().debug("Written to: " + f);
	}
}