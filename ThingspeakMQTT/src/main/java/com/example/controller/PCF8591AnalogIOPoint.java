package com.example.controller;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.example.i2c_dev.drivers.PCF8591Device;

public class PCF8591AnalogIOPoint extends OutputControlPoint {
	private static AtomicReference<PCF8591Device> adcDevice = 
			new AtomicReference<PCF8591Device>();
	
	private static PCF8591Device getAdcDevice() {
		try {
			if (adcDevice.get() == null)
				adcDevice.set(new PCF8591Device());			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return adcDevice.get();
	}

	private static final AtomicInteger OPEN_COUNT = new AtomicInteger(0);
	private static final int PWM_PIN = 4;
	
	private int aioPin;
	private Future pollingFuture;
	
	public PCF8591AnalogIOPoint(int aioPin) {
		super();
		this.aioPin = aioPin;
	}
	
	@Override
	public void open() {
		OPEN_COUNT.incrementAndGet();
		
		if (isAnalogInput()) {
			pollingFuture = POLLING.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						int oldValue = presentValue.get();
						int newValue = getAdcDevice().analogRead(aioPin);
						presentValue.set(newValue);
						if (oldValue != newValue) {
							System.out.println(String.format("Notice : Photometer value Changed to %d", newValue));
							fireChanged();
						}					
					}
				}, 5, 5, TimeUnit.SECONDS);
		}
	}

	@Override
	public void close() {
		int ref_count = OPEN_COUNT.decrementAndGet();
		if (ref_count >= 0) {
			if (isAnalogInput()) {
				pollingFuture.cancel(false);
			}
			
			if (ref_count == 0) {
				getAdcDevice().close();
				adcDevice.set(null);
			}
		}
		else {
			OPEN_COUNT.set(0);
		}
	}

	@Override
	public boolean isEnabled() {
		return (getAdcDevice().device.isOpen());
	}

	@Override
	public Type getType() {
		return  isAnalogInput() ? Type.AI : Type.AO;
	}

	@Override
	public void setPresentValue(int value) {
		try {
			if (!isAnalogInput()) {
				int oldValue = presentValue.get();
				getAdcDevice().analogWrite(value);
				presentValue.set(value);
				if (oldValue != value) {
					fireChanged();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isAnalogInput() {
		return (this.aioPin < PWM_PIN);
	}
}