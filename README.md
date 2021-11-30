# GDELT Genetic Algorithm Search

## Input Data
The input data for \gdgas searches consists of four kinds of files:

* GDELT Data, including:
   * Events Data
   * Mentions Data (can be omitted)
* Time Series Comparative Data
* The GDELT Properties File
* Run Group Specification File (for sets of parallelized runs)

The GDELT Properties File and the Run Group Specification File are used with single runs and with parallelized runs, which are both discussed in subsequent sections

### GDELT Data
GDELT data consists of *events* and *mentions*. Both of these are given in CSV files downloadable from GDELT's archives. This toolkit provides a pair of tools that can automatically download events and related mentions from within a given time frame. (Note that mentions may accumulate after the initial event, and thus some events' mentions may fall outside the original time limit for events). An alternative data structure in JSON, from the original project for which this tool was designed, is also accommodated but is not documented.

### Time Series Comparative Data
The search will compare the GDELT data with a time series; the input data for the time series has the following structure:

```
platform,frame,ConvertedDate,counts
twitter,capitol,2020-10-01,1034
twitter,capitol,2020-10-02,1081
twitter,capitol,2020-10-03,1096
twitter,capitol,2020-10-04,1116
twitter,capitol,2020-10-05,1198
twitter,capitol,2020-10-06,1234
twitter,capitol,2020-10-07,1271
twitter,capitol,2020-10-08,1368
etc.
```

The platform and frame can include multiple combinations; for each combination, the collection of dates should be repeated.


## Running Single Runs

### The GDELT properties file

The GDELT properties file describes the location of the GDELT data, its chronological boundaries, the parameters of the GDELT queries (including their mutation properties), the location of the social media data, and some other properties of exploration. The structure of the file is simple: each line can contain one property specification, with a non-whitespaced property name on the left, an equals sign, and the property value on the right. Lines that start with number signs are ignored and can be used as comments.

Here is an example of a file

```
# GIT DIRECTORY
gitDir = /home/username/gdelt_ga_search

# GDELT DATA FILE LOCATION AND INFO
dataLocation     = ./data/examples/GDELT_Events_Data.csv
mentionsLocation = ./data/exanples/GDELT_Mentions_Data.csv
beginHistory     = 2020-01-31
dayZero          = 2021-01-01  

# GDELT QUERY SPECIFICATIONS
MaxShift              = 24
MinShift              = 24
MaxVizOffset          = 0
MinVizOffset          = 0
Actor1CountryCodes    = null,KEN,ZWE,UGA,AFR
Actor2CountryCodes    = null,KEN,ZWE,UGA,AFR
MatchGoldsteinScale   = FALSE
MatchAverageTone      = FALSE
MatchRootCode         = FALSE
MatchLatLon           = FALSE

# MUTATION WEIGHTS
Actor1CountryCodesMutateWeight = 10
Actor2CountryCodesMutateWeight = 10

# TIME SHIFT MUTATION PROBABILITY
MutatingTimeshiftProb           =  0

# TIME SERIES DATA FILE LOCATION AND INFO
timeSeriesDataLocation = ./examples/DummySocialMediaCounts.csv
timeSeriesDataStart    = 2020-10-01T00:00:00

# BAILOUT
bailoutInterval  = 3600
bailoutValue     = 100
```

See the full documentation pdf for definitions of these properties.

### Using the Runner Class from the Command Line

It is possible to use the Runner class to run single runs of the search. This requires a collection of arguments. A typical command line will include arguments following this example:


* */usr/lib/jvm/java-8-openjdk-amd64/bin/java* is the path to the Java RunTime
* *-DGDELT\_GAS\_PSCRIPT\_PATH=``/home/gdelt\_ga\_search/pscripts/"*  is the path to the Python Scripts. This is required even if Python Scripts are not used
* *-Dfile.encoding=UTF-8* A specification of file encoding
* *-classpath ./bin:./lib/gson-2.8.5.jar:./lib/jeromq-0.4.0.jar:./lib/commons-math3-3.6.1.jar gdelt.runners.Runner* The classpath
* *RUNNER\_0* An identifier that is used for some interim files
* *./examples/SampleConfig.props* The properties file in which the characteristics of the input data and queries are specified
* *LINEAR\_REGRESSION* The type of prediction to be used
* *NRMSE\_MEAN* The type of scoring to be used
* *2021-01-01* The date to be used as day zero
* *28* The duration of the training period
* *21* The number of days forward to predict for the final prediction
* *21* The number of days to predict for the training periods
* *50*The population size
* *10* The number of survivors
* *15* The number of generations to run if no stopping condition is encountered
* *7* The number of days prior to day zero minus the test prediction period to take as the training period day zero
* *twitter\_capitol* the data source ('platform') and topic ('frame') to be used; must match the input data given in the configuration properties file named above 
* *DEMO\_001\_000000::NewQueries* An alphanumeric identifier and a description; the description can use underscores as spaces. The description will be written with every output file.

## Running Parallelized Suites of Runs

### The Run Group Specification File

The Run Group Specification File contains the information needed to structure a collection of parallelized runs. The format is a property on the left, a pair of colons, and a value on the right. The term on the right may include several different values. The configuration will create a set of runs based on all of the possible combinations of values.

An example listing of a Run Group Specification File (from ./examples/SampleConfig.props)

```
predMethods::LINEAR\_REGRESSION
scoreMethods::NRMSE\_MEAN
zeroTimes::2021-01-01
trainDurations::28
predDurations::21
predDurationTrains::21
popSizes::50
survivorValues::10
generations::15
predictionPoints::7,0
platforms::twitter,youtube
frames::capitol,insurrection
```

The meanings of these are:


* *predMethods* The method to be used for prediction; valid values are the names of the values in the PREDICTION\_METHOD enum of the Predictor class.
* *scoreMethods* The method of scoring to be used; value values are the names of the values in the SCORE\_METHOD enum of the Scorer class.
* *zeroTimes* Times that are to be used as the boundary between training and prediction
* *trainDurations* Number of days that are used for training periods
* *predDurations* Number of days forward (ahead of zero time) that is considered prediction
* *predDurationTrains* Number of days forward used by the training samples. 
* *popSizes* Size of the GA population
* *survivorValues* Number of best-scoring survivors in the population
* *generations* Number of generations the search will run if it does not hit a stopping condition first
* *predictionPoints* Days prior to zero time \textit{minus the predDurationTrains* value that will mark the train/test boundary for the predictions.
* *platforms* Social media platforms to be explored
* *frames* Frames (topics) to be explored

### Running The ParallelMultiRunner Class from the Command Line

When running the ParallelMultiRunner from the command line, the arguments should be

* The full path to your Java runtime, e.g. /usr/lib/jvm/java-8-openjdk-amd64/bin/java 
* The relative path to your GDELT Properties file, e.g. ./examples/SampleConfig.props 
* A group name for this set of runs, e.g. DEMO\_001 
* A description for this group of runs, surrounded by quotation marks, e.g. ``Queries with  Actor 1 and Actor 2 fewer test periods, longer training periods, larger population, fewer survivors"
* The number of repetitions to run, i.e. the number of duplicate runs for every combination in the Run Group Configuration File (e.g., 2)
* The number of processes available for parallelization (e.g. 2)
* The relative path to the Run Group Configuration file, e.g. examples/Example\_Spec.rgspec


As a full example:
```
/usr/lib/jvm/java-8-openjdk-amd64/bin/java ./examples/SampleConfig.props DEMO\_001 ``Queries with  Actor 1 and Actor 2 fewer test periods, longer training periods, larger population, fewer survivors" 2 2 ``examples/Example\_Spec.rgspec"
```

