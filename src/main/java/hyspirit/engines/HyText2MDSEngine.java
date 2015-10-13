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
 * Created on 14-Feb-2006 01:30:22
 * $Revision: 1.1.1.1 $
 */
package hyspirit.engines;

import java.util.Enumeration;
import java.util.Vector;

import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

/**
 * @author <a href="mailto:ingo@is.informatik.uni-duisburg.de">Ingo
 *         Frommholz</a>
 *         <p>
 *         Created on 14-Feb-2006 01:30:22
 *
 */
public class HyText2MDSEngine extends HyEngine {
    private String hyper = null;
    private String context = null;
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
    private String wd = null;
    private Vector textfiles = new Vector();

    private static final String ENGINE_NAME = "hyp_text2mds";

    /**
     * This constructor tries to determine the HySpirit environment
     * automatically
     * 
     * @throws HySpiritException
     *             if the HySiprit environment cannot be determined
     */
    public HyText2MDSEngine() throws HySpiritException {
	super(ENGINE_NAME);
    }

    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * 
     * @param hyspirit
     *            the HySpirit properties containing the environment
     * @throws HySpiritException
     *             if we can't determine the environment
     */
    public HyText2MDSEngine(HySpiritProperties hyspirit)
	    throws HySpiritException {
	super(ENGINE_NAME, hyspirit);
    }

    /**
     * With hyper set, hy_text2mds uses its fast variant, hyp_text2mds, if
     * available and options allow. See hy_text2mds manual for further details.
     * 
     * @param hyper
     *            flag if fast version should be used or not
     * @deprecated Always used hyp now
     */
    @Deprecated
    public void hyper(boolean hyper) {
	/*
	if (hyper)
	    this.hyper = "hyper";
	else
	    this.hyper = "nohyper";
	    */
    }

    /**
     * Whether stemming should be applied or not.
     * 
     * @param stemming
     *            ue if stemming should be applied, false if not
     */
    public void stemming(boolean stemming) {
	if (stemming)
	    this.stemming = "stem";
	else
	    this.stemming = "nostem";
    }

    /**
     * Adds a file that contains stemming rules. See hy_text2MDS manual for
     * further details.
     * 
     * @param filename
     *            the name of the file containing stemming rules
     */
    public void addStemRulesFile(String filename) {
	this.stemRuleFiles.add(filename);
    }

    /**
     * Delare a word as a morpheme, i.e. exclude it from stemming. For a huge
     * amount of morphemes, store them in a file and use addMorphemeFile(). See
     * hy_text2MDS manual for further details.
     * 
     * @param morpheme
     *            the word to be excluded from stemming
     */
    public void addMorpheme(String morpheme) {
	this.morphemes.add(morpheme);
    }

    /**
     * Add a file containing morphemes. See hy_text2MDS manual for further
     * details.
     * 
     * @param filename
     */
    public void addMorphemeFile(String filename) {
	this.morphemeFiles.add(filename);
    }

    /**
     * Delare a word as a stopword. Usually, stopwords are stored in a file
     * which is added with addStopwordFile(). See hy_text2MDS manual for further
     * details.
     * 
     * @param stopword
     *            the word to be excluded from stemming
     */
    public void addStopword(String stopword) {
	this.stopwords.add(stopword);
    }

    /**
     * Add a file containing stopword. See hy_text2MDS manual for further
     * details.
     * 
     * @param filename
     */
    public void addStopwordFile(String filename) {
	this.stopwordFiles.add(filename);
    }

    /**
     * Whether to translate umlauts to aeoeue. See hy_text2MDS manual for
     * further details.
     * 
     * @param umlauts
     *            - flag for using umlauts
     */
    public void translateUmlauts(boolean umlauts) {
	if (umlauts)
	    this.umlauts = "umlaute";
	else
	    this.umlauts = "noumlaute";
    }

    /**
     * The number of troigrams to be generated. See hy_text2MDS manual for
     * further details.
     * 
     * @param numberOfTrigrams
     */
    public void numberOfTrigrams(int numberOfTrigrams) {
	this.trigrams = numberOfTrigrams;
    }

    /**
     * Whether to interpret the word "not" or not. Only useful with open world
     * assumption. See hy_text2MDS manual for further details.
     * 
     * @param negation
     */
    public void parseNegation(boolean negation) {
	if (negation)
	    this.negation = "negation";
	else
	    this.negation = "nonegation";
    }

    /**
     * Sets open world assumption (cwa = false) or closed world assumption (cwa
     * = true). See hy_text2mds manual for further details.
     * 
     * @param cwa
     *            flag if closed or open world assumption
     */
    public void cwa(boolean cwa) {
	if (cwa)
	    this.cwa = "cwa";
	else
	    this.cwa = "owa";
    }

    /**
     * Whether to recognise strings or not. See hy_text2mds manual for further
     * details.
     * 
     * @param strings
     */
    public void recogniseStrings(boolean strings) {
	if (strings)
	    this.strings = "strings";
	else
	    this.strings = "nostrings";
    }

    /**
     * Whether to recognise numbers or not. See hy_text2mds manual for further
     * details.
     * 
     * @param numbers
     */
    public void recogniseNumbers(boolean numbers) {
	if (numbers)
	    this.numbers = "number";
	else
	    this.numbers = "nonumbers";
    }

    /**
     * Regular expression of word separators. See hy_text2mds manual for further
     * details.
     * 
     * @param wordSep
     */
    public void wordSep(String wordSep) {
	this.wordSep = wordSep;
    }

    /**
     * Skip signs and words matching the given regular expression. See
     * hy_text2mds manual for further details.
     * 
     * @param skipRegex
     *            the regular expression of words and signs to skip
     */
    public void skip(String skipRegex) {
	this.skip = skipRegex;
    }

    /**
     * Sets the minimum string length. See hy_text2mds manual for further
     * details.
     * 
     * @param minStringLength
     */
    public void minStringLength(int minStringLength) {
	this.minStringLength = minStringLength;
    }

    /**
     * Sets the maximum string length. See hy_text2mds manual for further
     * details.
     * 
     * @param maxStringLength
     */
    public void maxStringLength(int maxStringLength) {
	this.maxStringLength = maxStringLength;
    }

    /**
     * Sets the minimum word length. See hy_text2mds manual for further details.
     * 
     * @param minWordLength
     */
    public void minWordLength(int minWordLength) {
	this.minWordLength = minWordLength;
    }

    /**
     * Sets the maximum word length. See hy_text2mds manual for further details.
     * 
     * @param maxWordLength
     */
    public void maxWordLength(int maxWordLength) {
	this.maxWordLength = maxWordLength;
    }

    /**
     * The regular expression for word recognition. If this option is set,
     * wordSep() does not have any effect. See hy_text2mds manual for further
     * details.
     * 
     * @param wordRegex
     */
    public void wordRegex(String wordRegex) {
	this.wordRegex = wordRegex;
    }

    /**
     * The regular expression for number recognition. See hy_text2mds manual for
     * further details.
     * 
     * @param numberRegex
     */
    public void numberRegex(String numberRegex) {
	this.numberRegex = numberRegex;
    }

    /**
     * The regular expression for string recognition. See hy_text2mds manual for
     * further details.
     * 
     * @param stringRegex
     */
    public void stringRegex(String stringRegex) {
	this.stringRegex = stringRegex;
    }

    /**
     * The regular expression for negation recognition. See hy_text2mds manual
     * for further details.
     * 
     * @param negationRegex
     */
    public void negationRegex(String negationRegex) {
	this.negationRegex = negationRegex;
    }

    /**
     * Sets the context for the text. The context can be, e.g. the URI (Uniform
     * Resource Identifier) of a document. See hy_text2mds manual for further
     * details.
     * 
     * @param context
     *            the context
     */
    public void context(String context) {
	this.context = context;
    }

    /**
     * Same as context().
     * 
     * @param uri
     *            the URI
     */
    public void uri(String uri) {
	this.context = uri;
    }

    public void addTextFile(String filename) {
	this.textfiles.add(filename);
    }

    /**
     * Sets the woring directory, i.e. the directory in which the file term.mds
     * is written. See hy_text2mds manual for further details.
     * 
     * @param wd
     *            the working directory
     */
    public void workingDirectory(String wd) {
	this.wd = wd;
    }

    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     */
    @Override
    public void reset() {
	super.reset();
	hyper = null;
	context = null;
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
	wd = null;
	textfiles = new Vector();
    }

    /**
     * Builds the command from the parameters.
     * 
     * @return the command string array
     */
    @Override
    protected String[] buildCommand() {
	Vector commandVec = new Vector();
	commandVec.add(super.getCommand());

	if (hyper != null)
	    commandVec.add(hyper);
	if (context != null) {
	    commandVec.add("-context");
	    commandVec.add(context);
	}
	if (stemming != null)
	    commandVec.add("-" + stemming);
	int stemFileIndex = 1;
	for (Enumeration e = stemRuleFiles.elements(); e.hasMoreElements();) {
	    commandVec.add("-stem_rules" + stemFileIndex++);
	    commandVec.add(e.nextElement());
	}
	for (Enumeration e = morphemes.elements(); e.hasMoreElements();) {
	    commandVec.add("-morpheme");
	    commandVec.add(e.nextElement());
	}
	for (Enumeration e = morphemeFiles.elements(); e.hasMoreElements();) {
	    commandVec.add("-morpheme_file");
	    commandVec.add(e.nextElement());
	}
	for (Enumeration e = stopwords.elements(); e.hasMoreElements();) {
	    commandVec.add("-stopword");
	    commandVec.add(e.nextElement());
	}
	for (Enumeration e = stopwordFiles.elements(); e.hasMoreElements();) {
	    commandVec.add("-stopword_file");
	    commandVec.add(e.nextElement());
	}
	if (umlauts != null)
	    commandVec.add("-" + umlauts);
	if (trigrams > -1) {
	    commandVec.add("-trigrams");
	    commandVec.add(Integer.toString(trigrams));
	}
	if (negation != null)
	    commandVec.add("-" + negation);
	if (strings != null)
	    commandVec.add("-" + strings);
	if (numbers != null)
	    commandVec.add("-" + numbers);
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
	if (cwa != null)
	    commandVec.add("-" + cwa);
	if (wd != null) {
	    commandVec.add("-wd");
	    commandVec.add(wd);
	}
	for (Enumeration e = textfiles.elements(); e.hasMoreElements();)
	    commandVec.add(e.nextElement());
	if (stdin)
	    commandVec.add("-");

	String[] commandString = new String[commandVec.size()];
	int i = 0;
	for (Enumeration e = commandVec.elements(); e.hasMoreElements();)
	    commandString[i++] = (String) e.nextElement();
	return commandString;
    }

}
