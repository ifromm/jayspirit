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
 * Created on 11-Apr-2005 13:49:27. Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An indexing object contains all data of a document to be indexed, i.e. the
 * full text of a documents its attributes and it classification (document
 * type). An indexing object is the basic unit for the HySpirit/Apriorie
 * indexers.  
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 11-Apr-2005 13:49:27
 *
 */
public class IndexingObject {
    private String uri = null;
    private String docType = null;
    private String fulltext = null;
    private HashMap attributes = new HashMap();
    
    /**
     * Constructor of class.
     * @param uri the URI of the document to index
     * @param documentType the document type (e.g. "article", "annotation")
     */
    public IndexingObject(String uri, String documentType) {
        this.uri = uri;
        this.docType = documentType.toLowerCase();
    }
    
    /**
     * The full text of the indexing object.
     * @param fulltext the full text as a String
     */
    public void setFulltext(String fulltext) {
        if (fulltext != null) this.fulltext = fulltext.toLowerCase();
        else this.fulltext = null;
    }
    
    /**
     * Adds an attribute-value pairs
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     */
    public void addAttribute(String attributeName, String attributeValue) {
        if (attributeName != null && attributeValue != null ) {
            HashSet aValues = (HashSet) attributes.get(attributeName);
            if (aValues == null) aValues = new HashSet();
            aValues.add(attributeValue.toLowerCase());
            attributes.put(attributeName.toLowerCase(), aValues);            
        }
    }
    
    /**
     * Returns the attribute names of this object as a set of Strings.
     * @return the attribute names
     */
    public Set attributeNames() {
        return attributes.keySet();
    }
    
    /**
     * Returns the attribute values of a specific attribute name as a set of
     * Strings.
     * @param attributeName the name of the attribute
     * @return the attribute values of this attriute
     */
    public Set attributeValues(String attributeName) {
        return (Set) attributes.get(attributeName);
    }
    
    /**
     * Returns the full text of this indexing object
     * @return the full text
     */
    public String fulltext() {
        return this.fulltext;
    }
    
    /**
     * Returns the document URI of this indexing object
     * @return the document URI
     */
    public String documentURI() {
        return this.uri;
    }
    
    /**
     * Returns the document type of this indexing object
     * @return the document type
     */
    public String documentType() {
        return this.docType;
    }
}
	
	