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

import org.apache.commons.math3.stat.regression.SimpleRegression;

import gdelt.utils.IO;
import gdelt.utils.ZeroIndexedSeries;

/**
 * Linear Regression predictor
 */
public class Predictor_LR extends Predictor{
	
	public double[] getPredictions(ZeroIndexedSeries gdeltData, ZeroIndexedSeries socialMediaEventData) {
		IO.log(IO.LEVEL.LEVEL_3, "Predictor_Extrapolation: getPredictions");
		
		// Get the coefficient from the relationship between test and train during the train period
		double[] train_x = gdeltData.getBaseValues();
		double[] train_y = socialMediaEventData.getBaseValues();
		double[] test_x  = gdeltData.getPredictions();
		double[] test_y  = socialMediaEventData.getPredictions(); // Not needed
		
		SimpleRegression regression = new SimpleRegression();
		IO.log(IO.LEVEL.LEVEL_4, "Correlation: " + train_x.length + " | " + train_y.length);
		for(int i = 0; i < train_x.length; i++) {
			IO.log(IO.LEVEL.LEVEL_4, "   " + train_x[i] + " " + train_y[i]);
			regression.addData(train_x[i], train_y[i]);
		}
		
		double intercept = regression.getIntercept();
		double slope     = regression.getSlope();
		IO.log(IO.LEVEL.LEVEL_3, "INTERCEPT A: " + intercept + " SLOPE: " + slope + " Correlation: " + regression.getR());
		
		double[] ret = new double[socialMediaEventData.getPredictions().length];
		IO.log(IO.LEVEL.LEVEL_3, "Length of predictions is " + ret.length);
		IO.log(IO.LEVEL.LEVEL_3, "Length of test_x " + test_x.length);
		for(int i = 0; i < ret.length; i++) {
			ret[i] = Math.max((test_x[i] *  slope) + intercept, 0); 
			IO.log("PREDICTIONS: " + test_x[i] + " " + test_y[i] + " " + ret[i]);
		}
	
		return ret;
	}
}
