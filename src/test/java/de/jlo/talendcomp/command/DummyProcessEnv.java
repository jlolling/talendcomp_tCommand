package de.jlo.talendcomp.command;

import java.util.Map;

public class DummyProcessEnv {

	public static void main(String[] args) {
		System.out.println("Test process started");
		for (Map.Entry<String, String> ee : System.getenv().entrySet()) {
			System.out.println(ee.getKey() + "=" + ee.getValue());
		}
		System.exit(0);
	}

}
