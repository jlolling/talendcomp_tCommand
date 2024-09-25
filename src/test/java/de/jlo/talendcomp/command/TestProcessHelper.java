package de.jlo.talendcomp.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestProcessHelper {
	
	@Test
	public void testProcessHelper() throws Exception {
		int lines = 20;
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine("java");
		h.addToCommandLine("de.jlo.talendcomp.command.TestProcess");
		h.addToCommandLine(lines);
		h.setWorkDir("C:\\development\\eclipse-workspace\\talendcomp_tCommand\\target\\test-classes");
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
		h.setWorkDir("C:\\development\\eclipse-workspace\\talendcomp_tCommand\\target\\test-classes");
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
		h.setWorkDir("C:\\development\\eclipse-workspace\\talendcomp_tCommand\\target\\test-classes");
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
		h.setWorkDir("C:\\development\\eclipse-workspace\\talendcomp_tCommand\\target\\test-classes");
		h.execute();
		System.out.println("TEST: Process started");
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
		h.setWorkDir("C:\\development\\eclipse-workspace\\talendcomp_tCommand\\target\\test-classes");
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

}