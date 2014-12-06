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
 * Created on 01-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
 */

package hyspirit.engines;

import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import hyspirit.util.HySpiritException;

/** A PSQL engine runs PSQL (probabilistic SQL) programs. For example:
 
 <p>
 INSERT INTO index VALUES <br/>
 0.5 ('sailing', 'doc1'),<br/>
 0.9 ('boats', 'doc1'),<br/>
 0.6 ('sailing', 'doc2');<br/>
 </p>
 
 <p>
 INSERT INTO query VALUES 0.5 ('sailing'), 0.5('boats');
 </p>

 <p>
 -- Simple retrieval matching query against index:<br/>
 SELECT DISJOINT * FROM query, index where query.term = index.term;<br/>
 </p>

 <p>Describing full-text retrieval with SQL is intutive, but causes
 quickly scalability problems. For solving this, the Apriorie
 framework runs external scalable engines. The Java-API engines are
 just convenient Java front-ends for accessing the functionality of
 the background engines, that serve knowledge bases.</p>

 <p>Apriorie has patented its technology for large-scale probability
 estimations in PSQL. Basically, there are just two additional clauses
 needed, namely "ASSUMPTION" and "EVIDENCE KEY".</p>
 
 <p>For example, the following view definition is crucial for IR:<br/>
 CREATE VIEW invDocFreq AS<br/>
 SELECT term<br/>
 FROM docFreq<br/>
 ASSUMPTION MAX INFORMATIVE<br/>
 EVIDENCE KEY ();<br/>
 </p>

 <p>The view invDocFreq(term) yields a probabilistic interpretation of
 the common inverse document frequency, an important parameter for
 relevance-based information retrieval.<p/>

 <p>Many more retrieval aspects can be described in PSQL. The Apriorie
 framework comes with various PSQL and other logical programs for
 implementing retrieval tasks such as classification and ad-hoc
 retrieval.<p/>
*/
public class HyPSQLEngine extends HyInferenceEngine {
    
    private final static String ENGINE_NAME = "hy_psql";

    /** 
     * Create an engine. Sets defaults of deleteQueryProgram (DELETE
     * FROM qterm) and retrieveProgram (SELECT * FROM retrieve).
     */
    public HyPSQLEngine() throws HySpiritException {
        super(ENGINE_NAME);
        deleteQueryProgram = "DELETE FROM qterm;";
        retrieveProgram = "SELECT * FROM retrieve;";
    }
 
    
    /** 
     * Create an engine and run (evaluate) the argument file. This is
     * just a short cut for engine = new HyPSQLEngine();
     * engine.runFile(fileName).
     * @param fileName the file name
     */
    public HyPSQLEngine (String fileName) throws HySpiritException, IOException  
    {
        this();
        evalFile(fileName);
    }
    /**
     * Returns the stream end message. The stream end message is needed for
     * send in order to determine when the whole output is read.
     * @return the stream end message
     */
    protected String getStreamEndMessage() {
        return "END";
    }
    
    /**
     * Returns a string which lets the engine echo the given message
     * @param message the message to be echoed
     * @return the echo string
     */
    public String echoSpecial(String message) {
        return("INSERT INTO _echo VALUES ('" + message + "');");
    }
    
    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     */
    public void reset() {
        super.reset();
        // XXX Reset all other values
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
