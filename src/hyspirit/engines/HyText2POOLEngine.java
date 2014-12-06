/*
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
 */

package hyspirit.engines;

import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


//Generated engine front-end; do not edit; see make-engine.
public class HyText2POOLEngine extends HyAnalysisEngine {
    private String stemming = null;
    private Vector stemRuleFiles = new Vector();
    private Vector morphemes = new Vector();
    private Vector morphemeFiles = new Vector();
    private Vector stopwords = new Vector();
    private Vector stopwordFiles = new Vector();
    private String umlauts = null;
    private int trigrams = -1;
    private String negation = null;
    private String strings = null;
    private String classifications = null;
    private String relationships = null;
    private String numbers = null;
    private String wordSep = null;
    private String skip = null;
    private int minStringLength = -1;
    private int maxStringLength = -1;
    private int minWordLength = -1;
    private int maxWordLength = -1;
    private String wordRegex = null;
    private String numberRegex = null;  
    private String stringRegex = null;
    private String negationRegex = null;
    private String cwa = null;
    private Vector textfiles = new Vector();
    
    private static final String ENGINE_NAME = "hy_text2pool";
    
    /**
     * This constructor tries to determine the HySpirit environment 
     * automatically
     * @throws HySpiritException if the HySiprit environment cannot be 
     * determined
     */
    public HyText2POOLEngine() throws HySpiritException {
        super(ENGINE_NAME);
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyText2POOLEngine(HySpiritProperties hyspirit)
    throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
    }
    
    
    
    /**
     * Returns the stream end message. The stream end message is needed for
     * send in order to determine when the whole output is read.
     * @return the stream end message (null if there is no stream end message)
     */
    protected String getStreamEndMessage() {
        return "dduummyy" + "tteerrmm123";
    }
    
    /**
     * Returns a string which lets the engine echo the given message
     * @param message the message to be echoed
     * @return the echo string (null if no echoing is allowed)
     */
    public String echoSpecial(String message) {
        return "\n" + message + "\n";
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
     * Adds a file that contains stemming rules. See hy_text2pool manual for
     * further details.
     * @param filename the name of the file containing stemming rules
     */
    public void addStemRulesFile(String filename) {
        this.stemRuleFiles.add(filename);
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
     * Whether to translate umlauts to aeoeue. See hy_text2pool manual for 
     * further details.
     * @param umlauts - flag for using umlauts
     */
    public void translateUmlauts(boolean umlauts) {
        if (umlauts) this.umlauts = "umlaute";
        else this.umlauts = "noumlaute";
    }
    
    /**
     * The number of troigrams to be generated. See hy_text2pool manual for 
     * further details.
     * @param numberOfTrigrams
     */
    public void numberOfTrigrams(int numberOfTrigrams) {
        this.trigrams = numberOfTrigrams;
    }
    
    /**
     * Whether to interpret the word "not" or not. Only useful with open world
     * assumption. See hy_text2pool manual for further details.
     * @param negation
     */
    public void parseNegation(boolean negation) {
        if (negation) this.negation = "negation";
        else this.negation = "nonegation";
    }
    
    /**
     * Sets open world assumption (cwa = false) or closed world assumption
     * (cwa = true). See hy_text2pool manual for further details. 
     * @param cwa flag if closed or open world assumption
     */
    public void cwa(boolean cwa) {
        if (cwa) this.cwa = "cwa";
        else this.cwa = "owa";
    }
    
    /**
     * Whether to recognise strings or not. See hy_text2pool manual for further
     * details.
     * @param strings
     */
    public void recogniseStrings(boolean strings) {
        if (strings) this.strings = "strings";
        else this.strings = "nostrings";
    }
    
    /**
     * Whether to recognise numbers or not. See hy_text2pool manual for further
     * details.
     * @param numbers
     */
    public void recogniseNumbers(boolean numbers) {
        if (numbers) this.numbers = "number";
        else this.numbers = "nonumbers";
    }
    
    /**
     * Whether to recognise classifications or not. See hy_text2pool manual for
     * further details.
     * @param classifications
     */
    public void recogniseClassifications(boolean classifications) {
        if (classifications) this.classifications = "classifications";
        else this.classifications = "noclassifications";
    }
    
    /**
     * Whether to recognise relationships or not. See hy_text2pool manual for
     * further details.
     * @param relationships true if relationships should be recognised
     */
    public void recogniseRelationships(boolean relationships) {
        if (relationships) this.relationships= "relationships";
        else this.relationships = "norelationships";
    }
    
    /**
     * Regular expression of word separators. See hy_text2pool manual for
     * further details.
     * @param wordSep
     */
    public void wordSep(String wordSep) {
        this.wordSep = wordSep;
    }
    
    /**
     * Skip signs and words matching the given regular expression. See
     * hy_text2pool manual for further details.
     * @param skipRegex the regular expression of words and signs to skip
     */
    public void skip(String skipRegex) {
        this.skip = skipRegex;
    }
    
    /**
     * Sets the minimum string length. See hy_text2pool manual for further
     * details.
     * @param minStringLength
     */
    public void minStringLength(int minStringLength) {
        this.minStringLength = minStringLength;
    }
    
    /**
     * Sets the maximum string length. See hy_text2pool manual for further
     * details.
     * @param maxStringLength
     */
    public void maxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }
    
    /**
     * Sets the minimum word length. See hy_text2pool manual for further
     * details.
     * @param minWordLength
     */
    public void minWordLength(int minWordLength) {
        this.minWordLength = minWordLength;
    }
    
    /**
     * Sets the maximum word length. See hy_text2pool manual for further
     * details.
     * @param maxWordLength
     */
    public void maxWordLength(int maxWordLength) {
        this.maxWordLength = maxWordLength;
    }
    
    /**
     * The regular expression for word recognition. If this option is set,
     * wordSep() does not have any effect. See hy_text2pool manual for
     * further details.
     * @param wordRegex
     */
    public void wordRegex(String wordRegex) {
        this.wordRegex = wordRegex;
    }
    
    /**
     * The regular expression for number recognition. See hy_text2pool manual
     * for further details.
     * @param numberRegex
     */
    public void numberRegex(String numberRegex) {
        this.numberRegex = numberRegex;
    }
    
    /**
     * The regular expression for string recognition. See hy_text2pool manual
     * for further details.
     * @param stringRegex
     */
    public void stringRegex(String stringRegex) {
        this.stringRegex = stringRegex;
    }   
    
    /**
     * The regular expression for negation recognition. See hy_text2pool manual
     * for further details.
     * @param negationRegex
     */
    public void negationRegex(String negationRegex) {
        this.negationRegex = negationRegex;
    }   
    
    
    public void addTextFile(String filename) {
        this.textfiles.add(filename);
    }
    
    
    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     *
     */
    public void reset() {
        super.reset();
        stemming = null;
        stemRuleFiles = new Vector();
        morphemes = new Vector();
        morphemeFiles = new Vector();
        stopwords = new Vector();
        stopwordFiles = new Vector();
        umlauts = null;
        trigrams = -1;
        negation = null;
        strings = null;
        numbers = null;
        wordSep = null;
        skip = null;
        minStringLength = -1;
        maxStringLength = -1;
        minWordLength = -1;
        maxWordLength = -1;
        wordRegex = null;
        numberRegex = null; 
        stringRegex = null;
        negationRegex = null;
        cwa = null;
        textfiles = new Vector();
        stdin = false;
    }
    
    
    protected String[] buildCommand() {
        Vector commandVec = new Vector();
        commandVec.add(super.getCommand());
        
        if (stemming != null) commandVec.add("-" + stemming);
        int stemFileIndex = 1;
        for (Enumeration e = stemRuleFiles.elements(); e.hasMoreElements();) {
            commandVec.add("-stem_rules" + stemFileIndex++);
            commandVec.add((String)e.nextElement());
        }       
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
        if (umlauts != null) commandVec.add("-" + umlauts);
        if (trigrams > -1) {
            commandVec.add("-trigrams");
            commandVec.add(Integer.toString(trigrams));
        }
        if (negation != null) commandVec.add("-" + negation);
        if (strings != null) commandVec.add("-" + strings);
        if (classifications != null) commandVec.add("-" + classifications);
        if (relationships != null) commandVec.add("-" + relationships);
        if (numbers != null) commandVec.add("-" + numbers);
        if (wordSep != null) {
            commandVec.add("-word_sep");
            commandVec.add(wordSep);
        }
        if (skip != null) {
            commandVec.add("-skip");
            commandVec.add(skip);
        }
        if (minStringLength > -1) {
            commandVec.add("-min_string_length");
            commandVec.add(Integer.toString(minStringLength));
        }
        if (maxStringLength > -1) {
            commandVec.add("-max_string_length");
            commandVec.add(Integer.toString(maxStringLength));
        }
        if (minWordLength > -1) {
            commandVec.add("-min_word_length");
            commandVec.add(Integer.toString(minWordLength));
        }
        if (maxWordLength > -1) {
            commandVec.add("-max_word_length");
            commandVec.add(Integer.toString(maxWordLength));
        }       
        if (wordRegex != null) {
            commandVec.add("-word_regex");
            commandVec.add(wordRegex);
        }
        if (numberRegex != null) {
            commandVec.add("-number_regex");
            commandVec.add(numberRegex);
        }
        if (stringRegex != null) {
            commandVec.add("-string_regex");
            commandVec.add(stringRegex);
        }
        if (negationRegex != null) {
            commandVec.add("-negation_regex");
            commandVec.add(negationRegex);
        }
        if (cwa != null) commandVec.add("-" + cwa);
        
        // add additional arguments
        if (super.argumentString != null) {
            StringTokenizer strTok= new StringTokenizer(super.argumentString);
            while (strTok.hasMoreTokens())
                commandVec.add(strTok.nextToken());
        }
        
        for (Enumeration e = textfiles.elements(); e.hasMoreElements();) 
            commandVec.add((String)e.nextElement());        
        if (stdin) commandVec.add("-");
        
        
        String[] commandString = new String[commandVec.size()];
        int i = 0;
        for (Enumeration e = commandVec.elements(); e.hasMoreElements();) 
            commandString[i++] = (String)e.nextElement();
        return commandString;   
    }
}
