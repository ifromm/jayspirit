/*
s * Copyright 2000-2006 University of Duisburg-Essen, Working group
 *   "Information Systems"
 * Copyright 2005-2006 Apriorie Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Created on 01-Dec-2005 13:23:48
 * $Revision: 1.3 $
 */
package hyspirit.engines;

import hyspirit.knowledgeBase.HyKB;
import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;
import hyspirit.util.StreamCatcher;
import hyspirit.util.StreamGobbler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements all required methods and communications for the
 * HySpirit engines. There are two possible modes:
 * <ul>
 * <li>Client/Server mode: to start an own engine process and query it
 * <li>Client mode: to connect to an existing engine process and query it
 * </ul>
 * 
 * @author <a href="mailto:ingo@is.informatik.uni-duisburg.de">Ingo
 *         Frommholz</a>
 *         <p>
 *         Created on 02-Dec-2005 10:34:51
 *
 */
public abstract class HyEngine implements Runnable {
    private HySpiritProperties hyspirit = null;
    private String command = null;
    private Process process = null;
    public boolean stdin = false;
    private Socket socket = null;
    boolean clientmode = false;
    private String engineName = null;
    private StreamCatcher streamCatcher = null;
    private ErrorStreamHandler err = null;
    private final boolean verbose = false;
    private String realTime;
    private String sysTime;
    private String userTime;
    private String percentageCPU;

    protected final static String STREAM_END_MESSAGE = "#! END";
    protected String argumentString = null;

    protected static Logger LOG = LogManager
	    .getLogger(HyEngine.class.getName());

    private boolean suppressSTDERR = false;

    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * 
     * @param engineName
     *            the name of the engine (e.g., 'hy_pra')
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyEngine(String engineName)
	    throws HySpiritException {
	this(engineName, null);
    }

    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * 
     * @param engineName
     *            the name of the engine (e.g., 'hy_pra')
     * @param hyspirit
     *            the HySpirit properties containing the environment
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyEngine(String engineName, HySpiritProperties hyspirit)
	    throws HySpiritException {
	if (hyspirit == null)
	    hyspirit = new HySpiritProperties();
	this.hyspirit = hyspirit;
	this.command = hyspirit.getHySpiritPath() + "/bin/" + engineName;
	this.engineName = engineName;
    }

    /**
     * This constructor must be used if you want to attach to a running engine
     * process (client mode) through a socket
     * 
     * @param hostname
     *            the hostname of the engine server
     * @param port
     *            the port of the engine server
     * @throws HySpiritException
     *             if the connection failed
     */
    public HyEngine(String engineName, String hostname, int port)
	    throws HySpiritException {
	try {
	    this.socket = new Socket(hostname, port);
	    clientmode = true;
	    this.engineName = engineName;
	} catch (Exception e) {
	    throw new HySpiritException(e.getMessage());
	}
    }

    /**
     * Sets a logger for the engine. Replaces the default one.
     * 
     * @param log
     *            the logger.
     */
    public void setLogger(Logger log) {
	LOG = log;
    }

    /**
     * The format string for the {@code time} command
     */
    private final static String TIME_FORMAT =
	    "Real: %E\tUser: %U\tSys: %S\tCPU: %P";

    /**
     * The prefix that identifies a line returned by the {@code time} command
     */
    protected final static String TIME_PREFIX = "***TIME";

    private boolean takeTime = false;

    /**
     * Additional arguments which are not supported (yet) by an engine can be
     * set here.
     * 
     * @param argumentString
     *            the argument string
     */
    public void setArgumentString(String argumentString) {
	this.argumentString = argumentString;
    }

    /**
     * Gets the version of command. Works in client/server mode only.
     * 
     * @return the version or null if in client mode
     */
    /* public String getVersion() {
         if (! clientmode) {
             String[] com = new String[2];
             com[0] = command;
             com[1] = "-version";
             run();
             return streamToString(getSTDOUT());
         }
         else return null;
     }
     */

    /**
     * Starting this object as a thread.
     */
    public void start() {
	// processThread = new Thread(this);
	// processThread.start();
	run();
    }

    /**
     * Returns the absolute path of the current command if in client/server mode
     * and null if in client mode
     */
    public String getCommand() {
	if (clientmode)
	    return null;
	else
	    return this.command;
    }

    /**
     * Returns the engine name
     * 
     * @return the engine name
     */
    public String getEngineName() {
	return this.engineName;
    }

    /**
     * Sets the real time used by the engine process
     * 
     * @param realTime
     */
    private void setRealTime(String realTime) {
	LOG.trace(realTime);
	this.realTime = realTime;
    }

    /**
     * If we used the Unix {@code time} command, this method gets the elapsed
     * real time in [hours:]minutes:seconds.
     * 
     * @see #takeTime(boolean)
     */
    public String getRealTime() {
	try {
	    if (takesTime())
		err.join();
	} catch (InterruptedException e) {
	    LOG.warn(e);
	}
	return this.realTime;
    }

    /**
     * Sets the user time used by the engine process
     * 
     * @param userTime
     */
    private void setUserTime(String userTime) {
	this.userTime = userTime;
    }

    /**
     * If we used the Unix {@code time} command, this method gets the elapsed
     * user mode CPU seconds
     * 
     * @see #takeTime(boolean)
     */
    public String getUserTime() {
	try {
	    if (takesTime())
		err.join();
	} catch (InterruptedException e) {
	    LOG.warn(e);
	}
	return this.userTime;
    }

    /**
     * Sets the real time used by the engine process
     * 
     * @param sysTime
     */
    private void setSysTime(String sysTime) {
	this.sysTime = sysTime;
    }

    /**
     * If we used the Unix {@code time} command, this method gets the elapsed
     * sys mode CPU seconds
     * 
     * @see #takeTime(boolean)
     */
    public String getSysTime() {
	try {
	    if (takesTime())
		err.join();
	} catch (InterruptedException e) {
	    LOG.warn(e);
	}
	return this.sysTime;
    }

    /**
     * Sets the percentage of the CPU that the engine got
     * 
     * @param sysTi
     */
    private void setPercentageCPU(String percentageCPU) {
	this.percentageCPU = percentageCPU;
    }

    /**
     * If we used the Unix {@code time} command, this method gets the elapsed
     * sys mode CPU seconds
     * 
     * @see #takeTime(boolean)
     */
    public String getPercentageCPU() {
	try {
	    if (takesTime())
		err.join();
	} catch (InterruptedException e) {
	    LOG.warn(e);
	}
	return this.percentageCPU;
    }

    /**
     * Ensures that the process reads input from STDIN. This makes sense in
     * client/server mode only.
     */
    public void readFromSTDIN() {
	stdin = true;
    }

    /**
     * Gets STDIN if not in clientmode. Note that the stream is null unless the
     * actual process really started, so check this first.
     * 
     * @return STDIN as buffered writer
     */
    public BufferedWriter getSTDIN() {
	if (!clientmode && process != null)
	    return new BufferedWriter(
		    new OutputStreamWriter(process.getOutputStream()));
	else
	    return null;
    }

    /**
     * Gets STDOUT if not in clientmode. Note that the stream is null unless the
     * actual process really started, so check this first.
     * 
     * @return STDOUT as buffered reader
     */
    public BufferedReader getSTDOUT() {
	if (!clientmode && process != null)
	    return new BufferedReader(
		    new InputStreamReader(process.getInputStream()));
	else
	    return null;
    }

    /**
     * Gets STDERR if not in clientmode. Note that the stream is null unless the
     * actual process really started, so check this first.
     * 
     * @return STDERR as buffered reader
     * @deprecated STDERR now redirected to {@link System.err}. Returns null.
     */
    @Deprecated
    public BufferedReader getSTDERR() {
	return null;
	/*
	if (!clientmode && process != null) 
	    return new BufferedReader(
	            new InputStreamReader(process.getErrorStream()));
	else return null;
	*/
    }

    /**
     * Closes the STDIN. Some processes (like {@code hyp_pra}) need either
     * enough input or an EOF in STDIN before they release their buffered
     * output. So try this if you don't receive any output from the underlying
     * engine.
     * 
     * @throws IOException
     */
    public void closeSTDIN() throws IOException {
	BufferedWriter stdin = getSTDIN();
	if (stdin != null)
	    stdin.close();
    }

    /**
     * Gets the output writer, which is STDIN in client/server mode, and the
     * socket input writer otherwise
     * 
     * @return the input writer
     * @throws IOException
     */
    public BufferedWriter getOutputWriter() throws IOException {
	if (clientmode)
	    return getSocketIn();
	else
	    return getSTDIN();
    }

    /**
     * Gets the input reader, which is STDOUT in client/server mode, and the
     * socket output reader otherwise
     * 
     * @return the output reader
     * @throws IOException
     */
    public BufferedReader getInputReader() throws IOException {
	if (clientmode)
	    return getSocketOut();
	else
	    return getSTDOUT();
    }

    /*
     * Gets the input steam from the socket if in clientmode or null otherwise
     * @return the input stream as a buffered Reader
     * @throws IOException
     */
    private BufferedReader getSocketOut() throws IOException {
	BufferedReader socketOut = null;
	if (clientmode) {
	    socketOut = new BufferedReader(
		    new InputStreamReader(socket.getInputStream()));
	}
	return socketOut;
    }

    /*
     * Gets the output steam from the socket if in clientmode or null otherwise
     * @return the output stream as a buffered Writer
     * @throws IOException
     */
    private BufferedWriter getSocketIn() throws IOException {
	BufferedWriter socketIn = null;
	if (clientmode) {
	    socketIn = new BufferedWriter(
		    new OutputStreamWriter(socket.getOutputStream()));
	}
	return socketIn;
    }

    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     *
     */
    public void reset() {
	try {
	    destroy();
	} catch (IllegalThreadStateException ie) {
	}
	stdin = false;
	argumentString = null;
    }

    /**
     * Run command. If in client mode, this does nothing.
     *
     */
    public void run() {
	if (!clientmode)
	    run(buildCommand(), hyspirit);
    }

    /*
     * Runs the given command with the given arguments and an input stream to be
     * sent to STDIN. The HySpiritProperties object contains all neccessary
     * environment variables. Does nothing in client mode.
     * 
     * @param com the command plus its arguments
     * @param hyspirit the HySpiritProperties environment
     */
    private void run(String[] com, HySpiritProperties hyspirit)
    {
	if (!clientmode) {
	    try {
		if (hyspirit == null)
		    hyspirit = new HySpiritProperties();
		/* we better take the environment everything was started in! Please set 
		 * $HYSPIRIT etc. externally!
		                process =
		                    Runtime.getRuntime().exec(com, hyspirit.getEnvironment(),
		                                    new File(hyspirit.getWorkingDirectory())); 
		*/
		if (LOG.isDebugEnabled()) {
		    String command = "";
		    for (int i = 0; i < com.length; i++) {
			command += com[i] + " ";
		    }
		    LOG.debug("Starting engine: " + command.trim()
			    + " [" + hyspirit.getWorkingDirectory() + "]");
		}

		if (takeTime) {
		    // We execute the engine with the Unix time command
		    String[] tmpCmd = new String[com.length + 2];
		    tmpCmd[0] = "time";
		    tmpCmd[1] = "--format=" + TIME_PREFIX
			    + TIME_FORMAT;
		    for (int i = 0; i < com.length; i++) {
			tmpCmd[i + 2] = com[i];
		    }
		    com = tmpCmd;
		}
		process =
			Runtime.getRuntime().exec(com, null,
				new File(hyspirit.getWorkingDirectory()));
		LOG.debug("Engine started: " + process.toString());

		// handle stderr
		err =
			new ErrorStreamHandler(
				new BufferedReader(
					new InputStreamReader(
						process.getErrorStream())),
				this, LOG);
		err.start();

	    } catch (Exception e)
	    {
		String command = "";
		for (int i = 0; i < com.length; i++) {
		    command += com[i] + " ";
		}
		LOG.error("Exception  caught while starting engine:\n"
			+ command, e);
		if (process != null)
		    destroy();
	    }
	}
    }

    /**
     * Kills a running process. This does nothing if in client mode.
     * 
     * @throws IllegalThreadStateException
     *             if the process hasn't started yet.
     */
    public void destroy() throws IllegalThreadStateException {
	if (!clientmode) {
	    if (process != null) {
		process.destroy();
		LOG.debug("Process destroyed.");
		// process = null;
	    }
	    else
		throw new IllegalThreadStateException("Process not started!");
	}
    }

    /**
     * Restarts the engine. Does nothing in client mode.
     * 
     * @throws IllegalThreadStateException
     */
    public void restart() throws IllegalThreadStateException {
	if (!clientmode) {
	    destroy();
	    streamCatcher = null;
	    run(buildCommand(), hyspirit);
	}
    }

    /**
     * Causes the current thread to wait, if necessary, until the process has
     * terminated. By convention, 0 indicates normal termination. If in client
     * mode, this does nothing and returns -1.
     * 
     * @return the exit value of the process
     * @throws InterruptedException
     *             - if the current thread is interrupted by another thread
     *             while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     * @throws IllegalThreadStateException
     *             if the process hasn't started yet.
     */
    public int waitFor() throws IllegalThreadStateException,
	    InterruptedException {
	if (clientmode)
	    return -1;
	else {
	    int waitFor;
	    if (process != null) {
		waitFor = process.waitFor();
		process = null;
	    }
	    else
		throw new IllegalThreadStateException("Process not started!");
	    return waitFor;
	}
    }

    /**
     * Returns true if the underlying process is running, false otherwise.
     * Returns always true if in client mode.
     * 
     * @return whether the underlying process is running
     */
    public boolean isRunning() {
	if (verbose)
	    System.out.println(getEngineName() + ".isRunning: process = "
		    + process);
	if (clientmode || process != null)
	    return true;
	else
	    return false;
    }

    /**
     * Returns true if this object is in client mode, and false if it is in
     * client/server mode.
     * 
     * @return true if in client mode, false otherwise
     */
    public boolean isInClientMode() {
	return clientmode;
    }

    /**
     * This does nothing but wait until an underlying bridge process is actually
     * running, and it return then. Do not invoke if you did not invoke the
     * run() or start() method first! Returns immediately in client mode
     *
     */
    public void waitTillRunning() {
	if (!clientmode)
	    while (!isRunning()) {
	    }
    }

    /**
     * Returns the exit value of the process or -1 if in client mode
     * 
     * @return the exit value of the process
     * @throws IllegalThreadStateException
     *             - if the process has not yet terminated or started.
     */
    public int exitValue() throws IllegalThreadStateException {
	int exitValue = -1;
	if (!clientmode) {
	    if (process != null) {
		exitValue = process.exitValue();
		process = null;
	    }
	    else
		throw new IllegalThreadStateException("Process not started!");
	}
	return exitValue;
    }

    /**
     * Send input string to engine. Use next() to read the output from the
     * process. Be sure you read the results of a previous send() first because
     * otherwise they will be dropped!
     * 
     * @param input
     *            the string to be sent to the engine.
     */
    public void send(String input) throws IOException {
	BufferedWriter in = getOutputWriter();
	if (in != null && input != null &&
		!input.trim().equals("")) {
	    LOG.trace(input);
	    BufferedReader str = new BufferedReader(new StringReader(input));
	    send(str);
	}
    }

    /**
     * Send content of file to engine. Use next() to read the output from the
     * process. Be sure you read the results of a previous send() first because
     * otherwise they will be dropped!
     * 
     * @param filename
     *            the content of this file will be sent to the engine
     */
    public void sendFile(String filename) throws IOException {
	BufferedWriter in = getOutputWriter();
	if (in != null && filename != null &&
		!filename.trim().equals("")) {
	    BufferedReader fileReader =
		    new BufferedReader(new FileReader(new File(filename)));
	    send(fileReader);
	}
    }

    /*
     * Returns the stream end message. The stream end message is needed for
     * send() in order to determine when the whole output is read.
     * @return the stream end message (null if there is no stream end message)
     */
    protected String getStreamEndMessage() {
	if (clientmode) {
	    return "#! END";
	}
	else {
	    return null;
	}
    }

    /**
     * Returns a string which lets the engine echo the given message
     * 
     * @param message
     *            the message to be echoed
     * @return the echo string (null if no echoing is allowed)
     */
    public String echoSpecial(String message) {
	return null;
    }

    /**
     * This is a convenience method: it sends the input given in the string to
     * the STDIN of the engine and returns the output of the engine. The STDIN
     * of the engine process is closed after the string was sent. The output of
     * the process is read and returned. STDERR is caught and redirected to
     * System.err. This method is for engines which read something from STDIN,
     * process it after STDIN is closed and return something to STDOUT before
     * exiting.
     * 
     * @param input
     * @return
     */
    public String sendAndReceive(String input) {
	String output = null;
	try {
	    // Ensure that STDERR of the engine process is read and piped to
	    // our STDERR.
	    BufferedWriter bSyserr =
		    new BufferedWriter(new OutputStreamWriter(System.err));
	    StreamGobbler err = new StreamGobbler(getSTDERR(), bSyserr,
		    null);
	    err.start();

	    // Catch everything what the engine writes to STDOUT
	    BufferedReader out = getInputReader();
	    StreamCatcher streamCatcher = new StreamCatcher(out,
		    getStreamEndMessage());
	    streamCatcher.start();

	    // Send input to STDIN
	    BufferedWriter in = getOutputWriter();
	    in.write(input);
	    in.newLine();
	    in.write(echoSpecial(getStreamEndMessage()));
	    in.newLine();
	    in.close();

	    streamCatcher.waitTillFinished();
	    StringBuffer strbuf = new StringBuffer();
	    while (streamCatcher.hasNext()) {
		strbuf.append(streamCatcher.next() + "\n");
	    }
	    out.close();
	    output = new String(strbuf);
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
	return output;
    }

    /*
     * Reads from BufferedReader input and send its content to the output
     * writer.  Use next() to read the output from the process. Be sure you read
     * the results of a previous send() first because otherwise they will be
     * dropped!
     */
    protected void send(BufferedReader input) throws IOException {
	BufferedWriter in = getOutputWriter();
	BufferedReader out = getInputReader();
	if (in != null && input != null && out != null) {
	    if (streamCatcher != null) {
		// it might be that the previous stream catcher reads output
		// from our process. We should wait until it finished.
		streamCatcher.waitTillFinished();
	    }
	    streamCatcher = new StreamCatcher(out, getStreamEndMessage());
	    streamCatcher.start();

	    /*
	    // Ensure that STDERR is read and piped to our STDERR.
	    if (!clientmode && err == null) {
	        BufferedWriter bSyserr =  
	            new BufferedWriter(new OutputStreamWriter(System.err));
	        err = new StreamGobbler(getSTDERR(), bSyserr, engineName);
	        err.start();
	    }
	    */

	    String line = null;
	    while ((line = input.readLine()) != null) {
		LOG.trace(line);
		in.write(line);
		in.newLine();
	    }
	    in.flush();

	    if (getStreamEndMessage() != null) {
		String endMessage = echoSpecial(getStreamEndMessage());
		LOG.trace(endMessage);
		in.write(endMessage);
		in.newLine();

		/*
		 * This is a hack: some HySpirit tools output the end stream
		 * message only when they are forced to produce an additional
		 * line.  So we write an empty echo message trigger the
		 * required output ... hope this doesn't hurt!  -- IFr
		 */
		// in.write(echoSpecial(" "));
		// in.newLine();
	    }
	    in.flush();
	    input.close();
	}
	else
	    throw new IOException("in, input or out null!");
    }

    /**
     * Returns if there is a next element to read or not. Might block in the
     * situation when all received lines were read and we are waiting for the
     * next line of the process. In this case, this method waits until the next
     * line from the process is read.
     * 
     * @return whether there is another element to read or not.
     */
    public boolean hasNext() {
	boolean hasNext = false;
	if (streamCatcher != null) {
	    // streamCatcher.waitTillFinished(); // XXX avoids streaming, should
	    // be parameterised!
	    hasNext = streamCatcher.hasNext();
	}
	return hasNext;
    }

    /**
     * Returns the next line of output from the underlying engine or null if
     * there's nothing more to read. This method might block if and as long as
     * the output stream from the underlying process blocks.
     * 
     * @return the next line as a string
     */
    public String next() {
	String nextLine = null;
	if (streamCatcher != null) {
	    streamCatcher.waitTillFinished(); // XXX avoids streaming, should
	    // be parameterised!
	    nextLine = streamCatcher.next();
	}
	return nextLine;
    }

    /**
     * By default, the engine process' output to STDERR is redirected to the
     * System.err and to the engine's logger. If you do not want this, you can
     * suppress this here. The error output of the process is still captured by
     * the Logger on the WARN level.
     * 
     * @param suppress
     *            true if the process output should not be sent to System.err,
     *            false else.
     */
    public void suppressSTDERR(boolean suppress) {
	this.suppressSTDERR = suppress;
    }

    /**
     * Whether to take runtime statistics of the engine process or not. If this
     * is set to {@code true}, the engine is started using the Unix {@code time}
     * command with the format defined in {@link #TIME_FORMAT}. The output is
     * appended to STDOUT.
     * 
     * @param takeTime
     *            whether to take file or not. Default: don't take time
     *            {@code false}.
     */
    public void takeTime(boolean taketime) {
	takeTime = taketime;
    }

    /**
     * Returns true if we use the Unix {@code time} command
     * 
     * @return true if we use the Unix {@code time} command, false else
     */
    protected boolean takesTime() {
	return takeTime;
    }

    /**
     * Builds the command from the parameters. If subclasses support other
     * parameters, they have to override the <code>buildCommand</code> and
     * <code>reset</code> methods. Be sure that you place the support for new,
     * additional parameters somewhere. This means the block
     * 
     * <pre>
     * if (this.argumentString != null) {
     *     StringTokenizer strTok = new StringTokenizer(this.argumentString);
     *     while (strTok.hasMoreTokens())
     * 	commandVec.add(strTok.nextToken());
     * }
     * </pre>
     * 
     * should appear somewhere in your <code>buildCommand</code> implementation.
     * <p>
     * The output of this method is used in the <code>run()</code> method.
     * 
     * @return the command string array
     */
    protected String[] buildCommand() {
	Vector commandVec = new Vector();
	commandVec.add(getCommand());

	// add additional arguments
	if (this.argumentString != null) {
	    StringTokenizer strTok = new StringTokenizer(this.argumentString);
	    while (strTok.hasMoreTokens())
		commandVec.add(strTok.nextToken());
	}

	String[] commandString = new String[commandVec.size()];
	int i = 0;
	for (Enumeration e = commandVec.elements(); e.hasMoreElements();)
	    commandString[i++] = (String) e.nextElement();
	return commandString;
    }

    /*
     * Methods dealing with the knowledge base
     */
    private HyKB kb;

    /**
     * Set the knowledge base of this engine.
     */
    public void kb(HyKB kb) {
	this.kb = kb;
    }

    /**
     * Set the knowledge base of this engine. This method creates a knowledge
     * base object with the argument name.
     * 
     * @param kbName
     *            the name of the knowledge base
     */
    public void kb(String kbName) throws IOException {
	this.kb = new HyKB(kbName);
    }

    /**
     * Get the knowledge base of this engine.
     */
    public HyKB kb() {
	return this.kb;
    }

    /*
     * Methods and classes handling and parsing STDERR
     */

    private class ErrorStreamHandler extends Thread {
	private final BufferedReader stderr;
	private final Logger LOG;
	private final HyEngine engine;
	private boolean completed = false;

	/**
	 * Constructor of class.
	 *
	 */
	public ErrorStreamHandler(BufferedReader stderr,
		HyEngine engine,
		Logger log) {
	    this.stderr = stderr;
	    this.LOG = log;
	    this.engine = engine;
	}

	public boolean completed() {
	    return completed;
	}

	@Override
	public void run() {
	    try {
		String line = null;
		while ((line = stderr.readLine()) != null) {
		    LOG.trace(line);
		    if (engine.takesTime() &&
			    line.startsWith(HyEngine.TIME_PREFIX)) {
			// line is an output of the time command, so we
			// parse it and set the values in the engine
			line =
				line.substring(HyEngine.TIME_PREFIX.length());
			String[] timeOut = line.split("\t");
			engine.setRealTime(timeOut[0]);
			engine.setUserTime(timeOut[1]);
			engine.setSysTime(timeOut[2]);
			engine.setPercentageCPU(timeOut[3]);
		    }
		    else {
			if (!engine.suppressSTDERR) {
			    System.err.println(line);
			}
			LOG.warn("<" + engine.getEngineName() + "> " + line);
		    }
		}
		completed = true;
	    } catch (Exception e) {
		LOG.error("Exception in error stream handler!", e);
	    }
	}
    }

}
