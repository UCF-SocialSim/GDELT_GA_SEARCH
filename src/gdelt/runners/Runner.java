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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

import gdelt.data.TimeSeriesDataSet;
import gdelt.explorers.EcosystemGenerationLoopIterator;
import gdelt.explorers.GDELT_Explorer;
import gdelt.explorers.GDELT_Explorer_Iterated;
import gdelt.explorers.QueryIterator;
import gdelt.query.GDELT_Query;
import gdelt.query.GDELT_QueryTemplate;
import gdelt.query.domains.components.AbstractQueryDomain.INIT_METHOD;
import gdelt.runners.configuration.RunnerConfiguration;
import gdelt.scorers.Scorer_NRMSE_Mean;
import gdelt.server.GDELT_Local_Service;
import gdelt.utils.Args;
import gdelt.utils.DateFormats;
import gdelt.utils.EnvironmentCheck;
import gdelt.utils.IO;
import gdelt.utils.PropertiesReader;
import gdelt.utils.TimeSeries;

/**
 * This code is the main class for running the GDELT Genetic Algorithm Search
 * 
 * Invoking the main class starts the process. It assumes:
 * 
 * Static variables at the start of the class are set
 *
 */
public class Runner {
	
	public static class GDELT_DataFileInfo{
		public String        dataLocation        = null;
		public String        mentionsLocation    = null;
		public LocalDateTime beginHistory        = null;
		public LocalDateTime dayZero             = null;
		
		public GDELT_DataFileInfo(Map<String, String> properties) {
			this(properties.get("dataLocation"),
			     properties.get("mentionsLocation"),
			     DateFormats.convertBasicDate(properties.get("dayZero")),
			     DateFormats.convertBasicDate(properties.get("dayZero")));
		}
		
		public GDELT_DataFileInfo(String dataLocation, String mentionsLocation, LocalDateTime beginHistory, LocalDateTime dayZero) {
			this.dataLocation     = dataLocation;
			this.mentionsLocation = mentionsLocation;
			this.beginHistory     = beginHistory;
			this.dayZero          = dayZero;
		}
		
	}

    private TimeSeriesDataSet       eventCountData  = null;
	private GDELT_DataFileInfo      gdeltData       = null;
	private String                  gitDir          = null;
	private GDELT_Explorer_Iterated explorer        = null;
	private GDELT_Query[]           seedQueries     = null;
	private int                     bailoutValue    = -1;
	private int                     bailoutInterval = -1;
	
	// Constructor
	public Runner(Map<String, String> properties) {
		// Configure the base GDELT query
		GDELT_QueryTemplate.setAll(properties);
		GDELT_Query.setOffsets(properties);
		
		// Retrieve the Time Series Data that will be used to test
		eventCountData = 
			new TimeSeriesDataSet(properties.get("timeSeriesDataLocation"),  
				                  LocalDateTime.parse(properties.get("timeSeriesDataStart"), DateTimeFormatter.ISO_DATE_TIME), 1, ChronoUnit.DAYS);
		
		gitDir = properties.get("gitDir");
		
		// How to connect to the GDELT data
		gdeltData = new GDELT_DataFileInfo(properties);
        
		// What kind of 'explorer' to use
		explorer =	new GDELT_Explorer_Iterated(new GDELT_Local_Service(gdeltData.dataLocation, gdeltData.mentionsLocation));
		
		if(properties.containsKey("bailoutValue"))    bailoutValue    = Integer.parseInt(properties.get("bailoutValue"));
		if(properties.containsKey("bailoutInterval")) bailoutInterval = Integer.parseInt(properties.get("bailoutInterval"));
	}
	
	// Run 
	public void run(String[] args) {
		// Grab the Git Status
		String gitStatus = EnvironmentCheck.getGitStatus(gitDir);
		
		// Which social media data set to explore
		String[] dataSetNames = args[args.length - 2].split(";"); // This is always the second-last arg
		Map<String, TimeSeries> dataSets = new TreeMap<String, TimeSeries>();
		for(String dataSetName: dataSetNames) dataSets.put(dataSetName, eventCountData.get(dataSetName));

		// How should this run be described?
		String[] lastArgSplit = args[args.length - 1].split("::");
		String runGroup = lastArgSplit[0];
		String desc = "No description provided.";
		if(lastArgSplit.length > 1) desc = lastArgSplit[1].replace("__", " ");

		// Create the run configuration
		RunnerConfiguration config = new RunnerConfiguration(Args.trimLeading(2, args));

		// Configure the default seed queries
		GDELT_Query full = new GDELT_Query(config.tMin, config.tMax, config.predMethod, INIT_METHOD.FULL);
		GDELT_Query all  = new GDELT_Query(config.tMin, config.tMax, config.predMethod, INIT_METHOD.FULL);
		all.all = true;
		seedQueries = new GDELT_Query[] { all, full };
		
		explorer.useDateTimeAdded = config.useTimeAdded; // This will use the date time that the event was added to GDELT instead of the date/time that the event occurred

		for(Map.Entry<String, TimeSeries> dataSeries: dataSets.entrySet()) {
			
			String     dataSeriesName = dataSeries.getKey();
			TimeSeries dataSet        = dataSeries.getValue();
			
			// Re-Initialize output
			String outputDir = dataSeriesName + "/" + runGroup;
			IO.log(IO.LEVEL.LEVEL_0, "Closing current output file; new file is in " + outputDir);
			IO.close();
			IO.init(outputDir);
			
			// Set log level; 2 is best
			IO.log_level = IO.LEVEL.LEVEL_2;
			IO.memCheck();
			IO.log(IO.LEVEL.LEVEL_0, "Starting run: " + outputDir);
			IO.log(IO.LEVEL.LEVEL_0, "RUN GROUP DESCRIPTION: " + desc);
			
			IO.log(IO.LEVEL.LEVEL_0, "Starting run: " + outputDir);
			IO.log(IO.LEVEL.LEVEL_0, "RUN GROUP DESCRIPTION: " + desc);
			
			for(String g: gitStatus.split(IO.LS)) IO.log(IO.LEVEL.LEVEL_0, g);
		
			IO.log(IO.LEVEL.LEVEL_1, "Starting exploration of " + dataSeriesName);
			
			// Create the basic scorer; this must work within the data set, so it's created here
			config.scorer.init(dataSet, config.tMin, config.tMax, config.startDates);

			// Output to the log
			config.report();
			dataSet.report(IO.LEVEL.LEVEL_0, "TIME_SERIES_REPORT:");
			
			// Create the initial set of queries
			IO.log(IO.LEVEL.LEVEL_1, "Initializing queries; popsize = " + config.populationSize + " seedqueries " + seedQueries.length);
	    	GDELT_Query[] queries = new GDELT_Query[config.populationSize]; 
	    	for(int i = 0; i < config.populationSize; i++) {
	    		IO.log(IO.LEVEL.LEVEL_1, "Creating query " + i);
	    		queries[i] = (i < seedQueries.length) ? seedQueries[i] : (new GDELT_Query(config.tMin, config.tMax, config.predMethod, INIT_METHOD.RANDOM));
	    		IO.log(IO.LEVEL.LEVEL_4,queries[i].toString());
	    	}

	    	
	    	// Reset the explorer
	    	IO.log(IO.LEVEL.LEVEL_1, "Resetting explorer...");
	    	QueryIterator iter = new EcosystemGenerationLoopIterator(config.numberOfGenerations, config.populationSize, config.numberOfSurvivors, queries);
	    	explorer.reset(config.scorer, dataSeries.getKey(), iter);
	    	
			explorer.gtScorer = new Scorer_NRMSE_Mean();
			explorer.gtScorer.init(dataSet, config.tMin, config.tMax, config.outputStart);
	    	
			if(bailoutValue > 0)    explorer.bailoutValue    = bailoutValue;
			if(bailoutInterval > 0) explorer.bailoutInterval = bailoutInterval;

			IO.log(IO.LEVEL.LEVEL_1, "Specifying test criteria");
			// Specify the test criteria
	    	for(LocalDateTime ldt: config.startDates) explorer.testCriteria.add(new GDELT_Explorer.TestCriteria("TEST_" + ldt.format(DateTimeFormatter.ISO_LOCAL_DATE), dataSet.lift(ldt, config.tMin, config.tMax)));
			explorer.testCriteria.add(new GDELT_Explorer.TestCriteria("Result", dataSet.lift(config.outputStart, config.tMin, config.tMaxPred)));
	    		    	
			IO.log(IO.LEVEL.LEVEL_1, "Executing explore");
			GDELT_Query bestQuery = GDELT_Query.get(explorer.execute(dataSeriesName));
			
			// Some output
			String toShow = bestQuery.blankClone().toString().replaceAll("\\{\"order\":", IO.LS + "\\{\"order\":");
			IO.log(IO.LEVEL.LEVEL_1, "Best query for " + dataSeriesName + " : " + toShow);
			bestQuery.report(IO.LEVEL.LEVEL_3);			
					
		}
		System.out.println("Done.");
		
	}
	
	
	/**
	 * The arguments to this class should be
	 * 
	 * Arguments for Configuration
	 * data set name                            // E.g. within the data set file, the name of the specific data to run.
	 * run group name::run group description    // Used to created identifiers in the output
	 */
	public static void main(String[] args) {
		// Perform basic (non-git) environment check
		if(!EnvironmentCheck.environmentIsValid()) return;
		
		IO.init("./" + args[0] + "/");
		// Read properties from file
		IO.log(IO.LEVEL.LEVEL_0, "About to load properties from " + args[1]);
		Map<String, String> properties = PropertiesReader.getProperties(args[1]); // Will need to be arg[1]
		IO.log(IO.LEVEL.LEVEL_0, "Done; properties = " + properties.size());
		// Display in alphabetical order- less messy
		TreeMap<String, String> propsSorted = new TreeMap<String, String>();
		propsSorted.putAll(properties);
		for(Map.Entry<String, String> entry: propsSorted.entrySet()) {
			IO.log(IO.LEVEL.LEVEL_0, "  " + entry.getKey() + " = " + entry.getValue());
		}
		IO.log(IO.LEVEL.LEVEL_0, "About to create new runner " + args[0]);
		Runner runner = new Runner(properties);
		IO.log(IO.LEVEL.LEVEL_0, "Done creating runner");
		IO.log(IO.LEVEL.LEVEL_0, "Launching run. ");
		runner.run(args);
	}
	
}
