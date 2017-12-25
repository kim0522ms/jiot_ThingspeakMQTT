/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.i2c_dev;

import jdk.dio.i2cbus.I2CDevice;

public enum TMP102 {
    
    /**
     *  Temperature register
     */
    TERMPERATURE(0x00),
    /**
     *  Config register
     */
    CONFIG(0x01),
    /**
     *  Analog Input 2
     */
    LOW_LIMIT(0x02),
    /**
     *  Analog Input 3
     */
    HIGH_LIMIT(0x03);

    /**
     *
     */
    public byte cmd;

    private TMP102(int cmd) {
        this.cmd = (byte) cmd;
    }

    /**
     *
     * @param device
     * @return
     */
    public int read(I2CDevice device) {
        return I2CUtils.readShort(device, this.cmd);
    }

    /**
     *
     * @param device
     * @param value
     */
    public void write(I2CDevice device, int value) {
        I2CUtils.writeShort(device, this.cmd, value);
    }
}
