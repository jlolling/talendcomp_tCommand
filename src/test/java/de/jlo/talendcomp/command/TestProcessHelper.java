package de.jlo.talendcomp.command;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestProcessHelper {
	
	@Test
	public void testProcessHelper() throws Exception {
		int lines = 7;
		ProcessHelper h = new ProcessHelper();
		h.addToCommandLine("java");
		h.addToCommandLine("de.jlo.talendcomp.command.TestProcess");
		h.addToCommandLine(7);
		h.setWorkDir("C:\\development\\eclipse-workspace\\talendcomp_tCommand\\target\\test-classes");
		h.execute();
		int count = 0;
		System.out.println("Process started");
		while (h.hasNextStd()) {
			System.out.println("STD: " + h.nextStdOutLine());
			count++;
		}
		System.out.println("Process ended");
		assertEquals("Out line count wrong", lines + 2, count);
		int exitCode = h.getExitCode();
		System.out.println("Exit code: " + exitCode);
		assertEquals("Exit code wrong", 2, exitCode);
	}

}
