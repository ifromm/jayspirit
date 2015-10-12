package hyspirit.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 */
public class RetrievalstrategyManagerTestCase extends TestCase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
	try {
	    String[] filesBasic = { "pd/test1.pd", "pd/test2.pd" };
	    String[] filesAdv = { "pd/test1_adv.pd", "pd/test2_adv.pd",
		    "pd/test3_adv.pd" };
	    RetrievalstrategyManager rm = new RetrievalstrategyManager(
		    fileLocation("retrievalstrategy.xml"));
	    List<String> files = rm.getSequence("basic");
	    if (files.size() != 2)
		fail("Wrong size (basic)");
	    int i = 0;
	    for (Iterator<String> it = files.iterator(); it.hasNext();) {
		String fileS = it.next();
		System.out.println(fileS);
		if (!filesBasic[i++].equals(fileS))
		    fail("Unexpected file (basic)");
	    }
	    files = rm.getSequence("advanced");
	    if (files.size() != 3)
		fail("Wrong size (advanced)");
	    i = 0;
	    for (Iterator<String> it = files.iterator(); it.hasNext();) {
		String fileS = it.next();
		System.out.println(fileS);
		if (!filesAdv[i++].equals(fileS))
		    fail("Unexpected file (adv)");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

    /**
     * Checks of the file name if present at some pre-defined location. Returns
     * the absolute path of the file if present or null if it does not exist at
     * the given locations.
     *
     * @param fileName
     *            the file name
     * @return the file
     */
    private File fileLocation(String fileName) {
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
	return aFile;
    }

}
