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
 * Created on 20-Jul-2005 12:01:47
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;

import hyspirit.util.Util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;



/**
 * Handles document frequencies for each term. Returns (normed) idf values for
 * terms
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 20-Jul-2005 12:01:47. Moved to JaySpirit 19-Feb-2006.
 *
 */
public class DocFreqList {
    // Enable logging
    private static Logger LOG = Logger.getLogger(DocFreqList.class);
    
    /**
     * Whether to use max_idf norm for some calculations (e.g. when 
     * using {@link #writeList(BufferedWriter)})
     */
    public final static int MAX_IDF = 0;
    
    /**
     * Whether to use sum_idf norm for some calculations (e.g. when 
     * using {@link #writeList(BufferedWriter)})
     */  
    public final static int SUM_IDF = 1;
    
   
    private HashMap<String,Integer> termIDF = new HashMap<String,Integer>();
    private int numberOfDocuments = 0;
    private float idfmax = -1;
    private float idfsum = -1;
    
    /**
     * Flag saying whether the input is sorted 
     */
    private boolean sorted = false;
    
    /**
     * Needed to track doc changed if sorted = true.
     */
    private String lastDoc = null;
    
    /**
     * Keeps track of term-document pairs
     */
    private HashSet<String> termdocs = new HashSet<String>();
    
    /**
     * Keeps track of documents
     */
    private HashSet<String> docs = new HashSet<String>();
   
    
    
    /**
     * Constructor of class.
     */
    public DocFreqList() {}
    
    /**
     * Constructor of class.
     * @param log a logger
     */
    public DocFreqList(Logger log) {
        LOG = log;
    }


    /**
     * Sets sorted flag to {@code true}. This means: if you add term-document
     * pairs with the {@link #addTermDoc(String, String)} method, this input
     * has to be sorted in two ways:
     * <ul>
     * <li> Each term-document pair has to be submitted once and only once;
     * <li> All terms of a document have to be added in one bulk; if another
     *      document is submitted, there must not be any term-document pair
     *      with another already submitted document.
     * </ul>
     * An example of a sorted list is the sequence
     * <pre>
     *  t1,d1
     *  t2,d1
     *  t3,d2
     * </pre>
     * whereas the adding sequence
     * <pre>
     *  t1,d1
     *  t3,d2
     *  t2,d1
     * </pre>
     * is not allowed when the sorted flag is set. If the sorted flag is set,
     * {@link #addTermDoc(String, String)} does not check if a document-term
     * pair or a document has been submitted before. Advantage is that it
     * consumes way less memory than when the sorted flag is not set.
     * 
     * @param sorted the {@code sorted} flag
     */
    public void sortedInput(boolean sorted) {
        this.sorted = sorted;
    }
    
    
    /**
     * Increments the document frequency of a term. Returns the new document
     * frequency
     * @param term the term
     * @return the new document frequency for the term
     */
    public int inc(String term) {
        Integer docFreq = (Integer)termIDF.get(term);
        if (docFreq == null) {
            docFreq = new Integer(1);
        }
        else docFreq =
            new Integer((docFreq.intValue()) + 1);
        termIDF.put(term, docFreq);
        idfmax = -1;
        idfsum = -1;
        return docFreq.intValue();
    }

    /**
     * Increments the number of documents in the collection
     *
     */
    public void incNumberOfDocuments() {
        numberOfDocuments++;
        idfmax = -1;
    }
    /**
     * Returns the document frequency of the given term.
     * @param term the term
     * @return the term's document frequency
     */
    public int docFreq(String term) {
        Integer docFreq = termIDF.get(term);
        if (docFreq == null) return 0;
        else return docFreq.intValue();
    }
    
    /**
     * Returns the inverse document frequency (-ln (df/N)) of the term
     * @param term the term
     * @return the inverse document frequency of the term
     */
    public float idf(String term) {
        int df = docFreq(term);
        if (df > 0) 
            return (float) - Math.log((float)df/(float)numberOfDocuments);
        else return (float) 0;
    }
    
    /**
     * Returns the terms in an iterator of strings
     * @return the terms of the document
     */
    public Iterator<String> terms() {
        return termIDF.keySet().iterator();
    }
    
    /**
     * Convenience method to add term-document pairs, so you don't have to
     * invoke {@link #inc} or {@link #incNumberOfDocuments}. Required checks are
     * performed automatically
     * @param term the term
     * @param doc the corresponding document
     */
    public void addTermDoc(String term, String doc) {
        if (sorted) {
            if (lastDoc == null || !doc.equals(lastDoc)) {
                LOG.trace("Last doc: " + lastDoc + " New doc: " + doc); 
                lastDoc = doc;
                incNumberOfDocuments();
            }
            inc(term);
        }
        else {
            String termdoc = term + "%%%" + doc;
            if (!termdocs.contains(termdoc)) {
                // a new document term pair
                termdocs.add(termdoc);
                inc(term);
                if (!docs.contains(doc)) {
                    // a new document
                    docs.add(doc);
                    incNumberOfDocuments();
                }
            }
        }
        if (LOG.isInfoEnabled()) {
            if (termIDF.size() % 10000 == 0)
                LOG.info("Added " + termIDF.size() + " terms in "
                        + numberOfDocuments + " documents");
        }
    }
    
    /**
     * Writes the docfreq list to the given writer. Does not flush or close.
     * @param writer the writer to write the data to
     * @param idfNorm which norm to use ({@link #MAX_IDF} or {@link #SUM_IDF})
     * @throws IOException if data could not be written
     */
    public void writeList(BufferedWriter writer, int idfNorm) 
    throws IOException {
        Iterator<String> it = terms();
        while (it.hasNext()) {
            String term = it.next();
            float idf = 0;
            switch (idfNorm) {
            case MAX_IDF:
                idf = maxIDF(term);
                break;
            case SUM_IDF:
                idf = sumIDF(term);
            }
            writer.write(Util.floatToString(idf) + " (" + term + ")");
            writer.newLine();
        }
    }
    
    /**
     * Returns the idf sum norm for the term (idf(term)/sumidf)
     * @param term the term
     * @return the sum norm for the term
     */
    public float sumIDF(String term) {
        if (idfsum == -1 ) calculateMaxAndSum();
        return (float) (idf(term)/idfsum);
    }

    /**
     * Returns the idf max norm for the term (idf(term)/maxidf)
     * @param term the term
     * @return the max norm for the term
     */
    public float maxIDF(String term) {
        if (idfmax == -1 ) calculateMaxAndSum();
        return (float) (idf(term)/idfmax);
    }
    

    
    /*
     * Calculate max idf and sum of idfs
     */
    private void calculateMaxAndSum() {
        idfsum = 0;
        for (Iterator terms = terms(); terms.hasNext();) {
            float idf = idf((String)terms.next());
            if (idf > idfmax) idfmax = idf;
            idfsum += idf;
        }        
    }
}
