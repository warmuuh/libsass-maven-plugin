package wrm;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Compilation of all scss files from inputpath to outputpath using includePaths
 *
 * @goal compile
 * @phase generate-resources
 */
public class CompilationMojo extends AbstractSassMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		validateConfig();
		compiler = initCompiler();

		inputPath = inputPath.replaceAll("\\\\", "/");

		getLog().debug("Input Path=" + inputPath);
		getLog().debug("Output Path=" + outputPath);
		
		try {
			compile();
		} catch (Exception e) {
			throw new MojoExecutionException("Failed", e);
		}
	}

}