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
 * Created on 01-Dec-2005 13:46:26
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
 */

package hyspirit.util;


import java.util.Properties;
import java.util.Enumeration;

/**
 * This class stores all neccessary environment variables for HySpiritProperties/Apriorie.
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 01-Dec-2005 12:47:46
 *
 */
public class HySpiritProperties extends Properties{

    
    /**
     * SET ENVIRONMENT EXTERNALLY AND DON'T USE THIS CONSTRUCTOR!
     * <br/>
     * Constructor of class. From the asoulute path to the HySpiritProperties
     * implementation, the variables $HYSPIRIT, $PATH and $PERL5LIB are
     * derived (similar to invoking INSTALL.bash).
     * @param hyspiritPath abolute path to HySpiritProperties implementation
     * @param workingDir the working directory
     * @deprecated
     */
    public HySpiritProperties(String hyspiritPath, String workingDir) {       
        super.setProperty("workingDir", workingDir);
        try {
            setPathAndPerl5Lib(hyspiritPath);
        }
        catch (HySpiritException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * This constructor tries to read all neccessary information from given
     * system properties or environment variables. The following algorithm is
     * used:
     * 
     * <code>
     * <ol>
     * <li> Determine $HYSPIRIT:</li>
     *    <ul>
     *    <li> Read system property HYSPIRIT (set with "java -D")</li>
     *    <li> If not null: goto 2.</li>
     *    <li>Read environment variable HYSPIRIT</li>
     *    <li>If null: throw Exception - we cannot determine environment!</li>
     *    </ul>
     * <li> Determine $PATH and $PERL5LIB: </li>
     *    <ul>
     *    <li> Read corresponding system property (PATH or PERL5LIB, 
     *         respectively) </li>
     *    <li> If null: Read environment variable (PATH or PERL5LIB,
     *         respectively)</li>
     *    <li> Attach HYSPIRIT/bin or HYSPIRIT/lib to PATH or PERL5LIB,
     *         respectively.</li>
     *    </ul>
     * </ol>         
     * </code>
     * 
     * System properties are thus perferred over environment variables.
     * @param workingDir the working directory    
     * @throws HySpiritException if HYSPIRIT environment cannot be determined.
     * 
     */
    public HySpiritProperties(String workingDir) throws HySpiritException{
        super.setProperty("workingDir", workingDir);
        setPathAndPerl5Lib(getSystemProperty("HYSPIRIT"));
    }
    
    /**
     * This constructor tries to read all neccessary information from given
     * system properties or environment variables. The following algorithm is
     * used:
     * 
     * <code>
     * <ol>
     * <li> Determine $HYSPIRIT:
     *    <ul>
     *    <li> Read system property HYSPIRIT (set with "java -D")</li>
     *    <li> If not null: goto 2.</li>
     *    <li>Read environment variable HYSPIRIT</li>
     *    <li>If null: throw Exception - we cannot determine environment!</li>
     *    </ul>
     *    </li>
     * <li> Determine $PATH and $PERL5LIB: 
     *    <ul>
     *    <li> Read corresponding system property (PATH or PERL5LIB, 
     *         respectively) </li>
     *    <li> If null: Read environment variable (PATH or PERL5LIB,
     *         respectively)</li>
     *    <li> Attach HYSPIRIT/bin or HYSPIRIT/lib to PATH or PERL5LIB,
     *         respectively.</li>
     *    </ul>
     *    </li>
     * </ol>         
     * </code>
     * 
     * System properties are thus perferred over environment variables.
     * 
     * @throws HySpiritException if HYSPIRIT environment cannot be determined.
     */
    public HySpiritProperties() throws HySpiritException {
        super.setProperty("workingDir", System.getProperty("user.dir"));
        setPathAndPerl5Lib(getSystemProperty("HYSPIRIT"));
    }
    
    /**
     * Returns all environment variables as string array. Each entry has the
     * format "propertyName=propertyValue", so it can be uses for
     * runtime.exec().
     * @return the environment variables as string array
     */
    public String[] getEnvironment() {
        String[] environment = new String[super.size()];
        int i = 0;
        for (Enumeration e = super.propertyNames(); e.hasMoreElements();){
            String propertyName = (String)e.nextElement();
            String propertyValue = super.getProperty(propertyName);
            environment[i++] = propertyName + "=" + propertyValue;
        }
        return environment; 
    }
    
    /**
     * Returns the HySpiritProperties path
     * @return $HYSPIRIT
     */
    public String getHySpiritPath() {
        return super.getProperty("HYSPIRIT");
    }
    
    
    
    
    
    /**
     * Returns the working directory of this HySpiritProperties object.
     * @return the working directory
     */
    public String getWorkingDirectory() {
        return super.getProperty("workingDir");
    }
    
    /**
     * Sets the working directory of this HySpiritProperties object.
     * @param workingDir the working directory
     */
    public void setWorkingDirectory(String workingDir) {
        super.setProperty("workingDir", workingDir);
    }
    
    
    /*
     * Sets $HYSPIRIT, $PERL5LIB and $PATH from HySpiritProperties path
     */
    private void setPathAndPerl5Lib(String hyspiritPath) 
    throws HySpiritException{
        if (hyspiritPath != null) {
            // $HYSPIRIT
            super.setProperty("HYSPIRIT", hyspiritPath);
            
            // $PERL5LIB
            String perl5lib = getSystemProperty("PERL5LIB");
            if (perl5lib != null) perl5lib = hyspiritPath + "/lib:" + perl5lib;
            else perl5lib = hyspiritPath + "/lib";
            super.setProperty("PER5LIB", perl5lib);
        
            // $PATH
            String path = getSystemProperty("PATH"); 
            String hypath = hyspiritPath + "/bin:" + hyspiritPath + "/etc";
            if (path == null) path = hypath;
            else path = hypath + ":" + path;
            super.setProperty("PATH", path);
        }
        else {
            throw
                new HySpiritException("Could not fetch HySpirit environment. Try java -DHYSPIRIT=$HYSPIRIT.");
        }
    }
    
    
    /**
     * This methods tries to determine the value of a system property. First, it
     * tries to read the value with <code>System.getProperty()</code>. If this 
     * fails, it tries to read the value with <code>System.getenv()</code> to 
     * check if the value was set in an environment variable (this does not work
     * with java versions below 1.5).
     * @param propertyName the name of the property/enironment variable to be
     *        read
     *  @return the property value or null if it can't be determined.
     */
    private static String getSystemProperty(String propertyName) {
        String property = System.getProperty(propertyName);
        if (property == null) {
            try {
                property = System.getenv(propertyName);
            }
            catch (java.lang.Error e) {
                // the error will be thrown in VMs < 1.5!
            }
        }
        return property;
    }
}
   
