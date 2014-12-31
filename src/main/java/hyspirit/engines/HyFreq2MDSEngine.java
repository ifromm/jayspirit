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
 * Created on 14-Feb-2006 01:15:04
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
 * Created on 14-Feb-2006 01:15:04
 *
 */
public class HyFreq2MDSEngine extends HyEngine {
    private Vector columns = new Vector();
    private String norm = null;
    private String distribution = null;
    private Vector lambdas = new Vector();
    private Vector mdsFiles = new Vector();
    private String stream = null;
    private String avg = null;
    private float exp = -1;
    private float intervalExp = -1;
    private static final String ENGINE_NAME = "hy_freq2mds";
    
    /**
     * This constructor tries to determine the HySpirit environment 
     * automatically
     * @throws HySpiritException if the HySiprit environment cannot be 
     * determined
     */
    public HyFreq2MDSEngine() throws HySpiritException {
        super(ENGINE_NAME);
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyFreq2MDSEngine(HySpiritProperties hyspirit)
    throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
    }
    
    
    /**
     * Adds a column forming the frequency key. Note that the order of adding
     * columns does matter. See hy_freq2mds manual for further details.
     * @param column the column
     */
    public void addColumn(int column) {
        this.columns.add(Integer.toString(column));
    }
    
    /**
     * Use max norm. See hy_freq2mds manual for further details.
     */
    public void useMaxNorm() {
        this.norm = "max";
    }
    
    /**
     * Use max_idf norm. See hy_freq2mds manual for further details.
     */
    public void useMaxIDFNorm() {
        this.norm = "max_idf";
    }   
    
    /**
     * Use sum norm. See hy_freq2mds manual for further details.
     */
    public void useSumNorm() {
        this.norm = "sum";
    }
    
    /**
     * Use sum_idf norm. See hy_freq2mds manual for further details.
     */
    public void useSumIDFNorm() {
        this.norm = "sum_idf";
    }
    
    /**
     * Use maxmin norm. See hy_freq2mds manual for further details.
     */
    public void useMaxMinNorm() {
        this.norm = "maxmin";
    }
    
    /**
     * Use maxmin_idf norm. See hy_freq2mds manual for further details.
     */
    public void useMaxMinIDFNorm() {
        this.norm = "maxmin_idf";
    }
    
    /**
     * Use pivoted norm. See hy_freq2mds manual for further details.
     */
    public void usePivotedNorm() {
        this.norm = "pivoted";
    }
    
    /**
     * The exponent applied to the probability. See hy_freq2mds manual for
     * further details. Default: 1
     * @param exponent
     */
    public void setExponent(float exponent){
        this.exp = exponent;
    }
    
    /**
     * Assume a disjoint distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumeDisjointDistribution() {
        this.distribution = "disjoint";
    }
    
    /**
     * Same as assumeDisjointDistribution.
     */
    public void assumeSumDistribution() {
        this.distribution = "disjoint";
    }
    
    
    /**
     * Assume an apoisson distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumeApoissonDistribution() {
        this.distribution = "apoisson";
    }
    
    /**
     * Assume a pivoted distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumePivotedDistribution() {
        this.distribution = "pivoted";
    }
    
    /**
     * Assume an independent distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumeIndependentDistribution() {
        this.distribution = "independent";
    }
    
    /**
     * Assume an exponential distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumeExponentialDistribution() {
        this.distribution = "exponential";
    }
    
    /**
     * Assume a poisson distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumePoissonDistribution() {
        this.distribution = "poisson";
    }
    
    
    /**
     * Adds a lambda value. See hy_freq2mds manual for further details.
     * @param lambda
     */
    public void addLambda(float lambda) {
        this.lambdas.add(new Float(lambda));
    }
    
    
    /**
     * Whether or not to use average for Poisson estimations. Make sure that
     * the average freqency is contained in the input (file or STDIN). See 
     * hy_freq2mds manual for further details.
     * @param avg
     */
    public void setAvg(boolean avg) {
        if (avg) this.avg = "avg";
        else this.avg = "noavg";
    }
    
    
    
    /**
     * Sets the interval exponent for Poisson distribution. See hy_freq2mds
     * manual for further details.
     * @param intervalExp the interval exponent
     */
    public void setIntervalExponent(float intervalExp) {
        this.intervalExp = intervalExp;
    }
    
    
    /**
     * Sets or unsets the -stream option. See hy_freq2mds manual for further
     * details.
     * @param stream true if stream option should be set, false for nostream
     *        option
     */
    public void setStream(boolean stream) {
        if (stream) this.stream = "stream";
        else this.stream = "nostream";
    }
    
    /**
     * Add a file to read the MDS data from. 
     * @param filename the name of the MDS file
     */
    public void addMDSFile(String filename) {
        this.mdsFiles.add(filename);
    }
    
    
    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     *
     */
    public void reset() {
        super.reset();
        this.columns = new Vector();
        this.norm = null;
        this.distribution = null;
        this.lambdas = new Vector();
        this.mdsFiles = new Vector(); 
        this.avg = null;
        this.stream = null;
        this.exp = -1;
        this.intervalExp = -1;
        this.stdin = false;
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
        if (norm != null) {
            commandVec.add("-norm");
            commandVec.add(norm);
        }
        if (distribution != null) commandVec.add("-" + distribution);
        for (Enumeration e = lambdas.elements(); e.hasMoreElements();) {
            commandVec.add("-lambda");
            commandVec.add(((Float)e.nextElement()).toString());
        }
        if (avg != null) commandVec.add("-" + avg); 
        if (stream != null) commandVec.add("-" + stream);
        if (exp > -1) {
            commandVec.add("-prob_exp");
            commandVec.add(Float.toString(exp));
        }
        if (intervalExp > -1) {
            commandVec.add("-interval_exp");
            commandVec.add(Float.toString(intervalExp));
        }
        
        // add additional arguments
        if (super.argumentString != null) {
            StringTokenizer strTok= new StringTokenizer(super.argumentString);
            while (strTok.hasMoreTokens())
                commandVec.add(strTok.nextToken());
        }
        
        // file names
        for (Enumeration e = mdsFiles.elements(); e.hasMoreElements();) {
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
