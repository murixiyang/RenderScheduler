
# RenderScheduler
**Take home challenge from incident.io**

## Access code
This challenge is written in Java. The source files can be found in `./RenderScheduler/src`.
Also, you can access https://github.com/murixiyang/RenderScheduler to view the code.

This small project is built according to the instruction provided by incient.io, which could be found here: https://github.com/incident-io/internships/blob/master/README.md#2-complete-our-take-home-challenge

## How to run

1. Unzip the file
2. Please check you have java installed and added to your system path to execute the shell script.
3. In the `RenderScheduler` folder, there is a shell script called`render-schedule.sh`, two example json files: `schedule.json` and `overrides.json`(which contains the example data from instruction).

   Give execution permission to the shell script.
   ```console
   chmod +x render-schedule.sh 
   ```

   To run with example json files and from&until time given in instruction, execute the shell script through terminal.
    ```console
   ./render-schedule.sh --schedule=schedule.json --overrides=overrides.json --from='2023-11-17T17:00:00Z' --until='2023-12-01T17:00:00Z'
   ```

   To run customised files and from&until time, please follows:
   ```console
   ./render-schedule.sh --schedule=<scheduleFilePath> --overrides=<overrideFilePath> --from=<fromTime> --until=<untilTime>
   ```
   Please make sure you use the format like `2023-12-01T17:00:00Z` to express date and time, otherwise there will be a parse error.


## Dependency
Java 17 was used to build and run the project.
`com.googlecode.json-simple:json-simple:1.1.1` was used to manipulate with JSONArray and JSONObject. The jar file is included in the `lib` folder, so no additional download needed.
Original website: https://code.google.com/archive/p/json-simple/


## Comments on the work
I approached the problem in these several steps:

1. Processing arguments passed in
2. Read schedule file and override file and store information
3. Add regular schedule into return list, starting from `fromTime` to `untilTime`
4. For each overrides, find the affected schedules and split them. Remove overtook schedules from return list and add the &lt;leftSplit&gt;&lt;override&gt;&lt;rightSplit&gt; to the corresponding index of return list.
   (Section 3 and 4 will be discussed with more details in the following sections)

A class called SingleSchedule is frequently used in the program. It contains the user, startTime and endTime of a certain schedule.

### Details
In Step3, the regular schedule is trimmed according to `fromTime` and `untilTime`

For Step4, let us think what an override will do.
Imaging an axis and override is like putting a colored sector on the axis and it will cut the axis into 3 parts: part before; colored sector; part after. Similarly with override, it will divide regular schedule into 3 parts: &lt;leftSplit&gt;&lt;override&gt;&lt;rightSplit&gt;.
But different from a continuous axis, our regular schedule already had some consective schedules on it. Therefore we need to consider what to remove and what to add in.

First consider what to add in to the schedule, which is the patter mentioned before: &lt;leftSplit&gt;&lt;override&gt;&lt;rightSplit&gt;. Just one thing to notice that leftSplit and rightSplits might be empty if the override happens exactly at the handover time.
The schedules that needs to be removed are the covered ones,  which include the schedules that are splited and the ones are completely overtook (that will be a long override...). To calculate this, I found the first schedule the override will affect and the last one it will affect, then simply remove all schedules between these two, inclusively.


### Exception Handling
In my approach, when an exception is catched, it will print an error message to console and then exit with error code 1.
The exceptions that could be catched are:

1. Wrong argument name
2. Not enough arguments
3. Wrong date formatting
4. File not exists
5. Cannot read file
6. Wrong JSON style in file (Schedule file should contain single JSONObject, and users should be a JSONArray. Override file should contain single JSONArray)



### Weakness and improvement

1. According to the instructions, the output should be a JSONArray. However, with my chosen dependency, `json-simple:1.1.1` uses raw types which will trigger `unchecked` warnings in the compiler. Therefore I chose to return an ArrayList of JSONObject as the result. With a better and newer library, this might be fixed.
2. The program uses several for-loops to iterate through the whole half-finished render schedule list. If the query time period is long, this can slow down the process.
3. The program now highly depends on correct input from JSON files, if the type/time in schedule or overrides file is wrong, then it is likely to cause a problem.
4. JSONObject is holding an HashMap inside, so there is no guaranteed order of output. Therefore the console output does not followed the sequence shown in the instructions.

## Future development

JSON files are hard to write by hands and there can easily be a typo. Also, reading a long JSONArray is not user-friendly.

Therefore my thought is to build a website that employees can track who is taking the shift.
The website should display a graphic rendered schedule for the near future, like the graph shown in the instructions, so that people can check who is taking the shift more conviniently.
There should be a feature to change the regular schedule, such as adding, removing users, change handover intervals and so on.
Also, there should be a place to submit an override, from where data will be collected and updated to the override json file. Then the `render-schedule`scheduler can be called to calculate the new rendered schecule and the graphic page should be updated accordingly. For each submitted override, there should be possible to modify them several times, in case of a typo.
I would also suggest to add a filter, that can select certain users. Some people may only interested in when they will take the shift and they can plan earlier. With the filter, the website should display the recent schedules of selected users.

The overall idea is to avoiding changing, reading JSON files directly, but to use graphic panels to trigger functions to collect and update JSON files in the background. This can make the process easier and more convinient.
