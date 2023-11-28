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

   To run with example json files and from&until time given in instruction, execute the shell script through commandline.
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
