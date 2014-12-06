/*
 * Copyright 2000-2006 University Duisburg-Essen, Working group
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
 * Created on 17-Apr-2005 15:11:22. Moved to JaySpirit 18-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
 */
package hyspirit.util;

import hyspirit.engines.HyText2POOLEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A text filter based on hy_text2pool. Starts a hy_text2pool thread. Please
 * invoke close() if you don't need the filter any more!
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 *         <p>
 *         Created on 17-Apr-2005 15:11:22
 *
 */
public class HyText2PoolFilter {
    private HyText2POOLEngine hytext2pool = null;
    private BufferedWriter stdin = null;
    private BufferedReader stdout = null;

    private Logger LOG = LogManager.getLogger(HyText2PoolFilter.class);

    /**
     * Constructor of class.
     * 
     * @param hyspirit
     *            the HySpirit environment
     * @param stemming
     *            whether stemming should be applied
     * @param stopwordFile
     *            absoulte name of a stop word file; null if no stop word
     *            elimination should take place
     * @param morphemeFile
     *            absolute name of a morpheme file; null if there are none.
     * @throws HySpiritException
     *             if something goes wrong
     */
    public HyText2PoolFilter(HySpiritProperties hyspirit, boolean stemming,
	    String stopwordFile, String morphemeFile)
	    throws HySpiritException {
	this(hyspirit, stemming, stopwordFile, morphemeFile, null);
    }

    /**
     * Constructor of class.
     * 
     * @param hyspirit
     *            the HySpirit environment
     * @param stemming
     *            whether stemming should be applied
     * @param stopwordFile
     *            absoulte name of a stop word file; null if no stop word
     *            elimination should take place
     * @param morphemeFile
     *            absolute name of a morpheme file; null if there are none.
     * @param log
     *            a logger
     * @throws HySpiritException
     *             if something goes wrong
     */
    public HyText2PoolFilter(HySpiritProperties hyspirit, boolean stemming,
	    String stopwordFile, String morphemeFile,
	    Logger log)
	    throws HySpiritException {
	if (log != null)
	    LOG = log;
	hytext2pool = new HyText2POOLEngine(hyspirit);
	hytext2pool.stemming(stemming);
	if (stopwordFile != null)
	    hytext2pool.addStopwordFile(stopwordFile);
	if (morphemeFile != null)
	    hytext2pool.addMorphemeFile(morphemeFile);
	hytext2pool.translateUmlauts(true);
	hytext2pool.recogniseClassifications(false);
	hytext2pool.recogniseRelationships(false);
	hytext2pool.recogniseNumbers(false);
	hytext2pool.readFromSTDIN();
	hytext2pool.setLogger(LOG);
	hytext2pool.start();
	hytext2pool.waitTillRunning();
    }

    /**
     * With this constructor, a hytext2pool object is created and configured by
     * the invoking method. The hytext2pool is started and used by this object,
     * so you can configure it with whatever options hytext2pool offers. The
     * hytext2pool object should not be started before.
     * 
     * @param hytext2pool
     *            the HyText2Pool object
     */
    public HyText2PoolFilter(HyText2POOLEngine hytext2pool) {
	this.hytext2pool = hytext2pool;
	this.hytext2pool.readFromSTDIN();
	this.hytext2pool.start();
	this.hytext2pool.waitTillRunning();
    }

    /**
     * Closes the filter by destroying the underlying hy_test2pool process.
     *
     */
    public void close() {
	hytext2pool.destroy();
	stdin = null;
	stdout = null;
    }

    /**
     * Filters the text, returns a filtered version
     * 
     * @param text
     *            the text to filter
     * @return the filtered text or null if nothing is left after filtering
     */
    public String filter(String text) {
	String filteredText = "";
	if (text != null && !text.equals("")) {
	    StringBuffer strbuf = new StringBuffer();
	    try {
		/*
		hytext2pool.send(text);
		while (hytext2pool.hasNext()) {
		    String str = hytext2pool.next();
		    strbuf.append(str + " ");
		}
		*/
		filteredText = hytext2pool.sendAndReceive(text);
	    } catch (Exception io) {
		io.printStackTrace(System.err);
	    }
	    // filteredText = new String(strbuf);
	}
	filteredText = filteredText.trim();
	if (filteredText.equals(""))
	    filteredText = null;
	return filteredText;
    }

    /**
     * Filters the text, returns filtered terms as iterator. Removes the
     * annoying "NOT" ;-)
     * 
     * @param text
     *            the text to filter
     * @return the filtered terms
     */
    public Iterator filterIt(String text) {
	Iterator it = null;
	if (text != null && !text.equals("")) {
	    String filteredText = filter(text);
	    if (filteredText != null) {
		List list = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(filteredText);
		while (tokenizer.hasMoreTokens()) {
		    String token = tokenizer.nextToken();
		    if (!token.equals("NOT"))
			list.add(token);
		}
		it = list.iterator();
	    }
	}
	return it;
    }

    public static void main(String[] args) {
	String hsPath = "/home/ingo/HySpirit-Academic++-2.3.1";
	System.out.println("HyText2PoolFilter $Revision: 1.1.1.1 $\n");
	System.out.print("Path to HySpirit [" + hsPath + "]: ");
	String input = Util.readln();
	if (input != null && !input.trim().equals(""))
	    hsPath = input;
	String stopwords = "/home/ingo/zdnet/hyspirit/stopwords_eng.txt";
	System.out.print("Stopword List [" + stopwords + "]: ");
	String slist = Util.readln();
	if (slist == null || slist.equals(""))
	    slist = stopwords;
	HySpiritProperties hyspirit = new HySpiritProperties(hsPath, ".");
	try {
	    HyText2PoolFilter filter =
		    new HyText2PoolFilter(hyspirit, true, slist, null);
	    System.out.print("> ");
	    while (!(input = Util.readln()).equals("\\Q")) {
		System.out.println(filter.filter(input));
		System.out.print("> ");
	    }
	    filter.close();
	    System.out.print("Filter closed.");
	} catch (HySpiritException he) {
	    he.printStackTrace();
	}
    }
}
