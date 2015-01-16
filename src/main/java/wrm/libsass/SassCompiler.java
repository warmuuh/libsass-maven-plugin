package wrm.libsass;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.File;
import wrm.libsass.api.LibSassDll;
import wrm.libsass.api.OutputStyle;
import wrm.libsass.api.sass_file_context;

public class SassCompiler {
  private static final LibSassDll sass = (LibSassDll) Native.loadLibrary("sass", LibSassDll.class);

  public String compileFile(String inputFile, String includePath, String imgPath) throws SassCompilationException {
    sass_file_context ctx = null;
    try{
      ctx = sass.sass_new_file_context();

      ctx.input_path = str(inputFile);
      ctx.output_path = null;

      String includePaths = new File(inputFile).getParent();

      if (includePath != null) {
        includePaths = includePath + File.pathSeparator + includePaths;
      }

      ctx.options.include_paths = str(includePaths);
      ctx.options.image_path = str(imgPath);
      ctx.options.source_comments = 0;
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

  /**
   * converts a string to a pointer
   * @param string
   * @return pointer to a copy of the string
   */
  private Pointer str(String string){
    if (string == null) {
      return null;
    }

    Memory mem = new Memory(string.length() +1);
    mem.setString(0, string);
    return mem;
  }
}