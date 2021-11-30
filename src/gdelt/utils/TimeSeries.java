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
package gdelt.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;

public class TimeSeries extends ZeroIndexedSeries{
	
	protected LocalDateTime tZero;
	protected ChronoUnit    resolutionUnit;
	protected int           resolution;
	
	public int getIndexOf(LocalDateTime time) {
		//System.out.println("GETTING INDEX OF " + time);
		long numberOfIntervals = (resolutionUnit.between(tZero, time) / resolution); // Simple case
		if(time.isBefore(tZero)) {
	    	numberOfIntervals = (-1 * (resolutionUnit.between(time,  tZero) / resolution)) - 1;
	    	if(time.plus((resolutionUnit.between(time,  tZero) / resolution), resolutionUnit).equals(tZero)) numberOfIntervals++;
	    }
		//System.out.println("NUM INT: " + numberOfIntervals + " final: " + (numberOfIntervals + zeroIndex));
		return (int)numberOfIntervals + zeroIndex;
	}
	
	public TimeSeries(LocalDateTime zeroTime, int res, ChronoUnit resUnit) {
		tZero          = zeroTime;
		resolution     = res;
		resolutionUnit = resUnit;
	}
	
	public TimeSeries(LocalDateTime zeroTime, int res, ChronoUnit resUnit, int zeroPosition, double[] series) {
		super(zeroPosition, series);
		tZero          = zeroTime;
		resolution     = res;
		resolutionUnit = resUnit;
	}
	
	public void addCounts(LocalDateTime time, double elementsToAdd) {
		addCounts(getIndexOf(time), elementsToAdd);
	}

    public void increment(LocalDateTime time) {
	  addCounts(time, 1);
    }
	
	public double getCountAt(LocalDateTime time) {
		return getCountAt(getIndexOf(time));
	}
	
	
    public void report(IO.LEVEL level) {
    	if(IO.log(level, "Reporting Time series: + " + tZero + " " + resolutionUnit)) // Skip this if it would write nothing
    		for(int i = 0; i < values.length; i++) IO.log(level, tZero.plus((i - zeroIndex) * resolution, resolutionUnit) + " " + values[i]);
	}
    
    
    public void report(IO.LEVEL level, String prefix) {
    	if(IO.log(level, "Reporting Time series:")) // Skip this if it would write nothing
    		for(int i = 0; i < values.length; i++) IO.log(level, prefix + " " + tZero.plus((i - zeroIndex) * resolution, resolutionUnit) + " " + values[i]);
	}
	
    public void report() {
		for(int i = 0; i < values.length; i++) System.out.println(tZero.plus((i - zeroIndex) * resolution, resolutionUnit) + " " + values[i]);	
    }
	
	
//    public void report(boolean reportToLog) {
//		for(int i = 0; i < values.length; i++) 
//			if(reportToLog) IO.log(tZero.plus(i - zeroIndex, resolutionUnit) + " " + values[i]);
//			else            System.out.println(tZero.plus(i - zeroIndex, resolutionUnit) + " " + values[i]);	
//	}
	
	public TreeMap<LocalDateTime, Double> getMapOfValues(){
		TreeMap<LocalDateTime, Double> ret = new TreeMap<LocalDateTime, Double>();
		for(int i = 0; i < values.length; i++) ret.put(tZero.plus((i - zeroIndex) * resolution, resolutionUnit), values[i]);
		return ret;
	}
	
	public void add(TimeSeries other) {
		TreeMap<LocalDateTime, Double> otherResults = other.getMapOfValues();
		for(int i = 0; i < values.length; i++) {
			Double otherResult = otherResults.get(tZero.plus((i - zeroIndex) * resolution, resolutionUnit));
			if(otherResult != null) values[i] += otherResult;
		}
	}
	
	public TimeSeries lift(LocalDateTime zeroTime, LocalDateTime earliestTime, LocalDateTime latest) {
		TimeSeries ret = new TimeSeries(zeroTime, resolution, resolutionUnit);
		LocalDateTime pos = earliestTime; 
		while(pos.isBefore(latest)) {
			ret.addCounts(pos, getCountAt(pos));
			pos = pos.plus(resolution, resolutionUnit);
		}
		return ret;
	}
	
	public TimeSeries lift(LocalDateTime zeroTime, int tMin, int tMax) {
		TimeSeries ret = new TimeSeries(zeroTime, resolution, resolutionUnit);
		LocalDateTime pos = zeroTime.plus(tMin, resolutionUnit);
		LocalDateTime end = zeroTime.plus(tMax, resolutionUnit);
		while(pos.isBefore(end)) {
			ret.addCounts(pos, getCountAt(pos));
			pos = pos.plus(resolution, resolutionUnit);
		}
		return ret;
	}
	
	public LocalDateTime getTZero() {
		return tZero;
	}
	
	public int getResolution() {
		return resolution;
	}
	
	public ChronoUnit getChronoUnit() {
		return resolutionUnit;
	}
	
	public void trim(int tMin, int tMax, ChronoUnit resUnit) {
		// Need to convert these to the resolutionUnit in order to call the parent class's method
		trim(((int)(resolutionUnit.between(tZero, tZero.plus(tMin, resUnit))/resolution)), 
			 ((int)(resolutionUnit.between(tZero, tZero.plus(tMax, resUnit))/resolution)));
	}

}