package de.jlo.talendcomp.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;

public class ProcessHelper {
	
	private CommandLine commandLine = null;
	private final Map<String, String> placeHolderMap = new HashMap<String, String>();
	private String workDir = null;
	private DefaultExecutor executor = null;
	private final ProcessStreamProvider streamProvider = new ProcessStreamProvider();
	private String stdLine = null;
	private String errorLine = null;
	private final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
	private final Map<String, String> environmentMap = new HashMap<String, String>();
	private final Queue<String> stdQueue = new ConcurrentLinkedQueue<String>();
	private final Queue<String> errQueue = new ConcurrentLinkedQueue<String>();
	private final String endMarker = "PROCESSHELPER_STREAM_END";
	private int countStdLines = 0;
	private int countErrLines = 0;
	
	/**
	 * Creates a new command line or adds arguments to it.
	 * 
	 * @param command
	 * @return this to allow builder pattern
	 */
	public ProcessHelper addToCommandLine(Object command) {
		if (command != null) {
			if (commandLine == null) {
				commandLine = new CommandLine(String.valueOf(command));
			} else {
				commandLine.addArgument(String.valueOf(command));
			}
		}
		return this;
	}
	
	/**
	 * In command you can use place holders like ${file} and 
	 * @param key
	 * @param value
	 * @return
	 */
	public ProcessHelper setPlaceholderValue(String key, String value) {
		placeHolderMap.put(key, value);
		return this;
	}
	
	public void execute() throws Exception {
		if (workDir == null) {
			workDir = System.getProperty("user.dir");
		}
		File fwd = new File(workDir);
		if (fwd.exists() == false) {
			fwd.mkdirs();
		}
		if (fwd.exists() == false) {
			throw new Exception("work-dir: " + fwd.getAbsolutePath() + " does not exist and cannot be created");
		}
		executor = DefaultExecutor.builder()
				.setExecuteStreamHandler(streamProvider)
				.setWorkingDirectory(new File(workDir))
				.get();
		// the streams will be set to the streamProvider inside the execute method.
		// once the process is started and the streams are available the running state is set
		executor.execute(commandLine, environmentMap, resultHandler);
		while (true) {
			if (streamProvider.isRunning()) {
				break;
			} else {
				Thread.sleep(100l);
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
							stdQueue.add(line);
						}
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} while (line != null);
				stdQueue.add(endMarker); // to notify about the end
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
							errQueue.add(line);
						}
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} while (line != null);
				errQueue.add(endMarker); // to notify about the end
			}
			
		};
		stdStreamer.start();
		errStreamer.start();
	}
	
	public boolean next() {
		boolean hasNext = false;
		// fetch a standard out line
		stdLine = stdQueue.poll(); // returns null if the queue is empty
		if (stdLine != null && endMarker.equals(stdLine) == false) {
			hasNext = true;
		} else {
			stdLine = null;
		}
		// fetch a error out line
		errorLine = errQueue.poll(); // returns null if the queue is empty
		if (errorLine != null && endMarker.equals(errorLine) == false) {
			hasNext = true;
		} else {
			errorLine = null;
		}
		return hasNext;
	}
	
	public boolean hasCurrentStdLine() throws IOException {
		return (stdLine != null);
	}
	
	public boolean hasCurrentErrLine() throws IOException {
		return (errorLine != null);
	}
	
	public String getStdOutLine() {
		return stdLine;
	}
	
	public String getErrorOutLine() {
		return errorLine;
	}

	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
	
	public int getExitCode() throws InterruptedException {
		resultHandler.waitFor();
		return resultHandler.getExitValue();
	}

	public int getCountStdLines() {
		return countStdLines;
	}

	public int getCountErrLines() {
		return countErrLines;
	}

}
