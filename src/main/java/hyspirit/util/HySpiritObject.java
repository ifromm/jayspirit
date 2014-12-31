package hyspirit.util;

public class HySpiritObject {

    static boolean DEBUG = false;
    private boolean debug = false;
    private boolean verbose = false;
    
    public boolean debug () {
	return(debug || DEBUG);
    }
    public void setDebug () {
	debug = true;
    }
    public void unsetDebug () {
	debug = false;
    }
    public void setVerbose () {
	verbose = true;
    }
    public void unsetVerbose () {
	verbose = false;
    }
}
