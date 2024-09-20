package de.jlo.talendcomp.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.exec.ExecuteStreamHandler;

public class ProcessStreamProvider implements ExecuteStreamHandler {
	
	private BufferedReader errorOutReader = null;
	private OutputStream inputOutputStream = null;
	private BufferedReader standardOutReader = null;
	private boolean running = false;
	private boolean redirectErrOutToStdOut = true;
	
	@Override
	public void setProcessErrorStream(InputStream inputStream) throws IOException {
		this.errorOutReader = new BufferedReader(new InputStreamReader(inputStream));
	}

	@Override
	public void setProcessInputStream(OutputStream outputStream) throws IOException {
		this.inputOutputStream = outputStream;
	}

	@Override
	public void setProcessOutputStream(InputStream inputStream) throws IOException {
		this.standardOutReader = new BufferedReader(new InputStreamReader(inputStream));
	}

	@Override
	public void start() throws IOException {
		running = true;
	}

	@Override
	public void stop() throws IOException {
		if (standardOutReader != null) {
			standardOutReader.close();
		}
		if (errorOutReader != null) {
			errorOutReader.close();
		}
		running = false;
	}

	/**
	 * returns the process error output stream as BufferedReader
	 * @return
	 */
	public BufferedReader getErrorOutReader() {
		return errorOutReader;
	}

	/**
	 * returns the process input stream
	 * @return
	 */
	public OutputStream getInputOutputStream() {
		return inputOutputStream;
	}

	/**
	 * returns standard out as BufferedReader
	 * @return
	 */
	public BufferedReader getStandardOutReader() {
		return standardOutReader;
	}

	/**
	 * process is running and streams are active if true
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	public boolean isRedirectErrOutToStdOut() {
		return redirectErrOutToStdOut;
	}

	public void setRedirectErrOutToStdOut(boolean redirectErrOutToStdOut) {
		this.redirectErrOutToStdOut = redirectErrOutToStdOut;
	}

}
