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
 * Created on 01-Dec-2005 18:49:26
 * $Revision: 1.1.1.1 $
 */
package hyspirit.engines;

import hyspirit.knowledgeBase.HyTuple;
import hyspirit.knowledgeBase.HyTupleFormatException;
import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;


public abstract class HyInferenceEngine extends HyEngine {
    
    
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param engineName the name of the engine (e.g., 'hy_pra')
     * @throws HySpiritException if we can't determine the environment
     */
    public HyInferenceEngine(String engineName)
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
    public HyInferenceEngine(String engineName, HySpiritProperties hyspirit)
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
    public HyInferenceEngine(String engineName, String hostname, int port)
    throws HySpiritException {
        super(engineName, hostname, port);
    }
    
    
    
    /**
     * Evaluates the given expression by sending it to stdin of the underlying
     * engine process/server. Use nextTuple() to receive the output from the
     * underlying process.
     *
     * @param input the input to be evaluated
     * @throws IOException if communication fails
     */
    public void eval(String input) throws IOException {
        send(input);
    }
    
    
    public void evalFile(String filename) throws IOException {
        sendFile(filename);
    }
    
    /**
     * Returns the next tuple from the underlying engine or null if
     * there's no tuple left to read. This method might block if and as long as
     * the output stream from the underlying process blocks.
     * @return the next tuple or null if there is none
     */   
    public HyTuple nextTuple() {
        //System.out.print("Next tuple: ");
        HyTuple tuple = null;
        boolean foundTuple = false;
        while (!foundTuple && this.hasNext()) {
            String nextLine = this.next();
            if (nextLine != null) {
                try{
                    tuple = new HyTuple(nextLine);
                    //System.out.println(tuple.toString());
                    foundTuple = true; // never reached if exception is thrown
                }
                catch (HyTupleFormatException he) {}
            }
        }
        return tuple;
    }
    
    
    /**
     * Get the echo special command of this engine.
     * For POOL, FVPD, PD and PRA, this is "_echo(message)". For PSQL,
     * this is "INSERT INTO _echo VALUES ('message')".
     */
    public String echoSpecial(String message) {
        return("_echo(\"" + message + "\").");
    }
    
    /*
     * Returns the stream end message. The stream end message is needed for
     * send() in order to determine when the whole output is read.
     * @return the stream end message
     */
    protected String getStreamEndMessage() {
        return "END";
    }
    
    
    // Ingo: New methods, 01/02/2006
    protected String deleteQueryProgram;
    protected String retrieveProgram;
    /**
     * Get the program for retrieving objects.
     */
    public String deleteQueryProgram () {
        return null; // XXX to make it compileable
    }
    /**
     * Get the program for retrieving objects.
     */
    public String retrieveProgram () {
        return null; // XXX to make it compileable
    }
    
 
    /**
     * Let this engine echo the argument message.
     */
    public void echo (String message) {
        runProgram((message));
    }
    
    /**
     * Let this engine retrieve a result.
     * This method passes the retrieve program to the engine.
     */
    public void retrieve () {
        runProgram(retrieveProgram());
    }
    /**
     * Let this engine delete the previous query.
     * This method passes the delete query program to the engine.
     */
    public void deleteQuery () {
        runProgram(deleteQueryProgram());
    }
    
    /**
     * Let this engine run the argument program.
     */
    public void runProgram (String program) {
    }
    
    // THIS SHOULD GO?
    // No. ;-) fixed it. - IFr
//  public String[] buildCommand() {
//  Vector commandVec = new Vector();
//  commandVec.add(super.getCommand());
//  
//  /*
//  * Set parameters here.
//  */
//  
//  
//  
//  String[] commandString = new String[commandVec.size()];
//  int i = 0;
//  for (Enumeration e = commandVec.elements(); e.hasMoreElements();) 
//  commandString[i++] = (String)e.nextElement();
//  return commandString;   
//  }   
}
