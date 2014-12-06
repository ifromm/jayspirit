package apriorie.util;

import java.io.InputStream;
import java.io.PrintStream;

public class StreamGobbler extends hyspirit.util.StreamGobbler {
	//public StreamGobbler () {
	//}
	public StreamGobbler (InputStream inputStream,
				PrintStream printStream) {
		super(inputStream, printStream);
	}
}
