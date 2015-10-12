/*
 * Copyright 2000-2006 University of Duisburg-Essen, Working group
 *   "Information Systems"
 * Copyright 2005-2006 Apriorie Ltd.
 * Copyright 2015 Ingo Frommholz
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
 * Created on 01-Dec-2005 18:49:26
 */
package hyspirit.engines;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import hyspirit.knowledgeBase.HyTuple;
import hyspirit.knowledgeBase.HyTupleFormatException;
import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;
import hyspirit.util.RetrievalstrategyManager;
import hyspirit.util.StreamCatcher;

/**
 * This abstract class provides funtionality for inference engines like hyp_pra,
 * hy_psql or hyp_pd.
 *
 * This class also provide means to executive several queries (e.g. "?- ...") in
 * one HySpirit process.
 *
 * One application is the processing of query queues, as the hyp_pra example
 * shows:
 *
 * <pre>
 * ...
 * hypraEngine.addQuerytoQueue("query1", "person(fred); ?- person;");
 * hypraEngine.addQuerytoQueue("query2", "human(X) :- person(X); ?- human;");
 * try {
 * 	hypraEngine.executeQueryQueue();
 * 	List<HyTuple> tuplesQuery1 = getResultForQuery("query1");
 * }
 * catch (IOException io) {io.printStackTrace();}
 * ...
 * </pre>
 *
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 */
public abstract class HyInferenceEngine extends HyEngine {

    /**
     * The knowledge base
     */
    protected String kb = null;

    /**
     * The list of files to be executed
     */
    protected List<String> files = null;

    /**
     * A retrieval strategy manager
     */
    protected RetrievalstrategyManager rsMgr = null;

    /**
     * The query queue
     */
    BlockingQueue<Query> queryQueue = null;

    /** The result set for query queues */
    Map<String, List<HyTuple>> resultSet = null;

    /**
     * The string to delimit the output of different queries in the queue
     */
    private static final String DELIMITER = "_ENDQUERY_";

    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     *
     * @param engineName
     *            the name of the engine (e.g., 'hy_pra')
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyInferenceEngine(String engineName)
	    throws HySpiritException {
	super(engineName);
	initFiles();
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
    public HyInferenceEngine(String engineName, HySpiritProperties hyspirit)
	    throws HySpiritException {
	super(engineName, hyspirit);
	initFiles();
    }

    /**
     * This constructor must be used if you use an engine server (client mode).
     *
     * @param engineName
     *            the name of the engine (e.g., 'hy_pra')
     * @param hostname
     *            the server host name
     * @param port
     *            the port
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyInferenceEngine(String engineName, String hostname, int port)
	    throws HySpiritException {
	super(engineName, hostname, port);
    }

    /**
     * Evaluates the given expression by sending it to stdin of the underlying
     * engine process/server. Use nextTuple() to receive the output from the
     * underlying process.
     *
     * @param input
     *            the input to be evaluated
     * @throws IOException
     *             if communication fails
     */
    public void eval(String input) throws IOException {
	send(input);
    }

    public void evalFile(String filename) throws IOException {
	sendFile(filename);
    }

    /* (non-Javadoc)
     * @see hyspirit.engines.HyEngine#reset()
     */
    @Override
    public void reset() {
	// TODO Auto-generated method stub
	super.reset();
	initFiles();
    }

    /**
     * Helper class
     */
    private void initFiles() {
	this.files = new Vector<String>();
    }

    /**
     * Adds a query to the queue. "Query" means any HySpirit code that contains
     * exactly one query in the last line (e.g. "?- retrieve(*);" for a hyp_pd
     * engine, "?- retrieve;" for a hyp_pra engine). Please make sure the code
     * you submit here really consists of one query as everything else may have
     * side effects.
     *
     * @param id
     *            the query ID
     * @param query
     *            the query
     * @since 1.1.0
     * @see #executeQueryQueue()
     */
    public void addQueryToQueue(String id, String query) {
	if (queryQueue == null) {
	    queryQueue = new LinkedBlockingQueue<Query>();
	}

	queryQueue.add(new Query(id, query));
    }

    /**
     * Adds a new PRA or MDS file to be executed by hy_pra. File are executed in
     * the order they are added with this method.
     *
     * @param filename
     *            name of a PRA or MDS file
     */
    public void addFile(String filename) {
	this.files.add(filename);
    }

    /**
     * Executes the given retrieval strategy (by invoking {@link run()})
     * 
     * @param retrievalstrategy
     *            the retrieval strategy
     * @throws HySpiritException
     *             if no matching strategy was found
     * @see RetrievalstrategyManager
     */
    public void run(String retrievalstrategy) throws HySpiritException {
	if (this.rsMgr != null) {
	    List<String> files = rsMgr.getSequence(retrievalstrategy);
	    if (files != null) {
		for (Iterator<String> iter = files.iterator(); iter
			.hasNext();) {
		    addFile(iter.next());
		}
		run();
	    } else {
		throw new HySpiritException("Retrieval strategy "
			+ retrievalstrategy + "not found.");
	    }
	} else
	    throw new HySpiritException("No retrieval strategy manager found.");
    }

    /**
     * Sets the retrieval strategy manager for this engine.
     * 
     * @param rsm
     * @see RetrievalstrategyManager
     */
    public void setRetrievalstrategyMgr(RetrievalstrategyManager rsm) {
	this.rsMgr = rsm;
    }

    /**
     * Executes all queries in the queue. This method closes the STDIN of the
     * underlying HySpirit process, effectively terminating it. Use this method
     * to run a HySpirit program that contains more than one query. Use the
     * {@link #addQueryToQueue(String, String)} method to build your query
     * queue. Use {@link #getResultForQuery(String)} to fetch the result for a
     * specific query in the queue.
     *
     * @throws IOException
     * @since 1.1.0
     * @see #addQueryToQueue(String, String)
     * @see #getResultForQuery(String)
     */
    public void executeQueryQueue() throws IOException {
	BufferedWriter in = getOutputWriter(); // stdin of process
	BufferedReader out = getInputReader(); // stdout of process
	if (in != null && out != null) {
	    /*
	     * Start stream catcher to catch the output and separate it
	     */
	    StreamCatcher streamCatcher = new StreamCatcher(out, DELIMITER,
		    new LinkedBlockingQueue<HyInferenceEngine.Query>(
			    queryQueue));
	    // streamCatcher.useQueryQueue(queryQueue);
	    streamCatcher.start();

	    /*
	     * Send queries
	     */
	    Query q;
	    while ((q = queryQueue.poll()) != null) {
		in.write(q.getQuery());
		in.newLine();
		in.write(echoSpecial(DELIMITER)); // echo delimiter
		in.newLine();
		in.flush();
	    }
	    in.close(); // close stdin

	    /*
	     * Fetch results, translate into HyTuples
	     */
	    Map<String, List<String>> results = streamCatcher
		    .getQueryQueueResults();

	    Iterator<Map.Entry<String, List<String>>> it = results.entrySet()
		    .iterator();

	    resultSet = new HashMap<String, List<HyTuple>>();

	    // Iterate over results for queries in queue

	    while (it.hasNext()) {
		// Translate this into HyTuples
		Map.Entry<String, List<String>> entry = it.next();
		List<String> resultLines = entry.getValue();
		List<HyTuple> resultTuples = new ArrayList<HyTuple>();
		for (String line : resultLines) {
		    if (line != null && !line.trim().equals("") &&
			    !line.startsWith("#") && !line.equals(DELIMITER)) {
			try {
			    resultTuples.add(new HyTuple(line));
			} catch (HyTupleFormatException h) {
			    LOG.trace("Couldn't parse " + line);
			    LOG.trace(h);
			}
		    }
		}
		resultSet.put(entry.getKey(), resultTuples);
	    }
	} else
	    throw new IOException("in, input or out null!");

	queryQueue = null;
    }

    /**
     * Returns the result of a query as a list of HyTuple objects. Please invoke
     * {@link #executeQueryQueue()} first!
     *
     * @param queryID
     *            the query ID as given in
     *            {@link #addQueryToQueue(String, String)}
     * @return the result set
     * @since 1.1.0
     * @see #executeQueryQueue()
     */
    public List<HyTuple> getResultForQuery(String queryID) {
	return resultSet.get(queryID);
    }

    /**
     * Convenience class to store a query id and the query itself
     *
     */
    public class Query {
	private final String id;
	private final String query;

	/**
	 * @param id
	 *            the ID
	 * @param query
	 *            the query
	 */
	public Query(String id, String query) {
	    this.id = id;
	    this.query = query;
	}

	/**
	 * @return the id
	 */
	public final String getId() {
	    return this.id;
	}

	/**
	 * @return the query
	 */
	public final String getQuery() {
	    return this.query;
	}

    }

    /**
     * Returns the next tuple from the underlying engine or null if there's no
     * tuple left to read. This method might block if and as long as the output
     * stream from the underlying process blocks.
     *
     * @return the next tuple or null if there is none
     */
    public HyTuple nextTuple() {
	// System.out.print("Next tuple: ");
	HyTuple tuple = null;
	boolean foundTuple = false;
	while (!foundTuple && this.hasNext()) {
	    String nextLine = this.next();
	    if (nextLine != null) {
		try {
		    tuple = new HyTuple(nextLine);
		    // System.out.println(tuple.toString());
		    foundTuple = true; // never reached if exception is thrown
		} catch (HyTupleFormatException he) {
		}
	    }
	}
	return tuple;
    }

    /**
     * Get the echo special command of this engine. For POOL, FVPD, PD and PRA,
     * this is "_echo(message)". For PSQL, this is
     * "INSERT INTO _echo VALUES ('message')".
     */
    @Override
    public String echoSpecial(String message) {
	return ("_echo(\"" + message + "\").");
    }

    /*
     * Returns the stream end message. The stream end message is needed for
     * send() in order to determine when the whole output is read.
     * @return the stream end message
     */
    @Override
    protected String getStreamEndMessage() {
	return "END";
    }

    // Ingo: New methods, 01/02/2006
    protected String deleteQueryProgram;
    protected String retrieveProgram;

    /**
     * Get the program for retrieving objects.
     */
    public String deleteQueryProgram() {
	return null; // XXX to make it compileable
    }

    /**
     * Get the program for retrieving objects.
     */
    public String retrieveProgram() {
	return null; // XXX to make it compileable
    }

    /**
     * Let this engine echo the argument message.
     */
    public void echo(String message) {
	runProgram((message));
    }

    /**
     * Let this engine retrieve a result. This method passes the retrieve
     * program to the engine.
     */
    public void retrieve() {
	runProgram(retrieveProgram());
    }

    /**
     * Let this engine delete the previous query. This method passes the delete
     * query program to the engine.
     */
    public void deleteQuery() {
	runProgram(deleteQueryProgram());
    }

    /**
     * Let this engine run the argument program.
     */
    public void runProgram(String program) {
    }

    /**
     * Determines which knowledge base to use.
     *
     * @param kb
     *            the knowledge base to use
     */
    public void useKB(String kb) {
	this.kb = kb;
    }
}
