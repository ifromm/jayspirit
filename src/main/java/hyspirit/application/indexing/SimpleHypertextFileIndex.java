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
 * Created on 04-Apr-2005 16:09:33.  Moved to JaySpirit 19-Feb-2006.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1 $
 */
package hyspirit.application.indexing;

import hyspirit.util.*;
import hyspirit.engines.*;
//import de.unidu.is.text.*;

import java.io.*;
import java.util.*;


/**
 * This class can be used to index hypertexts, using the directory index for
 * full texts as offered by HySpirit V2.x. Based on the indexing directory,
 * a subdirectories "HyKB" and "index" are created, storing the knowledge
 * base and the full text index, respectively. This implementation does not
 * support any link types and attributes (this is why it is called "simple").
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 04-Apr-2005 16:09:33
 *
 */
public class SimpleHypertextFileIndex implements HypertextIndex {
    protected HySpiritProperties hyspirit = null;
    protected String indexBaseDir = null;
    protected String knowledgebaseDir = null;
    protected String indexDir = null;
    protected static final String KB_DIR = "HyKB";
    protected static final String IDX_DIR = "index";
    protected static final String TERM_MDS = "term.mds";
    protected static final String TF_MDS = "tf.mds";	
    protected static final String IDF_MDS = "idf.mds";
    protected static final String LINK = "link.mds";
    protected static final String TERM_FREQ = "term_tf.freq";
    protected static final String DOC_FREQ = "term_df.freq";	
    protected static final String INSTANCE_OF = "instance_of.mds";
    protected static final String ATTRIBUTE = "attribute.mds";
    protected static final String CONNECTOR = "connector";
    protected String stopwordFile = null;
    protected String morphemeFile = null;
    protected String termMDSFile = null;
    protected String tfMDSFile = null;
    protected String idfMDSFile = null;
    protected String termTFFile = null;
    protected String termIDFFile = null;
    protected String connectorFile = null;
    private HashSet linkStrings = new HashSet();
    private HashSet attributeStrings = new HashSet();
    private HashSet instanceStrings = new HashSet();
    private String linkMDSFile = null;
    private String attributeMDSFile = null;
    private String instanceMDSFile = null;
    private boolean useCache = false;
    private HashSet indexedDocumentsCache = null;
    private HashSet indexedLinksCache = null;
    private int windowSize = 1000;
    protected HyText2PoolFilter filter = null;
    private ArrayList keyelems = null;
    private int tf_keys = 0;
    private int tf_tuples = 0;
    private int idf_keys = 0;
    private int idf_values = 0;
    private HashMap termIDF = new HashMap();
    private HashMap docList = new HashMap();
    private boolean verbose = false;
    
    
    /*
     *Codes for several norms for tf. See description of DocTermList class!
     */
    protected static final int TF_SUM_NORM = 1;
    protected static final int TF_MAX_NORM = 2;
    protected static final int TF_SMART_NORM = 3;
    protected static final int TF_APOISSON_AVG = 4;
    protected int tfNorm = TF_MAX_NORM;
    
    
    // whether to write term.mds or not
    private boolean writeTermMDSFile = true;
    
    
    /**
     * Constructor of class. Needs a HySpirit object containing all 
     * necessary information about the HySpirit implementation on your
     * machine, and the base directory of the index. If the base directories
     * and/lor all its subdirectories do not exist, they are created.
     * @param hyspirit the HySpirit object, containing all necessary environment
     * information for the HySpirit installation.
     * @param indexBaseDir the (absolute) index directory
     */
    public SimpleHypertextFileIndex(HySpiritProperties hyspirit,
            String indexBaseDir) {
        this.hyspirit = hyspirit;
        this.indexBaseDir = indexBaseDir;
        
        createDirs();
        
        //this.termMDSFile = knowledgebaseDir + File.separator + TERM_MDS;
        this.tfMDSFile = knowledgebaseDir + File.separator + TF_MDS;
        this.idfMDSFile = knowledgebaseDir + File.separator + IDF_MDS;
        this.termMDSFile = knowledgebaseDir + File.separator + TERM_MDS;    	
        this.linkMDSFile = knowledgebaseDir + File.separator + LINK;
        this.attributeMDSFile = knowledgebaseDir + File.separator + ATTRIBUTE;
        this.instanceMDSFile = knowledgebaseDir + File.separator + INSTANCE_OF;
        this.termTFFile =  knowledgebaseDir + File.separator + TERM_FREQ;
        this.termIDFFile =  knowledgebaseDir + File.separator + DOC_FREQ;
        this.connectorFile = knowledgebaseDir + File.separator + CONNECTOR;
        
        
    }
    
    /**
     * Returns the connector string for hy_pra etc.
     * @return the connector string
     */
    public String getConnector() {
        return "_SQL_connect(tf, \"hy_dir_connect -datasource " + indexDir + 
        " -socket tf\")\n" +
        "_SQL_connect(idf, \"hy_dir_connect -datasource " + indexDir +
        " -socket idf\")\n" +
        "_SQL_connect(instance_of, \"hy_dir_connect -datasource " + 
        indexDir + " -socket instance_of\")\n" +
        "_SQL_connect(attribute, \"hy_dir_connect -datasource " +
        indexDir + " -socket attribute\")\n" +
        "_SQL_connect(link, \"hy_dir_connect -datasource " +
        indexDir + " -socket link\")\n";							   
    }
    
    
    /**
     * Gets the HySpirit environment
     * @return the HySpirit environment
     */
    public HySpiritProperties getEnvironment() {
        return this.hyspirit;
    }
    
    /**
     * Whether or not to write term.mds. Default: Write it.
     * @param termmds Flag if term.mds should be written.
     */
    public void writeTermMDSFile(boolean termmds) {
        this.writeTermMDSFile = termmds;
    }
    
    /**
     * Adds an indexing object, which represents a document to be indexed.
     * @param idx the indexing obect
     */
    public void addIndexingObject(IndexingObject idx){
        if (idx != null) {
            try {
                // start filter if not already running
                if (filter == null)
                    filter = new HyText2PoolFilter(hyspirit, true, stopwordFile,
                            morphemeFile);
                
                
                String fulltext = idx.fulltext();
                String uri = idx.documentURI();
                
                /*
                 * document type
                 */
                addDocument(uri, idx.documentType());
                
                /*
                 * Attribute-value pairs
                 */
                Set aNames = idx.attributeNames();
                if (aNames != null) {
                    for (Iterator ita = aNames.iterator(); ita.hasNext();) {
                        String aName = (String) ita.next();
                        Set aValues =  idx.attributeValues(aName);
                        for (Iterator it2 = aValues.iterator();
                        it2.hasNext();) {
                            String aValue = (String)it2.next();
                            addAttribute(uri, aName, aValue);
                        }
                    }
                }	    
                
                /*
                 * fulltext: update frequency information
                 */
                if (fulltext != null) {
                    fulltext = fulltext.toLowerCase();
                    Iterator terms = filter.filterIt(fulltext);
                    if (terms != null) {
                        DocTermList docTermList = new DocTermList();
                        while (terms.hasNext())  {
                            String term = (String)terms.next();
                            
                            // collect distinct terms for document, update 
                            // statistics
                            if (docTermList.addTerm(term) == 1) {
                                // a new term for this documents
                                tf_keys++;
                                
                                // update idf value
                                Integer docFreq = (Integer)termIDF.get(term);
                                if (docFreq == null) {
                                    docFreq = new Integer(1);
                                }
                                else docFreq =
                                    new Integer((docFreq.intValue()) + 1);
                                termIDF.put(term, docFreq);
                            }
                            tf_tuples++;
                        }
                        docList.put(uri, docTermList);
                    }
                }
            }
            
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }	    
    
    /*
     * Creates a frequency key from key elements.
     */
    protected String createKey(List keyelems) {
        String key = null;
        if (keyelems != null) {
            key = "(";
            boolean first = true;
            for (Iterator it = keyelems.iterator(); it.hasNext();){
                if (first) first = false;
                else key += ",";
                key += "\"" + (String)it.next() + "\"";
            }
            key += ")";
            
        }
        return key;
    }
    
    
    /*
     * Returns the prefix for frequency files
     */
    protected String getPrefix(int keys, int tuples, String type) {
        String prefix = "#! number of keys: " + keys + "\n"; 
        prefix += "#! number of " + type + ": " + tuples + "\n";
        prefix += "#! average number of " + type + ": " 
        + (float)tuples/(float)keys;
        return prefix;
    }
    
    /*
     * Add a document with the given URI and docuemnt type to the index.
     * @param documentURI - the Uniform Resource Identifier of the document
     * @param documentType - the type the document is instance of
     */
    protected void addDocument(String documentURI, String documentType) {
        instanceStrings.add("(\"" + documentURI + "\"," + 
                documentType + ")");		
    }
    
    
//  /*
//  * Henrik's filter
//  */    
//  private Iterator filterText(String text) {
//  StemmerFilter stemmerFilter = 
//  new StemmerFilter(
//  new StopwordFilter(
//  new LowercaseFilter(
//  new WordSplitterFilter(null,0)
//  )
//  , stopwordFile)
//  );
//  return stemmerFilter.apply(Util.convert(text));
//  }
    
    /**
     * Adds an attribute-value pair to be indexed for the given document.
     * @param documentURI the document URI
     * @param attributeName the name of the attribute
     * @param attributeValue the attribute value
     */
    protected void addAttribute(String documentURI, String attributeName,
            String attributeValue) {
        attributeStrings.add("(" + attributeName + ",\"" + 
                documentURI + "\",\"" + attributeValue + "\")");		
    }
    
    
    
    /**
     * Adds a hyperlink between two documents. 
     * @param sourceDocumentURI the URI of the link source
     * @param destinationDocumentURI the URI of the link destination
     * @param linktype the link type
     */
    public void addHyperlink(String sourceDocumentURI,
            String destinationDocumentURI,
            String linktype) {
        linkStrings.add("(\"" + sourceDocumentURI + "\",\"" + 
                destinationDocumentURI + "\"," + linktype + ")");	 	
    }
    
    
    /**
     * Returns true if the document was already indexed
     * @param documentURI
     * @return true if the document was already indexed, false elsewhere
     */
    public boolean isIndexed(String documentURI) {
        boolean isIndexed = false;
        // cache lookup, if cache is not there, create it
        if (useCache) {
            if (indexedDocumentsCache != null) 
                isIndexed = indexedDocumentsCache.contains(documentURI);
            else {
                indexedDocumentsCache = new HashSet();
                try {
                    String line = null;
                    BufferedReader br =
                        new BufferedReader(new FileReader(instanceMDSFile));
                    while ((line = br.readLine()) != null) {
                        line = line.replace('(', ' ');
                        line = line.replace(')', ' ');
                        String token[] = line.split(",");
                        line = token[0];
                        line = line.replace('"', ' ');
                        line = line.trim();
                        indexedDocumentsCache.add(line);
                        if (line.equals(documentURI)) isIndexed = true;
                    }
                    br.close();
                }
                catch(FileNotFoundException fnf) {}
                catch(IOException io){
                    io.printStackTrace(System.err);
                }
            }
        }
        else {
            try {
                String line = null;
                BufferedReader br =
                    new BufferedReader(new FileReader(instanceMDSFile));
                while ((line = br.readLine()) != null) {
                    line = line.replace('(', ' ');
                    line = line.replace(')', ' ');
                    String token[] = line.split(",");
                    line = token[0];
                    line = line.replace('"', ' ');
                    line = line.trim();
                    if (line.equals(documentURI)) {
                        isIndexed = true;
                        break;
                    }
                }
                br.close();
            }
            catch (FileNotFoundException fnf) {}
            catch (IOException io) {
                io.printStackTrace(System.err);
            }           
        }
        return isIndexed;
    }
    
    
    /**
     * Returns true if the typed link already exists in the index
     * @param sourceURI the URI of the link source
     * @param destinationURI the URI of the link destination
     * @param linktype the link type
     * @return true if the typed link exists in the index, false elsewhere
     */
    public boolean isIndexed(String sourceURI, String destinationURI,
            String linktype) {
        boolean isIndexed = false;
        String linkString = "(\"" + sourceURI + "\",\"" + 
        destinationURI + "\"," + linktype + ")";
        // cache lookup, if cache is not there, create it
        if (useCache) {
            if (indexedDocumentsCache != null) 
                isIndexed = indexedDocumentsCache.contains(linkString);
            else {
                indexedDocumentsCache = new HashSet();
                try {
                    String line = null;
                    BufferedReader br =
                        new BufferedReader(new FileReader(linkMDSFile));
                    while ((line = br.readLine()) != null) {
                        indexedDocumentsCache.add(line);
                        if (line.equals(linkString)) isIndexed = true;
                    }
                    br.close();
                }
                catch(FileNotFoundException fnf) {}
                catch(IOException io){
                    io.printStackTrace(System.err);
                }
            }
        }
        else {
            try {
                String line = null;
                BufferedReader br =
                    new BufferedReader(new FileReader(linkMDSFile));
                while ((line = br.readLine()) != null) {
                    if (line.equals(linkString)) {
                        isIndexed = true;
                        break;
                    }
                }
                br.close();
            }
            catch (FileNotFoundException fnf) {}
            catch (IOException io) {
                io.printStackTrace(System.err);
            }           
        }
        return isIndexed;
    }
    
    
    /**
     * Whether or not use a cache for the isIndexed() method. If caching is
     * set, all relevant information is read into memory, which can be time
     * and memory consuming; but next time isIndexed is invoked, the cache is
     * used, resulting in faster lookup times. For many invocations if
     * isIndexed, better use a cache.
     *  
     * @param useCache whether or not to use the cache.
     */
    public void useCache(boolean useCache) {
        this.useCache = useCache;
    }
    
    
    /**
     * Use sum norm for tf calculation
     * @see DocTermList
     *
     */
    public void useTFSumNorm() {
        tfNorm = TF_MAX_NORM;
    }
    
    
    /**
     * Use max norm for tf calculation
     * @see DocTermList
     *
     */
    public void useTFMaxNorm() {
        tfNorm = TF_MAX_NORM;
    }
    
    /**
     * Use SMART norm for tf calculation
     * @see DocTermList
     */
    public void useTFSMARTNorm() {
        tfNorm = TF_SMART_NORM;
    }
    
    /**
     * Use APoisson average tf weight for tf calculation
     * @see DocTermList
     */  
    public void useTFAPoissonWeight() {
        tfNorm = TF_APOISSON_AVG;
    }
    
    
    
    
    /*
     * Indexes the hyperlinks.
     */
    private void indexHyperlinks() {
        try {
            // write collected stuff
            writeToFile(linkMDSFile, linkStrings);
            
            // write to index directory
            HyMDS2DirEngine hymds2dir = new HyMDS2DirEngine(hyspirit);
            hymds2dir.addMDSFile(linkMDSFile);
            hymds2dir.addColumn(1);	// indexkey: source URI
            hymds2dir.run();
            hymds2dir.reset();
            hymds2dir.addMDSFile(linkMDSFile);
            hymds2dir.addColumn(2);	// indexkey: destination URI
            hymds2dir.run();
            hymds2dir.reset();
            hymds2dir.addMDSFile(linkMDSFile);
            hymds2dir.addColumn(3);	// indexkey: link type
            hymds2dir.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Indexes the attribute-value pairs.
     */
    private void indexAttributes() {
        try {
            // write collected stuff       
            writeToFile(attributeMDSFile, attributeStrings);
            
            // write to index directory
            HyMDS2DirEngine hymds2dir = new HyMDS2DirEngine(hyspirit);
            hymds2dir.addMDSFile(attributeMDSFile);
            hymds2dir.addColumn(1);	// indexkey: attribute name
            hymds2dir.run();
            hymds2dir.reset();
            hymds2dir.addMDSFile(attributeMDSFile);
            hymds2dir.addColumn(2);	// indexkey: URI
            hymds2dir.run();
            hymds2dir.reset();
            hymds2dir.addMDSFile(attributeMDSFile);
            hymds2dir.addColumn(3);	// indexkey: attributeValue
            hymds2dir.run(); 
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /*
     * Indexes the document types (instance_of)
     */
    private void indexDocumentTypes() {
        try {
            writeToFile(instanceMDSFile, instanceStrings);
            HyMDS2DirEngine hymds2dir = new HyMDS2DirEngine(hyspirit);
            hymds2dir.addMDSFile(instanceMDSFile);
            hymds2dir.addColumn(1);	// indexkey: URI
            hymds2dir.run();
            hymds2dir.reset();
            hymds2dir.addMDSFile(instanceMDSFile);
            hymds2dir.addColumn(2);	// indexkey: document type
            hymds2dir.run();}
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Incrementally indexes the full texts. What happens here depends on the
     * indexing strategy and the backing storage. Usually, tfxidf-weights are
     * calculated for the full text and stored in the index.
     */
    private void indexFullText() {    	
        try {
            /*
             * TODO Incremental indexing: 
             * 1. read term_tf.freq. Update internal structure; if document
             * exists in term_tf.freq nd docList, take values from docList.
             * Otherwise append to docList. Update tf and idf statistics.
             * Write tf and idf frequency files.
             */
            
            
            /*
             * Write term frequencies and tf values
             */
            BufferedWriter tfFile = new BufferedWriter(
                    new FileWriter(termTFFile));
            BufferedWriter tmdsFile = null;
            if (writeTermMDSFile) tmdsFile =  new BufferedWriter(
                    new FileWriter(termMDSFile));
            BufferedWriter termWeightFile =
                new BufferedWriter(new FileWriter(tfMDSFile));
            tfFile.write(getPrefix(tf_keys, tf_tuples, "tuples"));
            tfFile.newLine();
            tfFile.flush();
            for (Iterator it = docList.keySet().iterator() ; it.hasNext();) {
                String uri = (String) it.next();
                DocTermList docterms = (DocTermList)docList.get(uri);
                for (Iterator it2 = docterms.docTerms().iterator();
                it2.hasNext(); ) {
                    TermFreq tf = (TermFreq) it2.next();
                    try {
                        ArrayList tflist = new ArrayList(2);
                        tflist.add(tf.term());
                        tflist.add(uri);
                        String key = createKey(tflist);
                        tfFile.write(tf.frequency() + " " + key);
                        tfFile.newLine();
                        
                        // get the actual term weight
                        float termweight = 0;
                        switch (tfNorm) {
                        case TF_MAX_NORM:
                            termweight = docterms.maxNTF(tf.term());
                            break;
                        case TF_SUM_NORM:
                            termweight = docterms.sumNTF(tf.term());
                            break;
                        case TF_APOISSON_AVG:
                            termweight = docterms.APoissonWeight(tf.term());
                            break;
                        case TF_SMART_NORM:
                            termweight = docterms.smartNTF(tf.term());
                        }
                        
                        termWeightFile.write(termweight + " " + key);
                        termWeightFile.newLine();
                        
                        if (writeTermMDSFile) {
                            for (int i=0; i < tf.frequency(); i++) {
                                tmdsFile.write(key);
                                tmdsFile.newLine();
                            }
                        }
                    }
                    catch (NullPointerException ne) {
                        System.err.println("Warning: Null Pointer Exception " +
                        "caught!");
                        System.err.println("\tURI: " + uri);
                        System.err.println("\tTermFreq tf: " + tf);
                    }
                }
                tfFile.flush();
                if (writeTermMDSFile) tmdsFile.flush();
            }
            tfFile.close();
            if (writeTermMDSFile) tmdsFile.close();
            
            
            /*
             * Write document frequencies
             */
            idf_values = docList.size(); // the number of documents
            idf_keys = termIDF.size(); // the number of terms
            
            
            // get minumum document frequency
            int minDocFreq = 0;
            String minDocFreqTerm = null;
            Set keys = termIDF.keySet();
            for (Iterator it = keys.iterator(); it.hasNext();) {
                String term = (String)it.next();
                int value = ((Integer)termIDF.get(term)).intValue();
                if (minDocFreq == 0 || value < minDocFreq) {
                    minDocFreq = value;
                    minDocFreqTerm = term;
                }
            }
            termIDF.remove(minDocFreqTerm);
            
            BufferedWriter dfFile = new BufferedWriter(
                    new FileWriter(termIDFFile));
            dfFile.write(getPrefix(idf_keys, idf_values, "values"));
            dfFile.newLine();
            ArrayList idflist = new ArrayList(1);
            idflist.add(minDocFreqTerm);
            String key = createKey(idflist);    
            dfFile.write(minDocFreq + " " + key);
            dfFile.newLine();
            dfFile.flush();
            keys = termIDF.keySet();
            for (Iterator it = keys.iterator(); it.hasNext();) {
                String term = (String)it.next();
                int value = ((Integer)termIDF.get(term)).intValue();
                idflist = new ArrayList(1);
                idflist.add(term);
                key = createKey(idflist);               
                dfFile.write(value + " " + key);
                dfFile.newLine();
            }
            dfFile.close();
            termIDF.put(minDocFreqTerm, new Integer(minDocFreq)); //just in case
        }
        catch (IOException io) {
            io.printStackTrace(System.err);
        }
        
        
        
        try {
            /*
             * Calculate inverse document frequency (idf), using max idf norm
             * hy_freq2mds term_df.freq | hy_mds2mds -norm max_idf -max_stream - 
             * 
             */
            
            HyFreq2MDSEngine hyfreq2mds = new HyFreq2MDSEngine(this.hyspirit);
            hyfreq2mds.addMDSFile(termIDFFile);
            
            HyMDS2MDSEngine hymds2mds = new HyMDS2MDSEngine(this.hyspirit);
            hymds2mds.useMaxIDFNorm();
            hymds2mds.maxStream(true);
            hymds2mds.readFromSTDIN();
            
            hyfreq2mds.start();
            hyfreq2mds.waitTillRunning();
            hymds2mds.start();
            hymds2mds.waitTillRunning();
            
            StreamGobbler st = new StreamGobbler(hyfreq2mds.getSTDOUT(), 
                    hymds2mds.getSTDIN());
            st.start();
            
            // STDERR to System.err
            StreamGobbler st2 = new StreamGobbler(hyfreq2mds.getSTDERR(), 
                    System.err);
            st2.start();
            BufferedWriter fileIn =
                new BufferedWriter(new FileWriter(idfMDSFile));
            StreamGobbler st3 = new StreamGobbler(hymds2mds.getSTDOUT(), 
                    fileIn);
            st3.start();
            StreamGobbler st4 = new StreamGobbler(hymds2mds.getSTDERR(), 
                    System.err);
            st4.start();
            
            // wait for completion
            try {
                hymds2mds.waitFor();
            }
            catch (InterruptedException ie) {
                ie.printStackTrace(System.err);
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        
        /*
         * write to index directory
         */
        try {
            HyMDS2DirEngine hymds2dir = new HyMDS2DirEngine(hyspirit);
            hymds2dir.addMDSFile(tfMDSFile);
            hymds2dir.addColumn(1);	// indexkey: terms
            hymds2dir.run();
            
            hymds2dir.reset();
            hymds2dir.addMDSFile(tfMDSFile);
            hymds2dir.addColumn(2);	// indexkey: documents
            hymds2dir.run();
            
            hymds2dir.reset();
            hymds2dir.addMDSFile(idfMDSFile);
            hymds2dir.addColumn(1);
            hymds2dir.run();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Indexes everything based on the knowledge base. Erases the index before
     * beginning to index new. Writes the connector string to file.
     *
     */
    public void index() {
        index(true);
    }
    
    /**
     * Indexes everything based on the knowledge base. Writes the connector 
     * string to file.
     * @param erase Whether to erase the index before indexing, or not.
     */
    public void index(boolean erase) {
        if (erase) eraseDirAndFiles(indexDir);
        
        indexDocumentTypes();
        indexAttributes();
        indexHyperlinks(); 
        indexFullText();
        
        // write connector file
        try {
            BufferedWriter fb = 
                new BufferedWriter(
                        new FileWriter(connectorFile));
            fb.write(getConnector());
            fb.flush();
            fb.close();
        }
        catch (IOException io) {
            io.printStackTrace(System.err);
        }
        
        // close filter
        if (filter != null) {
            filter.close();
            filter = null;
        }
    }
    
    
    /**
     * Totally erases the index by deleting the index base dir and its
     * subdirectories.
     *
     */
    public void erase() {
        eraseDirAndFiles(this.indexBaseDir);
        createDirs();
    }
    
    /**
     * If you want to remove stopwords, give a file containing a stopword list
     * here. Each entry must be lower case.
     * @param stopwordFile the absolute filename of the stopword list file
     */
    public void stopwordFile(String stopwordFile){
        this.stopwordFile = stopwordFile;
    }
    
    /**
     * If you want some terms not to be stemmed, give a file containing a 
     * morpheme list here. Each entry must be lower case.
     * @param morphemeFile the absolute filename of the morpheme list file
     */
    public void morphemeFile(String morphemeFile){
        this.morphemeFile = morphemeFile;
    }    
    
    /*
     * Deletes the given directory and all its subdirectories.
     */  
    protected void eraseDirAndFiles(String dirName) {
        eraseDirAndFiles(new File(dirName));
    }
    
    
    /*
     * Deletes the given directory and all its subdirectories.
     */
    protected void eraseDirAndFiles(File dir) {
        try {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) eraseDirAndFiles(files[i]);
                else files[i].delete();
            }
            dir.delete();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /*
     * Deletes the give file.
     */
    private static void deleteFile(String filename) {
        new File(filename).delete();
    }
    
    /*
     * Writes (append) String set s to the given file. If the file does not 
     * exist it is created.
     */
    protected static void writeToFile(String filename, Set s) {
        if (filename != null && s != null) {
            try {
                // write outfile
                File outfile = new File(filename);
                //if (!outfile.exists()) outfile.createNewFile();
                FileWriter fw = new FileWriter(outfile, true);
                for (Iterator it = s.iterator(); it.hasNext();) {
                    String sOut = (String)it.next();
                    fw.write(sOut + "\n");
                }
                fw.flush();
                fw.close();
            }
            catch (IOException io) {
                io.printStackTrace(System.err);
            }
        }
    }
    
    
    /*
     * check if directories exist, create directories if necessary
     */
    protected void createDirs() {
        File indexBase = new File(indexBaseDir);
        if (!indexBase.exists()) indexBase.mkdirs();
        this.knowledgebaseDir = indexBaseDir + File.separator + KB_DIR;
        File knowledgeBase =
            new File(this.knowledgebaseDir);
        if (!knowledgeBase.exists()) knowledgeBase.mkdir();
        this.indexDir = indexBaseDir + File.separator + IDX_DIR;
        File idxDir =
            new File(this.indexDir);
        if (!idxDir.exists()) idxDir.mkdir();
    }
    
    
    
    public static void main(String[] args) {
        String hsPath = "/home/ingo/HySpirit-Academic++-2.3.1";
        String indexdir = "/home/ingo/zdnet/testindex";
        System.out.println("SimpleHypertextFileIndex $Revision: 1.1 $\n");
        
        System.out.print("Path to HySpirit [" + hsPath + "]: ");
        String input = Util.readln();
        if (input != null && !input.trim().equals("")) hsPath = input;
        
        System.out.print("Indexing directory [" + indexdir + "]: ");
        input = Util.readln();
        if (input != null && !input.trim().equals("")) indexdir = input;		
        
        HySpiritProperties hyspirit = new HySpiritProperties(hsPath, indexdir);
        
        System.out.println();
        System.out.println("1 -- Test constructor");
        System.out.println("2 -- Add Full Text to index");
        System.out.println("3 -- Add document to index");
        System.out.println("4 -- Add attribute-value-pair to index");
        System.out.println("5 -- Add link to index");
        System.out.println("6 -- Add indexing object");
        System.out.println("7 -- Check document indexed");
        System.out.println("8 -- Check link indexed");
        System.out.println("9 -- Get connector");
        System.out.println("0 -- Erase Directory Index");
        System.out.println("i -- Index KB");
        System.out.print("\nYour choice: ");
        input = Util.readln();
        if (input.equals("1")) 		
            new SimpleHypertextFileIndex(hyspirit, indexdir);
        else if (input.equals("2")) {
            /*
             String stopwords = "/home/ingo/zdnet/hyspirit/stopwords_eng.txt";
             System.out.print("Stopword List [" + stopwords +"]: ");
             String slist = Util.readln();
             if (slist == null || slist.equals("")) slist = stopwords;
             
             SimpleHypertextFileIndex idx =
             new SimpleHypertextFileIndex(hyspirit, indexdir);
             idx.stopwordFile(slist);
             System.out.print("URI: ");
             String uri = Util.readln();
             System.out.println("Insert text:");
             String fulltext = Util.readln();
             idx.addFullText(uri, fulltext);
             idx.indexFullText();
             */
        }
        else if (input.equals("3")) {
            SimpleHypertextFileIndex idx =
                new SimpleHypertextFileIndex(hyspirit, indexdir);
            System.out.print("URI: ");
            String uri = Util.readln();
            System.out.print("Document type: ");
            String doctype = Util.readln();			
            idx.addDocument(uri, doctype);
            idx.indexDocumentTypes();
        }
        else if (input.equals("4")) {
            SimpleHypertextFileIndex idx =
                new SimpleHypertextFileIndex(hyspirit, indexdir);
            System.out.print("URI: ");
            String uri = Util.readln();
            System.out.print("Attribute Name: ");
            String name = Util.readln();
            System.out.print("Attribute Value: ");
            String  value = Util.readln();
            idx.addAttribute(uri, name, value);
            idx.indexAttributes();
        }
        else if (input.equals("5")) {
            SimpleHypertextFileIndex idx =
                new SimpleHypertextFileIndex(hyspirit, indexdir);
            System.out.print("Source URI: ");
            String source = Util.readln();
            System.out.print("Destination URI: ");
            String dest = Util.readln();
            System.out.print("Type: ");
            String type = Util.readln();
            idx.addHyperlink(source, dest, type);
            idx.indexHyperlinks();
        }
        else if (input.equals("6")) {
            SimpleHypertextFileIndex idx =
                new SimpleHypertextFileIndex(hyspirit, indexdir);
            System.out.print("Document URI: ");
            String uri = Util.readln();
            System.out.print("Document Type: ");
            String type = Util.readln();
            IndexingObject idxObject = new IndexingObject(uri, type);
            System.out.print("Attribute Name: ");
            String aName = Util.readln();
            System.out.println("Attribute Value(s), comma separated: ");
            String aValues = Util.readln();
            String[] tokens = aValues.split(",");
            for (int i = 0; i < tokens.length; i++) 
                idxObject.addAttribute(aName, tokens[i].trim());
            System.out.println("Text:");
            String fulltext = Util.readln();
            idxObject.setFulltext(fulltext);
            idx.addIndexingObject(idxObject);
            idx.index();
        }
        else if (input.equals("7")) {
            SimpleHypertextFileIndex idx =
                new SimpleHypertextFileIndex(hyspirit, indexdir);
            System.out.print("Document URI: ");
            String uri = Util.readln();
            if (idx.isIndexed(uri)) System.out.println(uri + " is indexed.");
            else System.out.println(uri + " is not indexed.");		
        }
        else if (input.equals("8")) {
            SimpleHypertextFileIndex idx =
                new SimpleHypertextFileIndex(hyspirit, indexdir);
            System.out.print("Source URI: ");
            String sourceURI = Util.readln();
            System.out.print("Destination URI: ");
            String destinationURI = Util.readln();
            System.out.print("Link type: ");
            String type = Util.readln();
            if (idx.isIndexed(sourceURI, destinationURI, type)) 
                System.out.println("Link is indexed.");
            else System.out.println("Link is not indexed.");		
        }		
        else if (input.equals("9")) {
            SimpleHypertextFileIndex idx =
                new SimpleHypertextFileIndex(hyspirit, indexdir);
            System.out.print(idx.getConnector());
        }
        else if (input.equals("0")) 
            new SimpleHypertextFileIndex(hyspirit, indexdir).erase();
        else if (input.equals("i")) 
            new SimpleHypertextFileIndex(hyspirit, indexdir).index();		
        else System.out.println("Goodbye! :-)");
    }
}

