package hyspirit.engines;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.TestCase;
import hyspirit.engines.HyPRAEngine;
import hyspirit.knowledgeBase.HyTuple;
import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assume.*;


public class HyPRATestCase extends TestCase {


    HyPRAEngine hypra = null;

    @Before
    public void setUp() throws Exception {
	try {
	    HySpiritProperties hyspirit = new HySpiritProperties();
	    // we found a HySpirit environment and can run tests
	    hypra = new HyPRAEngine(hyspirit);
	}
	catch (HySpiritException h) {
	    System.out.println("Could not find a HySpirit environment, "
		    + "HyPRAEngine tests will be skipped. Please set up "
		    + "a proper HySpirit installation and repeat "
		    + "the test before using JaySpirit!");
	}
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testQueryQueue() {
	if (hypra != null) {
	    System.out.println("Testing hyp_pra query queue.");
	    String retrieveQuery =
		    "term(database, d1). " +
			    "0.35 term(ir, d2). " +
			    "qterm(ir). \n" +
			    "retrieve = " +
			    "UNITE(retrieve,PROJECT[$3]" +
			    "(JOIN[$1=$1](qterm,term))).\n" +
			    "?- PROJECT[$1](retrieve).";
	    hypra.readFromSTDIN();
	    hypra.run();
	    try {
		hypra.addQueryToQueue("retrieve", retrieveQuery);
		hypra.addQueryToQueue("qterm", "?- PROJECT[$1](qterm).");
		hypra.executeQueryQueue();

		List<HyTuple> retResults =
			hypra.getResultForQuery("retrieve");
		System.out.println("\nRetrieve:");
		String expected = "0.35 (d2)\n";
		String resultString = "";
		for (HyTuple tuple : retResults) {
		    resultString += tuple.toString() + "\n";
		}
		System.out.println(resultString);
		assertEquals(resultString,expected);

		expected = "(ir)\n";
		resultString = "";
		List<HyTuple> qtResults =
			hypra.getResultForQuery("qterm");
		System.out.println("QTerm:");
		for (HyTuple tuple : qtResults) {
		    resultString += tuple + "\n";
		}
		System.out.println(resultString);
		assertEquals(resultString,expected);
	    }

	    catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	    finally {
		hypra.destroy(); // important!
	    }
	}
    }

    /**
     * Test for empty results
     */
    @Test
    public void testEmptyResultQueryQueue() {
	if (hypra != null) {
	    System.out.println("Testing hyp_pra query queue empty result.");
	    hypra.readFromSTDIN();
	    hypra.run();
	    try {
		hypra.addQueryToQueue("emptytest", "?- PROJECT[$1](test).");
		hypra.addQueryToQueue("emptytest2",
			"_arity(test2,2); ?- PROJECT[$1](test2).");
		hypra.executeQueryQueue();
		String expected = "";

		List<HyTuple> retResults =
			hypra.getResultForQuery("emptytest");
		System.out.println("\nEmptytest:");
		String resultString = "";
		for (HyTuple tuple : retResults) {
		    resultString += tuple.toString() + "\n";
		}
		System.out.println(resultString);
		assertEquals(resultString,expected);


		resultString = "";
		retResults = hypra.getResultForQuery("emptytest2");
		System.out.println("Emptyest2:");
		for (HyTuple tuple : retResults) {
		    resultString += tuple + "\n";
		}
		System.out.println(resultString);
		assertEquals(resultString,expected);
	    }

	    catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	    finally {
		hypra.destroy(); // important!
	    }
	}
    }
}
