package hyspirit.engines;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;
import hyspirit.util.Util;

public class HyPDatalogEngine extends HyInferenceEngine {

    private String eval = null;
    private String to = null;
    private String hyPRAOpts = null;
    private long mdsFileLineLength = -1;
    private long cacheSize = -1;
    private long tupleWindowSize = -1;
    private long tupleWarningInterval = -1;
    private long tupleWindowTime = -1;
    private boolean silent = false;

    private static final String ENGINE_NAME = "hyp_pd";

    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     *
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyPDatalogEngine() throws HySpiritException {
	super(ENGINE_NAME);
	retrieveProgram = "?- retrieve(D, Q);";
    }

    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     *
     * @param hyspirit
     *            the HySpirit properties containing the environment
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyPDatalogEngine(HySpiritProperties hyspirit)
	    throws HySpiritException {
	super(ENGINE_NAME, hyspirit);
	retrieveProgram = "?- retrieve(D, Q);";
    }

    /**
     * This constructor must be used if you use an engine server (client mode).
     *
     * @param hostname
     *            the server host name
     * @param port
     *            the server port
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyPDatalogEngine(String hostname, int port)
	    throws HySpiritException {
	super(ENGINE_NAME, hostname, port);
	retrieveProgram = "?- retrieve(D, Q);";
    }

    /**
     * Sequential translation of rules. See hy_pd manual for further details.
     */
    public void evalSequential() {
	this.eval = "sequential";
    }

    /**
     * Set the MDS file line length
     * 
     * @param mdsFileLineLength
     */
    public void setMDSFileLineLength(long mdsFileLineLength) {
	this.mdsFileLineLength = mdsFileLineLength;
    }

    /**
     * Set the cache size
     * 
     * @param cacheSize
     */
    public void setCacheSize(long cacheSize) {
	this.cacheSize = cacheSize;
    }

    /**
     * Set the -silent option
     * 
     * @param silent
     *            the silent to set
     */
    public final void setSilent(boolean silent) {
	this.silent = silent;
    }

    /**
     * Set the tuple window size
     * 
     * @param tupleWIndowSize
     */
    public void setTupleWindowSize(long tupleWIndowSize) {
	this.tupleWindowSize = tupleWIndowSize;
    }

    /**
     * Set the tuple warning interval
     * 
     * @param tupleWarningInterval
     *            the tupleWarningInterval to set
     */
    public final void setTupleWarningInterval(long tupleWarningInterval) {
	this.tupleWarningInterval = tupleWarningInterval;
    }

    /**
     * Set the tuple window time
     * 
     * @param tupleWindowTime
     *            the tupleWindowTime to set
     */
    public final void setTupleWindowTime(long tupleWindowTime) {
	this.tupleWindowTime = tupleWindowTime;
    }

    /**
     * Recursive translation of rules, not available with some HySpirit
     * releases. See hy_fvpd manual for further details.
     */
    public void evalRecursive() {
	this.eval = "recursive";
    }

    /**
     * Sets option to be passed to hy_pra directly.
     *
     * @param hyPRAOpts
     *            the hy_pra options. See hy_pra manual for further details.
     */
    public void hyPRAOpts(String hyPRAOpts) {
	this.hyPRAOpts = hyPRAOpts;
    }

    /**
     * Tells to translate to PRA.
     *
     */
    public void toPRA() {
	to = "PRA";
    }

    /**
     * Gets the hy_pd call (command and parameters) as it would be executed
     *
     * @return the hy_pd call
     */
    public String getHyPDCall() {
	return Util.getCommand(buildCommand());
    }

    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     */
    @Override
    public void reset() {
	super.reset();
	eval = null;
	to = null;
    }

    /*
     * Builds the command string array.
     * @see hyspirit.engines.HyEngine#buildCommand()
     */
    @Override
    protected String[] buildCommand() {
	Vector<String> commandVec = new Vector<String>();
	commandVec.add(super.getCommand());

	if (this.eval != null) {
	    commandVec.add("-eval");
	    commandVec.add(eval);
	}

	if (this.kb != null) {
	    commandVec.add("-use");
	    commandVec.add(this.kb);
	}

	if (this.mdsFileLineLength > 0) {
	    commandVec.add("-mdsFileLineLen");
	    commandVec.add(Long.toString(this.mdsFileLineLength));
	}

	if (this.cacheSize > 0) {
	    commandVec.add("-cache_size");
	    commandVec.add(Long.toString(this.cacheSize));
	}

	if (this.tupleWindowSize > 0) {
	    commandVec.add("-tupleWindowSize");
	    commandVec.add(Long.toString(this.tupleWindowSize));
	}

	if (this.tupleWarningInterval > 0) {
	    commandVec.add("-tupleWarningInterval");
	    commandVec.add(Long.toString(this.tupleWarningInterval));
	}

	if (this.tupleWindowTime > 0) {
	    commandVec.add("-tupleWindowTime");
	    commandVec.add(Long.toString(this.tupleWindowTime));
	}

	if (silent)
	    commandVec.addElement("-silent");

	if (to != null) {
	    commandVec.add("-to");
	    commandVec.add(to);
	}

	// add additional arguments
	if (super.argumentString != null) {
	    StringTokenizer strTok = new StringTokenizer(super.argumentString);
	    while (strTok.hasMoreTokens())
		commandVec.add(strTok.nextToken());
	}

	if (hyPRAOpts != null) {
	    commandVec.add("-pra_opts");
	    commandVec.add(hyPRAOpts);
	}

	// STDIN
	if (stdin) {
	    // commandVec.add("--");
	    commandVec.add("-");
	}

	String[] commandString = new String[commandVec.size()];
	int i = 0;
	for (Enumeration e = commandVec.elements(); e.hasMoreElements();)
	    commandString[i++] = (String) e.nextElement();
	return commandString;
    }
}
