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
 * Created on 07-Jun-2005 09:25:42. Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;


import hyspirit.util.HySpiritProperties;


/**
 * This is an interface only. The exact indexing of the data (e.g. how to
 * weight terms, if titles are stemmed and split into terms, etc.) are subject
 * to classes implementing this interface. The strategy is that you insert the
 * data to be indexed first and then start the actual indexing process with
 * the index() method.
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 07-Jun-2005 09:25:42
 *
 */
public interface Index {
	
	/**
	 * Adds an indexing object, which represents a document to be indexed.
	 * @param idx the indexing obect
	 */
	public void addIndexingObject(IndexingObject idx);
	
	
    
	/**
	 * Returns the connector string for hy_pra etc.
	 * @return the connector string
	 */
	public String getConnector();
	
    
	
    /**
     * Indexes everything.
     */
    public void index();
    
    /**
     * Totally erases the index.
     *
     */
    public void erase();
    
    
    /**
     * Returns true if the document was already indexed
     * @param documentURI
     * @return true if the document was already indexed, false otherwise
     */
    public boolean isIndexed(String documentURI);          

    /**
     * Gets the underlying HySpirit environment for this indexer
     */
    public HySpiritProperties getEnvironment();
    
}
