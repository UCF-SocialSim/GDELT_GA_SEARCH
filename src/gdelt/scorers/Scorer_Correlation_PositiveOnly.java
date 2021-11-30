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
import java.util.Map;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import gdelt.predictors.Predictor;
import gdelt.runners.GDELT_Query_Task;
import gdelt.scorers.results.ScoreNaN;
import gdelt.scorers.results.SmartScore;
import gdelt.scorers.results.SmartScore_Correlation_PositiveOnly;
import gdelt.utils.TimeSeries;
import gdelt.utils.ZeroIndexedSeries;

/**
 * A correlation scorer that returns positive-only
 * correlations.
 */
public class Scorer_Correlation_PositiveOnly extends Scorer {
	
	public Scorer_Correlation_PositiveOnly() {
	  super();
	}
	
	public Scorer_Correlation_PositiveOnly(TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
		super(dataSet, tMin, tMax, dateTimes);
	}

	
	// Return a single score for this entire set
	// Assumes that the map in the task will have the same
	// dates as in this object, and all lengths will be
	// correct, etc.
	public SmartScore score(GDELT_Query_Task toScore) {
		if(trainAndTestMap.size() == 0) return new ScoreNaN();
		double sum = 0;
		for(Map.Entry<LocalDateTime, ZeroIndexedSeries> entry: trainAndTestMap.entrySet()) {
			ZeroIndexedSeries testAgainst = trainAndTestMap.get(entry.getKey());
			sum += score(
					toScore.query.prediction_method == 
					Predictor.PREDICTION_METHOD.TEST_BASE_VALUES ? testAgainst.getBaseValues() : testAgainst.getPredictions(), 					
					toScore.getPredictions(entry.getKey(), testAgainst));
		}
		return new SmartScore_Correlation_PositiveOnly(sum / (double)trainAndTestMap.size());
	}

	public double score(double[] testvals, double[] predictions) {
		PearsonsCorrelation pc = new PearsonsCorrelation();
		return pc.correlation(predictions, testvals);
	}
	
	
	public String getType() {
		return " Scorer_Correlation_PositiveOnly";
	}

}
