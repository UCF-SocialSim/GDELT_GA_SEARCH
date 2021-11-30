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

/**
 * Some simple geographic conversions for dealing with the Geo fields in the
 * gdelt data set.
 * 
 * For implementation of the Haversine formula, see:
 * 
 * http://www.movable-type.co.uk/scripts/latlong.html
 *
 */
public class Geo {

	public static final double RadiusOfTheEarth = 6371000; // volumetric mean radius in meters; https://nssdc.gsfc.nasa.gov/planetary/factsheet/earthfact.html 
	
	// Convert degrees to radians; may be better to do this upon load?
	public static double degToRad(double deg) {
		return deg * Math.PI / 180;
	}
	
	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		return RadiusOfTheEarth * haversine(degToRad(lat1), degToRad(lat2), degToRad(lat2 - lat1), degToRad(lon2 - lon1)); // in meters
	}

	
	public static double distanceRad(double lat1R, double lon1R, double lat2R, double lon2R) {
		return RadiusOfTheEarth * haversine(lat1R, lat2R, (lat2R - lat1R), (lon2R - lon1R)); // in meters
		
	}

	/**
	 * Arguments are:
	 * @param lat1R Latitude 1 in Radians
	 * @param lat2R Latitude 2 in Radians
	 * @param dPhi  Difference between lat1 and lat2
	 * @param dLmd  Difference in longitude
	 * @return
	 */
	private static double haversine(double lat1R, double lat2R, double dPhi, double dLmd) {
		double a =    (Math.cos(lat1R)  * Math.cos(lat2R)) * (Math.sin(dLmd/2) * Math.sin(dLmd/2))
		                       + Math.sin(dPhi/2) * Math.sin(dPhi/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return c;
	}

}
