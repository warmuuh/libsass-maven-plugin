package wrm.libsass;

public class SassCompilerOutput {

	private final String cssOutput;
	private final String sourceMapOutput;

	public SassCompilerOutput(final String cssOutput, final String sourceMapOutput) {
		this.cssOutput = cssOutput;
		this.sourceMapOutput = sourceMapOutput;
	}

	public String getCssOutput() {
		return cssOutput;
	}

	public String getSourceMapOutput() {
		return sourceMapOutput;
	}
}
