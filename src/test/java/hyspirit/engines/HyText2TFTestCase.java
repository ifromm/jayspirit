package hyspirit.engines;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import hyspirit.knowledgeBase.HyTuple;

/**
 * 
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 */
public class HyText2TFTestCase {

    private final String[] results = { "0.642857 (hutzliputz)",
	    "0.375 (cool)",
	    "0.375 (freak)" };

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
	HyText2TFEngine tf = null;
	try {
	    tf = new HyText2TFEngine();
	    tf.stemming(true);
	    tf.stopwordfile(fileLocation("stopword-list.txt"));
	    tf.morphemefile(fileLocation("morphemes-list.txt"));
	    System.out.println("Starting");
	    tf.run();
	    System.out.println("Sending");
	    tf.send("hutzliputz hutzliputz hutzliputz is freaking cool");
	    tf.closeSTDIN();
	    int i = 0;
	    while (tf.hasNext()) {
		HyTuple t = new HyTuple(tf.next());
		System.out.println(t);
		if (!t.toString().equals(results[i++]))
		    fail(t + " wrong.");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	} finally {
	    tf.destroy();
	}
    }

    /**
     * Checks of the file name if present at some pre-defined location. Returns
     * the absolute path of the file if present or null if it does not exist at
     * the given locations.
     *
     * @param fileName
     *            the file name
     * @return the absolute path
     */
    private String fileLocation(String fileName) {
	String baseDir = getClass().getProtectionDomain().getCodeSource()
		.getLocation().getFile();
	// check if the test XML document exists at one of the assumed locations
	File aFile = new File(baseDir + File.separator + "test-classes" +
		File.separator + fileName);
	System.out.println("Trying " + aFile.getAbsolutePath());
	if (!aFile.exists()) {
	    aFile = new File(baseDir + File.separator
		    + ".." + File.separator + fileName);
	    System.out.println("Trying " + aFile.getAbsolutePath());
	    if (!aFile.exists()) {
		aFile = new File(baseDir + File.separator
			+ fileName);
		System.out.println("Trying " + aFile.getAbsolutePath());
		if (!aFile.exists()) {
		    aFile = null;
		}
	    }
	}
	return aFile.getAbsolutePath();
    }

}
