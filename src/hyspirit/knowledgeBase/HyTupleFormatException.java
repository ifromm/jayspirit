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
 * Created on 27-Dec-2005 17:52:49
 *
 * @author Ingo Frommholz &lt;ingo@is.informatik.uni-duisburg.de&gt;
 * @version $Revision: 1.1.1.1 $
 */
package hyspirit.knowledgeBase;

/**
 * <p>
 * Thrown to indicate that a string representation of a HySpirit tuple is not
 * in the appropriate format.
 * </p>
 * @author <a href="mailto:ingo@is.informatik.uni-duisburg.de">Ingo Frommholz</a>
 * <p>
 * Created on 27-Dec-2005 17:53:22
 * </p>
 */
public class HyTupleFormatException extends IllegalArgumentException {

    public HyTupleFormatException() {
        super();
    }

    public HyTupleFormatException(String s) {
        super(s);
    }

// Use these for Java >1.5 only!   
//    public HyTupleFormatException(String message, Throwable cause) {
//        super(message, cause);
//    }
//
//    public HyTupleFormatException(Throwable cause) {
//        super(cause);
//    }

}
