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
 * Created on 18-Jul-2005 18:28:18.  Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;

/**
 * Helper class to manage terms and their frequencies.
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 18-Jul-2005 18:28:18
 *
 */
public class TermFreq {

	    private String term = null;
	    private int frequency = 1;
	    
	    /**
	     * Creates a new TermFreq object with the given frequency
	     * @param term the term
	     * @param frequency (initial) term frequency
	     */
	    public TermFreq(String term, int frequency) {
	        this.term = term;
	        this.frequency = frequency;
	    }
	    
	    /**
	     * Creates a new TermFreq object with frequency 1
	     * @param term the term
	     */
	    public TermFreq(String term) {
	        this.term = term;
	    }
	    
	    /**
	     * Increments the term frequency
	     */
	    public void inc() {
	        frequency++;
	    }
	    
	    /**
	     * Returns the term
	     * @return the term
	     */
	    public String term() {
	        return term;
	    }
	    
	    /**
	     * Returns the frequency
	     * @return the frequency
	     */
	    public int frequency() {
	        return frequency;
	    }
}
	

