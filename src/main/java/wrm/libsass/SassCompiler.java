package wrm.libsass;

import static wrm.libsass.SassCompiler.InputSyntax.sass;

import java.io.File;
import java.net.URI;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;

public class SassCompiler {

	private String includePaths;
	private io.bit3.jsass.OutputStyle outputStyle;
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
	public Output compileFile(
			String inputPathAbsolute, //
			String outputPathRelativeToInput, //
			String sourceMapPathRelativeToInput //

	) throws CompilationException {

		String inputOmitSpace = inputPathAbsolute.replaceAll("%20", " ");
		String outputOmitSpace = outputPathRelativeToInput.replaceAll("%20", " ");

		URI inputFile = new File(inputOmitSpace).toURI();
		URI outputFile = new File(outputOmitSpace).toURI();

		Options opt = getConfiguredOptions(inputPathAbsolute, sourceMapPathRelativeToInput);

		io.bit3.jsass.Compiler c = new io.bit3.jsass.Compiler();

		return c.compileFile(inputFile, outputFile, opt);
	}

	private Options getConfiguredOptions(String inputPathAbsolute, String sourceMapPathRelativeToInput) {
		Options opt = new Options();

		if(includePaths != null) {
			for (String path : includePaths.split(File.pathSeparator)) {
				opt.getIncludePaths().add(new File(path));
			}
		}
		String allIncludePaths = new File(inputPathAbsolute).getParent();
		opt.getIncludePaths().add(new File(allIncludePaths));

		opt.setIsIndentedSyntaxSrc(inputSyntax == sass);
		opt.setOutputStyle(outputStyle);

		opt.setSourceComments(generateSourceComments);
		opt.setPrecision(precision);


		if (generateSourceMap) {
			opt.setSourceMapFile(new File(sourceMapPathRelativeToInput).toURI());
			opt.setSourceMapContents(embedSourceContentsInSourceMap);
			opt.setSourceMapEmbed(embedSourceMapInCSS);
			opt.setOmitSourceMapUrl(omitSourceMappingURL);
		} else {
			opt.setSourceMapContents(false);
			opt.setSourceMapEmbed(false);
			opt.setOmitSourceMapUrl(true);
		}
		return opt;
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

	public void setOutputStyle(final io.bit3.jsass.OutputStyle outputStyle) {
		this.outputStyle = outputStyle;
	}

	public void setOutputStyle(final OutputStyle outputStyle) {
		this.outputStyle = io.bit3.jsass.OutputStyle.values()[outputStyle.ordinal()];
	}

	public static enum OutputStyle {
		nested, expanded, compact, compressed
	}

	public void setPrecision(final int precision) {
		this.precision = precision;
	}


	public static enum InputSyntax {
		sass, scss
	}
}