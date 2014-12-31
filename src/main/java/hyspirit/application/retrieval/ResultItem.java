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
 * Created on 02-Jun-2005 20:27:52.  Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.2 $
 */
package hyspirit.application.retrieval;

import hyspirit.knowledgeBase.HyTuple;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Simple class for items of retrieval result sets, consisting of a retrieval
 * weight (retrieval status value) and URI (Uniform Resource Identifier)
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 *         <p>
 *         Created on 02-Jun-2005 20:27:52
 *
 */
public class ResultItem implements Comparable<ResultItem> {
    private double rsv = 0;
    private String uri = null;

    /**
     * Constructor of class
     * 
     * @param rsv
     *            the retrieval status value of the item
     * @param uri
     *            the uniform resource identifier of the item
     */
    public ResultItem(double rsv, String uri) {
	this.rsv = rsv;
	this.uri = uri;
    }

    /**
     * Constructor of class. Takes a HyTuple object as input. It only considers
     * the first attribute of the tuples as a result and cuts everything else.
     * 
     * @param tuple
     *            the result tuple
     */
    public ResultItem(HyTuple tuple) {
	this.rsv = tuple.probability();
	this.uri = tuple.valueAt(0);
    }

    /**
     * Returns the retrieval status value of this item
     * 
     * @return the retrieval status value (RSV)
     */
    public double getRSV() {
	return this.rsv;
    }

    /**
     * Returns the URI of this item
     * 
     * @return the URI
     */
    public String getURI() {
	return this.uri;
    }

    /**
     * As Java tends to use scientifc notation for values < 10^-3 and some tools
     * don't like that, we need to format our probability string correctly.
     * Furthermore this ensures that an English locale is used to represent the
     * probability.
     * 
     * @return an English string representation of the retrieval status value
     */
    public String rsvString() {
	DecimalFormat form =
		(DecimalFormat)
		NumberFormat.getNumberInstance(Locale.ENGLISH);
	form.applyPattern("#.######");
	return form.format(this.rsv);
    }

    /**
     * Prepares result items to be sorted w.r.t. descending order
     * 
     * @param rItem
     *            the result item this object is compared with
     * @return 1, if this object has a lower RSV than the other, -1, if this
     *         object has a higher RSV, 0 if the RSV is the same
     */
    public int compareTo(ResultItem rItem) {
	ResultItem r = rItem;
	if (r.getRSV() > this.getRSV())
	    return 1;
	else if (r.getRSV() < this.getRSV())
	    return -1;
	else
	    return 0;
    }
}
