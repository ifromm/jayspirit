package hyspirit.engines;

import hyspirit.engines.HyPDatalogEngine;
import hyspirit.knowledgeBase.HyTuple;
import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assume.*;

public class HyPDTestCase extends TestCase {

    HyPDatalogEngine hypd = null;

    @Override
    @Before
    public void setUp() throws Exception {
	try {
	    HySpiritProperties hyspirit = new HySpiritProperties();
	    // we found a HySpirit environment and can run tests
	    hypd = new HyPDatalogEngine(hyspirit);
	} catch (HySpiritException h) {
	    System.out.println("Could not find a HySpirit environment, "
		    + "HyPDatalogEngine tests will be skipped. Please set up "
		    + "a proper HySpirit installation and repeat "
		    + "the test before using JaySpirit!");
	}
	// assumeTrue(hypd != null);
    }

    @Override
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testQueryQueue() {
	if (hypd != null) {
	    System.out.println("Testing hyp_pd query queue.");
	    String retrieveQuery =
		    "term(database, d1);\n" +
			    "0.35 term(ir, d2);\n" +
			    "qterm(ir);\n" +
			    "retrieve(D) :- qterm(T) & term(T,D);\n" +
			    "?- retrieve(D);\n";
	    hypd.readFromSTDIN();
	    hypd.run();
	    try {
		hypd.addQueryToQueue("retrieve", retrieveQuery);
		hypd.addQueryToQueue("qterm", "?- qterm(T).");
		hypd.executeQueryQueue();

		List<HyTuple> retResults =
			hypd.getResultForQuery("retrieve");
		System.out.println("\nRetrieve:");
		String expected = "0.35 (d2)\n";
		String resultString = "";
		for (HyTuple tuple : retResults) {
		    resultString += tuple.toString() + "\n";
		}
		System.out.println(resultString);
		assertEquals(resultString, expected);

		expected = "(ir)\n";
		resultString = "";
		List<HyTuple> qtResults =
			hypd.getResultForQuery("qterm");
		System.out.println("QTerm:");
		for (HyTuple tuple : qtResults) {
		    resultString += tuple + "\n";
		}
		System.out.println(resultString);
		assertEquals(resultString, expected);
	    }

	    catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    } finally {
		hypd.destroy(); // important!
	    }

	}
    }
}
