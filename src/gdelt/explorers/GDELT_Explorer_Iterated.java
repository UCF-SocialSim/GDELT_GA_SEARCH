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
package gdelt.explorers;

import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import gdelt.predictors.Predictor.PREDICTION_METHOD;
import gdelt.predictors.Predictor_Factory;
import gdelt.query.GDELT_Query;
import gdelt.runners.GDELT_Query_Task;
import gdelt.scorers.Scorer;
import gdelt.scorers.results.SmartScore;
import gdelt.scorers.results.SmartScore.COMPARISON;
import gdelt.server.GDELT_Service;
import gdelt.utils.IO;
import gdelt.utils.IO.LEVEL;

public class GDELT_Explorer_Iterated extends GDELT_Explorer{
	
	private QueryIterator iterator;
	
	public int bailoutValue    = -1;
	public int bailoutInterval = 1800;

	List<Integer> changes = new ArrayList<Integer>();
	
	public GDELT_Explorer_Iterated(GDELT_Service serviceToUse) {
		super(serviceToUse);
		iterator = null;
	}

	public GDELT_Explorer_Iterated(GDELT_Service serviceToUse, Scorer scorerToUse, QueryIterator ... queryIterators) {
		super(serviceToUse,scorerToUse, null);
		iterator = new MultiIterator(queryIterators);
	}

	public void reset(Scorer scorerToUse, QueryIterator ... queryIterators) {
		reset(scorerToUse, null, queryIterators);
	}

	public void reset(Scorer scorerToUse, String label, QueryIterator ... queryIterators) {
		super.reset(scorerToUse, null);
		iterator = new MultiIterator(queryIterators);
		iterator.label = label;
	}

	
	public boolean advance(int num) {
		if(iterator == null) return false;
		else return iterator.advance(num);
	}
	
	public String execute(String name) {
		if(iterator==null) return null; // Need to use 'reset' before executing

	  	GDELT_Query highScoreQuery = startQuery; // Will be null
    	SmartScore highScore = null;
		Instant t = Instant.now();
    	GDELT_Query toSend;
		while(( (bailoutValue == -1) || (iterator.sinceLastNotable() < bailoutValue)) && ((toSend = iterator.next()) != null)) {
			IO.log(IO.LEVEL.LEVEL_1, iterator.label + " ITERATION " + iterator.currentIteration + " of " + iterator.countOfTotalIterations() + " IterationsSince: " + iterator.sinceLastNotable());
			GDELT_Query_Task task = 
	    			GDELT_Query_Task.get(
	    					service.processQueryTask(new GDELT_Query_Task(toSend, 1, ChronoUnit.DAYS, useDateTimeAdded, scorer.getDates())));	
		  
	    	SmartScore score = scorer.score(task);
	    	iterator.recordScore(score);
	    	boolean changed = false;
	    	COMPARISON comparison = (highScore == null ? COMPARISON.BETTER : score.isBetter(highScore));
	    	IO.log(IO.LEVEL.LEVEL_3, "COMPARISON RESULT IS: " + comparison);
            if(highScore == null || comparison == COMPARISON.BETTER) {        
            	highScore      = score;
            	highScoreQuery = task.query;
            	changed = true;
            }
            IO.log(IO.LEVEL.LEVEL_3, "SCORE: " + score     + " VALUES: " + (task.query.all ? "(ALL)" : "(SUBSET)")); // + IO.LS + task.query.summaryOfCriteria());
            if(changed) {
            	IO.log(IO.LEVEL.LEVEL_2, "BEST:  " + highScore + " SUMMARY OF VALUES: " + IO.LS + highScoreQuery.summaryOfCriteria());
            	IO.log(IO.LEVEL.LEVEL_4, "Iteration: " + iterator.currentIteration + " IterationsSince: " + iterator.sinceLastNotable() + " BESTSCORE:  " + highScore);
            	
            	
            	// Is there a way to grab the prediction that results from this?
            	
            	changes.add(iterator.currentIteration);
            	for(TestCriteria test: testCriteria) {
            		
            		GDELT_Query testQuery = highScoreQuery.clone();
            		double[] actual      = 
            				testQuery.prediction_method == PREDICTION_METHOD.TEST_BASE_VALUES 
            				   ? test.groundTruth.getBaseValues() 
            				   : test.groundTruth.getPredictions();
            		testQuery.tMax = actual.length;
            		GDELT_Query_Task task1 = new GDELT_Query_Task(testQuery, test.groundTruth.getResolution(), test.groundTruth.getChronoUnit(), useDateTimeAdded, test.groundTruth.getTZero());
            		task1 = GDELT_Query_Task.get(service.processQueryTask(task1));
            		
            		double[] eventCounts = Predictor_Factory.get(task.query.prediction_method).getPredictions(task1.resultsMap.get(test.groundTruth.getTZero()), test.groundTruth);
            		
            		StringWriter out = new StringWriter();
            		out.write("PRED," + test.name + ",Date,GT");
            		for(int i =0; i < changes.size(); i++) {
            			out.write(",Iteration_" + changes.get(i));
            		}
            		IO.log(LEVEL.LEVEL_0, out.toString(), true);
            		
            		for(int i = 0; i < eventCounts.length; i++) {
            			if(test.values.size() >= i ){
            				List<Integer> toAdd = new ArrayList<Integer>();
            				toAdd.add((int)(i < actual.length ? actual[i] : -1));
            				test.values.add(toAdd);
            			}
            			StringWriter out2 = new StringWriter();
            			out2.write("PRED," + test.name + ",");
            			List<Integer> row = test.values.get(i);
            			row.add((int)(Math.round(eventCounts[i])));
            			int dateIndex = i;
            			if(testQuery.prediction_method == PREDICTION_METHOD.TEST_BASE_VALUES) dateIndex = i - testQuery.tMax;
            			out2.write((test.groundTruth.getTZero().plus(dateIndex, test.groundTruth.getChronoUnit())).toString().replace("T00:00", ""));
            			for(int val: row) {
            				out2.write("," + val);
            			}
            			IO.log(LEVEL.LEVEL_0, out2.toString(), true);
            		}
        
        		    if(test.name == "Result") {
		    			IO.log(IO.LEVEL.LEVEL_4, "Attempting to score for predictions...");
		    			double scoreForGTPred = scorer.score(eventCounts, test.groundTruth.getPredictions());
		    			IO.log(IO.LEVEL.LEVEL_4, "NEW BEST SCORE FOR FORWARD PREDICTION at Iteration " + iterator.currentIteration + " is " + scoreForGTPred);
		    			IO.log(IO.LEVEL.LEVEL_2, "Iteration: " + iterator.currentIteration + " IterationsSince: " + iterator.sinceLastNotable() + " BESTSCORE:  " + highScore + " BEST PRED SCORE " + scoreForGTPred);
        		    }
            	}
            	iterator.setNotable();
 
            	t = Instant.now();
            	IO.log(IO.LEVEL.LEVEL_2, iterator.getLabel() + ": BEST QUERY: " + highScoreQuery.blankClone().toString().replaceAll("\\{\"order\":", IO.LS + "\\{\"ordr\":").replaceAll("ordr",  "order"));

            }
            IO.log(IO.LEVEL.LEVEL_3, "BEST:  " + highScore + " VALUES: " + highScoreQuery.toString()); 
            IO.memCheck(IO.LEVEL.LEVEL_4);
            Instant d = Instant.now();
            if(t.plusSeconds(bailoutInterval).isBefore(d)) {
            	IO.log(IO.LEVEL.LEVEL_0, "Bailing because more than " + bailoutInterval + " seconds have passed.");
            	this.bailoutValue = 0; // This will cause it to stop
            }
		}
    	// Using the best query, retrieve the full sequence of GDELT event counts
		IO.log(IO.LEVEL.LEVEL_1, "Explorer: " + name + ": Best Score: " + highScore + " Query: " + highScoreQuery.summaryOfCriteria());		
		IO.log(IO.LEVEL.LEVEL_1, "Explorer: " + name + ": Best Score: " + highScore + " Query: " + highScoreQuery.blankClone().toString().replaceAll("\\{\"order\":", IO.LS + "\\{\"ordr\":").replaceAll("ordr",  "order"));	

		return highScoreQuery.toString();
	}
	
}
