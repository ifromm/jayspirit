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
 * Created on 04-Apr-2005 14:01:15.  Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;


/**
 * Interface for indexing hypertexts. Documents within this hypertext can have
 * metadata in form of attribute-value pairs. Each document is instance of a 
 * given type (e.g., article, annotation). There are typed and weighted links
 * between documents in the hypertext.
 * 
 * This is an interface only. The exact indexing of the data (e.g. how to
 * weight terms, if titles are stemmed and split into terms, etc.) are subject
 * to classes implementing this interface. The strategy is that you insert the
 * data to be indexed first and then start the actual indexing process with
 * the index() method.
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 04-Apr-2005 14:01:15
 *
 */
public interface HypertextIndex extends Index {
	
		
    /**
     * Adds a hyperlink between two documents. The link type might be null if
     * the implementation does not distinguish several link types.
     * @param sourceDocumentURI the URI of the link source
     * @param destinationDocumentURI the URI of the link destination
     * @param linktype the link type
     */
    public void addHyperlink(String sourceDocumentURI,
    			             String destinationDocumentURI,
							 String linktype);
    
    
    
    /**
     * Returns true if the typed link already exists in the index
     * @param sourceURI the URI of the link source
     * @param destinationURI the URI of the link destination
     * @param linktype the link type
     * @return true if the typed link exists in the index, false elsewhere
     */
    public boolean isIndexed(String sourceURI, String destinationURI,
                             String linktype);
    
             
}

