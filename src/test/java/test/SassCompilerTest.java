package test;

import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;
import wrm.libsass.SassCompiler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SassCompiler}.
 *
 * @author ogolberg@vecna.com
 */
public class SassCompilerTest {

	private SassCompiler compiler;
	private Output out;

	@Before
	public void initCompiler(){
		compiler = new SassCompiler();
		compiler.setPrecision(5);
		compiler.setOutputStyle(OutputStyle.EXPANDED);
		compiler.setOmitSourceMappingURL(false);
		compiler.setInputSyntax(SassCompiler.InputSyntax.scss);
		compiler.setEmbedSourceMapInCSS(false);
		compiler.setEmbedSourceContentsInSourceMap(false);
		compiler.setGenerateSourceComments(false);
		compiler.setGenerateSourceMap(true);
		compiler.setIncludePaths(null);
	}

	@Test
	public void testWithDefaultSettings() throws Exception {
		compile("/test.scss");

		assertCssContains("font: 100% Helvetica, sans-serif");
		assertCssContains("color: #333");
		assertCssContains("margin: 0");
	}

	@Test
	public void testWithOmitSourceMapUrlTrue() throws Exception {
		compiler.setOmitSourceMappingURL(true);
		compiler.setGenerateSourceMap(true);
		compile("/test.scss");

		assertCssDoesNotContain("/*# sourceMappingURL=");
		assertNotNull(out.getSourceMap());
	}

	@Test
	public void testWithOmitSourceMapUrlFalse() throws Exception {
		compiler.setOmitSourceMappingURL(false);
		compiler.setGenerateSourceMap(true);
		compile("/test.scss");

		assertCssContains("/*# sourceMappingURL=");
		assertNotNull(out.getSourceMap());
	}

	@Test
	public void testWithOutputStyleExpanded() throws Exception {
		// Warning: As of Libsass 3.1, expanded is the same as nested
		compiler.setOutputStyle(OutputStyle.EXPANDED);
		compile("/test.scss");

		assertCssContains("* {\n  margin: 0;\n}\n");
	}

	@Test
	public void testWithOutputStyleNested() throws Exception {
		compiler.setOutputStyle(OutputStyle.NESTED);
		compile("/test.scss");

		assertCssContains("* {\n  margin: 0; }\n");
	}

	@Test
	public void testWithOutputStyleCompressed() throws Exception {
		compiler.setOutputStyle(OutputStyle.COMPRESSED);
		compile("/test.scss");

		assertCssContains("*{margin:0}body{font:100% Helvetica,sans-serif;color:#333}");
	}

	@Test
	public void testWithOutputStyleCompact() throws Exception {
		// Warning: As of Libsass 3.1, compact is the same as nested
		compiler.setOutputStyle(OutputStyle.COMPACT);
		compile("/test.scss");

		assertCssContains("* { margin: 0; }\n");
	}

	@Test
	public void testWithGenerateSourceMapFalse() throws Exception {
		compiler.setGenerateSourceMap(false);
		compiler.setEmbedSourceContentsInSourceMap(true);
		compiler.setOmitSourceMappingURL(false);
		compile("/test.scss");

		assertNull(out.getSourceMap());
		assertCssDoesNotContain("/*# sourceMappingURL=");
	}

	@Test
	public void testDefaultPrecision() throws Exception{
		compiler.setOutputStyle(OutputStyle.COMPRESSED);
		compile("/precision.scss");

		assertCssContains(".something{padding:0 0.8em .71429 0.8em}");
	}

	@Test
	public void testHighPrecision() throws Exception{
		compiler.setOutputStyle(OutputStyle.COMPRESSED);
		compiler.setPrecision(10);
		compile("/precision.scss");

		assertCssContains(".something{padding:0 0.8em .7142857143 0.8em}");
	}

	private void compile(String file) throws Exception {
		String absolutePath = new java.io.File(getClass().getResource(file).getFile()).getAbsolutePath();
		out = compiler.compileFile(absolutePath, "prout", "denver");
	}

	private void assertCssContains(String expected){
		String css = out.getCss();
		String formatted = replaceNewLines(expected);
		assertTrue("Generated CSS does not contain: " + formatted + "\nbut got: " + css, css.contains(formatted));
	}

	private String replaceNewLines(String expected) {
		return expected.replace("\n", System.lineSeparator());
	}

	private void assertCssDoesNotContain(String unwanted){
		String css = out.getCss();
		String formatted = replaceNewLines(unwanted);
		assertFalse("Generated CSS contains: " + formatted + "\n" + css, css.contains(formatted));
	}

	private void assertMapContains(String expected){
		String sourceMap = out.getSourceMap();
		String formatted = replaceNewLines(expected);
		assertTrue("Generated SourceMap does not contain: " + formatted + "\n" + sourceMap, sourceMap.contains(formatted));
	}

	private void assertMapDoesNotContain(String unwanted){
		String sourceMap = out.getSourceMap();
		String formatted = replaceNewLines(unwanted);
		assertFalse("Generated SourceMap contains: " + formatted + "\n" + sourceMap, sourceMap.contains(formatted));
	}
}
