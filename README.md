# Talend component tCommand for running commands on the operating system
This new component tCommand was developed because the existing component tSystem in Talend Studio lacks some capabilities.
* Run the command asynchronous
* Provide the output of the started process as flows in real time
* Can kill the process after a configured time in seconds to prevent endless runs
* Can configure a exit code other than 0 if the process also normally do not ends with 0
* If command parameter contains spaces they will automatically encapsulated in double quotes
* Provides 2 output flows (standard and error)
* Can redirect the error flow to the standard flow
  
