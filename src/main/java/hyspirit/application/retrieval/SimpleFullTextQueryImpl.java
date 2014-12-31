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
 * Created on 07-Jun-2005 09:17:39.  Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */

package hyspirit.application.retrieval;

import hyspirit.application.indexing.SimpleHypertextFileIndex;
import hyspirit.engines.HyPRAEngine;
import hyspirit.knowledgeBase.HyTuple;
import hyspirit.util.HySpiritProperties;
import hyspirit.util.HyText2PoolFilter;

import java.io.IOException;
import java.util.Iterator;

/**
 * This implementation of SimpleFullTextQuery performs full text queries on an
 * index as created by hyspirit.application.indexing.SimpleHypertextFileIndex.
 *  
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 07-Jun-2005 09:17:39
 *
 */
public class SimpleFullTextQueryImpl extends Thread
implements SimpleFullTextQuery 
{
    private HySpiritProperties hyspirit = null;
    private String connector = null;
    private HyPRAEngine hypra = null;
    private HyText2PoolFilter filter = null;
    private Iterator queryTerms = null;
    
    /**
     * Constructor of class. Index objects contain the necessary information
     * about the index, so they are required to connect the index.
     * @param index the index object
     * @param stemming whether queries should be stemmed
     * @param stopwordFile the name of a file containing stopword, or null
     * @param morphemeFile the name of a file containing morphemes, or null
     */
    public SimpleFullTextQueryImpl(SimpleHypertextFileIndex index,
            boolean stemming,
            String stopwordFile,
            String morphemeFile) {
        try {
            this.connector = index.getConnector();
            this.hyspirit = index.getEnvironment();
            
            // start the hy_pra process
            hypra = new HyPRAEngine(hyspirit);
            hypra.start();
            
            // start the filter process
            filter = new HyText2PoolFilter(hyspirit, stemming, 
                    stopwordFile, morphemeFile);

            hypra.waitTillRunning();            
            hypra.send(this.connector);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Sets the query (i.e. a set  of terms in a string)
     * @param query the query terms as a string
     */
    public void setQuery(String query) {
        queryTerms = filter.filterIt(query);
        if (queryTerms != null && !queryTerms.hasNext()) queryTerms = null;
    }
    
    /**
     * Execute the query with the given filter
     * @return ranked list of query results
     */
    public RankedList executeQuery() {
        RankedList rList = new RankedList();
        try {
            hypra.eval(orQueryToPRA(queryTerms));
            HyTuple tuple;
            while ((tuple = hypra.nextTuple()) != null) {
                rList.add(new ResultItem(tuple.probability(), tuple.valueAt(0)));
            }
            rList.sort();
        }
        catch (IOException io) {
            io.printStackTrace(System.err);
        }
        return rList;
    }
    
    /**
     * Execute the given query with the given filter
     * @param query the query terms
     * @return ranked list of query results
     */
    public RankedList executeQuery(String query) {
        setQuery(query);
        return executeQuery();
    }
    
    
    /**
     * Closes this query object (by destroying the underlying processes).
     * The query is unusable after being closed.
     */
    public void close() {
        filter.close();
        hypra.destroy();
    }

    /**
     * Filter the results: Return only elements being instance of the specified
     * class
     * @param documentClass return only instances of the specified class
     *
     */
    public void filter(String documentClass) {}
    
    
    /**
     * Returns a PRA query for the query terms in the iterator
     * @param queryTerms the query terms
     * @return the corresponding PRA expression for an OR conjunction
     */
    public static String orQueryToPRA(Iterator queryTerms) {
        String wqtermPRA = "";
        String termPRA = "";
        
        String praExpression = null;
        if (queryTerms != null) {
            while (queryTerms.hasNext())
            { 
                String qterm = (String)queryTerms.next();
                wqtermPRA +=
                    "wqterm := UNITE(wqterm,SELECT[$1=\"" + qterm + "\"](idf)).\n";
                termPRA +=
                    "term := UNITE(term, PROJECT[$1,$2](SELECT[$1=\"" +
                        qterm + "\"](tf))).\n";
            }
            praExpression = wqtermPRA + "\n";
            praExpression += termPRA + "\n";
            praExpression += "?- PROJECT[$3](JOIN[$1=$1](wqterm,term)).";
        }
        return praExpression;
    }
   
}  
  