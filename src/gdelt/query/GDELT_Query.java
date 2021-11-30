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

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gdelt.predictors.Predictor;
import gdelt.query.GDELT_QueryTemplate.GDELT_QueryField;
import gdelt.query.domains.ActionLatitude;
import gdelt.query.domains.ActionLongitude;
import gdelt.query.domains.AvgTone;
import gdelt.query.domains.GoldsteinScore;
import gdelt.query.domains.RootCode;
import gdelt.query.domains.components.AbstractQueryDomain.INIT_METHOD;
import gdelt.query.domains.components.DoubleQueryDomain;
import gdelt.query.domains.components.OrderedSetQueryDomain;
import gdelt.query.domains.components.UnorderedSetQueryDomain;
import gdelt.server.elements.GDELT_Corpus;
import gdelt.server.elements.GDELT_Global_Event;
import gdelt.utils.IO;
import gdelt.utils.TimeSeries;

public class GDELT_Query implements Comparable<GDELT_Query>{
	
	public static int MAX_SHIFT = Integer.MIN_VALUE; // These are absurd values; they MUST be explicitly set to run the sim
	public static int MIN_SHIFT = Integer.MIN_VALUE;

	public static int MAX_VIZ_OFFSET = Integer.MIN_VALUE; // These are absurd values; they MUST be explicitly set to run the sim
	public static int MIN_VIZ_OFFSET = Integer.MIN_VALUE;

	
    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gsonInstance = new Gson(); 
    private static Gson gson;
    static {
		gsonBuilder.serializeSpecialFloatingPointValues();
		gsonBuilder.setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		gson = gsonBuilder.create();
    }
    
	private static int createdNum = 0;
	private        int order;
	
	@SerializedName("returnedValuesOnLastProcess")
	public boolean returnedValuesOnLastProcess = false;
	
	@Override
	public int compareTo(GDELT_Query o) {
		return (order < o.order ? -1 : (order == o.order ? 0 : 1));
	}
	
	private TimeSeries result;

	@SerializedName("prediction_method")
	public Predictor.PREDICTION_METHOD prediction_method = Predictor.PREDICTION_METHOD.RAW;
	
	@SerializedName("childqueries")
	public List<GDELT_Query> childQueries = new ArrayList<GDELT_Query>();

	@SerializedName("shift")
	public long shift;
	
	@SerializedName("maxShift")
	public long maxShift;
	
	@SerializedName("minShift")
	public long minShift;
	
	@SerializedName("tMax")
	public int tMax;
	
	@SerializedName("tMin")
	public int tMin;
	
	@SerializedName("vizOffset")
	public long vizOffset;

	@SerializedName("maxVizOffset")
	public long maxVizOffset;
	
	@SerializedName("minVizOffset")
	public long minVizOffset;
	
	// Queries can be performed such that they impose a horizon, beyond which they do not have any visibility
	@SerializedName("dateOfHorizon")
	long  horizon;
	
	@SerializedName("multiplier")
	int multiplier = 1;
	
	// Query domains
	
	@SerializedName("goldsteinScore")
	public GoldsteinScore goldsteinScore = new GoldsteinScore(INIT_METHOD.FULL);

	@SerializedName("rootCode")
	public RootCode rootCode = new RootCode(INIT_METHOD.FULL);
	
	@SerializedName("actor1CountryCode")
	public UnorderedSetQueryDomain<String> actor1CountryCode = 
	  new UnorderedSetQueryDomain<String>("Actor1Country", INIT_METHOD.FULL, GDELT_QueryTemplate.actor1CountryCodes);
//	public UnorderedSetQueryDomain<String> actor1CountryCode = new UnorderedSetQueryDomain<String>("Actor1Country", INIT_METHOD.FULL, "USA", "EUR");
//	public UnorderedSetQueryDomain<String> actor1CountryCode = new UnorderedSetQueryDomain<String>("Actor1Country", INIT_METHOD.FULL, "VEN", "USA", "RUS", "COL", "BRA", "ESP", "PER", "EUR");

	@SerializedName("actor2CountryCode")
	public UnorderedSetQueryDomain<String> actor2CountryCode = 
	  new UnorderedSetQueryDomain<String>("Actor2Country", INIT_METHOD.FULL, "KEN","ZWE","UGA","AFR","TZA","RWA","USA","MWI","MOZ","NAM","CHN","BWA","BDI","GBR","SOM","MDG","ETH","FRA","ZAF","SDN");
//	public UnorderedSetQueryDomain<String> actor2CountryCode = new UnorderedSetQueryDomain<String>("Actor2Country", INIT_METHOD.FULL, "USA", "EUR");
//	public UnorderedSetQueryDomain<String> actor2CountryCode = new UnorderedSetQueryDomain<String>("Actor2Country", INIT_METHOD.FULL, "VEN", "USA", "RUS", "COL", "BRA", "ESP", "PER", "EUR");

	@SerializedName("avgTone")
	public AvgTone avgTone = new AvgTone(INIT_METHOD.FULL);

	@SerializedName("actionLat")
	public ActionLatitude actionLat = new ActionLatitude(INIT_METHOD.FULL);
	
	@SerializedName("actionLon")
	public ActionLongitude actionLon = new ActionLongitude(INIT_METHOD.FULL);
	
	@SerializedName("all")
	public boolean all = false;
	
	// Given a string, create an instance
	public static GDELT_Query get(String construct) {
		return gsonInstance.fromJson(construct, GDELT_Query.class);		
	}
	
	public GDELT_Query(){ 
		order = createdNum++;
	}
	
	public GDELT_Query(int tMinimum, int tMaximum, Predictor.PREDICTION_METHOD predMethod) {
		this(tMinimum, tMaximum, predMethod, INIT_METHOD.RANDOM);
		order = createdNum++;
	}
	
	public GDELT_Query(int tMinimum, int tMaximum, Predictor.PREDICTION_METHOD predMethod, INIT_METHOD init_method) {
		order             = createdNum++;
		tMin              = tMinimum;
		tMax              = tMaximum;
		prediction_method = predMethod;
		goldsteinScore    = new GoldsteinScore(init_method);
		rootCode          = new RootCode(init_method);
//		actor1CountryCode = new UnorderedSetQueryDomain<String>("Actor1Country", init_method, "VEN", "USA", "RUS", "COL", "BRA", "ESP", "PER", "EUR");
//		actor2CountryCode = new UnorderedSetQueryDomain<String>("Actor2Country", init_method, "VEN", "USA", "RUS", "COL", "BRA", "ESP", "PER", "EUR");
//		actor1CountryCode = new UnorderedSetQueryDomain<String>("Actor1Country", init_method, "null", "PAK", "CHN", "AFG", "USA", "IND");
//		actor2CountryCode = new UnorderedSetQueryDomain<String>("Actor2Country", init_method, "null", "PAK", "AFG", "CHN", "USA", "IND");
		if(GDELT_QueryTemplate.matchActor1CountryCodes) actor1CountryCode = new UnorderedSetQueryDomain<String>("Actor1Country", init_method, GDELT_QueryTemplate.actor1CountryCodes);
		if(GDELT_QueryTemplate.matchActor2CountryCodes) actor2CountryCode = new UnorderedSetQueryDomain<String>("Actor2Country", init_method, GDELT_QueryTemplate.actor2CountryCodes);
		avgTone = new AvgTone(init_method);
		maxShift = MAX_SHIFT;
		minShift = MIN_SHIFT;	
		shift = minShift;
		maxVizOffset = MAX_VIZ_OFFSET;
		minVizOffset = MIN_VIZ_OFFSET;
		vizOffset    = minVizOffset;
	}
	
	// Given an instance, create the string
	public String toString() {
    	return gson.toJson(this);
	}
	
	public static void setOffsets(Map<String, String> offsets) {
		if(offsets.containsKey("MaxShift"))     MAX_SHIFT      = Integer.parseInt(offsets.get("MaxShift"));
		if(offsets.containsKey("MinShift"))     MIN_SHIFT      = Integer.parseInt(offsets.get("MinShift"));
		if(offsets.containsKey("MaxVizOffset")) MAX_VIZ_OFFSET = Integer.parseInt(offsets.get("MaxVizOffset"));
		if(offsets.containsKey("MinVizOffset")) MIN_VIZ_OFFSET = Integer.parseInt(offsets.get("MinVizOffset"));
	}
	
	public void clearResult() {
		result = null;
		resetReturn();
	}
	
	public void resetReturn() {
		returnedValuesOnLastProcess = false;
		for(GDELT_Query query: childQueries) query.resetReturn();
	}
	
	public String blankClone() {
		GDELT_Query ret = GDELT_Query.get(this.toString());
		ret.clearResult();
		return ret.toString();
	}
	
	// Note: This is not recursive to child queries
	private boolean articleIsValid(GDELT_Global_Event article){
		return  all || (
				(!GDELT_QueryTemplate.matchGoldsteinScale     || goldsteinScore.matches(article.GoldsteinScale)) &&
				(!GDELT_QueryTemplate.matchRootCode           || rootCode.matches((int)article.EventRootCode)) &&
				(!GDELT_QueryTemplate.matchAverageTone        || avgTone.matches(article.AvgTone)) &&
				(!GDELT_QueryTemplate.matchActor1CountryCodes || actor1CountryCode.matches(article.Actor1CountryCode)) && 
				(!GDELT_QueryTemplate.matchActor2CountryCodes || actor2CountryCode.matches(article.Actor2CountryCode)) &&
				(!GDELT_QueryTemplate.matchLatLon ||
						(actionLat.matches(article.ActionGeo_Lat) &&
				         actionLon.matches(article.ActionGeo_Long))));
	}
	
	
	public TimeSeries getResult(LocalDateTime zeroTime, int res, ChronoUnit resUnit, GDELT_Corpus corpus) {
		return getResult(zeroTime, res, resUnit, corpus, false);
	}
	
	public TimeSeries getResult(LocalDateTime zeroTime, int res, ChronoUnit resUnit, GDELT_Corpus corpus, boolean useDateAdded) {
		init(zeroTime, res, resUnit);
		process(corpus, useDateAdded);
		return getResult();
	}
	
	protected void init(LocalDateTime zeroTime, int res, ChronoUnit resUnit) {
		for(GDELT_Query query: childQueries) query.init(zeroTime, res, resUnit);
		result = new TimeSeries(zeroTime, res, resUnit);
	}
	
	public boolean process(GDELT_Corpus corpus){
		return process(corpus, null, false);
	}

	public boolean process(GDELT_Corpus corpus, boolean useDateAdded){
		return process(corpus, null, useDateAdded);
	}
	
	public boolean process(GDELT_Corpus corpus, Map<LocalDateTime, Map<GDELT_Global_Event, Double>> eventWeights) {
		return process(corpus, eventWeights, false);
	}
	
	public boolean process(GDELT_Corpus corpus, Map<LocalDateTime, Map<GDELT_Global_Event, Double>> eventWeights, boolean useDateAdded) {
		//IO.log(IO.LEVEL.LEVEL_3," ---------------      COUNT OF CHILD QUERIES          " + childQueries.size());
		if(all) IO.log(IO.LEVEL.LEVEL_0, "PROCESSING QUERY WITH ALL VALUE: " + all);
		if(multiplier > 1)IO.log(IO.LEVEL.LEVEL_4, "PROCESSING QUERY WITH MULTIPLIER VALUE: " + multiplier);
		for(GDELT_Query query: childQueries) returnedValuesOnLastProcess = (query.process(corpus) | returnedValuesOnLastProcess); // Note: do not short circuit! 
		for(GDELT_Global_Event event: corpus.allEvents) {
			LocalDateTime eventDate = (useDateAdded ? event.dateAndTimeAdded : event.dateOfEvent).toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
			LocalDateTime eventVisible = event.dateAndTimeAdded.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
			LocalDateTime position  = eventDate.plusHours(shift);
		    // This reflects the date/time that our query will be 'counting toward';
			LocalDateTime visibilityWindowCloses = position.plusHours(vizOffset); // e.g. -24
			if(!eventVisible.isAfter(visibilityWindowCloses)) { // Can't be later than our visibility window
				if(all || articleIsValid(event)) {
					returnedValuesOnLastProcess = true;
					result.addCounts(position, multiplier);
					
					if(eventWeights != null) {
						// Keep a record of the specific articles and their weights
						Map<GDELT_Global_Event, Double> map = eventWeights.get(position);
						if(map == null) {
							map = new HashMap<GDELT_Global_Event, Double>();
							eventWeights.put(position, map);
						}
						Double d = map.get(event);
						if(d == null) d = 0d;
						d += multiplier;
						map.put(event, d);
					}
				}
				IO.log(IO.LEVEL.LEVEL_4, "EVENT DATE " + eventDate + " pos " + position + " viz " + eventVisible + " = VALID; WINDOW: " + visibilityWindowCloses + " " + useDateAdded);
			}
			else IO.log(IO.LEVEL.LEVEL_4, "EVENT DATE " + eventDate + " pos " + position + " viz " + eventVisible + " = NON-VALID; WINDOW: " + visibilityWindowCloses + " " + useDateAdded);
		}
		if(IO.log(IO.LEVEL.LEVEL_4, "Giving query result report")) {
			result.report(IO.LEVEL.LEVEL_4);
			IO.log(IO.LEVEL.LEVEL_4, "DONE WITH REPORT");
		}
		return returnedValuesOnLastProcess;
	}

	
//	
//	public boolean process(GDELT_Corpus corpus, Map<LocalDateTime, Map<GDELT_Global_Event, Double>> eventWeights, boolean useDateAdded){
//		//IO.log(IO.LEVEL.LEVEL_3," ---------------      COUNT OF CHILD QUERIES          " + childQueries.size());
//		if(all) IO.log(IO.LEVEL.LEVEL_0, "PROCESSING QUERY WITH ALL VALUE: " + all);
//		if(multiplier > 1)IO.log(IO.LEVEL.LEVEL_4, "PROCESSING QUERY WITH MULTIPLIER VALUE: " + multiplier);
//		for(GDELT_Query query: childQueries) returnedValuesOnLastProcess = (query.process(corpus) | returnedValuesOnLastProcess); // Note: do not short circuit! 
//		for(GDELT_Global_Event event: corpus.articles) {
//			if(all || articleIsValid(event)) {
//				returnedValuesOnLastProcess = true;
//				LocalDateTime position = (useDateAdded ? event.dateAndTimeAdded : event.dateOfEvent).toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime().plusHours(shift);
//				result.addCounts(position, multiplier);
//
//			}
//		}
//		if(IO.log(IO.LEVEL.LEVEL_4, "Giving query result report")) {
//			result.report(IO.LEVEL.LEVEL_4);
//			IO.log(IO.LEVEL.LEVEL_4, "DONE WITH REPORT");
//		}
//		return returnedValuesOnLastProcess;
//	}
	
	protected TimeSeries getResult() {
		for(GDELT_Query query: childQueries) result.add(query.getResult());
		result.trim(tMin, tMax, ChronoUnit.DAYS); // Trimming always occurs in day intervals, governed by tMin and tMax
		return result;
	}
	
	
	public void evaluate(GDELT_Global_Event event, TimeSeries ... timeSeries ) {
		TimeSeries[] toPassToChild = new TimeSeries[timeSeries.length];
		int idx = 0;
		for(TimeSeries series: timeSeries) {
		  if(series != null) {
			  LocalDateTime eventTime = event.dateOfEvent.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime().plusHours(shift);
			  int index = series.getIndexOf(eventTime);
			  if(index >= tMin && index < tMax) {
				  toPassToChild[idx++] = series; // This will be passed to children
				  if(this.articleIsValid(event)) series.increment(index); // Note: Could pass event time, but we already have the index...
			  }
		  }
		}
		for(GDELT_Query query: childQueries) query.evaluate(event, toPassToChild);
		for(TimeSeries series: timeSeries) series.trim(tMin, tMax);
	}
	
	
	
	
	
	
	
	public GDELT_Query clone() {
		return get(this.toString()); // Serializes this and constructs a new, identical instance... inefficient? Maybe, but also never needs updated.
	}
	
	public void prune() {
		IO.log(IO.LEVEL.LEVEL_4, "PRUNING...");
		for(int i = childQueries.size() - 1; i >= 0; i--) {
			if(childQueries.get(i).returnedValuesOnLastProcess == false) childQueries.remove(i);
		}
	}
	
	// Mutate with no instructions about how
	public GDELT_Query mutate() {
		return mutate(new HashMap<String, String>());
	}
	
	// Note: This may return a new query, or it
	// May split the top query and make the new one
	// a child query, or it may void out the parent
	// query and make two new child queries
	public GDELT_Query mutate(Map<String, String> flags) {
		int before = childQueries.size();
		prune();
		if(childQueries.size() != before) IO.log(IO.LEVEL.LEVEL_4, "Pruned queries: " + before + " -> " + childQueries.size());
		GDELT_Query ret = null;
		if(flags.containsKey("MAKE_NEW_RANDOM")) {
			if(Math.random() < Double.parseDouble(flags.get("MAKE_NEW_RANDOM"))){
				return new GDELT_Query(this.tMin, this.tMax, this.prediction_method);
			}
		}
		String returnProcess = flags.containsKey("returnProcess") ? flags.get("returnProcess") : "modify"; // Default is to modify this element in place
		switch(returnProcess) {
			case("copy"):{
				ret = this.clone(); // Deep clone
				break;
			}
			case("modify"):{
				ret = this;
				if(all) return ret; // No modification to 'all' query
				break;
			}
			case("split"):{
				// Let's not use this right now...
			}
			default:
				// Do nothing
		}
		
		
		// Small chance of removing a child query, if there is one.
		if(ret.childQueries.size() > 0 && Math.random() < .1) {
			ret.childQueries.remove((int)(Math.floor(Math.random() * ret.childQueries.size())));
		}
		
		Map<String, String> childFlags = new HashMap<String, String>();
		childFlags.putAll(flags);
		childFlags.put("returnProcess", "modify"); // Override this- modify in place for children...
		for(GDELT_Query child: ret.childQueries) child.mutate(childFlags);
		
		
		// Different ways to mutate are placed here, possibly driven by flags
		// For now:		
		GDELT_QueryField[] fieldsToMutate = GDELT_QueryTemplate.getFieldsToMutate();
		for(GDELT_QueryField fieldToMutate: fieldsToMutate) {
			switch(fieldToMutate) {
				case ACTOR_1_COUNTRY_CODES: ret.actor1CountryCode.mutate(); break;
				case ACTOR_2_COUNTRY_CODES: ret.actor2CountryCode.mutate(); break;
				case GOLDSTEIN_SCALE:       ret.goldsteinScore.mutate();    break;
				case AVERAGE_TONE:          ret.avgTone.mutate();           break;
				case ROOT_CODE:             ret.rootCode.mutate();          break;
				case LAT_LON:{
					double v = Math.random();
					if(v < 0.66667) actionLat.mutate(); // Note that they can both mutate, but at least one will
					if(v > 0.33333) actionLon.mutate();
					break;
				}
			}
		}
				
		// If you want to change the time shift, use these lines
		if(Math.random() < GDELT_QueryTemplate.probabilityOfMutatingTimeShift) {
			long shiftMagnitude = 24; // Units are hours
			long initialVal = shift;
			if(shift == minShift) ret.shift += shiftMagnitude;
			else if(shift == maxShift) ret.shift -= shiftMagnitude;
			else {
				ret.shift += (Math.random() < .5) ? shiftMagnitude : (-1 * shiftMagnitude);
			}
			IO.log(IO.LEVEL.LEVEL_4, "SHIFTING THE SHIFT!!!!!!!!!!!!!!!!!!!!!!! " + initialVal + " -> " + ret.shift);
		}
		
		
		// Split By Value
		
		// Split By Time
		
		// Choose a time between 
//		double switch2 = Math.random();
//		if(switch2 < .05) {
//			IO.log(IO.LEVEL.LEVEL_0, "Multiplying multiplier... ORIG: " + multiplier);
//			if(multiplier < 10000000) multiplier += (((double)multiplier) * Math.random()); // need a ceiling- doubling gets big fast!
//			IO.log(IO.LEVEL.LEVEL_0, "Multiplying multiplier... END : " + multiplier);
//		}
//		else if(switch2 < .2) {
//			if   (multiplier > 1) multiplier += (Math.random() < .5 ? -1 : 1);
//			else                  multiplier += 1; // Yes, it can increase without bound... for now
//		}
		
		
		
		return ret;
	}
	
	public void report(IO.LEVEL level) {
		result.report(level);
	}
	
	
	// Modify to grab totals and weights...
	public String summaryOfCriteria() {
		// Which elements to retrieve
		ArrayList<String> toRetrieve = new ArrayList<String>();
		if(GDELT_QueryTemplate.matchActor1CountryCodes) toRetrieve.add("Actor1CountryCode");
		if(GDELT_QueryTemplate.matchActor2CountryCodes) toRetrieve.add("Actor2CountryCode");
		if(GDELT_QueryTemplate.matchGoldsteinScale)     toRetrieve.add("GoldsteinScore");
		if(GDELT_QueryTemplate.matchRootCode)           toRetrieve.add("RootCode");

		// Prepare the data structure to collect the data
		Map<String, Map<String, Long>>  toConstruct = new HashMap<String, Map<String, Long>>();;
		for(String domain: toRetrieve) toConstruct.put(domain, new HashMap<String, Long>());
		
		// Call the collection routine
	    summaryOfCriteria(toConstruct);
	    	    
	    // Prepare the string output to return
		StringWriter sw = new StringWriter();
		for(String domain: toRetrieve) {
			sw.write("SUMMARY: " + domain + ": Total Scores: " );
			String[] lines = IO.formatAsSortedCounts(toConstruct.get(domain), true).split(IO.LS);
			String sep = "";
			for(String line: lines) {
				String[] l = line.split(",");
				sw.write(sep + l[0] + " (" + l[1] + ")");
				sep = ",";
			}
			sw.write(IO.LS);
		}
		
		
		return sw.toString();
	}
	
	
	private void getMatchingValues(UnorderedSetQueryDomain<String> domain, Map<String, Long> results) {
		for(String countryCode: domain.getMatchingValues()) {
			if(!results.containsKey(countryCode)) results.put(countryCode, new Long(multiplier));
			else {
				long l = results.get(countryCode);
				results.put(countryCode, multiplier + l);
			}
		}
		
	}
	
	private void getMatchingValues(DoubleQueryDomain domain, Map<String, Long> results) {
		results.put("" + domain.min, new Long(multiplier));
		results.put("" + domain.max, new Long(multiplier));
	}
	
	private void getMatchingValues(OrderedSetQueryDomain<Integer> domain, Map<String, Long> results) {
	  for(Integer integer: domain.getMatchingValues()) {
		if(!results.containsKey("" + integer)) results.put("" + integer, new Long(multiplier));
		else {
			long l = results.get("" + integer);
			results.put("" + integer, multiplier + l);
		}		  
	  }
	}
	
	private void summaryOfCriteria(Map<String, Map<String, Long>> values) {
		// Retrieve the values for this query
		for(String domain: values.keySet()) {
			Map<String, Long> results = values.get(domain);
			switch(domain){
				case "Actor1CountryCode":
					getMatchingValues(actor1CountryCode, results);
					break;
				case "Actor2CountryCode":
					getMatchingValues(actor2CountryCode, results);
					break;
				case "RootCode":
					getMatchingValues(rootCode, results);
					break;
				case "GoldsteinScore":
					getMatchingValues(goldsteinScore, results);
					break;
			}
		}
		if(childQueries.size() > 0) IO.log(IO.LEVEL.LEVEL_0, "GETTING SUMMARY FOR CHILD QUERIES- " + childQueries.size());
		for(GDELT_Query query: childQueries) query.summaryOfCriteria(values);
	}
}