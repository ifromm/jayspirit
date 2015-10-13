package hyspirit.engines;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import hyspirit.knowledgeBase.HyTuple;
import hyspirit.util.Util;

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
	Util u = new Util();
	HyText2TFEngine tf = null;
	try {
	    tf = new HyText2TFEngine();
	    tf.stemming(true);
	    File sFile = u.fileLocation("stopword-list.txt");
	    File mFile = u.fileLocation("morphemes-list.txt");
	    if (sFile != null)
		tf.stopwordfile(sFile.getAbsolutePath());
	    if (mFile != null)
		tf.morphemefile(mFile.getAbsolutePath());
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

}
