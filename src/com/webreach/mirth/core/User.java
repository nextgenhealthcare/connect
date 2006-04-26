package com.webreach.mirth.core;

public class User {
	private int id;
	private String username;
	private String password;

	public User() {
		
	}

	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof User)) {
			return false;
		} else {
			User user = (User) obj;
			return (this.getUsername().equals(user.getUsername()) && this.getPassword().equals(user.getPassword()));
		}
	}
}
