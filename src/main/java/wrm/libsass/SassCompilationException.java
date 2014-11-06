package wrm.libsass;

public class SassCompilationException extends Exception {

  private static final long serialVersionUID = -1843613263417826330L;

  public SassCompilationException() {
    super();
  }

  public SassCompilationException(String arg0, Throwable arg1, boolean arg2,
                                  boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public SassCompilationException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public SassCompilationException(String arg0) {
    super(arg0);
  }

  public SassCompilationException(Throwable arg0) {
    super(arg0);
  }
}
