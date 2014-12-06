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
 * Created on 18-Jul-2005 18:21:56. Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import hyspirit.util.Util;

/**
 * Class to manage document-term lists, and document-term frequencies (tf).
 * Keeps track of the maximum term frequency.
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 18-Jul-2005 18:21:56
 *
 */

public class DocTermList {
	  private HashMap termFreqs = new HashMap();
	  private TermFreq maxTermFreq = null;
	  private int numberOfTerms = 0; // the number of terms in this document
	  private int numberOfDistinctTerms = 0; // the number of distinct terms
	  private String uri = null;
	  
	  /**
	   * Construtor of class. Each docterm list might be identified by the
	   * corresponding document URI.
	   * @param uri the document uri 
	   */
	  public DocTermList(String uri) {
	      this.uri = uri;
      }
	  
	  /**
	   * Construtor of class. For backward compatibility and cases where you
	   * don't need to stoire the document uri
	   */
	  public DocTermList() {}
	  
	  
	  /**
	   * Retuns the URI of the document of this docterm list (or NULL if there
	   * was none)
	   * @return the URI
	   */
	  public String getURI() {
	      return this.uri;
	  }
	  
	  
	  /**
	   * Adds a term; if the term already exists, its frequency is incremented;
	   * if not, a new TermFreq object is created with frequency 1. Returns the
	   * frequency of this term in this list.
	   * @param term the term to be added
       * @return the updated term frequency
	   */
	  public int addTerm(String term) {
	      int frequency = 0;
	      if (term != null) {
	          if (maxTermFreq == null) {
	              // the first term
	              maxTermFreq = new TermFreq(term);
	              frequency = 1;
	              numberOfDistinctTerms++;
	          }
	          else {
	              TermFreq termfreq = getTermFreq(term);
	              if (termfreq == null) {
	                  	// a new term
	              		termfreq = new TermFreq(term);
	              		numberOfDistinctTerms++;
	              }
	              else termfreq.inc();
	              addTerm(termfreq);
	              frequency = termfreq.frequency();
	          }
	          numberOfTerms++;
	      }
	      return frequency;
	  }
	  
	  /**
	   * Add TermFreq object, checks if it is the maximum one
	   * @param termFreq the TermFreq object
	   */
	  public void addTerm (TermFreq termFreq) {
	      if (termFreq != null) {
	          if (maxTermFreq == null) maxTermFreq = termFreq;
	          else if (termFreq.frequency() >= maxTermFreq.frequency()) {
	              if (!maxTermFreq.term().equals(termFreq.term())) {
	                  // termFreq is the new maxTermFreq, so insert former
	                  // maxTermFreq and remove termFreq from list
	                  termFreqs.put(maxTermFreq.term(), maxTermFreq);
	                  termFreqs.remove(termFreq.term());
	              }
	              maxTermFreq = termFreq;
	          }
	          else termFreqs.put(termFreq.term(), termFreq);
	      }
	  }
	  
	  /**
	   * Gets the term frequency object identified by the term.
	   * @return the TermFreq object of this term
	   */
	  public TermFreq getTermFreq(String term) {
	      TermFreq termFreq = null;
	      if (term != null && maxTermFreq != null) {
	          if ((maxTermFreq.term()).equals(term)) termFreq = maxTermFreq;
	          else termFreq = (TermFreq)termFreqs.get(term);
	      }
	      return termFreq;
	  }
	 
	  /**
	   * Retruns the normalised term frequency of a term as used by the SMART
	   * system: ntf =  0.5 * (1 + (tf/maxtf))
	   * @param term the term
	   * @return the normalised term frequency
	   */
	  public float smartNTF(String term) {
	      float ntf = 0;
	      if (maxTermFreq != null) {
	          int maxtf = maxTermFreq.frequency();
	          TermFreq termFreq = getTermFreq(term);
	          if (termFreq != null) {
	              int tf = termFreq.frequency();
	              ntf = (float) 0.5 * (1 + ((float) tf/maxtf));
	          }
	      }
	      return ntf;
	  }
	  
	  /**
	   * Returns the normalised term frequency of a term using the maximum
	   * term frequency in the document: ntf =  tf/maxtf
	   * @param term the term
	   * @return the normalised term frequency
	   */
	  public float maxNTF(String term) {
	      float ntf = 0;
	      if (maxTermFreq != null) {
	          int maxtf = maxTermFreq.frequency();
	          TermFreq termFreq = getTermFreq(term);
	          if (termFreq != null) {
	              int tf = termFreq.frequency();
	              ntf = (float) tf/maxtf;
	          }
	      }
	      return ntf;
	  }
	  
	  /**
	   * Returns the APoisson weight for the term and a given lambda, calculated
	   * as tf/(lambda +tf)
	   * @param term the term
	   * @param lambda the lambda
	   * @return the apoison weight
	   */
	  public float APoissonWeight(String term, float lambda) {
	      float weight = 0;
          TermFreq termFreq = getTermFreq(term);
          if (termFreq != null) {
              float tf = (float)termFreq.frequency();
              weight = (float) tf/(lambda + tf);
          }
          return weight;
	  }
	  
	  /**
	   * Returns the APoisson weight for the given term, where the lambda
	   * is the average term frequency (averageTF) in the document. So this
	   * method returns tf/(averageTF + tf) for the given term. 
	   * @param term the term
	   * @return the apossion weight based on the average term frequency
	   */
	  public float APoissonWeight(String term) {
	      return APoissonWeight(term, averageTF());
	  }
	  
	  /**
	   * Returns the normalised term frequency of a term using the sum of all
	   * term frequencies in the document: ntf =  tf/numberOfTerms
	   * @param term the term
	   * @return the normalised term frequency
	   */
	  public float sumNTF(String term) {
	      float ntf = 0;
	      if (maxTermFreq != null) {
	          int maxtf = maxTermFreq.frequency();
	          TermFreq termFreq = getTermFreq(term);
	          if (termFreq != null) {
	              int tf = termFreq.frequency();
	              ntf = (float) tf/numberOfTerms;
	          }
	      }
	      return ntf;
	  }
	  	      
	  
	  /**
	   * Returns all docterms. Ensures that the most frequent term is the
	   * first one
	   * @return A list of docterms
	   */
	  public List docTerms() {
	      ArrayList docterms = new ArrayList();
	      Set keyset = termFreqs.keySet();
	      if (maxTermFreq != null) {
	          docterms.add(maxTermFreq);
	          for (Iterator it = keyset.iterator(); it.hasNext();) {
	              String keyTerm = (String)it.next();
	              docterms.add((TermFreq) termFreqs.get(keyTerm));
	          }
	      }
	      return docterms;
	  }
	
	  /**
	   * Returns the avergae term frequency
	   * (numberOfTerms/numberOfDistinctTerms)
	   * @return the average TF
	   */
	  public float averageTF() {
	      return (float)((float)numberOfTerms/(float)numberOfDistinctTerms);
	  }
	  
	  public static void main(String[] args) {
	      System.out.println("Insert text:");
	      String text = Util.readln();
	      String[] terms = text.split(" ");
	      DocTermList dtl = new DocTermList();
	      for (int i = 0; i < terms.length;i++) {
	          dtl.addTerm(terms[i].trim());
	      }
	      System.out.println("\n\nAverage TF: " + dtl.averageTF());
	      System.out.println("Terms: " + dtl.numberOfTerms +
	              " (" +dtl.numberOfDistinctTerms + " distinct)");
	      for (Iterator i = dtl.docTerms().iterator(); i.hasNext();) {
	          TermFreq tf = (TermFreq)i.next();
	          String term = tf.term();
	          int freq = tf.frequency();
	          System.out.println(term + "\t\t\t" + 
	                  freq + "\t\t" + 
	                  dtl.maxNTF(term) + "\t\t" +
	                  dtl.sumNTF(term) + "\t\t" + 
	                  dtl.smartNTF(term) + "\t\t" +
	                  dtl.APoissonWeight(term));
	      }
	  }
}
