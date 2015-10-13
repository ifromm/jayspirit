package hyspirit.engines;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import hyspirit.knowledgeBase.HyTuple;
import hyspirit.util.Util;

public class Text2MDSEngineTest {
    String[] results = { "hutzliputz",
	    "hutzliputz",
	    "hutzliputz",
	    "freak",
	    "cool",
    };

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
	Util u = new Util();
	HyText2MDSEngine mds = null;
	try {
	    mds = new HyText2MDSEngine();
	    mds.stemming(true);
	    File sFile = u.fileLocation("stopword-list.txt");
	    File mFile = u.fileLocation("morphemes-list.txt");
	    if (sFile != null)
		mds.addStopwordFile(sFile.getAbsolutePath());
	    if (mFile != null)
		mds.addMorphemeFile(mFile.getAbsolutePath());
	    System.out.println("Starting");
	    mds.run();
	    System.out.println("Sending");
	    mds.send("hutzliputz hutzliputz hutzliputz is freaking cool");
	    mds.closeSTDIN();
	    int i = 0;
	    while (mds.hasNext()) {
		HyTuple t = new HyTuple(mds.next());
		String term = t.valueAt(0);
		System.out.println(term);
		if (!term.equals(results[i++]))
		    fail(term + " wrong.");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	} finally {
	    mds.destroy();
	}
    }

}
