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
import hyspirit.engines.HyPDatalogEngine;
import hyspirit.engines.HyInferenceEngine;
import hyspirit.util.HySpiritException;

public class HyPDatalogProgram extends HyProgram {
    
    private final String layer = "PDatalog";
    
    public HyInferenceEngine inferenceEngine () {
	if (inferenceEngine == null)
	try {
	    inferenceEngine = new HyPDatalogEngine();
	}
	catch (HySpiritException e) {
		e.printStackTrace();
	};
	return inferenceEngine;
    }
}
