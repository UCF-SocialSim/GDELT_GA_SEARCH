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

import gdelt.runners.GDELT_Query_Task;
import gdelt.scorers.results.ScoreNaN;
import gdelt.scorers.results.SmartScore;
import gdelt.scorers.results.SmartScore_Correlation;
import gdelt.utils.TimeSeries;
import gdelt.utils.ZeroIndexedSeries;

/**
 * A scorer that calculates a correlation over a trimmed
 * version of the variables. 
 */
public class Scorer_InputCorrelation_Trimmed extends Scorer {
	
	public Scorer_InputCorrelation_Trimmed() {
		super();
	}
	
	public Scorer_InputCorrelation_Trimmed(TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
		super(dataSet, tMin, tMax, dateTimes);
	}
	
	public SmartScore score(GDELT_Query_Task toScore) {
		if(trainAndTestMap.size() == 0) return new ScoreNaN();
//		PearsonsCorrelation pc = new PearsonsCorrelation();
		double sum = 0;
		for(Map.Entry<LocalDateTime, ZeroIndexedSeries> entry: trainAndTestMap.entrySet()) {
			ZeroIndexedSeries testAgainst = trainAndTestMap.get(entry.getKey());
			
			double[] predictionsORIG = toScore.getPredictions(entry.getKey(), testAgainst);
			double[] testvalsORIG    = testAgainst.getBaseValues();
			
			double[] predictions = new double[predictionsORIG.length];
			for(int i = 0; i < predictionsORIG.length; i++) predictions[i] = predictionsORIG[i];
			double[] testvals    = new double[testvalsORIG.length];
			for(int i = 0; i < testvalsORIG.length; i++) testvals[i] = testvalsORIG[i];
			
			
			// Trim out anything where the predicted values are lower than 3% of the max value
			double maxTestValue = 0;
			for(int i = 0; i < testvals.length; i++) maxTestValue = Math.max(maxTestValue, testvals[i]);
			double threshold = maxTestValue * .05; // 3%
			int count = 0;
			for(int i = 0; i < testvals.length; i++) {
				if(testvals[i] < threshold) {
					predictions[i] = 0;
					testvals[i] = 0;
				}
				else count++;
			}
			
			
			double[] newpredictions = new double[count];
			double[] newtestvals = new double[count];
			
			int indx = 0;
			for(int i = 0; i < predictions.length; i++) {
				if(testvals[i] > 0) {
					newpredictions[indx] = predictions[i];
					newtestvals[indx] = testvals[i];
					indx++;
				}
			}
			predictions = newpredictions;
			testvals = newtestvals;
			
			
			
			if(predictions.length > testvals.length) {
				double[] newPreds = new double[testvals.length];
				for(int i = 0; i < testvals.length; i++) newPreds[i] = predictions[predictions.length - testvals.length];
				predictions = newPreds;
			}
			for(int i = 0; i < predictions.length; i++) System.out.println(predictions[i] + "," + testvals[i]);
			sum += score(testvals, predictions);
		}
		return new SmartScore_Correlation(sum / (double)trainAndTestMap.size());
	}
	
	
	public double score(double[] testvals, double[] predictions) {
		PearsonsCorrelation pc = new PearsonsCorrelation();
		return pc.correlation(predictions, testvals);
	}
	
	
	public String getType() {
		return "Scorer_InputCorrelation_Trimmed";
	}
}
