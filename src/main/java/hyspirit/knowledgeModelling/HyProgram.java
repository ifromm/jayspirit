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
 *
 * @version $Revision: 1.1.1.1 $
 */
package hyspirit.knowledgeModelling;

import java.io.*;
import java.util.Vector;

import hyspirit.engines.HyInferenceEngine;
import hyspirit.util.HySpiritObject;

/** HyProgram provides the methods and variables common to POOL
 * (probabilistic object-oriented logic), FVPD (four-valuded
 * probabilistic Datalog), PD (probabilistic Datalog), PSQL
 * (probabilistic SQL), and PRA (probabilistic relational algebra)
 * programs.

 <p/>

 A program has a text and an inference engine to run (evaluate) the
 program. The suitable inference engine is automatically set when
 calling a constructor of the sub-classes of HyProgram.
 */
public abstract class HyProgram extends HySpiritObject {
    
    /** The inference engine for executing this program. Each
     * sub-class of HyProgram implements a method inferenceEngine() in
     * which the respective inference engine is created. For example,
     * HyPRAProgram#inferenceEngine() creates a HyPRAEngine. */
    protected HyInferenceEngine inferenceEngine;

    /** The text of this program. */
    private String text;

    // public methods
    /** Run the text of this program through the respective inference
     * engine. */
    public void run () {
	if (debug()) System.err.println(getClass() + "#run(text)");
	inferenceEngine().runProgram(text);
    }
    /** Eval the text of this program. Synonym of 'run()'. */
    public void eval () {
	run();
    }

    /** Clear the text of this program. */
    public void clear () {
	text = null;
    }


    // public attribute methods
    /** Set the text of this program. */
    public void text (String text) {
	this.text = text;
    }
    /** Get the text of this program. */
    public String text () {
	return text;
    }

    /** Set the inference engine for executing this program. */
    public void inferenceEngine (HyInferenceEngine inferenceEngine) {
	this.inferenceEngine = inferenceEngine;
    }
    /** Get the inference engine for executing this program; each
     * sub-class needs to provide this method. */
    abstract public HyInferenceEngine inferenceEngine ();
    /** Synonym of inferenceEngine(). */
    public HyInferenceEngine engine () {
	return(inferenceEngine());
    }

    // public methods that manipulate the text of this program
    /** Add the argument text to the text of this program. */
    public void addText (String text) {
	this.text = this.text + text;
    }
    /** Add the content of the argument file to the text of this program. */
    public void addFile (String fileName) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    String line;
	    if (text == null) text = "";
	    while ((line = br.readLine()) != null) {
		text = text + line + "\n";
	    }
	}
	catch (FileNotFoundException e) {
	    System.err.println("Could not find file " + fileName);
	}
	catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }
    /** Add the content of the argument files to the text of this program. */
    public void addFiles (String[] fileName) {
    }
    
}
