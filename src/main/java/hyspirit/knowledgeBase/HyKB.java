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
 * $Revision: 1.1.1.1 $
 */

package hyspirit.knowledgeBase;

import java.util.Vector;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hyspirit.engines.HyEngine;
import hyspirit.engines.HyInferenceEngine;
import hyspirit.engines.HyPSQLEngine; // default inference engine
import hyspirit.engines.HyAnalysisEngine;
import hyspirit.engines.HyText2KBEngine; // default analysis engine
import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritObject;
import hyspirit.util.Util;

public class HyKB extends HySpiritObject {
    
    private HyAnalysisEngine analysisEngine;
    private Vector indexes = new Vector();
    private String name;
    private HyInferenceEngine inferenceEngine; // The engine for retrieving from this knowledge base. See method "retrieve".
    
    // CONSTRUCTORS
    /** Create knowledge base with the argument name. */
    public HyKB (String name) throws IOException {
        this.name = name;
        Util.checkAndCreateDir(name);
        // Add default indexes.
        indexes.add(new HyIndex("term", 1));
    }
    /** Create knowledge base with name "HyKB". */
    public HyKB () throws IOException {
        this("HyKB");
    }
    
    
    // MAIN ATTRIBUTES
    /** Get name of this knowledge base. */
    public String name () {
        return name;
    }
    /** Set name of this knowledge base. */
    public void name (String name) {
        this.name = name;
    }
    
    
    // MAIN METHODS
    /** Add the argument document to this knowledge base. This methods
     * views the fileName as the contextName, and calls
     * addDocument(fileName,fileName). */
    public void addDocument (String fileName) {
        addDocument(fileName, fileName);
    }
    /** Add the argument document to this knowledge base and apply the
     * argument context name. */
    public void addDocument (String fileName, String contextName) {
        if (debug())
            System.err.println
            (getClass() + "#addDocument" +
                    "(" + fileName + "," + contextName + ")");
        //hy_data2kb.setDebug();
        analysisEngine().context(contextName);
        analysisEngine.runAndWait(fileName);
    }
    /** Add the argument array of documents to this knowledge base. */
    public void addDocuments (String[] fileNames) {
        for (int i = 0; i < fileNames.length; i++) {
            addDocument(fileNames[i]);
        }
    }
    /** Add the argument vector of documents to this knowledge base. */
    public void addDocuments (Vector fileNames) {
    }
    /** Add the document tree in the argument directory to this
     * knowledge base. */
    public void addDocumentTree (String dirName) {
        if (debug())
            System.err.println
            (getClass() + "#addDocumentTree(" + dirName + ")");
        analysisEngine().runAndWait(dirName);
    }
    /** Update the index of this knowledge base. */
    public void updateIndex () {
        for (Enumeration e = indexes.elements(); e.hasMoreElements();) {
            HyIndex index = (HyIndex) e.nextElement();
            index.update();
        }
    }
    
    /** Get the term space vector of this knowledge base. For a large
     * term space, this might cause a problem. Use termSpaceReader for
     * working with large term sapces. */
    public Vector termSpaceVector () {
        Vector termSpace = new Vector();
        termSpace.add("0.9 (sailing)");
        termSpace.add("0.8 (boats)");
        return termSpace;
    }
    /** Get the term space reader of this knowledge base. Read the
     * terms with termSpaceReader.readLine(). This stream-based access
     * to the term space is suitable for processing large term
     * spaces. */
    public BufferedReader termSpaceReader () {
        try {
            Process process = Runtime.getRuntime().exec("hy_pricat");
            return
            (new BufferedReader
                    (new InputStreamReader(process.getInputStream())));
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }
    
    /** Retrieve from this knowledge base. For retrieval, call the
     * retrieve method of the previously set inference engine. If no
     * inference engine has been set, then a HyPSQLEngine will be
     * used. */
    public void retrieve () {
        retrieve(inferenceEngine());
    }
    /** Retrieve from this knowledge base. For retrieval, call the
     * retrieve method of the argument inference engine. */
    public void retrieve (HyInferenceEngine inferenceEngine) {
        inferenceEngine.retrieve();
    }
    /** Get the inference engine for reasoning in this knowledge base. */
    public HyInferenceEngine inferenceEngine () {
        try {
            if (inferenceEngine == null) inferenceEngine = new HyPSQLEngine();
        }
        catch (HySpiritException he) {}
        return inferenceEngine;
    }
    /** Set the inference engine for reasoning in this knowledge base. */
    public void inferenceEngine (HyInferenceEngine inferenceEngine) {
        this.inferenceEngine = inferenceEngine;
    }
    /** Get the analysis engine for adding documents to this knowledge base. */
    public HyAnalysisEngine analysisEngine () {
        if (analysisEngine == null) {
            try {
                analysisEngine = new HyText2KBEngine();
                analysisEngine.kb(this);
            }
            catch (HySpiritException hs) {}
        }
        return analysisEngine;
    }
    /** Set the analysis engine for adding documents to this knowledge base. */
    public void analysisEngine (HyAnalysisEngine analysisEngine) {
        this.analysisEngine = analysisEngine;
    }
    
}
