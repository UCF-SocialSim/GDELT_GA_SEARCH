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
package gdelt.predictors;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;

import gdelt.utils.IO;
import gdelt.utils.ZeroIndexedSeries;

/**
 * Wrapper class that uses a python script to execute prediction.
 */
public class Predictor_Python extends Predictor{
	
	private static String convertArrayToString(double[] array) {
		StringWriter sw = new StringWriter();
		String sep = "";
		for(double d: array) {
			sw.write(sep + d);
			sep=",";
		}
		return sw.toString();
	}
	
	private static double[] convertToDoubleArray(String array) {
		IO.log(IO.LEVEL.LEVEL_4, "CONVERTING TO DOUBLE[] '" + array + "'");
		String[] strs = array.split(",");
		double[] ret = new double[strs.length];
		try {
			for(int i = 0; i < strs.length; i++) ret[i] = Double.parseDouble(strs[i]);
		}
		catch(Exception E) {
			System.out.println(E.getMessage());
		}
		return ret;
	}

	
	String scriptName = null;
	
	public Predictor_Python(String script) {
		scriptName = script;
	}
	
	
	@Override
	public double[] getPredictions(ZeroIndexedSeries testData, ZeroIndexedSeries trainData) {
	
		// Need to launch the python script as a separate process here...
		
		String trainX = convertArrayToString(testData.getBaseValues());
		String trainY = convertArrayToString(trainData.getBaseValues());
		String testX =  convertArrayToString(testData.getPredictions());
		String command = "python3 " + scriptName + " " + trainX + " " + trainY + " " + testX; 
		IO.log(IO.LEVEL.LEVEL_4, "RUNNING: " + command);
        Runtime run  = Runtime.getRuntime(); 
        double[] ret = null;
        try {
        	Process proc = run.exec(command, null, new File("./pscripts/")); 
 
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream() /*proc.getErrorStream()*/));

            proc.waitFor();
            
            String line;
            while ((line =  reader.readLine()) != null) {
            	IO.log(IO.LEVEL.LEVEL_4, "LINE IS: " + line);
            	ret = convertToDoubleArray(line);
            }
            IO.log(IO.LEVEL.LEVEL_4, "LINE IS: " + line);
        }
        catch(Exception E) {
        	System.out.println("Ooops" + E.getMessage());
        	E.printStackTrace();
        }
        if(ret == null) IO.log(IO.LEVEL.LEVEL_0, "NULL ARRAY RETURNED");
        else            IO.log(IO.LEVEL.LEVEL_4, "RETURNED: " + ret.length);
		return ret;
	}

}
