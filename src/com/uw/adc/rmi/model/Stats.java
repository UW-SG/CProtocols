package com.uw.adc.rmi.model;

public class Stats {
	
	private String operation;
	private long time;
	
	public Stats(String operation, long time){
		this.operation = operation;
		this.time = time;
	}
	
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public String toString(){
		return "Stats ["
				+ (operation != null ? "operation=" + operation + ", " : "") 
				+ "time=" + time + "]";
	}

}
