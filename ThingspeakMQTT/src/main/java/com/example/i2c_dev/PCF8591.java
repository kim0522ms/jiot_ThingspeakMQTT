/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.i2c_dev;

import jdk.dio.i2cbus.I2CDevice;

public enum PCF8591 {

    /**
     *  Analog Input 0
     */
    AIN0(0x0),
    /**
     *  Analog Input 1
     */
    AIN1(0x01),
    /**
     *  Analog Input 2
     */
    AIN2(0x02),
    /**
     *  Analog Input 3
     */
    AIN3(0x03),
    /**
     *  Analog Output
     */
    AOUT(0x40);

    /**
     *
     */
    public byte cmd;

    private PCF8591(int cmd) {
        this.cmd = (byte) cmd;
    }

    /**
     *
     * @param device
     * @return
     */
    public int read(I2CDevice device) {
        return I2CUtils.read(device, this.cmd);
    }

    /**
     *
     * @param device
     * @param value
     */
    public void write(I2CDevice device, int value) {
        I2CUtils.write(device, this.cmd, (byte)value);
    }
}
