package test;

import junit.framework.TestCase;
import wrm.libsass.SassCompiler;


/**
 * Tests for {@link SassCompiler}.
 * @author ogolberg@vecna.com
 */
public class SassCompilerTest extends TestCase {
  /**
   * Tests {@link SassCompiler#compileFile(String, String, String)}.
   */
  public void testCompileFile() throws Exception {
    SassCompiler sassCompiler = new SassCompiler();

    String out = sassCompiler.compileFile(getClass().getResource("/test.scss").getFile(), null, null);

    assertTrue("wrong CSS output", out.contains("font: 100% Helvetica, sans-serif"));
    assertTrue("wrong CSS output",out.contains("color: #333"));
  }
}
