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
 * Created on 14-Apr-2005 18:04:28. Imported to JaySpirit 02-Dec-2005.
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
 */
package hyspirit.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream; // e.g. process.getErrorStream()
import java.io.PrintStream; // e.g. System.err

/**
 * A stream gobbler pipes ("gobbles") an input stream to an output
 * stream. Use this one if you want to realise something similar to a
 * Unix Pipe. This code is based on the StreamGobbler class presented
 * in
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html.
 *
 * A stream gobbler is started for each engine to pipe the error
 * output of the engine either into a stream that is read by the
 * search/classification application, or directly so System.err.
 * 
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * <p>
 * Created on 14-Apr-2005 18:04:28. Imported to JaySpirit 02-Dec-2005.
 *
 */
public class StreamGobbler extends Thread {
    
    BufferedReader sourceOut = null;
    BufferedWriter destIn = null;
    String streamName = null;
    String verboseStreamName = null;
    boolean verbose = false;
    
    // Ingo, these are new.
    private BufferedReader bufferedReader;
    private PrintStream printStream;
    
    // CONSTRUCTORS



    /**
     * Constructor of class. Redirects sourceOut to destIn. Closes
     * both streams after that.
     * @param sourceOut the output stream (e.g., STDOUT) of the source
     * @param destIn the input stream (e.g., STDIN) of the
     * destination. If null, this is something like /dev/null.
     */
    public StreamGobbler (BufferedReader sourceOut, BufferedWriter destIn) {
        this.sourceOut = sourceOut;
        this.destIn = destIn;
        
    }
    
    /**
     * Constructor of class. Redirects sourceOut to destIn. Closes
     * both streams after that. If it reads "line" from sourceOut, it
     * writes "<streamName> line" to destIn.
     * @param sourceOut the output stream (e.g., STDOUT) of the source
     * @param destIn the input stream (e.g., STDIN) of the
     * destination. If null, this is something like /dev/null.
     * @param streamName the stream name (precedes every line written to destIn)       
     */
    public StreamGobbler (BufferedReader sourceOut, BufferedWriter destIn, 
            String streamName) {
        this.sourceOut = sourceOut;
        this.destIn = destIn;
        this.streamName = streamName;
    }
    
    /**
     * Create a stream gobbler that reads from inputStream and prints
     * to printStream. For example,
     * StreamGobbler(process.getErrorStream(),System.err) creates a
     * stream gobbler that reads the error output stream of the
     * process object, and prints the lines read to the System.err
     * stream.
     */
    public StreamGobbler (InputStream inputStream,
            PrintStream printStream) {
        this.bufferedReader =
            new BufferedReader(new InputStreamReader(inputStream));
        this.printStream = printStream;
    }
    /**
     * Create a stream gobbler that reads from inputStream and prints
     * to System.err.
     */
    public StreamGobbler (InputStream inputStream) {
        this(inputStream, System.err);
    }
    /**
     * Create a stream gobbler that reads from bufferedReader and
     * prints to printStream.
     */
    public StreamGobbler (BufferedReader bufferedReader,
            PrintStream printStream) {
        this.bufferedReader = bufferedReader;
        this.printStream = printStream;
    }
    /**
     * Create a stream gobbler that reads from bufferedReader and prints
     * to System.err.
     */
    public StreamGobbler (BufferedReader bufferedReader) {
        this(bufferedReader, System.err);
    }
    
    
    /**
     * Be verbose. The output of the stream is also written to
     * System.out in the form "streamName> content_line".
     * 
     * @param verboseStreamName the name of the verbose stream. If
     * null, be quiet.
     */
    public void verbose(String verboseStreamName) {
        this.verboseStreamName = verboseStreamName;
    }
    
    /**
     * Run this stream gobbler. The gobbler reads from its buffered
     * reader (reads from its input stream) and writes to its buffered
     * writer (writes to its output print stream).
     */
    public void run() {
        run_version1();
    }
    public void run_version1 () {
        if (sourceOut != null) {
            try {
                String line = null;
                while ((line = sourceOut.readLine()) != null) {
                    if (verboseStreamName != null)
                        System.out.println(verboseStreamName + "> " + line);
                    if (verbose)
                        System.out.println("SG> " + line);                    
                    if (streamName != null)
                        line = "<" + streamName + "> " + line;
                    if (destIn != null) {
                        destIn.write(line);
                        destIn.newLine();
                    }
                }
                if (destIn != null) destIn.flush();
                sourceOut.close();
                if (destIn != null) destIn.close();
            }
            catch (IOException io){
                io.printStackTrace(System.err);
            }
        }
    }
    public void run_version2 () {
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                printStream.println(line);
            }
        }
        catch (IOException io) {
            io.printStackTrace(System.err);
        }
    }
}
