package com.example.controller;

import java.io.IOException;

import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceManager;
import jdk.dio.DeviceNotFoundException;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.UnsupportedDeviceTypeException;
import jdk.dio.gpio.GPIOPin;

public class GPIOPinOutputControlPoint extends OutputControlPoint {
	private int pinId;
	private GPIOPin pinDev;
	
	public GPIOPinOutputControlPoint(int pinId) {
		super();
		this.pinId = pinId;
	}

	@Override
	public void open() {
		try {
			pinDev = (GPIOPin)DeviceManager.open(pinId, GPIOPin.class);
		} catch (UnsupportedDeviceTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeviceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnavailableDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setPresentValue(0);
	}

	@Override
	public void close() {
		if (isEnabled()) {
			try {
				pinDev.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pinDev = null;
		}
	}

	@Override
	public boolean isEnabled() {
		return (pinDev != null && pinDev.isOpen());
	}

	@Override
	public Type getType() {
		return Type.DO;
	}

	@Override
	public void setPresentValue(int value) {
		int oldValue = getPresentValue();
		
		if (writeValue(value) && oldValue != getPresentValue()) {
			fireChanged();
		}
	}
	
	private boolean writeValue(int value) {
		boolean success = false;
		try {
			pinDev.setValue(value == 1);
			presentValue.set(value);
			success = true;
		} catch (UnavailableDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClosedDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}	
}
