package com.example.controller;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ControlPoint extends Observable {
	public static enum Type {
		DI, DO, TDO, AI, AO
	};
	
	protected static final ScheduledExecutorService POLLING = 
			Executors.newSingleThreadScheduledExecutor();
	private static final AtomicInteger COUNT = new AtomicInteger(0);

	private int id;
	private String name;
	protected AtomicInteger presentValue = new AtomicInteger(0);
	
	public ControlPoint() {
		id = COUNT.getAndIncrement();
		name = getClass().getName() + "-" + id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		fireChanged("name");
	}

	public int getPresentValue() {
		return presentValue.get();
	}

	protected void fireChanged() {
		setChanged();
		notifyObservers();
	}

	protected void fireChanged(Object arg) {
		setChanged();
		notifyObservers(arg);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getName() + "(" + getId() + ") [type=" + getType() + ", enabled=" + isEnabled() + "]";
	}
	
	public abstract void open();
	
	public abstract void close();
	
	public abstract boolean isEnabled();
	
	public abstract Type getType();	
}
