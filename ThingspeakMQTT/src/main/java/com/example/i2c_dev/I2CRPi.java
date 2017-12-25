/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.i2c_dev;

import java.io.IOException;
import static jdk.dio.DeviceConfig.UNASSIGNED;
import jdk.dio.DeviceManager;
import jdk.dio.i2cbus.I2CDevice;
import jdk.dio.i2cbus.I2CDeviceConfig;

/**
 * Base definitions for create a device and its config
 *
 */
public class I2CRPi {

    private I2CDeviceConfig config;

    /**
     * Save device address establishing
     */
    public I2CDevice device = null;

    /**
     * Define device and config it
     *
     * @param i2cAddress
     * @throws IOException
     */
    public I2CRPi(int i2cAddress) throws IOException {
        config = new I2CDeviceConfig.Builder()
                .setControllerNumber(1)
                .setAddress(i2cAddress, I2CDeviceConfig.ADDR_SIZE_7)
                .setClockFrequency(UNASSIGNED)
                .build();

        device = (I2CDevice) DeviceManager.open(I2CDevice.class, config);
        if (device == null)
        	throw new IOException("I2CDevice open failed");
    }

    /**
     * free device resource
     *
     */
    public void close() {
        try {
            device.close();       
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}