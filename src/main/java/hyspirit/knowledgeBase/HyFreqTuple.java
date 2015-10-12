/*
 * Copyright 2007 University of Duisburg-Essen, working group
 *   "Information Systems"
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
 * Created on 07.08.2007 15:09:19
 * $Revision: 1.1.1.1 $
 */
package hyspirit.knowledgeBase;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a tuple as to be found in frequency files. Each tuple
 * has a certain frequency, which is a value >= 1
 * 
 * @author <a href="mailto:ingo@is.informatik.uni-duisburg.de">Ingo
 *         Frommholz</a>
 *         <p>
 *         Created on 07.08.2007 15:09:19
 *
 */
public class HyFreqTuple implements Comparable {
    private int frequency = 1;
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
    public HyFreqTuple(int frequency, String[] attributeValues) {
	this.frequency = frequency;
	this.attributeValues = attributeValues;
    }

    /**
     * Constructor of class with frequency 1.
     * 
     * @param attributeValues
     *            the attribute values of the tuple
     */
    public HyFreqTuple(String[] attributeValues) {
	this.attributeValues = attributeValues;
    }

    /**
     * Constructor of class. This constructor takes a line from a frequency file
     * <br>
     * &lt;frequency&gt; (&lt;tokenelement_1&gt;,...,&lt;tokenelement_n&gt;)
     * <br>
     * parses it and creates a corresponding HyTuple element
     * 
     * @param line
     *            the line as returned by the inference engine
     * @throws HyTupleFormatException
     *             if the input line does not represent a frequency tuple
     */
    public HyFreqTuple(String line) throws HyTupleFormatException {

	Matcher m = Pattern.compile("^([0-9]*)").matcher(line);
	if (m.find()) {
	    // explicitly given frequency
	    String freqString = m.group();
	    this.frequency = Integer.parseInt(freqString);
	} else {
	    // no explicitly given frequency value
	    m = Pattern.compile("^\\s*\\((.*)\\)").matcher(line);
	    if (m.find())
		this.frequency = 1;
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
	    String attributeString = m.group().substring(1,
		    m.group().length() - 1); // remove brackets

	    m = Pattern.compile("^(\".*?\"|[^,\"]*),?")
		    .matcher(attributeString);
	    Vector<String> attValues = new Vector<String>();
	    while (m.find() && !attributeString.equals("")) {
		String attributeValue = m.group();
		attributeString = attributeString
			.substring(m.end(), attributeString.length())
			.trim();
		if (attributeValue.endsWith(","))
		    // remove last ','
		    attributeValue = attributeValue.substring(0,
			    attributeValue.length() - 1);
		attValues.add(attributeValue.trim());
		m = Pattern.compile("^(\".*?\"|[^,\"]*),?")
			.matcher(attributeString);
	    }

	    this.attributeValues = new String[attValues.size() + 1];
	    for (int i = 0; i < attValues.size(); i++) {
		this.attributeValues[i] = attValues.elementAt(i);
	    }
	} else
	    throw new HyTupleFormatException("Malformed attributes in tuple!");
	this.stringRepresentation = line;
    }

    /**
     * Returns the frequency of this tuple
     * 
     * @return the frequency
     */
    public int frequency() {
	return frequency;
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
	    stringRepresentation = frequency + " (";
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
     * Enables tuples to be sorted w.r.t. descending probabilites
     * 
     * @param tupleObj
     *            the tuple this tuple is compared with
     * @return 1, if this object has a lower probability than the other, -1, if
     *         this object has a higher probability, 0 if the probability is the
     *         same
     */
    @Override
    public int compareTo(Object tupleObj) {
	HyFreqTuple tuple = (HyFreqTuple) tupleObj;
	if (tuple.frequency() > this.frequency())
	    return 1;
	else if (tuple.frequency() < this.frequency())
	    return -1;
	else
	    return 0;
    }

}
