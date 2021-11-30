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
package gdelt.scorers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import gdelt.runners.GDELT_Query_Task;
import gdelt.scorers.results.SmartScore;
import gdelt.utils.TimeSeries;
import gdelt.utils.ZeroIndexedSeries;

/**
 * Base class for all scorer objects
 */
public abstract class Scorer {

	public static enum SCORE_METHOD{
		CORRELATION_POSITIVE_ONLY,
		INPUT_CORRELATION_TRIMMED,
		INPUT_CORRELATION,
		INPUT_CORRELATION_POSITIVE_ONLY,
		NRMSE_MEAN,
		NRMSE_MINMAX,
		NRMSE_STD,
		RMSE
	}
	
	public Map<LocalDateTime, ZeroIndexedSeries> trainAndTestMap = new HashMap<LocalDateTime, ZeroIndexedSeries>();

	// Initialize with an empty map; this is useless
	public Scorer() { }
	
	public Scorer(TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
    	for(LocalDateTime ldt: dateTimes) {
            trainAndTestMap.put(ldt, new ZeroIndexedSeries(-1 * tMin, dataSet.lift(ldt, tMin, tMax).getValues()));    		
    	}
	}
	
	
	public void init(TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
    	for(LocalDateTime ldt: dateTimes) {
            trainAndTestMap.put(ldt, new ZeroIndexedSeries(-1 * tMin, dataSet.lift(ldt, tMin, tMax).getValues()));    		
    	}
	}
		
	/**
	 * Return a single score for this entire set
	 * Assumes that the map in the task will have the same
	 * dates as in this object, and all lengths will be
	 * correct, etc.
	 * @param toScore
	 * @return
	 */
	public abstract SmartScore score(GDELT_Query_Task toScore);
	
	public abstract double score(double[] testvals, double[] preds);
	
	public LocalDateTime[] getDates() {
		LocalDateTime[] ret = new LocalDateTime[trainAndTestMap.size()];
		int i = 0;
		for(LocalDateTime t: trainAndTestMap.keySet()) ret[i++] = t;
		return ret;
	}
	
	public abstract String getType();
	
}
