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
 * Created on 07-Jun-2005 08:46:15. Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.retrieval;

/**
 * Interface for simple (i.e. "Google-like") full text queries.
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 07-Jun-2005 08:46:15
 *
 */
public interface SimpleFullTextQuery {

    /**
     * Set query terms for ad hoc queries
     * @param query the query terms
     */
    public void setQuery(String query);
    
    /**
     * Execute the query with the given filter
     * @return ranked list of query results
     */
    public RankedList executeQuery();
    
    /**
     * Execute the given query with the given filter
     * @param query the query terms
     * @return ranked list of query results
     */
    public RankedList executeQuery(String query);

    /**
     * Filter the results: Return only elements being instance of the specified
     * class
     * @param documentClass return only instances of the specified class
     *
     */
    public void filter(String documentClass);
    
    
    /**
     * Closes this query object.
     */
    public void close();
    
}
