package de.jlo.talendcomp.command;

public class TestProcessSilent {

	public static void main(String[] args) {
		try {
			Thread.sleep(5000l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(2);
	}

}
