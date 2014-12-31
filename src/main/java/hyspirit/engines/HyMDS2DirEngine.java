/*
 * Copyright 2000-2006 University of Duisburg-Essen, working group
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
 * Created on 14-Feb-2006 00:58:56
 * $Revision: 1.1.1.1 $
 */
package hyspirit.engines;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

/**
 * @author <a href="mailto:ingo@is.informatik.uni-duisburg.de">Ingo Frommholz</a>
 * <p>
 * Created on 14-Feb-2006 00:58:56
 *
 */
public class HyMDS2DirEngine extends HyEngine {
    private Vector columns = new Vector();
    private String indexDir = null;
    private Vector filenames = new Vector();
    
    private static final String ENGINE_NAME = "hy_mds2dir";
    
    /**
     * This constructor tries to determine the HySpirit environment 
     * automatically
     * @throws HySpiritException if the HySiprit envirnoment cannot be 
     * determined
     */
    public HyMDS2DirEngine() throws HySpiritException {
        super(ENGINE_NAME);
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyMDS2DirEngine(HySpiritProperties hyspirit)
    throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
    }
    
    
    /**
     * Add a file to read the MDS data from. 
     * @param filename the name of the MDS file
     */
    public void addMDSFile(String filename) {
        this.filenames.add(filename);
    }
    
    /**
     * The columns for which the index should be created. See hy_mds2dir manual
     * for further details.
     * @param column the column
     */
    public void addColumn(int column) {
        this.columns.add(Integer.toString(column));
    }
    
    /**
     * The directory in which the index is created.
     * @param indexDir
     */
    public void indexDir(String indexDir) {
        this.indexDir = indexDir;
    }
    
    
    
    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     */
    public void reset() {
        super.reset();
        columns = new Vector();
        indexDir = null;
        filenames = new Vector();
    }
    
    
    
    
    /**
     * Builds the command from the parameters.
     * @return the command string array 
     */
    protected String[] buildCommand() {
        Vector commandVec = new Vector();
        commandVec.add(super.getCommand());
        
        for (int i = 0; i < columns.size(); i++) {
            commandVec.add("-column");
            commandVec.add((String)columns.get(i));
        }
        
        if (indexDir != null) {
            commandVec.add("-index_dir");
            commandVec.add(indexDir);
        }
        
        // add additional arguments
        if (super.argumentString != null) {
            StringTokenizer strTok= new StringTokenizer(super.argumentString);
            while (strTok.hasMoreTokens())
                commandVec.add(strTok.nextToken());
        }
        
        // file names
        for (Enumeration e = filenames.elements(); e.hasMoreElements();) {
            commandVec.add((String)e.nextElement());
        }
        
        if (stdin) commandVec.add("-");
        
        String[] commandString = new String[commandVec.size()];
        int i = 0;
        for (Enumeration e = commandVec.elements(); e.hasMoreElements();) 
            commandString[i++] = (String)e.nextElement();
        return commandString;   
    }
    
}
