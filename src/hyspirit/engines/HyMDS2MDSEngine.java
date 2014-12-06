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
 * Created on 14-Feb-2006 01:21:14
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
 * Created on 14-Feb-2006 01:21:14
 *
 */
public class HyMDS2MDSEngine extends HyEngine {
    private Vector columns = new Vector();
    private String count = null;
    private String probabilistic = null;
    private String stream = null;
    private String maxStream = null;
    private Vector filenames = new Vector();
    private String norm = null;
    private String assumption = null;
    private Vector lambdas = new Vector();
    private float exp = -1;
    private float minProb = -1;
    private String stemming = null;
    private Vector morphemes = new Vector();
    private Vector morphemeFiles = new Vector();
    private Vector stopwords = new Vector();
    private Vector stopwordFiles = new Vector();
    private String strings = null;
    private String soundex = null;
    private String context = null;
    private Vector keyColumns = new Vector();
    private Vector valueColumns = new Vector();
    
   private static final String ENGINE_NAME = "hy_mds2mds";
    
    /**
     * This constructor tries to determine the HySpirit environment 
     * automatically
     * @throws HySpiritException if the HySiprit environment cannot be 
     * determined
     */
    public HyMDS2MDSEngine() throws HySpiritException {
        super(ENGINE_NAME);
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyMDS2MDSEngine(HySpiritProperties hyspirit)
    throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
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
     * The exponent applied to the probability. See hy_freq2mds manual for
     * further details. Default: 1
     * @param exponent
     */
    public void exponent(float exponent){
        this.exp = exponent;
    }
    
    /**
     * The minimum probabiliy. See hy_mds2mds manual for
     * further details. 
     * @param minProb
     */
    public void minProb(float minProb){
        this.minProb = minProb;
    }
    
    
    /**
     * Assume a disjoint distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumeDisjoint() {
        this.assumption = "disjoint";
    }

    
    /**
     * Assume a subsumed distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumeSubsumed() {
        this.assumption = "subsumed";
    }
    
    
    /**
     * Assume an independent distribution for the probability estimation. See 
     * hy_freq2mds manual for further details.
     */
    public void assumeIndependent() {
        this.assumption = "independent";
    }
    
    
    
    
    /**
     * Adds a lambda value. See hy_freq2mds manual for further details.
     * @param lambda
     */
    public void addLambda(float lambda) {
        this.lambdas.add(new Float(lambda));
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
     * Adds a key column for nesting MDS files (using the "-nest" option. See
     * hy_mds2mds manual for  further details.
     * @param keyColumn the key column
     */
    public void addKeyColumn(int keyColumn) {
        this.keyColumns.add(Integer.toString(keyColumn));
    }
    
    /**
     * Adds a value column for nesting MDS files (using the "-nest" option. See
     * hy_mds2mds manual for  further details.
     * @param valueColumn the value column
     */
    public void addValueColumn(int valueColumn) {
        this.valueColumns.add(Integer.toString(valueColumn));
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
     * Sets or unsets the -stream option. See hy_mds2freq manual for further
     * details.
     * @param stream true if stream option should be set, false for nostream
     *        option
     */
    public void stream(boolean stream) {
        if (stream) this.stream = "stream";
        else this.stream = "nostream";
    }
    
    /**
     * Sets or unsets the -max_stream option. See hy_mds2mds manual for further
     * details.
     * @param maxStream true if max_stream option should be set, false for
     *        nomax_stream option
     */
    public void maxStream(boolean maxStream) {
        if (maxStream) this.maxStream = "max_stream";
        else this.maxStream = "nomax_stream";
    }
    
    
    
    /**
     * Whether stemming should be applied or not. 
     * @param stemming ue if stemming should be applied, false if not
     */
    public void stemming(boolean stemming) {
        if (stemming) this.stemming = "stem";
        else this.stemming = "nostem";
    }
    
    
    /**
     * Delare a word as a morpheme, i.e. exclude it from stemming. For a huge
     * amount of morphemes, store them in a file and use addMorphemeFile(). See
     * hy_text2pool manual for further details.
     * @param morpheme the word to be excluded from stemming
     */
    public void addMorpheme(String morpheme) {
        this.morphemes.add(morpheme);
    }
    
    /**
     * Add a file containing morphemes. See hy_text2pool manual for further
     * details.
     * @param filename
     */
    public void addMorphemeFile(String filename) {
        this.morphemeFiles.add(filename);
    }

    /**
     * Delare a word as a stopword. Usually, stopwords are stored in a file
     * which is added with addStopwordFile(). See hy_text2pool manual for 
     * further details.
     * @param stopword the word to be excluded from stemming
     */
    public void addStopword(String stopword) {
        this.stopwords.add(stopword);
    }
    
    /**
     * Add a file containing stopword. See hy_text2pool manual for further
     * details.
     * @param filename
     */
    public void addStopwordFile(String filename) {
        this.stopwordFiles.add(filename);
    }

    

    /**
     * Whether or not to recognise strings when stemming. See hy_mds2mds manual
     * for further details.
     * @param strings true if strings should be preserved
     */
    public void preserveStrings(boolean strings) {
        if (strings) this.strings = "strings";
        else this.strings = "nostrings";
    }
    
    /**
     * Whether or not to create a soundex code of the attribute values in the
     * respective column. See hy_mds2mds manual for further details.
     * @param soundex true if a soundex code should be created
     */
    public void createSoundex(boolean soundex) {
        if (soundex) this.soundex = "soundex";
        else this.soundex = "nosoundex";
    }
    
    /**
     * Attach a context name to the tuples. See hy_mds2mds manual for further
     * details
     * @param context the context name
     */
    public void context(String context) {
        this.context = context;
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
        columns = new Vector();
        count = null;
        probabilistic = null;
        stream = null;
        maxStream = null;
        filenames = new Vector();
        norm = null;
        assumption = null;
        lambdas = new Vector();
        exp = -1;
        minProb = -1;
        stemming = null;
        morphemes = new Vector();
        morphemeFiles = new Vector();
        stopwords = new Vector();
        stopwordFiles = new Vector();
        strings = null;
        soundex = null;
        context = null;
        keyColumns = new Vector();
        valueColumns = new Vector();
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
        if (stream != null) commandVec.add("-" + stream);
        if (maxStream != null) commandVec.add("-" + maxStream);

        if (context != null) {
            commandVec.add("-context");
            commandVec.add(context);
        }
        
    
        
        if (norm != null) {
            commandVec.add("-norm");
            commandVec.add(norm);
        }
        if (assumption != null) {
            commandVec.add("-assumption");
            commandVec.add(assumption);
        }       
        for (Enumeration e = lambdas.elements(); e.hasMoreElements();) {
            commandVec.add("-lambda");
            commandVec.add(((Float)e.nextElement()).toString());
        }

        if (exp > -1) {
            commandVec.add("-prob_exp");
            commandVec.add(Float.toString(exp));
        }
        if (minProb > -1) {
            commandVec.add("-min_prob");
            commandVec.add(Float.toString(minProb));
        }

    
        if (stemming != null) commandVec.add("-" + stemming);

        for (Enumeration e = morphemes.elements(); e.hasMoreElements();) {
            commandVec.add("-morpheme");
            commandVec.add((String)e.nextElement());
        }
        for (Enumeration e = morphemeFiles.elements(); e.hasMoreElements();) {
            commandVec.add("-morpheme_file");
            commandVec.add((String)e.nextElement());
        }
        for (Enumeration e = stopwords.elements(); e.hasMoreElements();) {
            commandVec.add("-stopword");
            commandVec.add((String)e.nextElement());
        }
        for (Enumeration e = stopwordFiles.elements(); e.hasMoreElements();) {
            commandVec.add("-stopword_file");
            commandVec.add((String)e.nextElement());
        }

        
        if (strings != null) commandVec.add("-" + strings);
        if (soundex != null) commandVec.add("-" + soundex);
        
        if ((valueColumns.size() > 0) || (keyColumns.size() > 0)) {
            commandVec.add("-nest");
            for (int i = 0; i < keyColumns.size(); i++) {
                commandVec.add("-key_col");
                commandVec.add((String)keyColumns.get(i));
            }
            for (int i = 0; i < valueColumns.size(); i++) {
                commandVec.add("-val_col");
                commandVec.add((String)valueColumns.get(i));
            }
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
