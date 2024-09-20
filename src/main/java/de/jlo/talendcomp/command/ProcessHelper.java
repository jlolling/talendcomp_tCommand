package de.jlo.talendcomp.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;

public class ProcessHelper {
	
	private CommandLine commandLine = null;
	private Map<String, String> placeHolderMap = new HashMap<String, String>();
	private String workDir = null;
	private DefaultExecutor executor = null;
	private ProcessStreamProvider streamProvider = null;
	private String stdLine = null;
	private String errorLine = null;
	private DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
	
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
		streamProvider = new ProcessStreamProvider();
		executor = DefaultExecutor.builder()
				.setExecuteStreamHandler(streamProvider)
				.setWorkingDirectory(new File(workDir))
				.get();
		// the streams will be set to the streamProvider inside the execute method.
		// once the process is started and the streams are available the running state is set
		executor.execute(commandLine, resultHandler);
		while (true) {
			if (streamProvider.isRunning()) {
				break;
			} else {
				Thread.sleep(100l);
			}
		}
	}
	
	public boolean hasNextStd() throws IOException {
		BufferedReader reader = streamProvider.getStandardOutReader();
		stdLine = reader.readLine();
		if (stdLine == null) {
			return false;
		}
		return streamProvider.isRunning();
	}
	
	public boolean hasNextErr() throws IOException {
		BufferedReader reader = streamProvider.getErrorOutReader();
		errorLine = reader.readLine();
		if (errorLine == null) {
			return false;
		}
		return streamProvider.isRunning();
	}
	
	public String nextStdOutLine() {
		return stdLine;
	}
	
	public String nextErrorOutLine() {
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

}
