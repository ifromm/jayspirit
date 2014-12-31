package hyspirit.engines;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import hyspirit.util.HySpiritException;
import hyspirit.util.HySpiritProperties;

public class HyFVPDatalogEngine extends HyInferenceEngine {
    private String eval = null;
    private String cwa = null;
    private String dynamic = null;
    private String CWAPostfix = null;
    private String OWAPostfix = null;
    private String posneg = null;
    private String posPrefix = null;
    private String negPrefix = null;
    private String to = null;
    private String hyPRAOpts = null;
     
    private static final String ENGINE_NAME = "hy_fvpd";
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @throws HySpiritException if we can't determine the environment
     */
    public HyFVPDatalogEngine () throws HySpiritException{
        super(ENGINE_NAME);
        retrieveProgram = "?- retrieve(D, Q);"; // XXX What is that?
    }
    
    /**
     * This constructor must be used if you are going to start your own engine
     * process (client/server mode).
     * @param hyspirit the HySpirit properties containing the environment
     * @throws HySpiritException if we can't determine the environment
     */
    public HyFVPDatalogEngine(HySpiritProperties hyspirit)
    throws HySpiritException {
        super(ENGINE_NAME, hyspirit);
    }
    
    /**
     * This constructor must be used if you use an engine server (client mode).
     * @param hostname the server host name
     * @param port the server port
     * @throws HySpiritException if we can't determine the environment
     */
    public HyFVPDatalogEngine(String hostname, int port)
    throws HySpiritException {
        super(ENGINE_NAME, hostname, port);
    }
    

    /**
     * Tells to translate a FVPD program to PRA.
     * 
     */
    public void toPRA() {
        to = "PRA";
    }
    
    /**
     * Tells to translate a FVPD program to PD.
     * 
     */
    public void toPD() {
        to = "PD";
    }

    
    /**
     * Sets open world assumption (cwa = false) or closed world assumption
     * (cwa = true). See hy_fvpd manual for further details. 
     * @param cwa flag if closed or open world assumption
     */
    public void setCWA(boolean cwa) {
        if (cwa) this.cwa = "cwa";
        else this.cwa = "owa";
    }
    
    /**
     * Specify static or dynamic for CWA or OWA. With static (dynamic = false),
     * world assumptions can not be altered. See also methods CWAPostfix() and
     * OWAPostFix(). See hy_fvpd manual for further details.
     * @param dynamic true if dynamic, false if static
     */
    public void setDynamic(boolean dynamic) {
        if (dynamic) this.dynamic = "static";
        else this.dynamic = "dynamic";
    }
    
    /**
     * Postfix used for dynamic relations for closed world assumption.
     * @param CWAPostfix the CWA postfix
     */
    public void CWAPostfix(String CWAPostfix) {
        this.CWAPostfix = CWAPostfix;
    }

    /**
     * Postfix used for dynamic relations for open world assumption.
     * @param OWAPostfix the OWA postfix
     */
    public void OWAPostfix(String OWAPostfix) {
        this.OWAPostfix = OWAPostfix;
    }
    
    /**
     * With this option, HyFVPD supports reasoning with partly inconsistent
     * knowledge. See hy_fvpd manual for further details.
     * @param posneg true or false
     */
    public void setPosNeg(boolean posneg) {
        if (posneg) this.posneg = "posneg";
        else this.posneg = "noposneg";
    }
    
    /**
     * Specification of the prefixes for the relations that contain positive
     * (true and inconsistent) propositions. Ignored if closed world assumption
     * is set (see setCWA()) or posneg is not set (see setPosNeg()). See hy_fvpd
     * manual for further details. 
     * @param posPrefix prefix for positive relations
     */
    public void posPrefix(String posPrefix) {
        this.posPrefix = posPrefix;
    }
    
    /**
     * Specification of the prefixes for the relations that contain positive
     * negative (false and inconsistent) propositions, respecitively. Ignored if
     * closed world assumption is set (see setCWA()) or posneg is not set (see
     * setPosNeg()). See hy_fvpd manual for further details. 
     * @param negPrefix prefix for negative relations
     */ 
    public void negPrefix(String negPrefix) {
        this.negPrefix = negPrefix;
    }
    

    /**
     * Sets option to be passed to hy_pra directly.
     * @param hyPRAOpts the hy_pra options. See hy_pra manual for further
     *        details.
     */
    public void hyPRAOpts(String hyPRAOpts) {
        this.hyPRAOpts = hyPRAOpts;
    }
    
    /**
     * Resets all parameters of the engine after destroying a possibly running
     * process. You have to restart the process with <code>run()</code>.
     */
    public void reset() {
        super.reset();
        eval = null;
        cwa = null;
        dynamic = null;
        CWAPostfix = null;
        OWAPostfix = null;
        posneg = null;
        posPrefix = null;
        negPrefix = null;
        to = null;
    }
    
    /*
     * Builds the command string array.
     * @see hyspirit.engines.HyEngine#buildCommand()
     */  
    protected String[] buildCommand() {
        Vector commandVec = new Vector();
        commandVec.add(super.getCommand());
 
        
        if (this.eval != null) {
            commandVec.add("-eval");
            commandVec.add(this.eval);
        }
        if (to != null) {
            commandVec.add("-to");
            commandVec.add(to);         
        }
        if (this.cwa != null) commandVec.add("-" + this.cwa);
        if (this.dynamic != null) commandVec.add (" -" + this.dynamic);
        if (this.dynamic != null && this.dynamic.equals("dynamic")) {
            if (this.CWAPostfix != null) {
                commandVec.add("-cwa_postfix");
                commandVec.add(this.CWAPostfix);
            }
            if (this.OWAPostfix != null) {
                commandVec.add("-owa_postfix");
                commandVec.add(this.OWAPostfix);
            }           
        }
        
        if (this.posneg != null) commandVec.add("-" + this.posneg);
        
        if (this.posneg != null && this.posneg.equals("posneg") 
                && this.cwa != null && this.cwa.equals("owa")) {
            if (this.posPrefix != null) {
                commandVec.add("-pos_prefix");
                commandVec.add(this.posPrefix);
            }
            if (this.negPrefix != null) {
                commandVec.add("-neg_prefix");
                commandVec.add(this.negPrefix);
            }
        }
        
        // add additional arguments
        if (super.argumentString != null) {
            StringTokenizer strTok= new StringTokenizer(super.argumentString);
            while (strTok.hasMoreTokens())
                commandVec.add(strTok.nextToken());
        }
 
        if (hyPRAOpts != null) {
            commandVec.add("-pra_opts");
            commandVec.add(hyPRAOpts);
        }
        
        // STDIN
        if (stdin) {
            commandVec.add("--");
            commandVec.add("-");
        }        
        
        
        String[] commandString = new String[commandVec.size()];
        int i = 0;
        for (Enumeration e = commandVec.elements(); e.hasMoreElements();) 
            commandString[i++] = (String)e.nextElement();
        return commandString;   
    }
}
