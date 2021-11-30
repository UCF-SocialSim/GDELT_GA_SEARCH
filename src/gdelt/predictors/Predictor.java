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

import gdelt.utils.ZeroIndexedSeries;

/**
 * Base class for all predictor classes, including
 * Python-based ones.
 */
public abstract class Predictor {

	public static enum PREDICTION_METHOD{
		RAW,
		LINEAR_REGRESSION,
		PY_LR("./predict_lr.py"),
		PY_LR_WEIGHT("./predict_lr_weighted.py"),
		PY_Elastic_Net("./predict_elastic_net.py"),
		PY_LASSO("./predict_lasso.py"),
		PY_Gradient_Boost("./predict_gradient_boost.py"),
		TEST_BASE_VALUES,
		REPLAY,
		PY_REPLAY_ONEBYONE__UPDATE_GDELT("./predict_replay_update_onebyone_Gdelt.py"),
		PY_REPLAY_ONEBYONE_7DAYS_NOGDELT("./predict_replay_onebyone_No_Gdelt.py"),
		PY_REPLAY_WHOLE_28DAYS_NOGDELT("./predict_replay_whole_28days_No_Gdelt.py"),
		PY_REPLAY_WHOLE_28DAYS_GDELT("./predict_replay_whole_28Days_Gdelt.py");
// Other possible elements (not fully tested at release):
//		PY_REPLAY_ONEBYONE_NOGDELT("./predict_replay_onebyone_No_Gdelt.py"),
//		PY_REPLAY_ONEBYONE_GDELT("./predict_replay_onebyone_Gdelt.py"),
//		PY_REPLAY_ONEBYONE_7DAYS_GDELT("./predict_replay_onebyone_7days_Gdelt.py"),
				
		private final String scriptPath;
		
		private PREDICTION_METHOD() {
			scriptPath = null;
		}
		
		private PREDICTION_METHOD(String path) {
			scriptPath = path;
		}
		
		public String getScriptPath() {
			return scriptPath;
		}
	}
	
	public abstract double[] getPredictions(ZeroIndexedSeries gdeltData, ZeroIndexedSeries socialMediaEventData);
	
}
