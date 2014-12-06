package hyspirit.knowledgeBase;

import java.util.Vector;
import java.util.Enumeration;
import hyspirit.util.HySpiritObject;

public class HyIndex extends HySpiritObject {
    
    private String relationName;
    private Vector columns;
    private String mdsFileName;
    private String priFileName;
    
    public HyIndex (String relationName, int column) {
        this.relationName = relationName;
        String columnString = String.valueOf(column);
        this.columns = new Vector();
        this.columns.add(columnString);
        this.mdsFileName = relationName + ".mds";
        this.priFileName = relationName + column + ".pri";
    }
    public HyIndex (String relationName, int[] columns) {
    }
    public HyIndex (String relationName, Vector columns) {
        this.relationName = relationName;
        this.columns = columns;
        this.mdsFileName = relationName + ".mds";
        this.priFileName = relationName + columns.toString() + ".pri";
    }
    
    /** Update this index. */
    public void update () {
        String columnString = "";
        for (Enumeration e = columns.elements(); e.hasMoreElements();) {
            columnString = columnString + "-col " + e.nextElement();
        }
        String cmd = "hy_mds2pri " + columnString + " " + mdsFileName + " -out " + priFileName + " -follow";
        System.err.println(getClass() + "#update: cmd = " + cmd);
        try {
            Runtime.getRuntime().exec(cmd);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
