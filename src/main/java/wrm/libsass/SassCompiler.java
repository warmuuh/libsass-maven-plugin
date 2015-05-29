package wrm.libsass;

import java.io.File;

import sass.SassLibrary;
import sass.SassLibrary.Sass_Compiler;
import sass.SassLibrary.Sass_Context;
import sass.SassLibrary.Sass_File_Context;
import sass.SassLibrary.Sass_Options;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class SassCompiler {

	private static final SassLibrary SASS = (SassLibrary) Native.loadLibrary("sass", SassLibrary.class);

	private String includePaths;
	private OutputStyle outputStyle;
	private boolean generateSourceComments;
	private boolean generateSourceMap;
	private boolean omitSourceMappingURL;
	private boolean embedSourceMapInCSS;
	private boolean embedSourceContentsInSourceMap;
	private SassCompiler.InputSyntax inputSyntax;
	private int precision;

	/**
	 * All paths passed to this method must be relative to the same directory.
	 */
	public SassCompilerOutput compileFile(
			String inputPath, //
			String outputPath, //
			String sourceMapPath //

	) throws SassCompilationException {
		Sass_Compiler compiler = null;
		try {
			SassLibrary.Sass_File_Context ctx = createConfiguredContext(inputPath, outputPath, sourceMapPath);

			compiler = SASS.sass_make_file_compiler(ctx);
			SASS.sass_compiler_parse(compiler);
			SASS.sass_compiler_execute(compiler);
			
			Sass_Context genericCtx = SASS.sass_file_context_get_context(ctx);
			
			if (SASS.sass_context_get_error_status(genericCtx) != 0) {
				String errMsg = SASS.sass_context_get_error_message(genericCtx);
				throw new SassCompilationException(errMsg);
			}

			String output_string = SASS.sass_context_get_output_string(genericCtx);
			if (output_string == null) {
				throw new SassCompilationException("libsass returned null");
			}

			String sourceMapOutput = SASS.sass_context_get_source_map_string(genericCtx);

			return new SassCompilerOutput(output_string, sourceMapOutput);
		}
		finally {
			try {
				if (compiler != null) {
					SASS.sass_delete_compiler(compiler);
				}
			}
			catch (Throwable t) {
				throw new SassCompilationException(t);
			}
		}
	}

	/**
	 * converts a string to a pointer
	 *
	 * @param string
	 * @return pointer to a copy of the string
	 */
	private Pointer str(String string) {
		if (string == null) {
			return null;
		}

		Memory mem = new Memory(string.length() + 1);
		mem.setString(0, string);
		return mem;
	}

	private Sass_File_Context createConfiguredContext( //
			String inputPathAbsolute, //
			String outputPathRelativeToInput, //
			String sourceMapPathRelativeToInput //
	) {
		String allIncludePaths = new File(inputPathAbsolute).getParent();
		if (this.includePaths != null) {
			allIncludePaths = this.includePaths + File.pathSeparator + allIncludePaths;
		}
		Sass_File_Context ctx = SASS.sass_make_file_context(allIncludePaths);
		
		Sass_Options opts = SASS.sass_file_context_get_options(ctx);
		SASS.sass_option_set_input_path(opts, inputPathAbsolute);
		SASS.sass_option_set_output_path(opts, outputPathRelativeToInput);
		SASS.sass_option_set_include_path(opts, allIncludePaths);
		SASS.sass_option_set_source_comments(opts, this.generateSourceComments ? (byte) 1 : 0);
		SASS.sass_option_set_output_style(opts, this.outputStyle.ordinal());
		SASS.sass_option_set_is_indented_syntax_src(opts, this.inputSyntax == InputSyntax.sass ? (byte) 1 : 0);
		SASS.sass_option_set_precision(opts, this.precision);

		if (this.generateSourceMap) {
			SASS.sass_option_set_source_map_file(opts, sourceMapPathRelativeToInput);
			SASS.sass_option_set_source_map_contents(opts, this.embedSourceContentsInSourceMap ? (byte) 1 : 0);
			SASS.sass_option_set_source_map_embed(opts, this.embedSourceMapInCSS ? (byte) 1 : 0);
			SASS.sass_option_set_omit_source_map_url(opts, this.omitSourceMappingURL ? (byte) 1 : 0);
		} else {
//			SASS.sass_option_set_source_map_file(opts, null);
			SASS.sass_option_set_source_map_contents(opts, (byte)0);
			SASS.sass_option_set_source_map_embed(opts, (byte)0);
			SASS.sass_option_set_omit_source_map_url(opts, (byte)1);
		}
		
		SASS.sass_file_context_set_options(ctx, opts);
		return ctx;
	}

	public void setEmbedSourceMapInCSS(final boolean embedSourceMapInCSS) {
		this.embedSourceMapInCSS = embedSourceMapInCSS;
	}

	public void setEmbedSourceContentsInSourceMap(final boolean embedSourceContentsInSourceMap) {
		this.embedSourceContentsInSourceMap = embedSourceContentsInSourceMap;
	}

	public void setGenerateSourceComments(final boolean generateSourceComments) {
		this.generateSourceComments = generateSourceComments;
	}

	public void setGenerateSourceMap(final boolean generateSourceMap) {
		this.generateSourceMap = generateSourceMap;
	}

	public void setIncludePaths(final String includePaths) {
		this.includePaths = includePaths;
	}

	public void setInputSyntax(final InputSyntax inputSyntax) {
		this.inputSyntax = inputSyntax;
	}

	public void setOmitSourceMappingURL(final boolean omitSourceMappingURL) {
		this.omitSourceMappingURL = omitSourceMappingURL;
	}

	public void setOutputStyle(final OutputStyle outputStyle) {
		this.outputStyle = outputStyle;
	}

	public void setPrecision(final int precision) {
		this.precision = precision;
	}

	public static enum OutputStyle {
		nested, expanded, compact, compressed
	}

	public static enum InputSyntax {
		sass, scss
	}
}