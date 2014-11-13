package wrm.libsass;

import java.io.File;

import wrm.libsass.api.LibSassDll;
import wrm.libsass.api.OutputStyle;
import wrm.libsass.api.sass_file_context;

import com.sun.jna.Native;

public class SassCompiler {
  private static final LibSassDll sass = (LibSassDll) Native.loadLibrary("sass", LibSassDll.class);

  public String compileFile(String inputFile, String includePath, String imgPath) throws SassCompilationException {
    sass_file_context ctx = null;
    try{
      ctx = sass.sass_new_file_context();

      ctx.input_path = inputFile;
      ctx.output_path = null;

      String includePaths = new File(inputFile).getParent();

      if (includePath != null) {
        includePaths = includePath + File.pathSeparator + includePaths;
      }

      ctx.options.include_paths = includePaths;
      ctx.options.image_path = imgPath;
      ctx.options.source_comments = false;
      ctx.source_map_string = null;
      ctx.options.output_style = OutputStyle.EXPANDED.value();

      sass.sass_compile_file(ctx);

      if (ctx.error_status != 0) {
        throw new SassCompilationException(ctx.error_message.getString(0));
      }

      if (ctx.output_string == null || ctx.output_string.getString(0) == null) {
        throw new SassCompilationException("libsass returned null");
      }

      String output = ctx.output_string.getString(0);

      return output;
    } finally {
      try{
        if (ctx != null) {
          sass.sass_free_file_context(ctx);
        }
      } catch(Throwable t){
        throw new SassCompilationException(t);
      }
    }
  }
}