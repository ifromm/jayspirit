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
 * Created on 19-Mar-2005 18:57:22. Copied to JaySpirit 02-Dec-2005.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
 */
package hyspirit.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Some utilities
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 19-Mar-2005 18:57:22. Copied to JaySpirit 02-Dec-2005.
 *
 */
public class Util {
	  	/**
	    * Reads a line from STDIN and returns it as a String.
	    *
	    * @return line read from STDIN or null
	    */
	   public static String readln () {
		String line = null;
		try {
		    BufferedReader stdin =
		        new BufferedReader(new InputStreamReader(System.in));
		    line = stdin.readLine();
		}
		catch (IOException io) {
		    System.out.println(io.toString());
		}
		return line;
	   }
	   
	   /**
	    * Gets the string representation of a command array
	    * @param com the command array
	    * @return the command string
	    */
	   public static String getCommand(String[] com) {
	   		String command = "";
	   		for (int i = 0; i < com.length; i++) 
	   			if (com[i] != null) command += com[i] + " ";
	   		return command.trim();
	   }
	   
	   /**
	    * Writes a stream to the given file.
	    * @param in the buffered reader to be written to the file
	    * @param filename the absolute name of the file
	    */
	   public static void streamToFile(BufferedReader in, String filename) {
			String str = null;
			if (in != null && filename != null) {
			    try {
				    BufferedWriter fb = 
				       new BufferedWriter(new FileWriter(filename));
				    
	                String line = null;
	                while ((line = in.readLine()) != null) {
	                    System.out.println("F> " + line);
	                    fb.write(line);
	                    fb.newLine();
	                }
	                fb.flush();
	                in.close();
	                fb.close();
			    }
			    catch (IOException io){
			        io.printStackTrace(System.err);
			    }
			}
	   }

       /**
        * Checks if directory exists, creates it if necessary.
        * @param dirName the name of the directory to be created
        * @throws IOException if the directory cannot be created.
        */
        public static void checkAndCreateDir(String dirName) 
        throws IOException {
            File dir = new File(dirName);
            if (!dir.exists()) dir.mkdirs();
            else {
                if (!dir.isDirectory()) {
                    throw new IOException(dirName + 
                            "exists and is not a directory!");
                }
            }
        }
        
        
        /**
         * As Java tends to use scientific notation for values < 10^-3 and HySpirit
         * doesn't like that, we need to format our probability string correctly.
         * Furthermore this ensures that an English locale is used to represent the
         * probability.
         * @param f the float value
         * @return a HySpirit-compatible String representation of the 
         *         float value (this value must be <9)
         */
        public static String floatToString(float f) {
            DecimalFormat form = 
                (DecimalFormat) 
                   NumberFormat.getNumberInstance(Locale.ENGLISH);
            form.applyPattern("#.######");
            return form.format(f);
        }
       
	   /**
	    * Convert characters
	    * @param term the string to convert
	    * @return the converted string
	    */
	  /* public static String convert(String term)
	    {
	        int i = 0;
	        term = term.replaceAll("�", "i");
	        term = term.replaceAll("\015", "");
	        term = term.replaceAll("\034", "");
	        term = term.replaceAll("\035", "");
	        term = term.replaceAll("\012", "");
	        term = term.replaceAll("�", "oe");
	        term = term.replaceAll("�", "oe");
	        term = term.replaceAll("�", "ue");
	        term = term.replaceAll("�", "ue");
	        term = term.replaceAll("�", "ae");
	        term = term.replaceAll("�", "ae");
	        term = term.replaceAll(":", "");
	        term = term.replaceAll("_", "");
	        term = term.replaceAll("\\.", "");
	        term = term.replaceAll(",", "");
	        term = term.replaceAll(";", "");
	        term = term.replaceAll("�", "ss");
	        term = term.replaceAll("n\'t", " not");
	        term = term.replaceAll("n�t", " not");
	        term = term.replaceAll("\'", "");
	        term = term.replaceAll("�", "");
	        term = term.replaceAll("`", "");
	        term = term.replaceAll("^", "");
	        term = term.replaceAll("/", "");
	        term = term.replaceAll("\\(", "");
	        term = term.replaceAll("\\)", "");
	        term = term.replaceAll("\\{", "");
	        term = term.replaceAll("\\}", "");
	        term = term.replaceAll("\\[", "");
	        term = term.replaceAll("\\]", "");
	        term = term.replaceAll("!", "");
	        term = term.replaceAll("\\?", "");
	        term = term.replaceAll("\"", "");
	        term = term.replaceAll(" - ", " ");

	        return term;
            
	    }
        */

}
