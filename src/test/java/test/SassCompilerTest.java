package test;

import wrm.libsass.SassCompiler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SassCompiler}.
 *
 * @author ogolberg@vecna.com
 */
public class SassCompilerTest {

	private SassCompiler compiler;

	@Before
	public void initCompiler(){
		compiler = new SassCompiler();
		compiler.setSourceMapPathPrefix("");
		compiler.setPrecision(5);
		compiler.setOutputStyle(SassCompiler.OutputStyle.expanded);
		compiler.setOmitSourceMapUrl(false);
		compiler.setInputSyntax(SassCompiler.InputSyntax.scss);
		compiler.setEmbedSourceMap(false);
		compiler.setEmbedSourceMapContents(false);
		compiler.setGenerateSourceComments(false);
		compiler.setGenerateSourceMap(true);
		compiler.setImagePath(null);
		compiler.setIncludePaths(null);
	}

	/**
	 * Tests {@link SassCompiler#compileFile(String)}.
	 */
	@Test
	public void testCompileFile() throws Exception {

		String out = compiler.compileFile(getClass().getResource("/test.scss").getFile());

		System.out.println(out);

		assertTrue("wrong CSS output", out.contains("font: 100% Helvetica, sans-serif"));
		assertTrue("wrong CSS output", out.contains("color: #333"));

		assertTrue("wrong CSS output", out.contains("margin: 0"));
	}
}
