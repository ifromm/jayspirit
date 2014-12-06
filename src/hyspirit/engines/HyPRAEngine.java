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
 * Created on 01-Dec-2005 18:51:28
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.2 $
 */
package hyspirit.engines;

import hyspirit.knowledgeBase.HyTuple;
import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;
import hyspirit.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The HyPRA engine executes and evaluates PRA code using hy_pra. It operates
 * in a client or server mode. In client mode, a hostname and port of a running
 * hy_pra server must be given. The run() method does not have any effect in
 * client mode. In server mode, a new hy_pra process is started. You must
 * invoke the run() method after setting all required parameters.
 * <p>
 * Example:
 *  <blockquote><pre>
 *  HyPRAEngine hypra = new HyPRAEngine();
 *  hypra.readFromSTDIN();
 *  hypra.run();
 *  try {
 *    hypra.eval(testProgram);
 *    hyTuple tuple = null;
 *    while ((tuple = hypra.nextTuple()) != null) { // preferred way of reading
 *      System.out.println("> " + tuple);         // tuples!
 *    }
 *  }
 *  catch (Exception e) {
 *    e.printStackTrace(System.err);
 *  }
 *  hypra.destroy();
 * </pre></blockquote></p>
 * <p>
 * starts a hypra_process in server mode, executes PRA code (in the variable
 * <code>testProgram</code>) and reads the result tuples.
 * 
 * <b>Important</b>: Invoke <code>destroy()</code> once you don't need the object
 * any more - this will kill all spawned subthreads which might otherwise run
 * endlessly!
 * <p>
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 19-Dec-2005 18:47:54
 *
 */
public class HyPRAEngine extends HyInferenceEngine {
    private boolean verboseEval = false;
    private boolean verboseInsert = false;
    private boolean verboseConnect = false;;
    private boolean verboseTime = false;
    private boolean verboseOptimise = false;
    private boolean verboseMakeDNF = false;
    private boolean verboseQuery = false;
    private boolean verboseAlgExpr = false;
    private boolean verboseAlgEqn = false;
    private boolean verboseEventExpr = false;
    private String eval = null;
    private String assumption = null;
    private String see = null;
    private String distinct = null;
    private String optimise = null;
    private String kb = null;
    private int joinGroupSize = -1;
    private int maxNumElementsInDisjunction = -1;
    private int maxNumberOfTuples = -1;
    private Vector files = new Vector();
    private String beginExpr = null;
    private String endExpr = null;
	// private boolean stdin = false;
	public static String ENGINE_NAME = "hyp_pra";
    
 
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @throws HySpiritException if we can't determine the environment
     */
    public HyPRAEngine()
    throws HySpiritException {
        super(ENGINE_NAME);
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyPRAEngine(HySpiritProperties hyspirit)
    throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
    }

    /**
     * This constructor must be used if you want to attach to a running engine
     * process (client mode) through a socket
     * @param hostname the hostname of the engine server
     * @param port the port of the engine server
     * @throws HySpiritException if the connection failed
     */
    public HyPRAEngine(String hostname, int port)
        throws HySpiritException {
        super(ENGINE_NAME, hostname, port);
    }


    /**
     * Invoke this before you create a new HyPRAEngine object to use hyp_pra
     * instead of hy_pra
     *
     */
    public static void useHyp() {
        HyPRAEngine.ENGINE_NAME = "hyp_pra";
    }
    
    /**
     * Invoke this before you create a new HyPRAEngine object to use hy_pra
     * instead of hyp_pra (this is the default)
     *
     */
    public static void useHy() {
        HyPRAEngine.ENGINE_NAME = "hy_pra";
    }    
    
    
    /**
     * Older hy_fvpd or hy_pd engines return PRA code without a trailing ".".
     * This simple routine checks if there is a dot at the end of each line.
     * If not, a dot is appended. Assumes one PRA instruction per line. Also
     * converts "?- PROJECT" to "?- PROJECT independent", so if you assume
     * something else than independence, you need to modify this method. 
     * <br/>
     * This is a very dirty hack and should be replaced with something more
     * intelligent.
     * 
     * @param praCode the PRA code to be fixed.
     * @return the fixed PRA code.
     */
    public static String fixPRA(String praCode) {
        String fixedPRA = "";
        try {
            BufferedReader sr = new BufferedReader(new StringReader(praCode));
            String line = null;
            while ((line = sr.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("?- PROJECT")) {
                    // ?- PROJECT -> ?- PROJECT disjoint
                    line = line.substring(10);
                    line = "?- PROJECT independent" + line;
                }
                if (! line.endsWith(".")) line += ".";
                fixedPRA += line + "\n";
            }
        }
        catch (IOException io) {}
        return fixedPRA;
    }
    
    /**
     * XXX Does verbose write to STDOUT? --> Dangerous with tuples!
     * Sets or unsets the verbosity for "eval". See hy_pra manual for further
     * details.
     * @param verboseEval
     */
    public void setVerboseEval(boolean verboseEval) {
        this.verboseEval = verboseEval;
    }
    
    /**
     * Sets or unsets the verbosity for "insert". See hy_pra manual for further
     * details.
     * @param verboseInsert
     */
    public void setVerboseInsert(boolean verboseInsert) {
        this.verboseInsert = verboseInsert;
    }
    
    /**
     * Sets or unsets the verbosity for "connect". See hy_pra manual for further
     * details.
     * @param verboseConnect
     */
    public void setVerboseConnect(boolean verboseConnect) {
        this.verboseConnect = verboseConnect;
    }
    
    /**
     * Sets or unsets the verbosity for "time". See hy_pra manual for further
     * details.
     * @param verboseTime
     */
    public void setVerboseTime(boolean verboseTime) {
        this.verboseTime = verboseTime;
    }
        
    /**
     * Sets or unsets the verbosity for "pptimise". See hy_pra manual for 
     * further details.
     * @param verboseOptimise
     */
    public void setVerboseOptimise(boolean verboseOptimise) {
        this.verboseOptimise = verboseOptimise;
    }   

    
    /**
     * Sets or unsets the verbosity for "make_DNF". See hy_pra manual for 
     * further details.
     * @param verboseMakeDNF
     */
    public void setVerboseMakeDNF(boolean verboseMakeDNF) {
        this.verboseMakeDNF = verboseMakeDNF;
    }
    
    /**
     * Sets or unsets the verbosity for "Query". See hy_pra manual for further
     * details.
     * @param verboseQuery
     */
    public void setVerboseQuery(boolean verboseQuery) {
        this.verboseQuery = verboseQuery;
    }
    
    /**
     * Sets or unsets the verbosity for "AlgExpr". See hy_pra manual for further
     * details.
     * @param verboseAlgExpr
     */
    public void setVerboseAlgExpr(boolean verboseAlgExpr) {
        this.verboseAlgExpr = verboseAlgExpr;
    }
    
    
    /**
     * Sets or unsets the verbosity for "EventExpr". See hy_pra manual for
     * further details.
     * @param verboseEventExpr
     */
    public void setVerboseEventExpr(boolean verboseEventExpr) {
        this.verboseEventExpr = verboseEventExpr;
    }
    
    
    /**
     * Sets evaluation method for probability computation to 'lazy'. See
     * hy_pra manual for further details.
     *
     */
    public void evalLazy() {
        setEval("lazy"); 
    }
    
    /**
     * Sets evaluation method for probability computation to 'extensional'. See
     * hy_pra manual for further details.
     *
     */ 
    public void evalExtensional() {
        setEval("extensional");
    }

    /**
     * Sets evaluation method for probability computation to 'intensional'. See
     * hy_pra manual for further details.
     *
     */ 
    public void evalIntensional() {
        setEval("intensional");
    }

    
    /**
     * Same as evalLazy().
     *
     */
    public void rankingLazy() {
        evalLazy();
    }
    
    /**
     * Same as evalExtensional().
     *
     */
    public void rankingExtensional() {
        evalExtensional();
    }
    
    /**
     * Same as setEvalIntensional().
     *
     */
    public void rankingIntensional() {
        evalIntensional();
    }

    /**
     * Sets 'independent' assumption. See hy_pra manual for further details.
     *
     */
    public void setAssumptionIndependent() {
        this.assumption = "independent";
    }
    
    /**
     * Sets 'indcluded' assumption. See hy_pra manual for further details.
     *
     */
    public void setAssumptionIncluded() {
        this.assumption = "included";
    }
    
    /**
     * Show event expressions in standard form. See hy_pra manual for further
     * details.
     *
     */
    public void seeSTD() {
        this.see = "STD";
    }
    
    /**
     * Show event expressions in disjunctive normal form (DNF) form. See hy_pra
     * manual for further details.
     *
     */ 
    public void seeDNF() {
        this.see ="DNF";
    }
    
    /**
     * Same as seeSTD().
     *
     */
    public void showEventExpressionsSTD() {
        seeSTD();
    }
    
    /**
     * Same as seeDNF().
     *
     */
    public void showEventExpressionsDNF() {
        seeDNF();
    }
    
    /**
     * Sets or unsets distinct parameter. See hy_pra manual for further details.
     * @param distinct true or false
     */
    public void setDistinct(boolean distinct) {
        if (distinct) this.distinct = "distinct";
        else this.distinct = "nodistinct";
    }
    
        
    /**
     * Sets or unsets optimise parameter. See hy_pra manual for further details.
     * @param optimise true or false
     */
    public void setOptimise(boolean optimise) {
        if (optimise) this.optimise = "optimise";
        else this.optimise = "nooptimise";
    }

    /**
     * Determines which knowledge base to use. See hy_pra manual for further
     * details.
     * @param kb the knowledge base to use
     */
    public void useKB(String kb) {
        this.kb = kb;
    }       
    
    /**
     * Sets the join group size, which increases the efficiency of a PRA
     * evaluation. See hy_pra manual for further details.
     * @param joinGroupSize
     */
    public void setJoinGroupSize(int joinGroupSize) {
        this.joinGroupSize = joinGroupSize;
    }
    
    /**
     * Sets the maxmimum number of elements that are kept in a disjunction as a
     * result of a projection when in streaming mode. See hy_pra manual for
     * further details.
     * @param maxNum maximum number of elements in disjunction
     */
    public void setMaxNumberOfElementsInDisjunction(int maxNum) {
        this.maxNumElementsInDisjunction = maxNum;
    }

    /**
     * Sets the maxmimum number of tuples loaded. See hy_pra manual for
     * further details.
     * @param maxNum maximum number of tuples
     */
    public void setMaxNumberOfTuples(int maxNum) {
        this.maxNumberOfTuples = maxNum;
    }
    
    
    /**
     * Adds a new PRA or MDS file to be executed by hy_pra. File are executed 
     * in the order they are added with this method.
     * @param filename name of a PRA or MDS file
     */
    public void addFile(String filename) {
        this.files.add(filename);
    }
    
    
    public void beginExpression(String begin) {
        this.beginExpr = begin;
    }

    public void endExpression(String end) {
        this.endExpr = end;
    }
    
    /*
     * Set the evaluation method for probability computation. MUST be one of
     * "ex[tensional]", "in[tensional]", or "lazy". Set null to oppress this
     * parameter and set the hy_pra default. See hy_pra manual for further
     * details.
     * @param eval (out of {"ex", "extensional", "in", "intensional", "lazy"})
     */
    private void setEval(String eval) {
        this.eval = eval;
    }
    
    
    /**
     * Gets the hy_pra call (command and parameters) as it would be executed
     * @return the hy_pra call
     */
   /* public String getHyPRACall() {
        return Util.getCommand(buildCommand());
    }*/
    
    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     *
     */
    @Override
	public void reset() {
        super.reset();
        verboseEval = false;
        verboseInsert = false;
        verboseConnect = false;;
        verboseTime = false;
        verboseOptimise = false;
        verboseMakeDNF = false;
        verboseQuery = false;
        verboseAlgExpr = false;
        verboseAlgEqn = false;
        verboseEventExpr = false;
        eval = null;
        assumption = null;
        see = null;
        kb = null;
        distinct = null;
        optimise = null;
        joinGroupSize = -1;
        maxNumElementsInDisjunction = -1;
        stdin = false;
        files = new Vector();
        beginExpr = null;
        endExpr = null;
    }
    
    
    
    
    
    @Override
	protected String[] buildCommand() {
        Vector commandVec = new Vector();
        commandVec.add(super.getCommand());
        
        // verbosity
        if (verboseEval) {
            commandVec.add("-verbose");
            commandVec.add("eval");
        }
        if (verboseInsert) {
            commandVec.add("-verbose");
            commandVec.add("insert");
        }
        if (verboseConnect) {
            commandVec.add("-verbose");
            commandVec.add("connect");
        }
        if (verboseTime) {
            commandVec.add("-verbose");
            commandVec.add("time");
        }
        if (verboseOptimise) {
            commandVec.add("-verbose");
            commandVec.add("optimise");
        }
        if (verboseMakeDNF) {
            commandVec.add("-verbose");
            commandVec.add("make_DNF");
        }
        if (verboseQuery) {
            commandVec.add("-verbose");
            commandVec.add("Query");
        }
        if (verboseAlgExpr) {
            commandVec.add("-verbose");
            commandVec.add("AlgExpr");
        }
        if (verboseAlgEqn) {
            commandVec.add("-verbose");
            commandVec.add("AlgEqn");
        }
        if (verboseEventExpr) {
            commandVec.add("-verbose");
            commandVec.add("EventExpr");
        }
        if (eval != null) {
            commandVec.add("-eval");
            commandVec.add(eval);
        }
        if (assumption != null) {
            commandVec.add("-assumption");
            commandVec.add(assumption);
        }
        if (see != null) {
            commandVec.add("-see");
            commandVec.add(see);
        }
        if (kb != null) {
            commandVec.add("-kb");
            commandVec.add(kb);
        }       
        if (distinct != null) commandVec.add("-" + distinct);
        if (optimise != null) commandVec.add("-" + optimise);
        if (joinGroupSize > 0) {
            commandVec.add("-join_group_size");
            commandVec.add(Integer.toString(joinGroupSize));
        }
        if (maxNumElementsInDisjunction > 0) {
            commandVec.add("-max_number_of_elements_in_disjunction");
            commandVec.add(Integer.toString(maxNumElementsInDisjunction));
        }

        if (maxNumberOfTuples > 0) {
            commandVec.add("-max_number_of_tuples");
            commandVec.add(Integer.toString(maxNumberOfTuples));
        }

        if (beginExpr != null) {
            commandVec.add("-begin");
            commandVec.add(beginExpr);
        }
        if (endExpr != null) {
            commandVec.add("-end");
            commandVec.add(endExpr);
        }
        
        // add additional arguments
        if (super.argumentString != null) {
            StringTokenizer strTok= new StringTokenizer(super.argumentString);
            while (strTok.hasMoreTokens())
                commandVec.add(strTok.nextToken());
        }
        
        // files
        for (Enumeration e = files.elements(); e.hasMoreElements();) {
            String filename = (String)e.nextElement();
            commandVec.add(filename);
        }
        
        // STDIN
        if (stdin) {
			// commandVec.add("--");
            commandVec.add("-");
        }
    
        String[] commandString = new String[commandVec.size()];
        int i = 0;
        for (Enumeration e = commandVec.elements(); e.hasMoreElements();) 
            commandString[i++] = (String)e.nextElement();
		LOG.debug("Command string: " + commandString);
        return commandString;   
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String hsPath = "/home/ingo/HySpirit-Academic++-2.3.1";
        String wd = "/tmp";
        System.out.println("hy_pra Java Wrapper $Revision: 1.2 $\n");
        
        System.out.print("Path to HySpirit [" + hsPath + "]: ");
        String input = Util.readln();
        if (input != null && !input.trim().equals("")) hsPath = input;

        System.out.print("Working directory [" + wd + "]: ");
        input = Util.readln();
        if (input != null && !input.trim().equals("")) wd = input;      

        try {
            HySpiritProperties hyspirit =
                new HySpiritProperties(hsPath, wd);
            HyPRAEngine hypra = new HyPRAEngine(hyspirit);
            System.out.println();
            //System.out.println("1 -- Get Version");
            System.out.println("1 -- Little test program");
            System.out.println("2 -- evalFile Test");          
            System.out.print("\nYour choice: ");
            input = Util.readln();
            //if (input.equals("1")) System.out.println(hypra.getVersion());
            if (input.equals("1")) {
                String testProgram = "term(database, d1). " +
                "term(ir, d2). " +
                "qterm(ir). \n" +
                "retrieve = " +
                "UNITE(retrieve,PROJECT[$3]" +
                "(JOIN[$1=$1](qterm,term))).\n" +
                "?- PROJECT[$1](retrieve).";
                System.out.println(testProgram + "\n");
                //hypra.setVerboseQuery(true);
                hypra.readFromSTDIN();
                hypra.run();
                try {
                    hypra.eval(testProgram);
                    
                    HyTuple tuple;
                    while ((tuple = hypra.nextTuple()) != null) {
                        System.out.println("> " + tuple);
                    }                 
        
                    hypra.eval("?- PROJECT[$1](qterm).");
                    while ((tuple = hypra.nextTuple()) != null) {
                        System.out.println("> " + tuple);
                    }                       

                }
                    
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                hypra.destroy(); // important!
            }
            else if (input.equals("2")) {
                System.out.print("Filename: ");
                String filename = Util.readln();
                hypra.readFromSTDIN();
                hypra.run();
                try {
                    hypra.evalFile(filename);
                    HyTuple tuple = null;
                    while ((tuple = hypra.nextTuple()) != null) {
                        System.out.println("> " + tuple);
                    }   
        
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }               
                hypra.destroy(); // important!
            }
            else System.out.println("Goodbye! :-)");
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
