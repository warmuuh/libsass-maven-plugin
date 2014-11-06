package wrm;

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

import wrm.libsass.SassCompilationException;
import wrm.libsass.SassCompiler;

/**
 * Compilation of all scss files from inputpath to outputpath using includePaths
 *
 * @goal compile
 *
 * @phase generate-resources
 */
public class CompilationMojo extends AbstractMojo {



  /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputPath;


    /**
     * Location of source files.
     * @parameter expression="src/main/sass"
     */
  private String inputPath;

  /**
     * Location of images.
     * @parameter
     */
  private String imgPath = "";

  /**
     * additional include path, ';'-separated
     * @parameter
     */
  private String includePath = "";




  private SassCompiler compiler = new SassCompiler();

  public void execute() throws MojoExecutionException, MojoFailureException {
    inputPath = inputPath.replaceAll("\\\\", "/");

    getLog().debug("Input Path=" + inputPath);
    getLog().debug("Output Path=" + outputPath);

    final Path root = Paths.get(inputPath);
    String globPattern = "glob:"+inputPath+"**/*.scss";
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
    } catch (IOException e) {
      throw new MojoExecutionException("Failed", e);
    }
  }


  private void processFile(final Path root, Path file)
      throws FileNotFoundException, IOException {
    getLog().debug("Processing File " + file);
    Path relPath = root.relativize(file);
    String outputFile = outputPath + File.separator + relPath.toString();
    outputFile = outputFile.substring(0, outputFile.lastIndexOf(".")) + ".css";
    convertFile(file.toString(), includePath, imgPath, outputFile);
  }


  private void convertFile(String inputFile, String includePath,
      String imgPath, String outputFile) throws FileNotFoundException,
      IOException {
    String content;
    try {
      content = compiler.compileFile(inputFile, includePath, imgPath);
    } catch (SassCompilationException e) {
      getLog().error(e.getMessage());
      getLog().debug(e);
      return;
    }

    getLog().debug("Compilation finished.");

    writeContentToFile(outputFile, content);
  }




  private void writeContentToFile(String outputFile, String content) throws IOException, FileNotFoundException {
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
