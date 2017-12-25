package com.example.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ControlPointContainer {

	private static AtomicReference<ControlPointContainer> instance = 
			new AtomicReference<ControlPointContainer>();
	
	public static ControlPointContainer getInstance() {
		if (instance.get() == null) {
			instance.set(new ControlPointContainer());
		}
		return instance.get();
	}
	
	private Map<Integer, ControlPoint> controlPoints = new HashMap<Integer, ControlPoint>();
	
	protected ControlPointContainer() {		
	}
	
	public void createControlPoints() {
		System.out.println("-------------------------------------------------");
		System.out.println("Init 1st LED...");
		ControlPoint point = new GPIOPinOutputControlPoint(17);
		putControlPoint(point);
		System.out.println("Finished.");
		
		System.out.println("Init 2nd LED...");
		point = new GPIOPinOutputControlPoint(27);
		putControlPoint(point);
		System.out.println("Finished.");
		
		System.out.println("Init 3rd LED...");
		point = new GPIOPinOutputControlPoint(22);
		putControlPoint(point);
		System.out.println("Finished.");
		
		System.out.println("Init Blink LED...");
		point = new AlertGPIOPinOutputControlPoint(26, 18);
		putControlPoint(point);
		System.out.println("Finished.");
		
		
		System.out.println("Init TMP102 Device...");
		point = new TMP102AnalogIOPoint(0);
		putControlPoint(point);
		System.out.println("Finished.");
		
		
		System.out.println("Init PCF8591 Device...");
		point = new PCF8591AnalogIOPoint(1);
		putControlPoint(point);
		System.out.println("Finished.");
		System.out.println("-------------------------------------------------");
	}

	public void start() {
		createControlPoints();

		for (ControlPoint cp : controlPoints.values()) {
			cp.open();
		}
	}
	
	public void stop() {
		for (ControlPoint cp : controlPoints.values()) {
			cp.close();
		}
		controlPoints.clear();
		ControlPoint.POLLING.shutdown();
	}
	
	public Collection<ControlPoint> getControlPoints() {
		return Collections.unmodifiableCollection(controlPoints.values());
	}
	
	public ControlPoint getControlPoint(int pointId) {
		return controlPoints.get(pointId);
	}
	
	public void putControlPoint(ControlPoint cp) {
		controlPoints.put(cp.getId(), cp);
	}
}