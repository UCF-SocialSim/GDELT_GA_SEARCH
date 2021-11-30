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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import gdelt.query.GDELT_Query;
import gdelt.scorers.results.SmartScore;
import gdelt.utils.IO;

public class GDELT_Query_Ecosystem extends QueryIterator{
	
	int popSize;
	int survivorCount;
	int index            = 0;
	List<GDELT_Query>  population = new ArrayList<GDELT_Query>();

	protected static class ScorePair implements Comparable<ScorePair>{
		SmartScore score;
		GDELT_Query query;
		
		public ScorePair(SmartScore s, GDELT_Query q) {
			score = s;
			query = q;
		}

		@Override
		public int compareTo(ScorePair o) {
			int c = score.compareTo(o.score);
			return c != 0 ? c : query.compareTo(o.query); 
		}
		
	}

	TreeSet<ScorePair> results = new TreeSet<ScorePair>();

	public GDELT_Query_Ecosystem(int populationSize, 
			int numSurvivorsEachGeneration, GDELT_Query ... queries) {
		popSize  = populationSize;
		survivorCount = numSurvivorsEachGeneration;
		int i = 0;
		for(GDELT_Query query: queries) {
			population.add(query);
			i++;
			if(i > populationSize) break; // This would be an error- more initial queries than the population allows
		}
		padPopulation(population.size());
	}
	
	// Argument defines the first N that will be copied
	// from the current population list. If argument
	// is larger than size of list, can copy any in the list,
	// including ones that are added during this padding
	// operation, thus allowing mutations of mutations
	private void padPopulation(int popCeiling) {
		IO.log(IO.LEVEL.LEVEL_0, "Padding population: INIT: current size is " + population.size() + " and target is " + popSize);
		Map<String, String> flags = new HashMap<String, String>();
		flags.put("returnProcess", "copy"); // Tell the mutation algorithm to return a mutated copy
		flags.put("MAKE_NEW_RANDOM", ".1");
        // Temporarily remove all 'all' queries; note that there should be only 1, and if there are too many it can crash this.
		List<GDELT_Query> queriesWithAll = new ArrayList<GDELT_Query>();
		for(int i = population.size() - 1; i >= 0; i--) {
			if(population.get(i).all) {
				queriesWithAll.add(population.remove(i));
			}
		}
		for(int i = 0; (i < 3) && (population.size() < popSize); i++) { // Just intentionally grab the 1st here; they were the best- copy them!
			for(int j = 0; j < 5; j++) population.add(population.get(i).mutate(flags));
		}
		for(int i = 0; (i < 5) && (population.size() < popSize); i++) {
			GDELT_Query toAdd = population.get((int)(Math.floor(Math.random() * Math.min(population.size(),popCeiling)))).clone();
			if(toAdd.childQueries.size() < 20) toAdd.childQueries.add(population.get((int)(Math.floor(Math.random() * Math.min(population.size(),popCeiling)))).clone());
			population.add(toAdd);
		}
		while(population.size() < popSize) {
            // System.out.println("population.size() = " + population.size());
			// TBD: Check to make sure the population does not contain an exactly equivalent query?
			population.add(population.get((int)(Math.floor(Math.pow(Math.random(),3) * Math.min(population.size(),popCeiling)))).mutate(flags));
		}
		if(queriesWithAll.size() > 0) {
			IO.log(IO.LEVEL.LEVEL_2, "Adding " + queriesWithAll.size() + " 'all' queries back into population");
			queriesWithAll.addAll(population);
			population.clear();
			population.addAll(queriesWithAll);
		}
	}
	

	public int countOfTotalIterations() {
		return population.size();
	}
	
	@Override
	public void recordScore(SmartScore score) {
		results.add(new ScorePair(score, population.get(index - 1)));
	}
	
	public void evolve() {
		population.clear();
		int i = 0;
		IO.log(IO.LEVEL.LEVEL_3, "EVOLVING: " + results.size());
		for(ScorePair result: results) {
			population.add(result.query);
			i++;
			if(i > survivorCount) break;
		}
		results.clear();
		padPopulation(survivorCount);
		IO.log(IO.LEVEL.LEVEL_3, "RESETTING INDEX FROM " + index);
		index = 0;
	}
	
	public GDELT_Query nextQuery() {
		GDELT_Query ret = null;
		if(index < population.size()) {
			ret = population.get(index);
			index++;
		}
		return ret;
	}
	
}
