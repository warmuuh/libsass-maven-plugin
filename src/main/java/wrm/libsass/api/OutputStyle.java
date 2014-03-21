package wrm.libsass.api;
public enum OutputStyle { 
	NESTED(0), EXPANDED(1), COMPACT(2), COMPRESSED(3), FORMATTED(4);

	int value;
	private OutputStyle(int value) {
		this.value=value;
	}
	public int value() {
		return value;
	}
};