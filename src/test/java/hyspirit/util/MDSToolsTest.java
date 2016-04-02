package hyspirit.util;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import hyspirit.application.indexing.MDSTools;
import hyspirit.knowledgeBase.HyTupleFormatException;

/**
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 */
public class MDSToolsTest {
    private static Logger LOG = LogManager.getLogger(MDSToolsTest.class);

    /** Our toy MDS string */
    String mdsString = "0.3 (123, e1, \"e1test1\")\n"
	    + "0.45 (234, e1, \"e1test2\")\n"
	    + "1 (345, e1, \"e1test3\")\n"
	    + "# e1 finished\n"
	    + "0.2 (456, e2, \"e2test1\")\n"
	    + " 0.1 (567, e2, \"e2test2\")\n"
	    + "# e2 finished\n"
	    + "  (678, e3, \"e3test1\")\n"
	    + "(789, e3, \"e3test2\")\n"
	    + "# e3 finished\n";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link hyspirit.application.indexing.MDSTools#filterByColumn(int, java.util.Set, java.io.BufferedReader, java.io.BufferedWriter, boolean)}
     * .
     */
    @Test
    public final void testFilterByColumn() {
	LOG.info("MDSTools: TestFilterByColumn 1");

	// set up filter for e1 and e2
	int column = 1;
	Set<String> filterValues = new HashSet<String>();
	filterValues.add("e1");
	filterValues.add("e2");

	String expected = "# e1 finished\n"
		+ "# e2 finished\n"
		+ "  (678, e3, \"e3test1\")\n"
		+ "(789, e3, \"e3test2\")\n"
		+ "# e3 finished\n";

	executeTest(column, filterValues, expected, true);
    }

    /**
     * Test method for
     * {@link hyspirit.application.indexing.MDSTools#filterByColumn(int, java.util.Set, java.io.BufferedReader, java.io.BufferedWriter, boolean)}
     * .
     */
    @Test
    public final void testFilterByColumn2() {
	LOG.info("MDSTools: TestFilterByColumn 2");
	// set up filter for e1 and e2
	int column = 2;
	Set<String> filterValues = new HashSet<String>();
	filterValues.add("\"e2test2\"");
	filterValues.add("\"e3test1\"");

	String expected = "0.3 (123, e1, \"e1test1\")\n"
		+ "0.45 (234, e1, \"e1test2\")\n"
		+ "1 (345, e1, \"e1test3\")\n"
		+ "# e1 finished\n"
		+ "0.2 (456, e2, \"e2test1\")\n"
		+ "# e2 finished\n"
		+ "(789, e3, \"e3test2\")\n"
		+ "# e3 finished\n";

	executeTest(column, filterValues, expected, true);
    }

    /**
     * Test method for
     * {@link hyspirit.application.indexing.MDSTools#filterByColumn(int, java.util.Set, java.io.BufferedReader, java.io.BufferedWriter, boolean)}
     * .
     */
    @Test
    public final void testFilterByColumn3() {
	LOG.info("MDSTools: TestFilterByColumn 3");
	// set up filter for e1 and e2
	int column = 2;
	Set<String> filterValues = new HashSet<String>();
	filterValues.add("\"e4test2\"");
	filterValues.add("\"e5test1\"");

	String expected = mdsString;

	executeTest(column, filterValues, expected, true);
    }

    /**
     * Test method for
     * {@link hyspirit.application.indexing.MDSTools#filterByColumn(int, java.util.Set, java.io.BufferedReader, java.io.BufferedWriter, boolean)}
     * .
     */
    @Test
    public final void testFilterByColumn4() {
	LOG.info("MDSTools: TestFilterByColumn 4");

	// set up filter for e1 and e2
	int column = 1;
	Set<String> filterValues = new HashSet<String>();
	filterValues.add("e1");
	filterValues.add("e2");

	String expected = "#.3 (123, e1, \"e1test1\")\n"
		+ "#.45 (234, e1, \"e1test2\")\n"
		+ "# (345, e1, \"e1test3\")\n"
		+ "# e1 finished\n"
		+ "#.2 (456, e2, \"e2test1\")\n"
		+ "#0.1 (567, e2, \"e2test2\")\n"
		+ "# e2 finished\n"
		+ "  (678, e3, \"e3test1\")\n"
		+ "(789, e3, \"e3test2\")\n"
		+ "# e3 finished\n";

	executeTest(column, filterValues, expected, false);
    }

    /**
     * Test method for
     * {@link hyspirit.application.indexing.MDSTools#filterByColumn(int, java.util.Set, java.io.BufferedReader, java.io.BufferedWriter, boolean)}
     * .
     */
    @Test
    public final void testFilterByColumn5() {
	LOG.info("MDSTools: TestFilterByColumn 5");

	// set up filter for e1 and e2
	int column = 1;
	Set<String> filterValues = new HashSet<String>();
	filterValues.add("e1");
	filterValues.add("e2");
	filterValues.add("e3");
	filterValues.add("e4");

	String expected = "# e1 finished\n"
		+ "# e2 finished\n"
		+ "# e3 finished\n";

	executeTest(column, filterValues, expected, true);
    }

    /**
     * Test method for
     * {@link hyspirit.application.indexing.MDSTools#filterByColumn(int, java.util.Set, java.io.BufferedReader, java.io.BufferedWriter, boolean)}
     * .
     */
    @Test
    public final void testFilterByColumn6() {
	LOG.info("MDSTools: TestFilterByColumn 6");

	// set up filter for e1 and e2
	int column = 1;
	Set<String> filterValues = new HashSet<String>();
	filterValues.add("e1");
	filterValues.add("e2");
	filterValues.add("e3");
	filterValues.add("e4");

	String expected = "#.3 (123, e1, \"e1test1\")\n"
		+ "#.45 (234, e1, \"e1test2\")\n"
		+ "# (345, e1, \"e1test3\")\n"
		+ "# e1 finished\n"
		+ "#.2 (456, e2, \"e2test1\")\n"
		+ "#0.1 (567, e2, \"e2test2\")\n"
		+ "# e2 finished\n"
		+ "# (678, e3, \"e3test1\")\n"
		+ "#789, e3, \"e3test2\")\n"
		+ "# e3 finished\n";

	executeTest(column, filterValues, expected, false);
    }

    /**
     * @param column
     * @param filterValues
     * @param expected
     * @param delete
     */
    private void executeTest(int column, Set<String> filterValues,
	    String expected, boolean delete) {
	StringWriter resultMDS = new StringWriter();
	try {
	    MDSTools.filterByColumn(column, filterValues,
		    new StringReader(mdsString),
		    resultMDS, delete);
	} catch (NullPointerException | HyTupleFormatException
		| IOException e) {
	    LOG.error("Exception caught!", e);
	    fail("Exception caught: " + e.getMessage());
	}

	String result = resultMDS.toString();
	LOG.debug("Expected:\n" + expected + "\n");
	LOG.debug("Result:\n" + result);
	if (!result.equals(expected)) {
	    fail("Got different result from expected:\n" + result
		    + "\n\nExpected was:\n" + expected);
	}
    }

}
