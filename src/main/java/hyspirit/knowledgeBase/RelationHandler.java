package hyspirit.knowledgeBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
/*
 * Copyright 2000-2006 University of Duisburg-Essen, Working group
 *   "Information Systems"
 * Copyright 2005-2006 Apriorie Ltd.
 * Copyright 2014-2017 Ingo Frommholz
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
 * Created on 09-Jan-2017
 */

/**
 * 
 * This class implements a handler for in-memory relations containing HyTuples
 * 
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 */
public class RelationHandler extends HashMap<String, Collection<HyTuple>>
	implements Serializable {

    private static final long serialVersionUID = -3085776684024637221L;

    /**
     * Adds a tuple to a relation.
     * 
     * @param relationName
     *            the relation name
     * @param tuple
     *            the tuple to add
     */
    public void addTupleToRelation(String relationName, HyTuple tuple) {
	Collection<HyTuple> tuples = this.get(relationName);
	if (tuples == null) {
	    tuples = new LinkedList<HyTuple>();
	    put(relationName, tuples);
	}
	tuples.add(tuple);
    }

    /**
     * String representation of a relation. Use toString() to get a
     * representation for all relations.
     * 
     * @param relationName
     *            the relation under consideration
     * @param printName
     * @return String representation of relation
     */
    public String relationToString(String relationName, boolean printName) {
	String s = null;
	Collection<HyTuple> tuples = this.get(relationName);

	if (tuples != null) {
	    s = "";
	    for (HyTuple t : tuples) {
		if (printName) {
		    t.setRelationName(relationName);
		    t.printRelationName(true);
		}
		s += t + "\n";
	    }
	}
	return s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractMap#toString()
     */
    @Override
    public String toString() {
	String s = "";
	List<String> keys = new ArrayList<String>(this.keySet());
	java.util.Collections.sort(keys);
	for (String key : keys) {
	    s += relationToString(key, true);
	}

	return s;
    }

}
