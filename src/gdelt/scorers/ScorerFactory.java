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

import gdelt.utils.TimeSeries;

public class ScorerFactory {

	public static Scorer get(Scorer.SCORE_METHOD scorerName) {
		switch(scorerName) {
			case CORRELATION_POSITIVE_ONLY: return new Scorer_Correlation_PositiveOnly();
			case INPUT_CORRELATION_TRIMMED: return new Scorer_InputCorrelation_Trimmed();
			case INPUT_CORRELATION: return new Scorer_InputCorrelation();
			case INPUT_CORRELATION_POSITIVE_ONLY: return new Scorer_InputCorrelation_PositiveOnly();
			case NRMSE_MEAN: return new Scorer_NRMSE_Mean();
			case NRMSE_MINMAX: return new Scorer_NRMSE_MinMax();			
			case NRMSE_STD: return new Scorer_NRMSE_STD();
			case RMSE: return new Scorer_RMSE();
		}
		return null;
	}
	
	public static Scorer get(String scorerName) {
		return get(Scorer.SCORE_METHOD.valueOf(scorerName));
	}
	
	public static Scorer get(Scorer.SCORE_METHOD scorerName, TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
		switch(scorerName) {
			case CORRELATION_POSITIVE_ONLY: return new Scorer_Correlation_PositiveOnly(dataSet, tMin, tMax, dateTimes);
			case INPUT_CORRELATION_TRIMMED: return new Scorer_InputCorrelation_Trimmed(dataSet, tMin, tMax, dateTimes);
			case INPUT_CORRELATION: return new Scorer_InputCorrelation(dataSet, tMin, tMax, dateTimes);
			case INPUT_CORRELATION_POSITIVE_ONLY: return new Scorer_InputCorrelation_PositiveOnly(dataSet, tMin, tMax, dateTimes);
			case NRMSE_MEAN: return new Scorer_NRMSE_Mean(dataSet, tMin, tMax, dateTimes);
			case NRMSE_MINMAX: return new Scorer_NRMSE_MinMax(dataSet, tMin, tMax, dateTimes);			
			case NRMSE_STD: return new Scorer_NRMSE_STD(dataSet, tMin, tMax, dateTimes);
			case RMSE: return new Scorer_RMSE(dataSet, tMin, tMax, dateTimes);
		}
		return null;
	}

	public static Scorer get(String scorerName, TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
		return get(Scorer.SCORE_METHOD.valueOf(scorerName), dataSet, tMin, tMax, dateTimes);
	}
	
}
