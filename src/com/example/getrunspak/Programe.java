package com.example.getrunspak;

import android.graphics.drawable.Drawable;

public class Programe {
	// ͼ��
	private Drawable icon;
	// ������
	private String name;
	private int pID;
	private String memory;

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPID(int id){
		this.pID = id;
	}
	public  int getPID(){
		return pID;
	}
	
	public void setMemory(String m){
		this.memory = m;
	}
	public String getMemory(){
		return memory;
	}
}