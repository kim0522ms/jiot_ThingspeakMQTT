package com.example.controller;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceManager;
import jdk.dio.DeviceNotFoundException;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.UnsupportedDeviceTypeException;
import jdk.dio.gpio.GPIOPin;

public class AlertGPIOPinOutputControlPoint extends OutputControlPoint {
	private int alertPinId;
	private int ledPinId;
	private GPIOPin alertDev;
	private GPIOPin ledPinDev;
	private Future pollingFuture;
	
	private static final AtomicInteger ALERT = new AtomicInteger(0);
	private static AtomicBoolean isAlerted = new AtomicBoolean(false);
	
	public AlertGPIOPinOutputControlPoint(int alertPinId, int ledPinId) {
		super();
		this.alertPinId = alertPinId;
		this.ledPinId = ledPinId;
	}
	
	public void blinkLed() {
		try {
			ledPinDev.setValue(!ledPinDev.getValue());
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
	}

	@Override
	public void open() {
		try {
			alertDev = (GPIOPin)DeviceManager.open(alertPinId, GPIOPin.class);
			ledPinDev = (GPIOPin)DeviceManager.open(ledPinId, GPIOPin.class);
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
		
		pollingFuture = POLLING.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					try {
						int oldValue = ALERT.get();
						int newValue = alertDev.getValue() ? 1 : 0;
						ALERT.set(newValue);
						if (oldValue != newValue) {
							System.out.println("Notice : Temperature High Limit Alert Detected !!!");
							
							// Alert가 꺼졌는지 확인할 수 있는 Flag를 true(켜져있음)으로 설정
							isAlerted.set(true);
							fireChanged();
						}
						else {
							// Alert GPIO Pin이 LOW가 되었지만 isAlerted Flag가 True일 경우 Flag를 False로 변경하고 LED를 끔
							if (isAlerted.get() == true)
							{
								isAlerted.set(false);
								ledPinDev.setValue(false);
							}
						}
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
				}
			}, 5, 2, TimeUnit.SECONDS);
	}

	@Override
	public void close() {
		if (isEnabled()) {
			try {
				ledPinDev.close();
				alertDev.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ledPinDev = null;
			alertDev = null;
		}
	}

	@Override
	public boolean isEnabled() {
		return (alertDev != null && alertDev.isOpen());
	}

	@Override
	public Type getType() {
		return Type.DO;
	}

	@Override
	public void setPresentValue(int value) {
	}
	
}
