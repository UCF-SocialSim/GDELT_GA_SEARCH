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

import gdelt.runners.GDELT_Query_Task;
import gdelt.scorers.results.ScoreNaN;
import gdelt.scorers.results.SmartScore;
import gdelt.scorers.results.SmartScore_RMSE;
import gdelt.utils.IO;
import gdelt.utils.TimeSeries;
import gdelt.utils.ZeroIndexedSeries;

/**
 * A class that calculates root mean squared
 * error. Serves as a base class for variations
 * like normalized rmse, etc.
 */
public class Scorer_RMSE extends Scorer {
	
	protected double mean;
	protected double std;
	protected double range;
	
	protected static double mean(double[] vals) {
		if(vals.length == 0) return 0;
		double total = 0;
		for(double val: vals) total += val;
		return total/vals.length;
	}
	
	protected static double stdDev(double[] vals) {
		double mean = mean(vals);
	   	double sum = 0;
		for(int i = 0; i < vals.length; i++) {
			sum = sum + Math.pow(vals[i] - mean, 2);
			
		}
		return Math.sqrt(sum/vals.length); 
	}
	
	protected static double range(double[] vals) {
		   double max = Double.NEGATIVE_INFINITY;
		   double min = Double.POSITIVE_INFINITY;
		   for(int i = 0; i < vals.length; i++) {
			   if (max < vals[i]) max = vals[i];
			   if(min > vals[i])  min = vals[i];
		   }
		   return(max - min);
	}
	
	public Scorer_RMSE() {
		super();
	}
	
	public Scorer_RMSE(TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
		super(dataSet, tMin, tMax, dateTimes);
	}
	
	public SmartScore score(GDELT_Query_Task toScore) {
		IO.log(IO.LEVEL.LEVEL_4, "BEGINNING SCORE...");
		if(trainAndTestMap.size() == 0) return new ScoreNaN();
		double ret = 0;

		for(Map.Entry<LocalDateTime, ZeroIndexedSeries> entry: trainAndTestMap.entrySet()) {
			ZeroIndexedSeries testAgainst = trainAndTestMap.get(entry.getKey());
			double[] predictions = toScore.getPredictions(entry.getKey(), testAgainst);
			double[] testvals    = testAgainst.getPredictions();
			
			ret+= score(testvals, predictions);

//			for(int i = 0; i < predictions.length; i++) System.out.println("RMSE SCORER IS SCORING B : " + predictions[i] + "," + testvals[i]);
		}
		IO.log(IO.LEVEL.LEVEL_4, "RET: " + ret);
		IO.log(IO.LEVEL.LEVEL_4, "TRAIN MAP SIZE" + (double)trainAndTestMap.size());
		SmartScore retScore = new SmartScore_RMSE(ret/ (double)(trainAndTestMap.size()));
		IO.log(IO.LEVEL.LEVEL_4, 	"FINAL NORMALIZED SCORE = " + retScore);
		return retScore;
	}
	
	public double score(double[] testvals, double[] predictions) {
		if(predictions.length > testvals.length) {
			double[] newPreds = new double[testvals.length];
			for(int i = 0; i < testvals.length; i++) {
				newPreds[i] = predictions[predictions.length - testvals.length];
			}
			predictions = newPreds;
		}
		double sumOfSquaredDiff = 0;
		double total = 0;
		for(int i = 0; i < predictions.length; i++) {
			total += testvals[i];
			double squaredDiff = (Math.pow(predictions[i] - testvals[i], 2));
//			IO.log("new total "+ total);
			sumOfSquaredDiff += squaredDiff;
			IO.log(IO.LEVEL.LEVEL_0, "Squared diff: " + squaredDiff + " ( sum: " + sumOfSquaredDiff +") " + "total " + total);
		}
		double avg   = sumOfSquaredDiff / (double)predictions.length;
		double rmse  = Math.sqrt(avg);
		double nrmse = normalize(rmse, testvals);
		IO.log(IO.LEVEL.LEVEL_0, "AVG: " + avg + " rmse: " + rmse + " nrmse: " + nrmse);
		return nrmse;
	}
	
	protected double normalize(double valToNormalize, double[] testvals) {
		return valToNormalize;		
	}
	
	public String getType() {
		return "Scorer_RMSE";
	}
}

	


