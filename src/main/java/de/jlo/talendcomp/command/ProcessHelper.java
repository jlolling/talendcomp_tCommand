/**
 * Copyright 2024 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jlo.talendcomp.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

public class ProcessHelper {
	
	private CommandLine commandLine = null;
	private final Map<String, String> placeHolderMap = new HashMap<String, String>();
	private String workDir = null;
	private DefaultExecutor executor = null;
	private final ProcessStreamProvider streamProvider;
	private String stdLine = null;
	private String errorLine = null;
	private final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
	private Map<String, String> environmentMap = new HashMap<String, String>();
	private final Queue<String> stdQueue = new ConcurrentLinkedQueue<String>();
	private final Queue<String> errQueue = new ConcurrentLinkedQueue<String>();
	private final String endMarker = "PROCESS@HELPER$STREAM%END";
	private int countStdLines = 0;
	private int countErrLines = 0;
	private boolean stdStreamQueueHasData = true;
	private boolean errStreamQueueHasData = true;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private int maxProcessRuntimeSec = 0;
	private StringBuilder stdAllContent = null;
	private StringBuilder errAllContent = null;
	private boolean provideAllContentAtOnce = false;
	private boolean redirectOutputToConsole = false;
	private boolean sendErrOutputToStdOut = false;
	private boolean commandProvided = false;
	private int defaultOkExitCode = 0;
	private boolean stdPumpThreadFinished = false;
	private boolean errPumpThreadFinished = false;
	private OutputListener listener = null;
	private String errorDetectionString = null;
	private boolean errorDetectedByOutput = false;
	private String errorLineDetected = null;
	
	public ProcessHelper() {
		environmentMap.putAll(System.getenv()); // do not use the env map directly because not modifiable
		workDir = System.getProperty("user.dir");
		streamProvider = new ProcessStreamProvider(this);
	}
	
	/**
	 * Creates a new command line or adds arguments to it.
	 * The first line have to be the actual program and all further calls the arguments
	 * @param command
	 * @return this to allow builder pattern
	 */
	public ProcessHelper addToCommandLine(Object command) {
		if (command != null) {
			if (commandLine == null) {
				commandLine = new CommandLine(String.valueOf(command));
				commandProvided = true;
			} else {
				commandLine.addArgument(String.valueOf(command), false);
			}
		}
		return this;
	}
	
	/**
	 * Creates the command line from a single line and parse it by spaces
	 * @param command
	 * @return
	 */
	public ProcessHelper setSingleLineCommand(String command) {
		if (command == null || command.trim().isEmpty()) {
			throw new IllegalArgumentException("single line command cannot be null or empty");
		}
		commandLine = CommandLine.parse(command);
		commandProvided = true;
		return this;
	}
	
	/**
	 * In command you can use place holders like ${file}
	 * @param key
	 * @param value
	 * @return this to allow builder pattern
	 */
	public ProcessHelper setPlaceholderValue(String key, String value) {
		placeHolderMap.put(key, value);
		return this;
	}
	
	/**
	 * Set a environment variable.
	 * The environment already contains the variables of the job process.
	 * 
	 * @param name name of variable
	 * @param value value with different types
	 * @return this to allow builder pattern
	 */
	public ProcessHelper setEnvironmentVariable(String name, Object value) {
		String valueStr = null;
		if (value instanceof String) {
			valueStr = (String) value;
		} else if (value instanceof Number) {
			valueStr = String.valueOf((Number) value);
		} else if (value instanceof Boolean) {
			valueStr = String.valueOf((Boolean) value);
		} else if (value instanceof Date) {
			valueStr = sdf.format((Date) value);
		} else if (value != null) {
			valueStr = value.toString();
		}
		environmentMap.put(name, valueStr);
		return this;
	}
	
	/**
	 * Starts the command asynchronously and returns when the process has been started and the streams are established.
	 * 
	 * @throws Exception
	 */
	public void execute() throws Exception {
		if (commandProvided == false) {
			throw new Exception("No command was set!");
		}
		File fwd = null;
		if (workDir != null && workDir.trim().isEmpty() == false) {
			fwd = new File(workDir);
			if (fwd.exists() == false) {
				fwd.mkdirs();
			}
			if (fwd.exists() == false) {
				throw new Exception("work-dir: " + fwd.getAbsolutePath() + " does not exist and cannot be created");
			}
		}
		executor = DefaultExecutor.builder()
				.setExecuteStreamHandler(streamProvider)
				.setWorkingDirectory(fwd)
				.get();
		executor.setExitValue(defaultOkExitCode);
		if (maxProcessRuntimeSec > 0) {
			ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(Duration.ofSeconds(maxProcessRuntimeSec)).get();
			executor.setWatchdog(watchdog);
		}
		// the streams will be set to the streamProvider inside the execute method.
		// once the process is started and the streams are available the running state is set#
		try {
			executor.execute(commandLine, environmentMap, resultHandler);
		} catch (Exception e) {
			throw new Exception("Execute command: " + commandLine + " failed: " + e.getMessage(), e);
		}
		// wait for the streams started
		while (true) {
			if (streamProvider.isStarted()) {
				break;
			} else if (resultHandler.hasResult() && resultHandler.getException() != null) {
				throw new Exception("Start of process: " + commandLine.toString() + " failed: " + resultHandler.getException().getMessage(), resultHandler.getException());
			} else {
				Thread.sleep(10l);
			}
		}
		startStreaming();
	}
	
	private void startStreaming() throws Exception {
		Thread stdStreamer = new Thread() {
			
			@Override
			public void run() {
				BufferedReader reader = streamProvider.getStandardOutReader();
				String line = null;
				do {
					try {
						line = reader.readLine();
						if (line != null) {
							countStdLines++;
							if (provideAllContentAtOnce) {
								if (stdAllContent == null) {
									stdAllContent = new StringBuilder();
								}
								stdAllContent.append(line);
								stdAllContent.append("\n");
							} else {
								stdQueue.add(line);
							}
							if (redirectOutputToConsole) {
								System.out.println(line);
							}
							if (listener != null) {
								listener.info(line);
							}
							if (errorDetectionString != null && line.contains(errorDetectionString)) {
								errorDetectedByOutput = true;
								errorLineDetected = line;
							}
						}
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} while (line != null);
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
				stdQueue.add(endMarker); // to notify about the end
				stdPumpThreadFinished = true;
			}
			
		};
		Thread errStreamer = new Thread() {
			
			@Override
			public void run() {
				BufferedReader reader = streamProvider.getErrorOutReader();
				String line = null;
				do {
					try {
						line = reader.readLine();
						if (line != null) {
							countErrLines++;
							if (provideAllContentAtOnce) {
								if (sendErrOutputToStdOut) {
									if (stdAllContent == null) {
										stdAllContent = new StringBuilder();
									}
									stdAllContent.append(line);
									stdAllContent.append("\n");
								} else {
									if (errAllContent == null) {
										errAllContent = new StringBuilder();
									}
									errAllContent.append(line);
									errAllContent.append("\n");
								}
							} else {
								if (sendErrOutputToStdOut) {
									stdQueue.add(line);
								} else {
									errQueue.add(line);
								}
							}
							if (redirectOutputToConsole) {
								if (sendErrOutputToStdOut) {
									System.out.println(line);
								} else {
									System.err.println(line);
								}
							}
							if (listener != null) {
								listener.error(line);
							}
							if (errorDetectionString != null && line.contains(errorDetectionString)) {
								errorDetectedByOutput = true;
								errorLineDetected = line;
							}
						}
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} while (line != null);
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
				errQueue.add(endMarker); // to notify about the end
				errPumpThreadFinished = true;
			}
			
		};
		stdStreamer.start();
		errStreamer.start();
	}
	
	public boolean isStreamPumpThreadsFinished() {
		return stdPumpThreadFinished && errPumpThreadFinished;
	}
	
	/**
	 * After the process has been started with the execute method, this method returns true if the standard or error output line is available. 
	 * @return true if output is available
	 */
	public boolean next() {
		// fetch a standard out line
		if (stdStreamQueueHasData) {
			stdLine = stdQueue.poll(); // returns null if the queue is empty
			if (endMarker.equals(stdLine)) {
				stdStreamQueueHasData = false;
				stdLine = null;
			}
		}
		// fetch a error out line
		if (errStreamQueueHasData) {
			errorLine = errQueue.poll(); // returns null if the queue is empty
			if (endMarker.equals(errorLine)) {
				errStreamQueueHasData = false;
				errorLine = null;
			}
		}
		return stdStreamQueueHasData || errStreamQueueHasData;
	}
	
	public boolean killed() {
		if (executor.getWatchdog() != null) {
			return executor.getWatchdog().killedProcess();
		}
		return false;
	}
	
	/**
	 * Because next checks for both channels, this method checks id the standard output is available.
	 * @return true if stdout is available
	 * @throws IOException
	 */
	public boolean hasCurrentStdLine() throws IOException {
		return (stdLine != null);
	}
	
	/**
	 * Because next checks for both channels, this method checks id the error output is available.
	 * @return true if errout is available
	 * @throws IOException
	 */
	public boolean hasCurrentErrLine() throws IOException {
		return (errorLine != null);
	}
	
	/**
	 * @return the current standard output line
	 */
	public String getStdCurrentOutLine() {
		return stdLine;
	}
	
	/**
	 * @return the current error output line
	 */
	public String getErrCurrentOutLine() {
		return errorLine;
	}

	/**
	 * @return work dir of the process
	 */
	public String getWorkDir() {
		return workDir;
	}

	/**
	 * set the work dir of the process
	 * @param workDir
	 */
	public ProcessHelper setWorkDir(String workDir) {
		this.workDir = workDir;
		return this;
	}
	
	/**
	 * Returns the exit code of the process. Waits for the end of the process.
	 * @return return code
	 * @throws InterruptedException
	 */
	public int getExitCode() throws InterruptedException {
		resultHandler.waitFor();
		return resultHandler.getExitValue();
	}
	
	public boolean successful() throws InterruptedException {
		return (defaultOkExitCode == getExitCode()) && killed() == false && errorDetectedByOutput == false;
	}

	public int getCountReceivedStdLines() {
		return countStdLines;
	}

	public int getCountReceivedErrLines() {
		return countErrLines;
	}

	public int getMaxProcessRuntimeSec() {
		return maxProcessRuntimeSec;
	}

	/**
	 * set a maximum runtime in seconds for the process. Set 0 to disable the watchdog
	 * @param maxProcessRuntimeSec
	 */
	public ProcessHelper setMaxProcessRuntimeSec(Integer maxProcessRuntimeSec) {
		if (maxProcessRuntimeSec != null) {
			this.maxProcessRuntimeSec = maxProcessRuntimeSec;
		}
		return this;
	}

	public boolean isProvideAllContentAtOnce() {
		return provideAllContentAtOnce;
	}

	/**
	 * if true the content of output will be kept in memory and provided at once
	 * Take care this can lead to memory leaks.
	 * @param provideAllContentAtOnce
	 * @return
	 */
	public ProcessHelper setProvideAllContentAtOnce(Boolean provideAllContentAtOnce) {
		if (provideAllContentAtOnce != null) {
			this.provideAllContentAtOnce = provideAllContentAtOnce;
		}
		return this;
	}
	
	public String getStdOutTextFull() {
		if (stdAllContent != null) {
			return stdAllContent.toString();
		} else {
			return null;
		}
	}

	public String getErrOutTextFull() {
		if (errAllContent != null) {
			return errAllContent.toString();
		} else {
			return null;
		}
	}

	public boolean isRedirectOutputToConsole() {
		return redirectOutputToConsole;
	}

	/**
	 * set true to send the output also to the console.
	 */
	public ProcessHelper setRedirectOutputToConsole(Boolean redirectOutputToConsole) {
		if (redirectOutputToConsole != null) {
			this.redirectOutputToConsole = redirectOutputToConsole;
		}
		return this;
	}

	public boolean isSendErrOutputToStdOut() {
		return sendErrOutputToStdOut;
	}

	public void setSendErrOutputToStdOut(boolean sendErrOutputToStdOut) {
		this.sendErrOutputToStdOut = sendErrOutputToStdOut;
	}

	public int getDefaultOkExitCode() {
		return defaultOkExitCode;
	}

	public void setDefaultOkExitCode(Integer defaultOkExitCode) {
		if (defaultOkExitCode != null) {
			this.defaultOkExitCode = defaultOkExitCode;
		}
	}

	public OutputListener getListener() {
		return listener;
	}

	public void setListener(OutputListener listener) {
		this.listener = listener;
	}

	public String getErrorDetectionString() {
		return errorDetectionString;
	}

	public void setErrorDetectionString(String errorDetectionString) {
		if (errorDetectionString != null) {
			errorDetectionString = errorDetectionString.trim();
		}
		this.errorDetectionString = errorDetectionString;
	}

	public boolean isErrorDetectedByOutput() {
		return errorDetectedByOutput;
	}

	public String getErrorLineDetected() {
		return errorLineDetected;
	}

}
