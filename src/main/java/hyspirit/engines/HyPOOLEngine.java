/*
 * Copyright 2006 University Duisburg-Essen, Working group
 * "Information Systems"
 *
 * Copyright 2006 Apriorie Ltd.
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
 * Created on 01-Feb-2006
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
 */
package hyspirit.engines;

import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/** A POOL engine runs POOL (probabilistic object-oriented logic)
 programs. For example:
 
 <p>
 database1[<br/>
 doc1.author(peter)<br/>
 0.9 doc1 [ 0.2 sailing; 0.5 sailor(peter); peter.friend(mary) ]<br/>
 0.5 doc2 [ 0.8 sec1[sailing]; 0.3 sec2[boats] ]<br/>
 ]<br/>
 </p>
 
 <p>The program above models a database with two documents (doc1,
 doc2), and the content and structure of the documents are modelled
 with the same logical componends; for the content representation,
 words, classifications, and relationships are applied.</p>
 
 <p>The document content augments the knowledge of the context in
 which the documents occur. For example, we view doc1 as more
 reliable then doc2, and sec1 contributes more to the description
 of doc2, than sec2 does.</p>
 
 <p>Querying is as follows:<br/>
 % Content-oriented querying:<br/>
 % All documents about sailing:<br/>
 ?- D[ sailing ];<br/>
 <br/>
 % Fact-oriented querying:<br/>
 % All document of author peter:<br/>
 ?- D.author(peter);<br/>
 <br/>
 % Integrated content and fact-oriented querying:<br/>
 % All documents about sailing and a sailor, where the sailor<br/>
 % is the author of that document.<br/>
 ?- D[sailing & sailor(X)] & D.author(X);<br/>
 </p>
 
 <p>POOL is described in Thomas Roelleke, "POOL: A probabilistic
 object-oriented logic for hypermedia retrieval", Shaker Verlag,
 Aachen, 1999.</p>
 */
public class HyPOOLEngine extends HyInferenceEngine {
    
    private static final String ENGINE_NAME = "hy_pool";
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @throws HySpiritException if we can't determine the environment
     */
    public HyPOOLEngine () throws HySpiritException{
        super(ENGINE_NAME);
        retrieveProgram = "?- retrieve(D);";
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyPOOLEngine(HySpiritProperties hyspirit)
    throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
        retrieveProgram = "?- retrieve(D);";
    }

    /**
     * This constructor must be used if you use an engine server (client mode).
     * @param hostname the server host name
     * @param port the server port
     * @throws HySpiritException if we can't determine the environment
     */
    public HyPOOLEngine(String hostname, int port)
        throws HySpiritException {
        super(ENGINE_NAME, hostname, port);
        retrieveProgram = "?- retrieve(D);";
    }
    
    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     */
    public void reset() {
        super.reset();
    }
    
    protected String[] buildCommand() {
        Vector commandVec = new Vector();
        commandVec.add(super.getCommand());
        
        /* XXX
         * Set parameters here.
         */
        
        // add additional arguments
        if (super.argumentString != null) {
            StringTokenizer strTok= new StringTokenizer(super.argumentString);
            while (strTok.hasMoreTokens())
                commandVec.add(strTok.nextToken());
        }
        
        String[] commandString = new String[commandVec.size()];
        int i = 0;
        for (Enumeration e = commandVec.elements(); e.hasMoreElements();) 
            commandString[i++] = (String)e.nextElement();
        return commandString;   
    }
}
