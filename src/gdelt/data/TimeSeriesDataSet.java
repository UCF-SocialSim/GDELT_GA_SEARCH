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
package gdelt.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

import gdelt.utils.TimeSeries;

/**
 * Represents a larger data collection, i.e.
 * a collection of time series that can be identified by
 * name and that have the same start, resolution,
 * and chrono unit values.
 */
public class TimeSeriesDataSet {

	private Map<String, TimeSeries> data        = new TreeMap<String, TimeSeries>();
	private LocalDateTime           start;
	private int                     resolution;
	private ChronoUnit              chronoUnit;

	/**
	 * Note: Will not load anything before start time
	 * @param filename
	 * @param startTime
	 * @param res
	 * @param chronoUnit
	 */
	public TimeSeriesDataSet(String filename, LocalDateTime startTime, int res, ChronoUnit unit) {
		start      = startTime;
		resolution = res;
		chronoUnit = unit;
	    try(BufferedReader reader = new BufferedReader(new FileReader(filename))){	   
	    	DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
	    	String line = null;	            	
	    	while ((line = reader.readLine()) != null) {
        		String[] values = line.split(",");
        		String date = values[2];
        		LocalDateTime time = null;
        		try {
        			time = (LocalDate.parse(date, dtf)).atStartOfDay();
        			if(startTime.isAfter(time)) time = null; // Just skip it; too early
        		}
        		catch(Exception E) {
        			time = null;
        		} // If it can't be parsed
        		if(time != null) {
	        		String label = values[0] + "_" + values[1];	
	    			long count = Long.parseLong(values[3]);	        	
	    			if(!data.containsKey(label)) data.put(label, new TimeSeries(start, resolution, chronoUnit));
	    			data.get(label).addCounts(time, count); 
        		}
        	}
		}
		catch(Exception E) {
            System.out.println(E.getMessage());
		}
	}
	
	public LocalDateTime getStart() {
		return start.plus(0, ChronoUnit.DAYS); // will force returning a copy
	}

	public int getResolution() {
		return resolution;
	}

	public ChronoUnit getChronoUnit() {
		return chronoUnit;
	}

	public TimeSeries get(String name) {
		return data.get(name);
	}
	
	public TimeSeries remove(String name) {
		return data.remove(name);
	}
	
	public void add(String name, TimeSeries dataseries) {
		if(
			(dataseries.getChronoUnit() == chronoUnit) && 
			(dataseries.getResolution() == resolution)   && 
			(dataseries.getTZero().isEqual(start))) {
			data.put(name, dataseries);
		}
	}
	
	public void report() {
		System.out.println("Data has " + data.size() + " elements.");
		for(Map.Entry<String, TimeSeries> entry: data.entrySet()) {
			System.out.print(entry.getKey() + ": ");
			double[] values = entry.getValue().getValues();
			for(double value: values) System.out.print(value + ",");
			System.out.println();
		}
	}	
}
