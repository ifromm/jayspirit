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
 */

package hyspirit.knowledgeModelling;
// Generated file. DO NOT EDIT. Generate with make program.
/** The HyPOOLProgram class allows for the creation, management and
 * execution of POOL programs. For example:
 <p>
 HyPOOLProgram p = new HyPOOLProgram();<br/>
 p.addFile("expertRetrieval.pool");<br/>
 p.addText("peter.friend(mary)");<br/>
 p.eval("?- Person[ sailing in greece ] & Person.friend(mary);")<br/>
 </p>
*/
import hyspirit.engines.HyPOOLEngine;
import hyspirit.engines.HyInferenceEngine;
import hyspirit.util.HySpiritException;

public class HyPOOLProgram extends HyProgram {
    
    private final String layer = "POOL";
    
    public HyInferenceEngine inferenceEngine () {
	if (inferenceEngine == null)
	try {
	    inferenceEngine = new HyPOOLEngine();
	}
	catch (HySpiritException e) {
		e.printStackTrace();
	};
	return inferenceEngine;
    }
}
