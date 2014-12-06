/*
 * Copyright 2000-2005 University Duisburg-Essen, Working group
 * "Information Systems"
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
 * Created on 12-Jun-2005 20:22:25. Imported to JaySpirit 02-Dec-2005.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.5 $
 */
package hyspirit.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Catches the data from the given stream and stores it in a string. The purpose
 * of this class is to permanently read the STDOUT or STDERR of another process,
 * which should be done in a separate thread, and buffer the data so they can be
 * read later by then calling thread. Instances of this class should thus be
 * running in their own thread. Reading if the input stream is finished when a
 * delimiter is read or the end of stream is reached.
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 *         <p>
 *         Created on 12-Jun-2005 20:22:25. Imported to JaySpirit 02-Dec-2005.
 *
 */
public class StreamCatcher extends Thread {

    private BufferedReader in = null;
    private String delimiter = null;
    private boolean finished = false;
    private Vector<String> contentvec = null;
    private int cursor = 0;
    private boolean verbose = false;
    Logger LOG = LogManager.getLogger(StreamCatcher.class);

    /**
     * Constructor of class
     * 
     * @param in
     *            a buffered reader, e.g. the STDOUT or STDERR from another
     *            executable.
     * @param delimiter
     *            the string nofifying this thread to stop reading
     */
    public StreamCatcher(BufferedReader in, String delimiter) {
	this.in = in;
	this.delimiter = delimiter;
    }

    /**
     * Waits until the the underlying stream is read.
     */
    public synchronized void waitTillFinished() {
	while (!finished) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
    }

    /**
     * Returns true when everyhing is read from the stream, false otherwise.
     * 
     * @return true when everything is read and false if we are still reading
     */
    public boolean finished() {
	return finished;
    }

    /**
     * Gets the content after all read/write operations are finished. Each line
     * of the content is stored in a vector element. This method blocks until
     * the delimiter is read.
     * 
     * @return the content vector
     */
    public Vector<String> getContentVector() {
	while (!finished) {
	}
	return contentvec;
    }

    /**
     * Sets or unsets the verbosity mode. In verbosity mode, every line read
     * from the stream is alos written to System.out.
     * 
     * @param verbose
     *            true if verbose mode should be set, false otherwise
     */
    public void verbose(boolean verbose) {
	this.verbose = verbose;
    }

    /**
     * Returns if there is a next element to read or not. Might block in the
     * situation when all received lines were read and we are waiting for the
     * next line of the stream. In this case, this method waits until the next
     * line from the stream is read.
     * 
     * @return whether there is another element to read or not.
     */
    public boolean hasNext() {
	boolean hasNext = false;
	// waits when not started yet or
	// until we can determine whether there is a next element
	// if (verbose) System.out.print("SC.hasNext(): ");
	while ((contentvec == null) ||
		(!finished && (contentvec.size() <= cursor))) {
	}
	if (contentvec.size() > cursor) {
	    hasNext = true;
	}
	if (verbose) {
	    System.out.println(hasNext);
	}
	return hasNext;
    }

    /**
     * Gets the next line of the stream or null if the stream is finished. If we
     * did not receive a delimiter yet but already delivered the last recent
     * line we got from the stream, this methods blocks until the next line or
     * the delimiter is read.
     * 
     * @return the next line of the stream or null if the stream is finished and
     *         everything was already delivered
     */
    public String next() {
	String nextLine = null;
	// waits when not started yet or
	// until we can determine deliver the next element or reading of stream
	// is finished

	// XXX Check if this solution synchronises well!

	// if (verbose) System.out.print("SC.next(): ");
	while ((contentvec == null) ||
		(!finished && (contentvec.size() <= cursor))) {
	}
	if (contentvec.size() > cursor) {
	    nextLine = contentvec.elementAt(cursor++);
	    // if (verbose) System.out.println(nextLine);
	}
	return nextLine;
    }

    /**
     * Read the input stream until the end of the stream is reached or the
     * delimiter is read. This method is synchronised since no other thread is
     * allowed to read the in stream unless the current thread has finished
     * reading it.
     */
    @Override
    public synchronized void run() {
	if (in != null) {
	    finished = false;
	    contentvec = new Vector<String>();
	    String line = null;
	    try {
		while ((line = in.readLine()) != null) {
		    LOG.trace(line);
		    // Changed how the end of line dilimiter was handled. The
		    // entire line does not necessarily need to match the
		    // delimiter.
		    if (true) {
			Pattern p = Pattern.compile(delimiter);
			Matcher m = p.matcher(line);
			if (m.find()) {
			    break;
			}
			contentvec.add(line);
		    } else {
			if ((delimiter != null) && (line.equals(delimiter))) {
			    break;
			}
			contentvec.add(line);
			// System.out.println(line);
		    }
		}
	    } catch (IOException io) {
		io.printStackTrace();
	    }
	    LOG.debug("StreamCatcher finished.");
	    finished = true;
	    notifyAll();
	}
    }
}
