/*
 * Copyright 2000-2006 University of Duisburg-Essen, Working group
 *   "Information Systems"
 * Copyright 2005-2006 Apriorie Ltd.
 * Copyright 2014-2015 Ingo Frommholz
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
 */
package hyspirit.knowledgeBase;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a tuples as to be found in MDS files or as output of
 * queries. Each tuple has a certain probability, which is a value between 0 and
 * 1.
 * 
 * @author <a href="mailto:ingo@frommholz.org">Ingo Frommholz</a>
 *         <p>
 *         Created on 14.12.2005 16:37:17
 *
 */
public class HyTuple implements Comparable<HyTuple>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8830254829713979656L;

    private double probability = 1;
    private String[] attributeValues = null;
    private String stringRepresentation = null;

    /** The name of the underlying relation */
    private String relationName = null;

    /**
     * Controls whether the relation name should be printed in toString() or not
     */
    boolean printRelName = false;

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
     * Constructor of class with probability 1. The data will be cleaned, i.e.
     * possible double quotes will be removed.
     * 
     * @param attributeValues
     *            the attribute values of the tuple
     */
    public HyTuple(String... attributeValues) throws HyTupleFormatException {
	// We need to remove any double quotes (") etc
	for (int i = 0; i < attributeValues.length; i++) {
	    if (attributeValues[i] != null) {
		attributeValues[i] = cleanStringValue(attributeValues[i]);
	    }
	}
	this.attributeValues = attributeValues;
    }

    /**
     * Helper method for cleaning attribute values. Here it means removing
     * double quotes and line breaks and put the string in quotes in case there
     * is a comma. If the string was within double quotes originally, these will
     * be preserved.
     * 
     * @param value
     *            the attribute value to clean.
     * @return the cleaned attribute value
     */
    private String cleanStringValue(String value) {
	/*
	 * TODO: In a later version we may refine cleaning so the PD terminal
	 * symbols are matched:
	 * 
	 * NAME ::= [a-z][A-Za-z0-9_]* NUMBER ::= [-+]?[0-9]+(\.[0-9]+)? | ...
	 * <scientific format> STRING ::= "[^\"]*"
	 */
	if (value != null) {
	    value = value.trim();
	    boolean doubleQuotes =
		    value.startsWith("\"") && value.endsWith("\"");
	    value = value
		    .replace("\"", "")
		    .replace("\'", "")
		    .replace("\n", "")
		    .replace("\r", "");

	    // in case there is a comma in the string, we need to put it in
	    // quotes.
	    if (value.contains(","))
		doubleQuotes = true;

	    if (doubleQuotes)
		value = "\"" + value + "\"";
	}
	return value;
    }

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
     * This gets the relation name corresponding to this tuple.
     * 
     * @return the relation name or null if this is not set
     */
    public String getRelationName() {
	return this.relationName;
    }

    /**
     * The relation name corresponding to this tuple can be set here.
     * 
     * @param relationName
     *            the relation name to set
     */
    public void setRelationName(String relationName) {
	this.relationName = relationName;
    }

    /**
     * Control whether the relation name should be printed in toString().
     * 
     * @param printRelName
     *            set to {@code true} if the relation name should be printed,
     *            {@code false} otherwise.
     * @see HyTuple.toString()
     */
    public void printRelationName(boolean printRelName) {
	this.printRelName = printRelName;
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
     * &lt;prob&gt; (&lt;tokenelement_1&gt;,...,&lt;tokenelement_n&gt;). A
     * relation name is printed if said so in printRelationName() (and the
     * relation name is not null).
     * 
     * @return string representation of the tuple
     * @see HyTuple.printRelationName()
     * @see HyTuple.setRelationName()
     */
    @Override
    public String toString() {
	// we don't print '1'
	String probPrefix =
		this.probability == 1 ? "" : probabilityString() + " ";
	if (stringRepresentation == null) {
	    stringRepresentation = probPrefix + "(";
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
		(DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
	form.applyPattern("#.######");
	return form.format(this.probability);
    }

    /**
     * Returns the number of attributes in the tuple
     * 
     * @return the number of attributes
     */
    public int size() {
	return attributeValues.length;
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
    public int compareTo(HyTuple tuple) {
	if (tuple.probability() > this.probability())
	    return 1;
	else
	    if (tuple.probability() < this.probability())
		return -1;
	    else
		return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(this.attributeValues);
	long temp;
	temp = Double.doubleToLongBits(this.probability);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	result = prime * result + ((this.relationName == null) ? 0
		: this.relationName.hashCode());
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof HyTuple)) {
	    return false;
	}
	HyTuple other = (HyTuple) obj;
	if (!Arrays.equals(this.attributeValues, other.attributeValues)) {
	    return false;
	}
	if (Double.doubleToLongBits(this.probability) != Double
		.doubleToLongBits(other.probability)) {
	    return false;
	}
	if (this.relationName == null) {
	    if (other.relationName != null) {
		return false;
	    }
	}
	else
	    if (!this.relationName.equals(other.relationName)) {
		return false;
	    }
	return true;
    }

}
