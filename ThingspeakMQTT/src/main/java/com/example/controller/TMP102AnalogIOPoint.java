package com.example.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.example.i2c_dev.drivers.TMP102Device;

public class TMP102AnalogIOPoint extends OutputControlPoint {
	private static AtomicReference<TMP102Device> tmp102Devices = 
			new AtomicReference<TMP102Device>();
	
	private static final AtomicInteger OPEN_COUNT = new AtomicInteger(0);
	private static final int PWM_PIN = 4;
	
	
	private int aioPin;
	private Future pollingFuture;
	
	private static TMP102Device getTmp102Device() {
		try {
			if (tmp102Devices.get() == null)
				tmp102Devices.set(new TMP102Device());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp102Devices.get();
	}
	
	
	
	public TMP102AnalogIOPoint(int aioPin) {
		super();
		this.aioPin = aioPin;
	}
	
	@Override
	public void setPresentValue(int value) {
		// TODO Auto-generated method stub

	}
	
	public void setHighLimitTemperature(int temp) {
		getTmp102Device().setHighLimitTempC(temp);
	}
	
	public void setLowLimitTemerature(int temp) {
		getTmp102Device().setLowLimitTempC(temp);
	}
	
	public String getHighLimitTemperature() {
		return Double.toString(getTmp102Device().readHighLimitTempC());
	}
	
	public String getLowLimitTemperature() {
		return Double.toString(getTmp102Device().readLowLimitTempC());
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		OPEN_COUNT.incrementAndGet();
		
		if (isAnalogInput()) {
			pollingFuture = POLLING.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						//int oldValue = presentValue.get();
						
						float oldValue = Math.round(Float.intBitsToFloat(presentValue.get()));
						
						// read(0) == TMP102.TERMPERATURE.read(device);
						float newValue = Math.round(getTmp102Device().readTempC());
						System.out.println(newValue);
												
						presentValue.set(Float.floatToIntBits(newValue));
						if (oldValue != newValue) {
							System.out.println("Notice : Thermometer value Changed to " + newValue);
							fireChanged();
						}					
					}
				}, 5, 5, TimeUnit.SECONDS);
		}

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		int ref_count = OPEN_COUNT.decrementAndGet();
		if (ref_count >= 0) {
			if (isAnalogInput()) {
				pollingFuture.cancel(false);
			}
			
			if (ref_count == 0) {
				getTmp102Device().close();
				tmp102Devices.set(null);
			}
		}
		else {
			OPEN_COUNT.set(0);
		}
	}

	@Override
	public boolean isEnabled() {
		return (getTmp102Device().device.isOpen());
	}

	@Override
	public Type getType() {
		return  isAnalogInput() ? Type.AI : Type.AO;
	}

	private boolean isAnalogInput() {
		return (this.aioPin < PWM_PIN);
	}
}
