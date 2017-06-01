package wrm;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Compilation of all scss files from inputpath to outputpath using includePaths
 *
 * @goal compile
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */
public class CompilationMojo extends AbstractSassMojo {

	/**
	 * @component
	 */
	private BuildContext buildContext;

	/**
	 * Returns project relative input path for sass files. If input path is absolute, remove base dir from string.
	 * @return project relative input path. 
	 */
	private String getRelativeInputPath() {
		String relativeInputPath = inputPath;
		String baseDirPath = project.getBasedir().getPath();
		if (relativeInputPath.startsWith(baseDirPath)) {
			relativeInputPath = relativeInputPath.substring(baseDirPath.length() + 1);
		}
		return relativeInputPath;
	}
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		validateConfig();
		if ((buildContext!=null) && 
				(!buildContext.isIncremental() || (buildContext.hasDelta(getRelativeInputPath())))) {

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

}