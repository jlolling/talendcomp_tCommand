# Talend component tCommand for running commands on the operating system
This new component tCommand was developed because the existing component tSystem in Talend Studio lacks some capabilities.
* Run the command asynchronous
* Provide the output of the started process as flows in real time
* Can kill the process after a configured time in seconds to prevent endless runs
* Can configure a exit code other than 0 if the process also normally do not ends with 0
* If command parameter contains spaces they will automatically encapsulated in double quotes
* Provides 2 output flows (standard and error)
* Can redirect the error flow to the standard flow
  
The settings are very similar to tSystem.
Some differences:
* The target for the output is one setting for standard and error output
* Additional you have check options for providing the output also to the console
* There is an option to join the error output to the standard output.
* New setting for maximum runtime - to kill the process if max duration reached
* New setting for the expected exit code for successful finished process

## Demo Job Design
![Demo job component settings](https://github.com/jlolling/talendcomp_tCommand/blob/master/doc/tCommand_single_commandline.png)

## Productive Job Design
![Productive job component settings](https://github.com/jlolling/talendcomp_tCommand/blob/master/doc/tCommand_real_job_design.png)

## Download the ready to use component
![Download the latest release here](https://github.com/jlolling/talendcomp_tCommand/releases/latest)
