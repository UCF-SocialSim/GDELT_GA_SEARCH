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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import gdelt.utils.Args;
import gdelt.utils.EnvironmentCheck;
import gdelt.utils.IO;

/**
 * This class permits the exploration in parallel of sets of
 * data and time series. Works by calling the Runner class
 * over the command line.
 */
public class ParallelMultiRunner {
	
	private static void createRunGroupFromFile(String fileName, String mainConfigFile, String runGroup, List<String[]> toQueue, String desc) {
		IO.log(IO.LEVEL.LEVEL_0, "Creating run group from file: " + fileName + " (" + runGroup + ")");
		try {
			String[] predMethods = null;
			String[] scoreMethods = null;
            String[] zeroTimes = null;
            String[] trainDurations = null;
            String[] predDurations = null;
            String[] predDurationTrains = null;
            String[] popSizes = null;
            String[] survivorValues = null;
            String[] generations = null;
            String[] predictionPoints = null;
            String[] platforms = null;
            String[] frames = null;
			
			
            BufferedReader reader = new BufferedReader(new FileReader(working_directory + fileName));
            String line = reader.readLine();
            while(line != null) {
            	String[] commands = line.split("::"); // Two semicolons
            	String command = commands[0];
            	switch(command) {
            		case "predMethods":         predMethods        = commands[1].split(",");    break;
            		case "scoreMethods":        scoreMethods       = commands[1].split(",");    break;
            		case "zeroTimes":           zeroTimes          = commands[1].split(",");    break;
            		case "trainDurations":      trainDurations     = commands[1].split(",");    break;
            		case "predDurations":       predDurations      = commands[1].split(",");    break;
            		case "predDurationTrains":  predDurationTrains = commands[1].split(",");    break;
            		case "popSizes":            popSizes           = commands[1].split(",");    break;
            		case "survivorValues":      survivorValues     = commands[1].split(",");    break;
            		case "generations":         generations        = commands[1].split(",");    break;
            		case "predictionPoints":    predictionPoints   = commands[1].split(",");    break;
            		case "platforms":           platforms          = commands[1].split(",");    break;
            		case "frames":              frames             = commands[1].split(",");    break;
            	}
            	line = reader.readLine();
            }
            reader.close();
            
            if(predMethods == null || scoreMethods == null || zeroTimes == null || trainDurations == null || predDurations == null || 
               predDurationTrains == null || popSizes == null || survivorValues == null || generations == null || 
               predictionPoints == null || platforms == null || frames == null) {
            	System.out.println("COULD NOT CREATE RUN GROUP FROM FILE " + fileName + "; does not include all required elements.");
            	return;
            }
			
            createRunGroup(mainConfigFile, predMethods, scoreMethods, zeroTimes, trainDurations, predDurationTrains, predDurations, popSizes, survivorValues, generations, predictionPoints, platforms, frames, runGroup, toQueue, desc);
			
		}
		catch(Exception E) {
			System.out.println(E.getMessage());
		}
	}
	
	private static void createRunGroup(String configFile, 
								  String[] predMethods,
								  String[] scoreMethods,
			                      String[] zeroTimes,
			                      String[] trainDurations,
			                      String[] predDurations,
			                      String[] predDurationTrains,
			                      String[] popSizes,
			                      String[] survivorValues,
			                      String[] generations,
			                      String[] predictionPoints,
			                      String[] platforms,
			                      String[] frames,
			                      String   runGroup,
			                      List<String[]> toQueue,
			                      String desc) {
		IO.log(IO.LEVEL.LEVEL_0, "Creating run group '" + runGroup + "' from arrays");
		int count = 0;
		for(String predMethod: predMethods) {
			for(String scoreMethod: scoreMethods) {
				for(String zeroTime: zeroTimes) {
					for(String trainDuration: trainDurations) {
						for(String predDuration: predDurations) {
							for(String predDurationTrain: predDurationTrains) {
								for (String popSize: popSizes){
									for(String survivors: survivorValues) {
										for(String generation: generations) {
											for(String predictionPoint: predictionPoints) {
												for(String platform: platforms) {
													for(String frame: frames) {
														toQueue.add(new String[]
																{"RUNNER_" + count, configFile, predMethod, scoreMethod, zeroTime, trainDuration, predDuration, predDurationTrain, popSize, survivors, generation, predictionPoint, platform+"_"+frame, runGroup + "::" + desc.replace(" ", "__")});
														count++;
													}
												}
											}
										}
									}
								}
							}
						}
					}
			    }
		    }
		}
		IO.log(IO.LEVEL.LEVEL_0, "Created " + count + " elements");
	}
	
	public static class ProcRecord{
		Process  process;
		String   command;
		String[] params;
		int      iter;
		
		public ProcRecord(String javaPath, String[] paramList, int iterationNumber) {
			params = paramList.clone();
			iter = iterationNumber;
			String iterNum = "000000" + iter;
			iterNum = iterNum.substring(iterNum.length() - 6);
			
			String testIDSpec = params[params.length - 1];
			
			String testIDSpecSplit[] = testIDSpec.split("::");
			String testID = testIDSpecSplit[0];
			
			String desc = "No description supplied";
			
			if(testIDSpecSplit.length > 1) {
				desc = testIDSpecSplit[1];
			}
			else {
				IO.log(IO.LEVEL.LEVEL_0, "WARNING!!! --> NO DESCRIPTION PROVIDED FOR " + testID);
			}
			
			testID += ("_" + iterNum) + "::" + desc.replace(" ", "__");
			params[params.length - 1] = testID;
			command = javaPath + " " + command_base + combine(params);
		}
	}
	
	public static String combine(String[] elements) {
		String ret = "";
		for(String element: elements) {
			ret += " " + (!element.contains(";") ? element : "\"" + element + "\"") + "";  // Note: we are incorporating the prepended " "
		}
		return ret;
	}
	
	public static String JDKPath           = "/usr/lib/jvm/java-8-openjdk-amd64/bin/java";
	
	public static String working_directory = "./";
	public static String command_base = 
			  "-DGDELT_GAS_PSCRIPT_PATH=\"" + System.getProperty("GDELT_GAS_PSCRIPT_PATH") + "\" "
			+ "-Dfile.encoding=UTF-8 "
			+ "-classpath "
			+ working_directory + "bin:"
			+ working_directory + "lib/gson-2.8.5.jar:"
			+ working_directory + "lib/jeromq-0.4.0.jar:"
			+ working_directory + "lib/commons-math3-3.6.1.jar "
			+ "gdelt.runners.Runner";

	
	public static Map<String, String> testRecord = new HashMap<String, String>();
	

	public static void run(String javaPath, String mainConfigFile, String runGroup, String desc, int repetitions, int numProcesses, String ... runGroupConfigFiles) {		
		// Initialize the logging system
		IO.init(".");
		IO.echo = true;

		List<String[]> queue = new ArrayList<String[]>();
//				createRunGroup(
//						new String[]{"LINEAR_REGRESSION"},
//		                new String[]{"NRMSE_MEAN"},		
//						new String[]{"2020-11-30"},
//						new String[]{"140"},
//						new String[]{"28"},
//						new String[]{"28"},
//						new String[]{"100"},
//						new String[]{"20"},
//						new String[]{"25"},
//						new String[]{"36"},
//						new String[]{"twitter"},
//						new String[]{"covid", "covid_assistance", "debt", "environmentalism", "infrastructure", "mistreatment", "other", "prejudice", "travel", "un"},
//						runGroup, queue);

		for(String configFile: runGroupConfigFiles) createRunGroupFromFile(configFile, mainConfigFile, runGroup, queue, desc);
		
		// Here is the manual way to do this:
//				String[][] queue_ARRAY = new String[][] {
//					{ "1", "/data/Configurations/SampleConfig.props", "LINEAR_REGRESSION", "NRMSE_MEAN", "2020-11-30", "140", "28", "28", "100", "20", "25", "36", "twitter_covid",                 runGroup},
//				};
//				for(String[] spec: queue_ARRAY) queue.add(spec);
		
		
		ArrayList<ProcRecord> fullQueue = new ArrayList<ProcRecord>();
		for(int i = 0; i < repetitions; i++) {
			for(String[] s: queue) {
				fullQueue.add(new ProcRecord(javaPath, s, i));
			}
		}
		Runtime run  = Runtime.getRuntime();
		int numProcs = numProcesses;
		try {
			File f = new File(working_directory + "output/runner_continue.txt");
			f.createNewFile();
			TimeUnit.SECONDS.sleep(2); // Let the filesystem catch up...
			int iter = 0;
			List<ProcRecord> activeProcRecords = new ArrayList<ProcRecord>();
			boolean cont = true;
			do {
				if(!f.exists()) { // Deleting 'runner_continue.txt' will cause the processes to be killed and removed
					for(ProcRecord p: activeProcRecords) p.process.destroyForcibly();
					cont = false;
				}
				
				// Check if any are done
				if(activeProcRecords.size() > 0) {
					List<ProcRecord> toRemove = new ArrayList<ProcRecord>();
					for(ProcRecord procRec: activeProcRecords) if((cont == false) || (!(procRec.process.isAlive()))) {
						IO.log(IO.LEVEL.LEVEL_0, "Removing: " + procRec.iter + ":" + combine(procRec.params));
						toRemove.add(procRec);
					}
					if(toRemove.size() > 0) {
						activeProcRecords.removeAll(toRemove);
						String records = "";
						for(ProcRecord procRec: activeProcRecords) records += procRec.iter + ";";	
						IO.log(IO.LEVEL.LEVEL_0, "Remaining: " + activeProcRecords.size() + " records: " + records);
					}
				}

				if(cont) {
					while(iter < fullQueue.size() && (activeProcRecords.size() < numProcs)) {
						ProcRecord toStart = fullQueue.get(iter);
						toStart.iter = (iter + 1);
						IO.log(IO.LEVEL.LEVEL_0, "Running " + (iter + 1) + " of " + fullQueue.size() + ": " + toStart.command);
						toStart.process = run.exec(toStart.command, null, null); 
						TimeUnit.MILLISECONDS.sleep(20); // Need this to make sure the random number generator seed varies
						activeProcRecords.add(toStart);					
						iter++;
					}
				}
			}while(activeProcRecords.size() > 0); // When the last one drains and there are no more, we're done; note that 'cont' flag may still be true
            
        }
        catch(Exception E) {
        	System.out.println("Error: " + E.getMessage());
        	E.printStackTrace();
        }
		
	}
	
	
	// To establish collection of runs, place each run description here and set the number of repetitions
    //******
	// Arguments
	//******
	// Path to the Java runtime to be used by the 
	// Main GDELT Configuration File
	// Run Group (name for group of runs)
	// Number of repetitions of all combinations
	// Description to be applied to the runs
	// Number of processes to employ
	// Names of configuration files to use to generate sets of runs
	public static void run(String[] args) {
		String JavaPath        = args[0];
		String mainConfigFile  = args[1];
		String runGroup        = args[2];
		String desc            = args[3];                   // Required
		int    repetitions     = Integer.parseInt(args[4]); // Also required
		int    numProcesses    = Integer.parseInt(args[5]); // Number of processes to deploy
		
		run(JavaPath, mainConfigFile, runGroup, desc, repetitions, numProcesses, Args.trimLeading(6,args));
	}
	

	public static void main(String[] args) {
		if(args[0].startsWith("GIT_DIR:")) {
			if(!EnvironmentCheck.environmentIsValid(args[0].split(":")[1])) {
				System.out.println("Failed environment check.");
						return;
			}
			args = Args.trimLeading(1, args);
		}
		run(args);
	}
	
	
}
