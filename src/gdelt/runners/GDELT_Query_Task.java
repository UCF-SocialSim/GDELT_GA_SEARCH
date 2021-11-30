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
package gdelt.runners;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gdelt.predictors.P_Scripts;
import gdelt.predictors.Predictor_Factory;
import gdelt.query.GDELT_Query;
import gdelt.server.elements.GDELT_Corpus;
import gdelt.utils.IO;
import gdelt.utils.ZeroIndexedSeries;

public class GDELT_Query_Task implements P_Scripts {
	
	// Static Variables
	private static Gson gson = null;

	// Retrieve an instance of the JSON builder
	public static Gson getGson() {
		if(gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.serializeSpecialFloatingPointValues();
			gsonBuilder.setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");	
			gsonBuilder.enableComplexMapKeySerialization();
			gson = gsonBuilder.create();
		}
		return gson;
	}

	// Build one of these from JSON
	public static GDELT_Query_Task get(String taskString) {
		GDELT_Query_Task ret = getGson().fromJson(taskString, GDELT_Query_Task.class);
		return ret;		
	}
	
	// Instance Variables
	@SerializedName("query")
	public GDELT_Query query;
	
	@SerializedName("resolution")
	int resolution;
	
	@SerializedName("resUnit")
	ChronoUnit resUnit = ChronoUnit.HOURS;

	@SerializedName("resultsMap")
	public Map<LocalDateTime, ZeroIndexedSeries> resultsMap = null;
	
	@SerializedName("useDateTimeAdded")
	public boolean useDateTimeAdded  = false;
	
	
	public GDELT_Query_Task(GDELT_Query q, int res, ChronoUnit resolutionUnit, boolean useDateTimeAdded, LocalDateTime ... timesZero) {
		query = q;
		resolution = res;
		resUnit = resolutionUnit;
		resultsMap = new TreeMap<LocalDateTime, ZeroIndexedSeries>();
		this.useDateTimeAdded = useDateTimeAdded;
		for(LocalDateTime t: timesZero) resultsMap.put(t, new ZeroIndexedSeries());
	}
	
	// Get a serialized representation of this object
	public String getJson() {
		return getGson().toJson(this);
	}
	
	// Evaluate a corpus and place the results in the results map
	public void evaluateCorpus(GDELT_Corpus corpus){
		// Version 1
		query.resetReturn();
		for(LocalDateTime tZero: resultsMap.keySet()) {
			TreeMap<LocalDateTime, Double> rets = query.getResult(tZero, resolution, resUnit, corpus, useDateTimeAdded).getMapOfValues();
			if(rets == null) System.out.println("RETS IS NULL: " + tZero);
			// Place in the map
			int z = 0;
			if(rets != null) {
				double[] toPlace = new double[rets.size()];	
				int i = 0;
				for(Map.Entry<LocalDateTime, Double> entry: rets.entrySet()) {
					toPlace[i] = entry.getValue();
					if(entry.getKey().equals(tZero)) z = i;
					i++;
				}
				resultsMap.put(tZero, new ZeroIndexedSeries(z, toPlace));
			}
		}
	}
	
	
	// Specify a set from  the results set and a training series,
	// and get back the predictions
	public double[] getPredictions(LocalDateTime whichSet, ZeroIndexedSeries trainseries) {
		return Predictor_Factory.get(query.prediction_method).getPredictions(resultsMap.get(whichSet), trainseries);
	}
	
	public void reportResults(IO.LEVEL level) {
		query.report(level);
	}

}