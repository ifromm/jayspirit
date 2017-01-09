package hyspirit.knowledgeBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
public class RelationHandler extends HashMap<String, Collection<HyTuple>> {

    private static final long serialVersionUID = 1L;

    /**
     * This Map contains all newly created relations. The key is the relation
     * name and the value is a collection of corresponding HyTuples.
     */
    private final Map<String, Collection<HyTuple>> relations =
	    new HashMap<String, Collection<HyTuple>>();

    /**
     * Adds a tuple to a relation.
     * 
     * @param relationName
     *            the relation name
     * @param tuple
     *            the tuple to add
     */
    public void addTupleToRelation(String relationName, HyTuple tuple) {
	if (this.relations != null) {
	    Collection<HyTuple> tuples = this.get(relationName);
	    if (tuples == null) {
		tuples = new LinkedList<HyTuple>();
		put(relationName, tuples);
	    }
	    tuples.add(tuple);

	}
    }

}
