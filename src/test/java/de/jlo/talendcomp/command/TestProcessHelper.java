package de.jlo.talendcomp.command;

import static org.junit.Assert.assertEquals;
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
		Thread.sleep(1l); // simulate the delay to make the resources available working with the outputs
		while (h.next()) {
			if (h.hasCurrentStdLine()) {
				System.out.println("STD: " + h.getStdOutLine());
				countStd++;
			}
			if (h.hasCurrentErrLine()) {
				System.out.println("ERR: " + h.getErrorOutLine());
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

}