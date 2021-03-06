/*
 * Copyright 2000-2005 University of Duisburg-Essen, Working group
 * "Information Systems"
 * 
 * Copyright 2016 Ingo Frommholz
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
 
 * Created on 19-Oct-2005 16:18:49.  Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz <ingo@frommholz.org>
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hyspirit.knowledgeBase.HyTuple;
import hyspirit.knowledgeBase.HyTupleFormatException;
import hyspirit.util.Util;

/**
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *         <p>
 *         Created on 19-Oct-2005 16:18:49
 *
 */
public class MDSTools {
    private static Logger LOG = LogManager.getLogger(MDSTools.class);

    /**
     * Reads an mds file which is supposed to contain document-term relations.
     * Ignores any weight. The first column must contain the term, the second
     * one the document. All other columns are ignored. Example entry: <br>
     * <br>
     * 0.78 ("test", "doc1") <br>
     * <br>
     * Terms and documents must not contain '"', '(', ')' and ',' in its name.
     * The input stream from the docterm file should be optimised w.r.t. the
     * second column: if the value changes here, the previous value will not
     * show up at a later position. For example, <br>
     * <br>
     * 0.78 ("test", "doc1") 0.43 ("test2", "doc1") 0.33 ("test", "doc2") <br>
     * <br>
     * is optimised, since "doc1" does not appear after the second line, whereas
     * <br>
     * <br>
     * 0.78 ("test", "doc1") 0.33 ("test", "doc2") 0.43 ("test2", "doc1") <br>
     * <br>
     * is not optimised, since "doc1" before and after "doc2". With such
     * optimised streams, the algorithm does not have to store a list of all
     * documents and terms already seen, but only the current one, which means
     * the process will probably need much less memory than with a non-optimised
     * stream. <br>
     * <br>
     * The method writes a corresponding IDF file using DocFreqList.
     * 
     * @param doctermFile
     *            absolute name of the docterm file
     * @param idfFile
     *            absolute name of the idf file
     * @param idfNorm
     *            "max_idf" or "sum_idf"
     * @param optimisedStream
     *            says if a stream is optimised or not
     */
    public static void convertDoctermToIDF(String doctermFile, String idfFile,
	    String idfNorm, boolean optimisedStream) {
	HashMap seenDocuments = null;
	HashSet currentTerms = null;
	String currentDoc = null;
	if (!optimisedStream)
	    seenDocuments = new HashMap();
	else
	    currentTerms = new HashSet();
	DocFreqList dfList = new DocFreqList();
	try {
	    BufferedReader in = new BufferedReader(new FileReader(doctermFile));
	    BufferedWriter out = new BufferedWriter(
		    new FileWriter(idfFile, false));
	    String line = null;
	    while ((line = in.readLine()) != null) {
		System.out.print("r");
		// parse line
		line = line.trim();
		String line2 = line.substring(line.indexOf(' ') + 2,
			line.length() - 1);
		String[] tokens = line2.split(",");
		if (tokens.length < 2)
		    System.err.println("WARN: Ignoring line\n\t" + line);
		else {
		    String term = tokens[0];
		    term = term.replace('"', ' ');
		    term = term.trim();
		    String doc = tokens[1];
		    doc = doc.replace('"', ' ');
		    doc = doc.trim();

		    if (!optimisedStream) {
			// non-optimised stream
			// check if we have previously seen this document
			if (!seenDocuments.containsKey(doc)) {
			    // the document is new
			    dfList.incNumberOfDocuments();
			    HashSet docterms = new HashSet();
			    docterms.add(term);
			    seenDocuments.put(doc, docterms);
			    dfList.inc(term);
			} else {
			    // check if we already counted the document for this
			    // term
			    HashSet docterms = (HashSet) seenDocuments.get(doc);
			    if (!docterms.contains(term)) {
				// count this document for this term
				dfList.inc(term);
				docterms.add(term);
				seenDocuments.put(doc, docterms);
			    }
			}
		    } else {
			// optimised stream
			if (currentDoc == null || !currentDoc.equals(doc)) {
			    // the document is new
			    dfList.incNumberOfDocuments();
			    dfList.inc(term);
			    currentDoc = doc;
			    currentTerms = new HashSet();
			    currentTerms.add(term);
			} else {
			    // check if we already counted the document for this
			    // term
			    if (!currentTerms.contains(term)) {
				// count this document for this term
				dfList.inc(term);
				currentTerms.add(term);
			    }
			}
		    }
		}
	    } // while
	    in.close();

	    /*
	     * Write IDF file
	     */
	    Iterator terms = dfList.terms();
	    while (terms.hasNext()) {
		String term = (String) terms.next();
		float weight;
		if (idfNorm.equals("sum_idf"))
		    weight = dfList.sumIDF(term);
		else
		    weight = dfList.maxIDF(term);
		String lineout = weight + " (\"" + term + "\")\n";
		System.out.print("w");
		out.write(lineout);
	    }
	    out.close();
	    System.out.println("\nFinished.");
	} catch (IOException io) {
	    io.printStackTrace(System.err);
	}
    }

    /**
     * This class takes an input MDS stream and deletes/marks any tuple that
     * matches the filter values in the provided column. Marking means to
     * overwrite the probability value with a '#' symbol to make sure old PRIs
     * on the MDS file are still valid. Deleting means to delete the whole
     * tuple. The behaviour whether to delete or mark is controlled using the
     * 'reallyDelete' flag. The method writes to the filtered MDS to a given
     * output stream. The MDS stream must contain one tuple per line. Closes
     * input stream after execution, but not the output stream.
     * 
     * @param column
     *            the column to filter (index starts with 0)
     * @param filterValues
     *            the values to filter
     * @param inputMDS
     *            the input MDS
     * @param outputMDS
     *            the output MDS
     * @param reallyDelete
     *            whether to delete ({@code true}) or mark ({@code false})
     * @return the number of lines modified or deleted
     * @throws IOException
     *             if there is a problem with the streams
     * @throws HyTupleFormatException
     *             if a line does not represent a tuple, comment or empty line
     * 
     */
    public static int filterByColumn(int column, Set<String> filterValues,
	    Reader inputMDS, Writer outputMDS,
	    boolean reallyDelete) throws IOException, HyTupleFormatException {
	int modified = 0;
	if (inputMDS != null && outputMDS != null) {
	    String line = null;
	    BufferedReader input = new BufferedReader(inputMDS);
	    while ((line = input.readLine()) != null) {
		String origline = line;
		line = line.trim();
		if (line.startsWith("#") || line.equals("")) {
		    outputMDS.write(origline + "\n");
		    continue;
		}

		// Check if filter is matched
		String lineToWrite = null;
		HyTuple tuple = new HyTuple(line);
		if (filterValues.contains(tuple.valueAt(column))) {
		    // tuple needs to be filtered
		    modified++;
		    if (!reallyDelete)
			lineToWrite = '#' + origline.substring(1);
		} else
		    lineToWrite = origline;
		if (lineToWrite != null)
		    outputMDS.write(lineToWrite + "\n");
	    }
	    inputMDS.close();
	    // outputMDS.close();
	} else {
	    LOG.warn("inputMDs or outputMDs is null!");
	    throw new NullPointerException("inputMDS or outputMDS is null!");
	}
	return modified;
    }

    /**
     * Some nifty tools for operations on MDS files
     * 
     * @param args
     */
    public static void main(String[] args) {
	System.out.println("MDSTools $Revision: 1.1 $\n");
	System.out.println("1 -- convert TF to IDF");
	System.out.print("Please Choose: ");
	String input = Util.readln();
	if (input != null) {
	    input = input.trim();
	    String input2 = null;
	    if (input.equals("1")) {
		String doctermfile = null;
		String idffile = null;
		System.out.print("Docterm File: ");
		input2 = Util.readln();
		if (input2 != null && !input2.trim().equals(""))
		    doctermfile = input2;
		System.out.print("IDF File: ");
		input2 = Util.readln();
		if (input2 != null && !input2.trim().equals(""))
		    idffile = input2;
		String idfNorm = "max_idf";
		System.out.print("max_idf or sum_idf (m/s) [m]: ");
		input2 = Util.readln();
		if (input2 != null && !input2.trim().equals(""))
		    if (input2.toLowerCase().equals("s"))
			idfNorm = "sum_idf";
		boolean optimised = true;
		System.out.print("Optimised Stream (y/n) [y]? ");
		input2 = Util.readln();
		if (input2 != null && !input2.trim().equals(""))
		    if (input2.toLowerCase().equals("n"))
			optimised = false;
		if (doctermfile != null && idffile != null)
		    convertDoctermToIDF(doctermfile, idffile, idfNorm,
			    optimised);
	    }
	}
    }

}
