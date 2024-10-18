package de.jlo.talendcomp.command;

public interface OutputListener {
	
	public void info(String message);
	
	public void error(String message);
	
	public int countInfoLines();
	
	public int countErrorLines();

}
