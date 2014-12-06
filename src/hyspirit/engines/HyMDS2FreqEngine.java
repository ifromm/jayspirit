/*
 * Copyright 2000-2005 University Duisburg-Essen, Working group
 * "Information Systems"
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
 * Created on 12-Feb-2006 01:07:06
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
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
 * Created on 12-Feb-2006 01:07:06
 *
 */
public class HyMDS2FreqEngine extends HyAnalysisEngine {

    private Vector columns = new Vector();
    private String hy_mds2freq = null;
    private String count = null;
    private String probabilistic = null;
    private String avg = null;
    private String stream = null;
    private long windowSize = -1;
    private String priFile = null;
    private Vector filenames = new Vector();
    private int group = -1;
    
    
    private static final String ENGINE_NAME = "hy_mds2freq";
    
    /**
     * This constructor tries to determine the HySpirit environment 
     * automatically
     * @throws HySpiritException if the HySiprit envirnoment cannot be 
     * determined
     */
    public HyMDS2FreqEngine() throws HySpiritException {
        super(ENGINE_NAME);
    }

    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyMDS2FreqEngine(HySpiritProperties hyspirit)
            throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
    }

    
    /**
     * Adds a column forming the frequency key. Note that the order of adding
     * columns does matter. See hy_mds2freq manual for further details.
     * @param column the column
     */
    public void addColumn(int column) {
        this.columns.add(Integer.toString(column));
    }
    
    /**
     * Count values. Sets the -values parameter. See hy_mds2freq manual for
     * further details.
     */
    public void countValues() {
        this.count = "values";  
    }
    
    /**
     * Count tuples. Sets the -tuples parameter. See hy_mds2freq manual for
     * further details.
     */
    public void countTuples() {
        this.count = "tuples";  
    }
    
    /**
     * Whether or not to take into account the tuple probabilities specified
     * in the inut file. See hy_mds2freq manual for further details.
     * @param probabilistic
     */
    public void setProbabilistic(boolean probabilistic) {
        if (probabilistic) this.probabilistic = "probabilistic";
        else this.probabilistic = "noprobabilistic";
    }
    
    /**
     * Whether or not to print the average frequency. See hy_mds2freq manual
     * for further details.
     * @param avg
     */
    public void setAvg(boolean avg) {
        if (avg) this.avg = "avg";
        else this.avg = "noavg";
    }
    
    /**
     * Sets or unsets the -stream option. See hy_mds2freq manual for further
     * details.
     * @param stream true if stream option should be set, false for nostream
     *        option
     */
    public void setStream(boolean stream) {
        if (stream) this.stream = "stream";
        else this.stream = "nostream";
    }
    
    /**
     * Sets the window size. See hy_mds2freq manual for further details.
     * @param windowSize
     */
    public void windowSize(long windowSize){
        this.windowSize = windowSize;
    }

    /**
     * Sets the pri file to process large input streams. See hy_mds2freq manual
     * for further details.
     * @param priFile the filename 
     */
    public void setPriFile(String priFile) {
        this.priFile = priFile;
    }
    
    /**
     * Sets the group parameter. See hy_mds2freq manual for further details.
     * @param group the value of the group parameter.
     */
    public void setGroup(int group) {
        this.group = group;
    }
    
    /**
     * Add a file to read the MDS data from. 
     * @param filename the name of the MDS file
     */
    public void addMDSFile(String filename) {
        this.filenames.add(filename);
    }
    

    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     */
    public void reset() {
        super.reset();
        this.count = null;
        this.probabilistic = null;
        this.avg = null;
        this.stream = null;
        this.windowSize = -1;
        this.priFile = null;
        this.group = -1;
        this.columns = new Vector();
        this.filenames = new Vector();
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
        
        if (count != null) commandVec.add("-" + count);
        if (probabilistic != null) commandVec.add("-" + probabilistic);
        if (avg != null) commandVec.add("-" + avg); 
        if (stream != null) commandVec.add("-" + stream);
        if (windowSize > 0) {
            commandVec.add("-window_size");
            commandVec.add(Long.toString(windowSize));
        }
        if (group > -1) {
            commandVec.add("-group");
            commandVec.add(Integer.toString(group));
        }
        if (priFile != null) {
            commandVec.add("-pri_file");
            commandVec.add(priFile);        
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
