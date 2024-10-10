package de.jlo.talendcomp.command;

public class DummyProcess {

	public static void main(String[] args) {
		System.out.println("Test process started");
		int n= 1;
		if (args.length > 0) {
			n = Integer.valueOf(args[0]);
			System.out.println("Loop count: " + n);
		}
		for (int i = 0; i < n; i++) {
			System.out.println("standard: " + i);
			System.err.println("failure: " + i);
			try {
				Thread.sleep(200l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.exit(2);
	}

}
