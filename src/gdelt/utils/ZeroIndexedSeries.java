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

import com.google.gson.annotations.SerializedName;

/**
 * A ZeroIndexedSeries is a collection of double values that
 * form a single array, but can be indexed as if the
 * element of the array with index = 0 is not the first element,
 * but one of the later ones. This allows references to element
 * -t, indicating an element prior to zero (and usually,
 * prior to time zero).
 */
public class ZeroIndexedSeries{
	
	@SerializedName("values")
	protected double[] values;
	
	@SerializedName("zeroIndex")
	protected int    zeroIndex = 0;       // Position in the allResults array that corresponds to tzero
	
	// Constructors
	public ZeroIndexedSeries() {
		zeroIndex = 0;
		values = new double[1];
	}
	
	public ZeroIndexedSeries(int zeroPosition, double[] series) {
		zeroIndex = zeroPosition;
		values    = series;
	}
	
	/**
	 * Appends a value to the end of this series
	 * @param toAdd
	 */
	public void append(double[] toAdd) {
		// Debug
		if(toAdd == null || toAdd.length <= 0) return; // Don't need to do anything
		if(IO.log(IO.LEVEL.LEVEL_0, "Appending to series of length " + values.length + " a new series of " + toAdd.length)) {
			for(int x = 0; x < values.length; x++) IO.log(IO.LEVEL.LEVEL_0, "ORIG[" + x + "] = " + values[x]);
			for(int x = 0; x < toAdd.length; x++) IO.log(IO.LEVEL.LEVEL_0, "ADD [" + x + "] = " + toAdd[x]);
		}
		
		double[] newValues = new double[values.length + toAdd.length];
		int i = 0;
		for(int x = 0; x < values.length; x++) newValues[i++] = values[x];
		for(int x = 0; x < toAdd.length;  x++) newValues[i++] = toAdd[x];
		values = newValues;
		
		if(IO.log(IO.LEVEL.LEVEL_0, "Result")) {
			for(int x = 0; x < values.length; x++) IO.log(IO.LEVEL.LEVEL_0, "NEW [" + x + "] = " + values[x]);
		}
	}
	
	
	// Simple getters
	public double[] getValues() {
		return values;
	}
	
	/**
	 * Get all the values from time tZero forward (inclusive of tZero)
	 * @return
	 */
	public double[] getPredictions() {
		double[] ret = new double[values.length - zeroIndex];
		for(int i = zeroIndex; i < values.length; i++) ret[i - zeroIndex] = values[i];
		return ret;
	}
	
	/**
	 * Gets all of the values prior to (exclusive of) time zero
	 * @return
	 */
	public double[] getBaseValues() {
		double[] ret = new double[zeroIndex];
		for(int i = 0; i < zeroIndex; i++) ret[i] = values[i];
		return ret;	
	}
	
	/**
	 * Trim the values here so that none prior to min nor later
	 * than max index are contained.
	 * @param tMin
	 * @param tMax
	 */
	public void trim(int tMin, int tMax) {
		// First set everything outside the range to zero 
		for(int i = 0; i < values.length; i++) if((i < (zeroIndex + tMin)) || (i > (zeroIndex + tMax))) values[i] = 0;
		// Now we're going to trim everything that's not between min and max, but it must include zero
		if(tMin > 0) tMin = 0;
		if(tMax < 0) tMax = 0;
		int numNewValues = tMax - tMin;
		double[] newValues = new double[numNewValues];
		for(int i = 0; i < numNewValues; i++) {
			int newIndex = i + zeroIndex + tMin;
		    if(newIndex >= 0 && newIndex < values.length) newValues[i] = values[newIndex];
		}
		values = newValues;
		zeroIndex = (-1 * tMin);
	}
	
	/**
	 * Adds the specified value at the index specified
	 * @param index
	 * @param countsToAdd
	 */
	public void addCounts(int index, double countsToAdd) {
		if(index >= values.length) { // Need to make the array longer by adding to the end; this is easy
			double[] newValues = new double[index + 1];
			for(int i = 0; i < values.length; i++) newValues[i] = values[i];
			values = newValues;
		}
		else if(index < 0) { // Need to make the array longer by adding to the beginning and re-centering
			double[] newValues = new double[values.length + (-1 * index)];
			for(int i = 0; i < values.length; i++) newValues[i + ( - 1 * index)] = values[i];
			values = newValues;
			zeroIndex += (-1 * index);
			index = 0;
		}
		values[index]+= countsToAdd;
	}
	
	/**
	 * Increments the count at the given index
	 * @param index
	 */
	public void increment(int index) {
		addCounts(index, 1);
	}
	
	/**
	 * Gets the value at the specified index.
	 * @param index
	 * @return
	 */
	public double getCountAt(int index) {
		return ((index < 0) || (index >= values.length)) ? 0 : values[index];
	}
	
	/**
	 * Writes a report using the IO reporting system at the specified log level
	 * @param level
	 */
    public void report(IO.LEVEL level) {
    	if(IO.log(level, "Reporting Zero-Indexed series")) // Skip the loop if it will not log anything
    		for(int i = 0; i < values.length; i++) IO.log(level, (i - zeroIndex) + " " + values[i]);
	}
	
    /**
     * Writes a report to std out.
     */
    public void report() {
		for(int i = 0; i < values.length; i++) System.out.println((i - zeroIndex) + " " + values[i]);	
    }
	
}