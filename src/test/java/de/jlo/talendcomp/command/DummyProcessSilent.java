package de.jlo.talendcomp.command;

public class DummyProcessSilent {

	public static void main(String[] args) {
		long duration = 0l;
		if (args.length > 0) {
			duration = Long.valueOf(args[0]);
		}
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

}
