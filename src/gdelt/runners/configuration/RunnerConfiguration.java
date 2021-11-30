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
package gdelt.runners.configuration;

import java.time.LocalDateTime;

import gdelt.predictors.Predictor;
import gdelt.scorers.Scorer;
import gdelt.scorers.ScorerFactory;
import gdelt.utils.DateFormats;
import gdelt.utils.IO;

public class RunnerConfiguration{

	// Settings that get changed as the date changes:
	public int    tMin;     // Number of dates backward to examine
	public int    tMax;     // Number of dates forward to predict during training CHANGE ME WHEN NEW DATA ARRIVES
	public int    tMaxPred; // Number of days forward to predict for final prediction
	
	// What prediction method:
	public Predictor.PREDICTION_METHOD predMethod;
	
	public Scorer scorer;
	
	// Ecosystem parameters
	public int populationSize;     		// Number of queries in population		
	public int numberOfSurvivors;		// Number of survivors to select each generation
	public int numberOfGenerations;		// Number of generations to run
	
	public LocalDateTime   outputStart;
	public LocalDateTime[] startDates;
	
	public LocalDateTime[] testDates = new LocalDateTime[] {};
	
	public boolean useTimeAdded = false;
	
	public RunnerConfiguration(
			int                         daysBackward,
			int                         daysForward,
			int                         daysForwardToPredict,
			Predictor.PREDICTION_METHOD predictionMethod,
			Scorer.SCORE_METHOD         scoreMethod,
			int                         popSize,
			int                         survivors,
			int                         generations, 
			LocalDateTime               outputStartDate,
			LocalDateTime[]             startDatesForAllTests) {
		tMin                = daysBackward;
		tMax                = daysForward;
		tMaxPred            = daysForwardToPredict;
		predMethod          = predictionMethod;
		scorer              = ScorerFactory.get(scoreMethod);
		populationSize      = popSize;
		numberOfSurvivors   = survivors;
		numberOfGenerations = generations;
		outputStart         = outputStartDate;
		startDates          = startDatesForAllTests;
	}		
	
	
	private static int PRED_TYPE_ARGNUM         = 0;
	private static int SCORER_TYPE_ARGNUM       = 1;
	private static int DAY_ZERO_ARGNUM          = 2;
	private static int DAYS_PRIOR_ARGNUM        = 3;
	private static int DAYS_TO_TEST_ARGNUM      = 4;
	private static int DAYS_TO_PREDICT_ARGNUM   = 5;
	private static int POPSIZE_ARGNUM           = 6;
	private static int SURVIVORS_ARGNUM         = 7;
	private static int GENERATIONS_ARGNUM       = 8;
	private static int TEST_DAYS_ARGNUM         = 9;
	
	
	public RunnerConfiguration(String[] specs) {
		predMethod           = Predictor.PREDICTION_METHOD.valueOf(specs[PRED_TYPE_ARGNUM]);
		scorer               = ScorerFactory.get(specs[SCORER_TYPE_ARGNUM]);
		outputStart          = DateFormats.convertISODate(specs.length >  (DAY_ZERO_ARGNUM) ? specs[(DAY_ZERO_ARGNUM)]  : "2020-08-04");
		tMin                 = (-1) * (Integer.parseInt(specs.length >  (DAYS_PRIOR_ARGNUM) ? specs[(DAYS_PRIOR_ARGNUM)]  : "35"));
		tMax                 = Integer.parseInt(specs.length >  (DAYS_TO_TEST_ARGNUM) ? specs[(DAYS_TO_TEST_ARGNUM)]  : "14");
		tMaxPred             = Integer.parseInt(specs.length >  (DAYS_TO_PREDICT_ARGNUM) ? specs[(DAYS_TO_PREDICT_ARGNUM)]  : "7");
		populationSize       = Integer.parseInt(specs.length >  (POPSIZE_ARGNUM) ? specs[(POPSIZE_ARGNUM)]  : "80");
		numberOfSurvivors    = Integer.parseInt(specs.length >  (SURVIVORS_ARGNUM) ? specs[(SURVIVORS_ARGNUM)]  : "25");
		numberOfGenerations  = Integer.parseInt(specs.length >  (GENERATIONS_ARGNUM) ? specs[(GENERATIONS_ARGNUM)]  : "10");
		
		String[] testDays = (specs.length > (TEST_DAYS_ARGNUM) ? specs[(TEST_DAYS_ARGNUM)]  : "1,14,28,42").split(",");
		startDates = new LocalDateTime[testDays.length];
		for(int i = 0; i < startDates.length; i++) {
			startDates[i] = outputStart.minusDays(tMax + Long.parseLong(testDays[i]));
		}
		
		// Stub here: 'Correlation' works differently from 'Prediction',
		// and so there may have to be a spot here where some of these values are
		// modified if the prediction method is one of the 'Correlation'
		// varieties- TBD
	}
	
	
	public void report() {
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: REPORT: ");
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: daysBackward          =  " + tMin);
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: daysForward           =  " + tMax);
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: daysForwardToPredict  =  " + tMaxPred);
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: predMethod            =  " + predMethod.toString());
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: scorer                =  " + scorer.getType());
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: populationSize        =  " + populationSize);
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: numberOfSurvivors     =  " + numberOfSurvivors);
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: numberOfGenerations   =  " + numberOfGenerations);
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: outputStart           =  " + outputStart.plusDays(tMin) + " --+-- " + outputStart + " --+-- " + outputStart.plusDays(tMaxPred));
		IO.log(IO.LEVEL.LEVEL_0, "CONFIG: ---------------------------------------------------");
		for(LocalDateTime startDate: startDates) IO.log(IO.LEVEL.LEVEL_0, "CONFIG: testStart             =  " +startDate.plusDays(tMin) + " --+-- " + startDate + " --+-- " + startDate.plusDays(tMax));
	}
	
}