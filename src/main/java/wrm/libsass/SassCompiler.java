package wrm.libsass;

import java.io.File;
import java.nio.file.Path;


import com.cathive.sass.*;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class SassCompiler {

    private String includePaths;
    private SassOutputStyle outputStyle;
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
//		Sass_Compiler compiler = null;
        SassFileContext ctx = createConfiguredContext(inputPath, outputPath, sourceMapPath);
        String output_string = ctx.compile();

//      TODO: how to get sourcemap string?
//		String sourceMapOutput = SASS.sass_context_get_source_map_string(genericCtx);

        return new SassCompilerOutput(output_string, /*TODO*/ null);
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

    private SassFileContext createConfiguredContext( //
                                                     String inputPathAbsolute, //
                                                     String outputPathRelativeToInput, //
                                                     String sourceMapPathRelativeToInput //
    ) {
        String allIncludePaths = new File(inputPathAbsolute).getParent();
        if (this.includePaths != null) {
            allIncludePaths = this.includePaths + File.pathSeparator + allIncludePaths;
        }
        SassFileContext ctx = SassFileContext.create(new File(allIncludePaths).toPath());

        SassOptions opts = ctx.getOptions();
        opts.setInputPath(inputPathAbsolute);
        opts.setOutputPath(outputPathRelativeToInput);
        opts.setIncludePath(allIncludePaths);
        opts.setSourceComments(this.generateSourceComments);
        opts.setOutputStyle(this.outputStyle);
        opts.setIsIndentedSyntaxSrc(this.inputSyntax == InputSyntax.sass);
        opts.setPrecision(this.precision);
        if (this.generateSourceMap) {
            opts.setSourceMapFile(sourceMapPathRelativeToInput);
            opts.setSourceMapContents(this.embedSourceContentsInSourceMap);
            opts.setSourceMapEmbed(this.embedSourceMapInCSS);
            opts.setOmitSourceMapUrl(this.omitSourceMappingURL);
        } else {
//			SASS.sass_option_set_source_map_file(opts, null);
            opts.setSourceMapContents(false);
            opts.setSourceMapEmbed(false);
            opts.setOmitSourceMapUrl(true);
        }

        ctx.setOptions(opts);
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

    public void setOutputStyle(final SassOutputStyle outputStyle) {
        this.outputStyle = outputStyle;
    }

    public void setOutputStyle(final OutputStyle outputStyle) {
        this.outputStyle = SassOutputStyle.values()[outputStyle.ordinal()];
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