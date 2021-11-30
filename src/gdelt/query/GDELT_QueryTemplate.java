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
package gdelt.query;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import gdelt.utils.IO;

/**
 * A QueryTemplate allows you to specify which elements of the query will be used;
 * in earlier versions of the code, this required hard-coding the query class,
 * but using this template the details can be specified at run-time.
 */
public class GDELT_QueryTemplate {
	
	public enum GDELT_QueryField{
		ACTOR_1_COUNTRY_CODES,
		ACTOR_2_COUNTRY_CODES,
		GOLDSTEIN_SCALE,
		AVERAGE_TONE,
		ROOT_CODE,
		LAT_LON
	}


	// Main collection of values
	
	public static boolean  matchActor1CountryCodes        = false;
	public static boolean  matchActor2CountryCodes        = false;
	public static boolean  matchGoldsteinScale            = false;
	public static boolean  matchRootCode                  = false;
	public static boolean  matchAverageTone               = false;
	public static boolean  matchLatLon                    = false;
	public static String[] actor1CountryCodes             = { };
	public static String[] actor2CountryCodes             = { };
	public static double   probabilityOfMutatingTimeShift = 0;


	// Mutation selection
	
	/**
	 * A method of specifying which elements of the query are
	 * mutated, and with what weights.
	 */
	private static class GDELT_Mutator_Selector{
		private int sum;
		
		private ArrayList<Integer>          weights  = new ArrayList<Integer>();
		private ArrayList<GDELT_QueryField> fields   = new ArrayList<GDELT_QueryField>();
		
		public void addField(GDELT_QueryField field, int weight) {
			fields.add(field);
			weights.add(weight);
			sum += weight;
		}
		
		public GDELT_QueryField chooseField() {
			if(fields.size() == 0) return null;
			if(fields.size() == 1) return fields.get(0);
			
			int val = (int)(Math.floor(Math.random() * sum));
			int index = 0;
			while(index < fields.size()) {
				int comp = weights.get(index);
				if(val <= comp) return fields.get(index);
				val -= comp;
				index++;
			}
			return fields.get(fields.size() - 1); // Should never happen
		}
	}
	
	private static ArrayList<GDELT_Mutator_Selector> mutatorSelectors = new ArrayList<GDELT_Mutator_Selector>();
	
	public static void initializeMutatorSelector(Vector<GDELT_QueryField> fields, Vector<Integer> weight) {
		if(fields.size() != weight.size()) return;
		GDELT_Mutator_Selector newSelector = new GDELT_Mutator_Selector();
		for(int i = 0; i < fields.size(); i++) {
			newSelector.addField(fields.elementAt(i), weight.elementAt(i));
		}
		IO.log(IO.LEVEL.LEVEL_2, "ADDING A MUTATOR SELECTOR WITH " + newSelector.fields.size());
		mutatorSelectors.add(newSelector);
	}
	
	public static  GDELT_QueryField[] getFieldsToMutate() {
		IO.log(IO.LEVEL.LEVEL_3, "GETTING FIELDS TO MUTATE: " + mutatorSelectors.size()	);
		GDELT_QueryField[] ret = new GDELT_QueryField[mutatorSelectors.size()];
		for(int i = 0; i < mutatorSelectors.size(); i++) {
			ret[i] = mutatorSelectors.get(i).chooseField();
			IO.log(IO.LEVEL.LEVEL_4, "  SELECTED " + ret[i].name());
		}
		return ret;
	}
	
	/**
	 * Configures the query template using the properties
	 * provided. Ignores properties that do not apply
	 * @param properties
	 */
	public static void setAll(Map<String, String> properties) {

		Vector<GDELT_QueryField> fieldsToMutate  = new Vector<GDELT_QueryField>();
		Vector<Integer>          mutationWeights = new Vector<Integer>();
		
		for(String prop: properties.keySet()) IO.log(IO.LEVEL.LEVEL_0, "PROPERTY: " + prop + " value: " + properties.get(prop));
		
		// Initialize the query template
		// Matching
		if(properties.containsKey("Actor1CountryCodes")) {
		  matchActor1CountryCodes(properties.get("Actor1CountryCodes").split(","));
		  if(properties.containsKey("Actor1CountryCodesMutateWeight")) {
			  fieldsToMutate.add(GDELT_QueryField.ACTOR_1_COUNTRY_CODES);
			  mutationWeights.add(Integer.parseInt(properties.get("Actor1CountryCodesMutateWeight")));
		  }
		  IO.log(IO.LEVEL.LEVEL_4, "Match Actor 1: " + matchActor1CountryCodes + " " + properties.get("Actor1CountryCodes"));
		}
		if(properties.containsKey("Actor2CountryCodes")) {
		  matchActor2CountryCodes(properties.get("Actor2CountryCodes").split(","));
		  if(properties.containsKey("Actor2CountryCodesMutateWeight")) {
			  fieldsToMutate.add(GDELT_QueryField.ACTOR_2_COUNTRY_CODES);
			  mutationWeights.add(Integer.parseInt(properties.get("Actor2CountryCodesMutateWeight")));
		  }
		  IO.log(IO.LEVEL.LEVEL_4, "Match Actor 2: " + matchActor2CountryCodes + " " + properties.get("Actor2CountryCodes"));
		}
		if(properties.containsKey("MatchGoldsteinScale")) {
		  matchGoldsteinScale = properties.get("MatchGoldsteinScale").compareToIgnoreCase("true") == 0;
		  if(properties.containsKey("GoldsteinScaleMutationWeight")) {
		    fieldsToMutate.add(GDELT_QueryField.GOLDSTEIN_SCALE);
		    mutationWeights.add(Integer.parseInt(properties.get("GoldsteinScaleMutateWeight")));
	      }
		  IO.log(IO.LEVEL.LEVEL_4, "Match Goldstein: " + matchGoldsteinScale);
		}
		if(properties.containsKey("MatchAverageTone")) {
		  matchAverageTone = properties.get("MatchAverageTone").compareToIgnoreCase("true") == 0;
		  if(properties.containsKey("AverageToneMutationWeight")) {
		    fieldsToMutate.add(GDELT_QueryField.AVERAGE_TONE);
		    mutationWeights.add(Integer.parseInt(properties.get("AverageToneMutateWeight")));
	      }
		  IO.log(IO.LEVEL.LEVEL_4, "Match AvgTone: " + matchAverageTone);
		}
		if(properties.containsKey("MatchRootCode")) {
		  matchRootCode = properties.get("MatchRootCode").compareToIgnoreCase("true") == 0;
		  if(properties.containsKey("RootCodeMutationWeight")) {
		    fieldsToMutate.add(GDELT_QueryField.GOLDSTEIN_SCALE);
		    mutationWeights.add(Integer.parseInt(properties.get("RootCodeMutateWeight")));
		  }
		  IO.log(IO.LEVEL.LEVEL_4, "Match Root Code: " + matchRootCode);
		}
		if(properties.containsKey("MatchLatLon")) {
		  matchLatLon = properties.get("MatchLatLon").compareToIgnoreCase("true") == 0;
		  if(properties.containsKey("LatLonMutationWeight")) {
		    fieldsToMutate.add(GDELT_QueryField.LAT_LON);
		    mutationWeights.add(Integer.parseInt(properties.get("LatLonMutateWeight")));
		  }
		  IO.log(IO.LEVEL.LEVEL_4, "Match LatLon: " + matchLatLon);
		}
		if(properties.containsKey("TimeShiftMutationProbability")) {
		  probabilityOfMutatingTimeShift = Double.parseDouble(properties.get("TimeShiftMutationProbability"));
	      IO.log(IO.LEVEL.LEVEL_0, "Match pmts: " + probabilityOfMutatingTimeShift);
		}	
		
		initializeMutatorSelector(fieldsToMutate, mutationWeights);
	}
	
	// Convenience setters
	public static void matchActor1CountryCodes(String[] codes) {
		actor1CountryCodes       = codes;
		matchActor1CountryCodes  = (actor1CountryCodes.length > 0);
	}
	
	public static void matchActor2CountryCodes(String[] codes) {
		actor2CountryCodes       = codes;
		matchActor2CountryCodes  = (actor2CountryCodes.length > 0);
	}

}
