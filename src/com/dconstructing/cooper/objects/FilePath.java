package com.dconstructing.cooper.objects;

public class FilePath {

	public boolean isDirectory = false;
	public String name;
	
	public FilePath(String name, boolean isDirectory) {
		this.isDirectory = isDirectory;
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}

}
