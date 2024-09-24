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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.exec.ExecuteStreamHandler;

/**
 * Helper class providing the IO streams of the started process
 */
public class ProcessStreamProvider implements ExecuteStreamHandler {
	
	private BufferedReader errorOutReader = null;
	private OutputStream inputOutputStream = null;
	private BufferedReader standardOutReader = null;
	private boolean running = false;
	
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
	 * @return reader for error output
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
	 * @return reader for standard output
	 */
	public BufferedReader getStandardOutReader() {
		return standardOutReader;
	}

	/**
	 * process is running and streams are active if true
	 * @return running state
	 */
	public boolean isRunning() {
		return running;
	}

}
