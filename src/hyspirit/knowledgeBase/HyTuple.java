/*
 * Copyright 2000-2006 University of Duisburg-Essen, Working group
 *   "Information Systems"
 * Copyright 2005-2006 Apriorie Ltd.
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
 * Created on 14-Dec-2005 16:37:17
 * $Revision: 1.2 $
 */
package hyspirit.knowledgeBase;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a tuples as to be found in MDS dfiles or as output of
 * queries. Each tuple has a certain probability, which is a value between 0 and
 * 1.
 * 
 * @author <a href="mailto:ingo@is.informatik.uni-duisburg.de">Ingo
 *         Frommholz</a>
 *         <p>
 *         Created on 14.12.2005 16:37:17
 *
 */
public class HyTuple implements Comparable<HyTuple> {

    private double probability = 1;
    private String[] attributeValues = null;
    private String stringRepresentation = null;

    /**
     * Constructor of class.
     * 
     * @param probability
     *            the tuple probability
     * @param attributeValues
     *            the attribute values of the tuple
     */
    public HyTuple(float probability, String[] attributeValues) {
	this.probability = probability;
	this.attributeValues = attributeValues;
    }

    /**
     * Constructor of class.
     * 
     * @param probability
     *            the tuple probability
     * @param attributeValues
     *            the attribute values of the tuple
     */
    public HyTuple(double probability, String... attributeValues) {
	this.probability = probability;
	this.attributeValues = attributeValues;
    }

    /**
     * 
     * @param attributeValues
     */
    public HyTuple(String... attributeValues) {
	this.attributeValues = attributeValues;
    }

    /**
     * Constructor of class with probability 1.
     * 
     * @param attributeValues
     *            the attribute values of the tuple
     */
    // public HyTuple(String[] attributeValues) {

    // }

    /**
     * Constructor of class. This constructor takes a line as returned by an
     * inference engine of the form <br>
     * &lt;prob&gt; (&lt;tokenelement_1&gt;,...,&lt;tokenelement_n&gt;) <br>
     * parses it and creates a corresponding HyTuple element
     * 
     * @param line
     *            the line as returned by the inference engine
     * @throws HyTupleFormatException
     *             if the input line does not represent a tuple
     */
    public HyTuple(String line) throws HyTupleFormatException {

	Matcher m = Pattern.compile("^([10](\\.[0-9]*)?)").matcher(line);
	if (m.find()) {
	    String probString = m.group();
	    this.probability = Float.parseFloat(probString);
	}
	else {
	    m = Pattern.compile("^\\s*\\((.*)\\)").matcher(line);
	    if (m.find())
		this.probability = 1;
	    else
		throw new HyTupleFormatException(
			"Malformed probability in tuple!");
	}

	/*
		 * We match attributes by matching the first attribute, removing it,
		 * matching the first attribute from the remaining attribute string,
		 * etc.
		 */
	m = Pattern.compile("\\((.*)\\)").matcher(line);
	if (m.find()) {
	    String attributeString =
		    m.group().substring(1, m.group().length() - 1); // remove
								    // brackets

	    m = Pattern.compile("^(\".*?\"|[^,\"]*),?")
		    .matcher(attributeString);
	    Vector<String> attValues = new Vector<String>();
	    while (m.find() && !attributeString.equals("")) {
		String attributeValue = m.group();
		attributeString =
			attributeString.substring(m.end(),
				attributeString.length())
				.trim();
		if (attributeValue.endsWith(","))
		    // remove last ','
		    attributeValue =
			    attributeValue.substring(0,
				    attributeValue.length() - 1);
		attValues.add(attributeValue.trim());
		m = Pattern.compile("^(\".*?\"|[^,\"]*),?")
			.matcher(attributeString);
	    }

	    this.attributeValues = new String[attValues.size()];
	    for (int i = 0; i < attValues.size(); i++) {
		this.attributeValues[i] = attValues.elementAt(i);
	    }
	}
	else
	    throw new HyTupleFormatException("Malformed attributes in tuple!");
	// this.stringRepresentation = line;
    }

    /**
     * Returns the probability of this tuple
     * 
     * 
     * @return the probability
     */
    public double probability() {
	return probability;
    }

    /**
     * Returns the attribute value at the give index (0 for the first attribute)
     * 
     * @param index
     *            the index
     * @return the element
     */
    public String valueAt(int index) {
	return attributeValues[index];
    }

    /**
     * Returns the attribute values of the tuple as an array of strings
     * 
     * @return the tuple attributeValues
     */
    public String[] attributeValues() {
	return attributeValues;
    }

    /**
     * Returns a string representation of the tuple, i.e., <br>
     * &lt;prob&gt; (&lt;tokenelement_1&gt;,...,&lt;tokenelement_n&gt;)
     * 
     * @return string representation of the tuple
     */
    @Override
    public String toString() {
	if (stringRepresentation == null) {
	    stringRepresentation = probabilityString() + " (";
	    for (int i = 0; i < attributeValues.length; i++) {
		stringRepresentation += attributeValues[i];
		if (i < attributeValues.length - 1)
		    stringRepresentation += ", ";
	    }
	    stringRepresentation += ")";
	}
	return stringRepresentation;
    }

    /**
     * As Java tends to use scientifc notation for values < 10^-3 and HySpirit
     * doesn't like that, we need to format our probability string correctly.
     * Furthermore this ensures that an English locale is used to represent the
     * probability.
     * 
     * @return a HySpirit-compatible String representation of the tuple
     *         probability
     */
    public String probabilityString() {
	DecimalFormat form =
		(DecimalFormat)
		NumberFormat.getNumberInstance(Locale.ENGLISH);
	form.applyPattern("#.######");
	return form.format(this.probability);
    }

    /**
     * Enables tuples to be sorted w.r.t. descending probabilites
     * 
     * @param tupleObj
     *            the tuple this tuple is compared with
     * @return 1, if this object has a lower probability than the other, -1, if
     *         this object has a higher probability, 0 if the probability is the
     *         same
     */
    public int compareTo(HyTuple tuple) {
	if (tuple.probability() > this.probability())
	    return 1;
	else if (tuple.probability() < this.probability())
	    return -1;
	else
	    return 0;
    }

}
