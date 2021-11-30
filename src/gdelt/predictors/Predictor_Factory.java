/**
 * GDELT_GA_SEARCH: GDELT Genetic Algorithm Search Tool
 *  
 * Copyright (C) 2021  John T. Murphy
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * This code was authored by John T. Murphy with contributions from Awrad Ali,
 * Harleen Lappano, and Lindsey Andrade
 * 
 * If you use this code or the tool in your work, please cite using the following bibtex:
 * @book{murphyAndWadsworth2021,
 *   author =       {Murphy, John T., and Wadsworth, Marin},
 *   title =        {GDELT GA Search Users Manual},
 *   year =         {2021},
 *   url =          {http://USER_MANUAL_URL}
 * }
 */
package gdelt.predictors;

import java.util.HashMap;
import java.util.Map;

public class Predictor_Factory {

	private static Map<Predictor.PREDICTION_METHOD, Predictor> predictorMap = new HashMap<Predictor.PREDICTION_METHOD, Predictor>();
	
	public static Predictor get(Predictor.PREDICTION_METHOD method) {
		Predictor predictor = predictorMap.get(method);
		if(predictor != null) return predictor;
		switch(method) {
			case RAW:{
				predictor = new Predictor_Raw();
				break;
			}
			case LINEAR_REGRESSION:{
				predictor = new Predictor_LR();
				break;
		    }
			case TEST_BASE_VALUES:{
				predictor = new Predictor_BaseValues();
				break;
			}
			case REPLAY:{
				predictor = new Predictor_Replay();
				break;
			}
			default:{
				if(method.getScriptPath() != null) {
					predictor = new Predictor_Python(method.getScriptPath());
				}
			}
		}
		if(predictorMap != null) predictorMap.put(method,  predictor);
		return predictor;
	}
	
}
