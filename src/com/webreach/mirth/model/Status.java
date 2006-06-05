package com.webreach.mirth.model;

public class Status {
	public enum State {
		STARTED, STOPPED, PAUSED
	};

	private int id;
	private String name;
	private State state;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
