package wrm.libsass;

import sass.SassLibrary;
import sass.sass_file_context;

import java.io.File;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class SassCompiler {

	private static final SassLibrary SASS = (SassLibrary) Native.loadLibrary("sass", SassLibrary.class);

	private String imagePath;
	private String includePaths;
	private OutputStyle outputStyle;
	private boolean generateSourceComments;
	private boolean generateSourceMap;
	private String sourceMapPathPrefix;
	private boolean omitSourceMapUrl;
	private boolean embedSourceMap;
	private boolean embedSourceMapContents;
	private SassCompiler.InputSyntax inputSyntax;
	private int precision;

	public SassCompilerOutput compileFile(String inputFile) throws SassCompilationException {
		sass_file_context ctx = null;
		try {
			ctx = SASS.sass_new_file_context();
			setOptions(ctx, inputFile);

			SASS.sass_compile_file(ctx);

			if (ctx.error_status != 0) {
				throw new SassCompilationException(ctx.error_message.getString(0));
			}

			if (ctx.output_string == null || ctx.output_string.getString(0) == null) {
				throw new SassCompilationException("libsass returned null");
			}

			String cssOutput = ctx.output_string.getString(0);
			String sourceMapOutput = null;
			if(ctx.source_map_string != null && ctx.source_map_string.getString(0) != null){
				sourceMapOutput = ctx.source_map_string.getString(0);
			}

			return new SassCompilerOutput(cssOutput, sourceMapOutput);
		}
		finally {
			try {
				if (ctx != null) {
					SASS.sass_free_file_context(ctx);
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

	private void setOptions(sass_file_context ctx, String inputFile) {

		String allIncludePaths = new File(inputFile).getParent();
		if (this.includePaths != null) {
			allIncludePaths = this.includePaths + File.pathSeparator + allIncludePaths;
		}

		ctx.input_path = str(inputFile);
		ctx.output_path = null; // FIXME: is that correct, seems like the source map back references would be wrong ?
		ctx.options.include_paths = str(allIncludePaths);
		ctx.options.image_path = str(this.imagePath);
		ctx.options.source_comments = this.generateSourceComments ? (byte) 1 : 0;
		ctx.options.output_style = this.outputStyle.ordinal();
		ctx.options.is_indented_syntax_src = this.inputSyntax == InputSyntax.sass ? (byte) 1 : 0;
		ctx.options.precision = this.precision;

		if (this.generateSourceMap) {
			// FIXME: there is a better way to do this...
			String inputFileName = new File(inputFile).getParent();
			ctx.options.source_map_file = str(sourceMapPathPrefix + "/" + inputFileName + ".map");
			ctx.options.source_map_contents = this.embedSourceMapContents ? (byte) 1 : 0;
			ctx.options.source_map_embed = this.embedSourceMap ? (byte) 1 : 0;
			ctx.options.omit_source_map_url = this.omitSourceMapUrl ? (byte) 1 : 0;

		} else {
			ctx.options.source_map_file = null;
			ctx.options.source_map_contents = 0;
			ctx.options.source_map_embed = 0;
			ctx.options.omit_source_map_url = 1;
		}
	}

	public void setEmbedSourceMap(final boolean embedSourceMap) {
		this.embedSourceMap = embedSourceMap;
	}

	public void setEmbedSourceMapContents(final boolean embedSourceMapContents) {
		this.embedSourceMapContents = embedSourceMapContents;
	}

	public void setGenerateSourceComments(final boolean generateSourceComments) {
		this.generateSourceComments = generateSourceComments;
	}

	public void setGenerateSourceMap(final boolean generateSourceMap) {
		this.generateSourceMap = generateSourceMap;
	}

	public void setImagePath(final String imagePath) {
		this.imagePath = imagePath;
	}

	public void setIncludePaths(final String includePaths) {
		this.includePaths = includePaths;
	}

	public void setInputSyntax(final InputSyntax inputSyntax) {
		this.inputSyntax = inputSyntax;
	}

	public void setOmitSourceMapUrl(final boolean omitSourceMapUrl) {
		this.omitSourceMapUrl = omitSourceMapUrl;
	}

	public void setOutputStyle(final OutputStyle outputStyle) {
		this.outputStyle = outputStyle;
	}

	public void setPrecision(final int precision) {
		this.precision = precision;
	}

	public void setSourceMapPathPrefix(final String sourceMapPathPrefix) {
		this.sourceMapPathPrefix = sourceMapPathPrefix;
	}

	public static enum OutputStyle {
		nested, expanded, compact, compressed
	}

	public static enum InputSyntax {
		sass, scss
	}
}