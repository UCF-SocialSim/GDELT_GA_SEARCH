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

/**
 * Normalized Root Mean Squared Error scorer
 * that uses a min/max approach
 */
public class Scorer_NRMSE_MinMax extends Scorer_RMSE{

	public Scorer_NRMSE_MinMax() {
		super();
	}
	
	public Scorer_NRMSE_MinMax(TimeSeries dataSet, int tMin, int tMax, LocalDateTime ... dateTimes) {
		super(dataSet, tMin, tMax, dateTimes);
	}
	
	@Override
	protected double normalize(double valToNormalize, double[] testvals) {
		double range = range(testvals);
		return range != 0 ? valToNormalize/range : valToNormalize;
	}
	
	public String getType() {
		return "Scorer_NRMSE_MinMax";
	}
}
