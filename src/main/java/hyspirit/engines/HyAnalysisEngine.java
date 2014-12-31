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
 * $Revision: 1.1.1.1 $
 */
package hyspirit.engines;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

public abstract class HyAnalysisEngine extends HyEngine {
    private String contextName;
    protected Vector filenames = new Vector();
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param engineName the name of the engine (e.g., 'hy_pra')
     * @throws HySpiritException if we can't determine the environment
     */
    public HyAnalysisEngine(String engineName)
    throws HySpiritException {
        super(engineName);
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param engineName the name of the engine (e.g., 'hy_pra') 
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyAnalysisEngine(String engineName, HySpiritProperties hyspirit)
    throws HySpiritException {
        super(engineName, hyspirit);
    }
    
    /**
     * This constructor must be used if you use an engine server (client mode).
     * @param engineName the name of the engine (e.g., 'hy_pra')
     * @param hostname the server host name
     * @param port the port
     * @throws HySpiritException if we can't determine the environment
     */
    public HyAnalysisEngine(String engineName, String hostname, int port)
    throws HySpiritException {
        super(engineName, hostname, port);
    }
    
    
    // MAIN ATTRIBUTES
    /**
     * Set the context name of this analysis engine.
     */
    public void context (String contextName) {
        this.contextName = contextName;
    }
    /**
     * Get the context name of this analysis engine.
     */
    public String context () {
        return this.contextName;
    }
    
    /**
     * Add a file to read the data from. 
     * @param filename the name of the file
     */
    public void addFile(String filename) {
        this.filenames.add(filename);
    }
    
    // MAIN METHODS
    /**
     * Run this analysis engine for the argument file, and wait until
     * processing is completed. Maximal waiting time can be specified with
     * maxWaitingTime.
     */
    public int runAndWait (String fileName) {
        addFile(fileName);
        int returnValue = -1;
        try {
            run();
            waitTillRunning();
            returnValue = waitFor();
        }
        catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
        }
        return returnValue;
    }
    
    
    /**
     * Builds the command from the parameters. If subclasses support other
     * parameters, they have to override the <code>buildCommand</code> and
     * <code>reset</code> methods and also ensure that filenames are included.
     * You can use the following code for this:
     * <pre>
     *   for (Enumeration e = super.filenames.elements(); e.hasMoreElements();)
     *   {
     *      commandVec.add((String)e.nextElement());
     *   }
     * </pre>    
     * Be sure that you also place the support for new,
     * additional parameters somewhere. This means the block
     * <pre>
     *   if (this.argumentString != null) {
     *     StringTokenizer strTok= new StringTokenizer(this.argumentString);
     *     while (strTok.hasMoreTokens()) commandVec.add(strTok.nextToken());
     *  }
     *  </pre>
     *  should appear somewhere in your <code>buildCommand</code> 
     *  implementation.
     *  <p>
     *  The output of this method is used in the <code>run()</code> method.
     *  
     * @return the command string array 
     */
    protected String[] buildCommand() {
        Vector commandVec = new Vector();
        commandVec.add(getCommand());
        
        // add additional arguments
        if (this.argumentString != null) {
            StringTokenizer strTok= new StringTokenizer(this.argumentString);
            while (strTok.hasMoreTokens())
                commandVec.add(strTok.nextToken());
        }
        
        // file names
        for (Enumeration e = filenames.elements(); e.hasMoreElements();) {
            commandVec.add((String)e.nextElement());
        }
        // TODO File names in subclasses
        
        String[] commandString = new String[commandVec.size()];
        int i = 0;
        for (Enumeration e = commandVec.elements(); e.hasMoreElements();) 
            commandString[i++] = (String)e.nextElement();
        return commandString;   
    }
    
}
