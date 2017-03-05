package hyspirit.engines;

import java.io.File;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import hyspirit.util.HySpiritException;

/**
 * This convenience class executes a pipe of HySpirit commands to perform
 * stemming, stopword elimination and tf computation (apoisson) for a given
 * piece of text. It returns a tuple with one element (the term) and the
 * computed probability. Please see {@link HyText2TFTestCase} for examples.
 * 
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 * @see HyText2TFTestCase
 */
public class HyText2TFEngine extends HyAnalysisEngine {

    private String stopwordfile = null;
    private String morphemefile = null;
    private boolean stemming = true;

    private Set<Integer> columns = null;
    private Set<Integer> groupBy = null;
    private boolean runText2MDS = true;

    private static final String ENGINE_NAME = "hy_text2tf";

    private String hy_text2mds = "hyp_text2mds";
    private String hy_mds2freq = "hyp_mds2freq";
    private String hy_freq2mds = "hyp_freq2mds";

    public HyText2TFEngine() throws HySpiritException {
	super(ENGINE_NAME);
	hy_text2mds = hyspirit.getHySpiritPath() + File.separator + "bin"
		+ File.separator + hy_text2mds;
	hy_mds2freq = hyspirit.getHySpiritPath() + File.separator + "bin"
		+ File.separator + hy_mds2freq;
	hy_freq2mds = hyspirit.getHySpiritPath() + File.separator + "bin"
		+ File.separator + hy_freq2mds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hyspirit.engines.HyAnalysisEngine#buildCommand()
     */
    @Override
    protected String[] buildCommand() {

	// hyp_text2mds and its parameters
	String hy_text2mds_call = "";
	if (runText2MDS) {
	    hy_text2mds_call = hy_text2mds;
	    if (stopwordfile != null)
		hy_text2mds_call += " -stopwords " + stopwordfile;
	    if (morphemefile != null)
		hy_text2mds_call += " -morpheme " + morphemefile;
	    if (stemming)
		hy_text2mds_call += " -stemming";
	    hy_text2mds_call += " - | ";
	}

	// hyp_mds2freq and its parameters
	String hy_mds2freq_call = hy_mds2freq;
	if (columns != null)
	    for (Integer c : columns)
	    hy_mds2freq_call += " -col " + c;
	if (groupBy != null)
	    for (Integer g : groupBy)
	    hy_mds2freq_call += " -group " + g;
	hy_mds2freq_call += " -tupleFreq - | ";

	Vector<String> commandVec = new Vector<String>();
	commandVec.add("/bin/sh");
	commandVec.add("-c");
	commandVec.add(hy_text2mds_call
		+ hy_mds2freq_call
		+ hy_freq2mds + " -avg -poissona");
	String[] commandString = new String[commandVec.size()];
	int i = 0;
	for (Enumeration<String> e = commandVec.elements(); e
		.hasMoreElements();)
	    commandString[i++] = e.nextElement();
	return commandString;
    };

    /**
     * Sets the stopwordfile. Set to null (default) if you don't want stopwords
     * removed.
     * 
     * @param stopwordfile
     *            the stop word file
     */
    public void stopwordfile(String stopwordfile) {
	this.stopwordfile = stopwordfile;
    }

    /**
     * Add a column for the -col parameter of hyp_mds2freq
     * 
     * @param column
     *            the column to add
     */
    public void addColumn(int column) {
	if (columns == null) columns = new LinkedHashSet<>();
	columns.add(column);
    }

    /**
     * Add a group for the -group parameter of hyp_mds2freq
     * 
     * @param group
     *            the group to add
     */
    public void addGroup(int group) {
	if (groupBy == null) groupBy = new LinkedHashSet<>();
	groupBy.add(group);
    }

    /**
     * Whether or not text2mds should be run. If you set this to {@code false},
     * make sure split and stem etc. have been running before and you sent
     * tokens instead of full text to the stream.
     * 
     * @param runText2MDS
     *            {@code true} is the default
     */
    public void runText2MDS(boolean runText2MDS) {
	this.runText2MDS = runText2MDS;
    }

    /**
     * Sets the morpheme file. Set to null (default) if there are no morphemes.
     * 
     * @param morphemefile
     *            the morpheme file
     */
    public void morphemefile(String morphemefile) {
	this.morphemefile = morphemefile;
    }

    /**
     * Whether stemming should be performed or not (default: true, stemming)
     * 
     * @param stemming
     *            true or false
     */
    public void stemming(boolean stemming) {
	this.stemming = stemming;
    }
}
