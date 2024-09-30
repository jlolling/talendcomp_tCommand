package de.jlo.talendcomp.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.apache.commons.exec.OS;
import org.junit.Test;

public class TestProcessHelper {
	
	@Test
	public void testProcessHelper() throws Exception {
		int lines = 20;
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine("java");
		h.addToCommandLine("de.jlo.talendcomp.command.TestProcess");
		h.addToCommandLine(lines);
		h.setWorkDir(Utils.getNativeFilePath(System.getProperty("user.dir") + "/target/test-classes"));
		h.execute();
		int countStd = 0;
		int countErr = 0;
		System.out.println("TEST: Process started");
		Thread.sleep(5000l); // simulate the delay to make the resources available working with the outputs
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				System.out.println("STD: " + h.getStdCurrentOutLine());
				countStd++;
			}
			if (h.hasCurrentErrLine()) {
				System.out.println("ERR: " + h.getErrCurrentOutLine());
				countErr++;
			}
		}
		System.out.println("TEST: Process ended");
		System.out.println("TEST: count std lines received: " + h.getCountReceivedStdLines() + " count err lines received: " + h.getCountReceivedErrLines());
		assertEquals("Std Out line count wrong", lines + 2, countStd);
		assertEquals("Err Out line count wrong", lines, countErr);
		int exitCode = h.getExitCode();
		System.out.println("TEST: Exit code: " + exitCode);
		assertEquals("Exit code wrong", 2, exitCode);
	}

	@Test
	public void testProcessHelperWrongCommand() throws Exception {
		ProcessHelper h = new ProcessHelper();
		h.setSingleLineCommand("xxx /c echo HELLO");
		try {
			h.execute();
		} catch (Exception e) {
			if (e.getMessage().contains("Execution failed (Exit value: -559038737)")) {
				assertTrue(true);
			} else {
				assertTrue("wrong error message", false);
			}
			return;
		}
		assertTrue("Failed to detect wrong command", false);
	}

	@Test
	public void testProcessHelperHello() throws Exception {
		int lines = 1;
		ProcessHelper h = new ProcessHelper();
		h.setSingleLineCommand("cmd /c echo HELLO");
		h.execute();
		int countStd = 0;
		int countErr = 0;
		System.out.println("TEST: Process started");
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				System.out.println("STD: " + h.getStdCurrentOutLine());
				countStd++;
			}
			if (h.hasCurrentErrLine()) {
				System.out.println("ERR: " + h.getErrCurrentOutLine());
				countErr++;
			}
		}
		System.out.println("TEST: Process ended");
		System.out.println("TEST: count std lines received: " + h.getCountReceivedStdLines() + " count err lines received: " + h.getCountReceivedErrLines());
		assertEquals("Std Out line count wrong", lines, countStd);
		assertEquals("Err Out line count wrong", 0, countErr);
		int exitCode = h.getExitCode();
		System.out.println("TEST: Exit code: " + exitCode);
		assertEquals("Exit code wrong", 0, exitCode);
	}

	@Test
	public void testProcessHelperErrToStd() throws Exception {
		int lines = 20;
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine("java");
		h.addToCommandLine("de.jlo.talendcomp.command.TestProcess");
		h.addToCommandLine(lines);
		h.setSendErrOutputToStdOut(true);
		h.setWorkDir(Utils.getNativeFilePath(System.getProperty("user.dir") + "/target/test-classes"));
		h.execute();
		int countStd = 0;
		int countErr = 0;
		System.out.println("TEST: Process started");
		Thread.sleep(1l); // simulate the delay to make the resources available working with the outputs
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				System.out.println("STD: " + h.getStdCurrentOutLine());
				countStd++;
			}
			if (h.hasCurrentErrLine()) {
				System.out.println("ERR: " + h.getErrCurrentOutLine());
				countErr++;
			}
		}
		System.out.println("TEST: Process ended");
		System.out.println("TEST: count std lines received: " + h.getCountReceivedStdLines() + " count err lines received: " + h.getCountReceivedErrLines());
		assertEquals("Std Out line count wrong", (lines * 2) + 2, countStd);
		assertEquals("Err Out line count wrong", 0, countErr);
		int exitCode = h.getExitCode();
		System.out.println("TEST: Exit code: " + exitCode);
		assertEquals("Exit code wrong", 2, exitCode);
	}

	@Test
	public void testProcessHelperMaxRuntime() throws Exception {
		int lines = 100;
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine("java");
		h.addToCommandLine("de.jlo.talendcomp.command.TestProcess");
		h.addToCommandLine(lines);
		h.setSendErrOutputToStdOut(true);
		h.setMaxProcessRuntimeSec(1);
		h.setWorkDir(Utils.getNativeFilePath(System.getProperty("user.dir") + "/target/test-classes"));
		h.execute();
		System.out.println("TEST: Process started");
		Thread.sleep(1l); // simulate the delay to make the resources available working with the outputs
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				System.out.println("STD: " + h.getStdCurrentOutLine());
			}
			if (h.hasCurrentErrLine()) {
				System.out.println("ERR: " + h.getErrCurrentOutLine());
			}
		}
		System.out.println("TEST: Process ended");
		System.out.println("TEST: count std lines received: " + h.getCountReceivedStdLines() + " count err lines received: " + h.getCountReceivedErrLines());
		int exitCode = h.getExitCode();
		System.out.println("TEST: Exit code: " + exitCode);
		assertEquals("Exit code wrong", 1, exitCode);
		assertTrue("Killed detection wrong", h.killed());
	}

	@Test
	public void testProcessHelperCheckSuccessfull() throws Exception {
		int lines = 1;
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine("java");
		h.addToCommandLine("de.jlo.talendcomp.command.TestProcess");
		h.addToCommandLine(lines);
		h.setSendErrOutputToStdOut(true);
		h.setDefaultOkExitCode(2);
		h.setWorkDir(Utils.getNativeFilePath(System.getProperty("user.dir") + "/target/test-classes"));
		h.execute();
		System.out.println("TEST: Process started. user.dir=" + System.getProperty("user.dir"));
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				System.out.println("STD: " + h.getStdCurrentOutLine());
			}
			if (h.hasCurrentErrLine()) {
				System.out.println("ERR: " + h.getErrCurrentOutLine());
			}
		}
		System.out.println("TEST: Process ended");
		System.out.println("TEST: count std lines received: " + h.getCountReceivedStdLines() + " count err lines received: " + h.getCountReceivedErrLines());
		int exitCode = h.getExitCode();
		System.out.println("TEST: Exit code: " + exitCode);
		assertEquals("Exit code wrong", 2, exitCode);
		assertTrue("Exit code not match default ok code", h.successful());
		assertTrue("Killed detection wrong", h.killed() == false);
	}

	@Test
	public void testProcessHelperSilent() throws Exception {
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine("java");
		h.addToCommandLine("de.jlo.talendcomp.command.TestProcessSilent");
		h.setWorkDir(Utils.getNativeFilePath(System.getProperty("user.dir") + "/target/test-classes"));
		h.execute();
		int countStd = 0;
		int countErr = 0;
		System.out.println("TEST: Process started");
		Thread.sleep(1l); // simulate the delay to make the resources available working with the outputs
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				System.out.println("STD: " + h.getStdCurrentOutLine());
				countStd++;
			}
			if (h.hasCurrentErrLine()) {
				System.out.println("ERR: " + h.getErrCurrentOutLine());
				countErr++;
			}
		}
		System.out.println("TEST: Process ended");
		System.out.println("TEST: count std lines received: " + h.getCountReceivedStdLines() + " count err lines received: " + h.getCountReceivedErrLines());
		assertEquals("Std Out line count wrong", 0, countStd);
		assertEquals("Err Out line count wrong", 0, countErr);
		int exitCode = h.getExitCode();
		System.out.println("TEST: Exit code: " + exitCode);
		assertEquals("Exit code wrong", 2, exitCode);
	}
	
	@Test
	public void testHandoverParameters() throws Exception, IOException {
		String filePath = null;
		if (OS.isFamilyUnix()) {
			String script = "while [ $# -gt 0 ]\n"
					+ "do\n"
					+ "    echo $1\n"
					+ "    shift\n"
					+ "done";
			filePath = "/tmp/test.sh";
			Files.write(java.nio.file.Paths.get(filePath), script.getBytes("UTF-8"), StandardOpenOption.CREATE);
			Files.setPosixFilePermissions(java.nio.file.Paths.get(filePath),  java.nio.file.attribute.PosixFilePermissions.fromString("rwxr--r--"));
		} else {
			String script = ":Loop\r\n"
					+ "IF [%1]==[] GOTO Continue\r\n"
					+ "    @ECHO %1\r\n"
					+ "SHIFT\r\n"
					+ "GOTO Loop\r\n"
					+ ":Continue";
			filePath = "c:\\temp\\test.cmd";
			Files.write(java.nio.file.Paths.get(filePath), script.getBytes("UTF-8"), StandardOpenOption.CREATE);
		}
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine(filePath);
		h.addToCommandLine("\"param_key1=param_value1\"");
		h.addToCommandLine("\"param_key2=param_value 2\"");
		h.execute();
		int countStd = 0;
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				String line = h.getStdCurrentOutLine();
				if (line.startsWith("\"param_")) {
					System.out.println(line);
					countStd++;
				}
			}
		}
		assertEquals("wrong number arguments", 2, countStd);
	}

}