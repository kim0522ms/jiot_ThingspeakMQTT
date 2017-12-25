/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.i2c_dev.drivers;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.example.i2c_dev.I2CRPi;
import com.example.i2c_dev.I2CUtils;
import com.example.i2c_dev.PCF8591;

@Component
public class PCF8591Device extends I2CRPi {
    private static final int PCF8591Addr = 0x48;
    private int currentAinPin = -1;

    public PCF8591Device() throws IOException {
        super(PCF8591Addr);
 //       device.write(0x00);		// IOException occured...
    }
    
    public int analogRead(int ainPin) {        
        int value = 0;
        
        if (currentAinPin == ainPin) {
            value = I2CUtils.read(device);  // a case when try to read the AIN having been previously read
        }
        else {
            currentAinPin = ainPin;
            switch (ainPin) {               // send analog input command
            case 0:
                PCF8591.AIN0.read(device);
                break;
            case 1:
                PCF8591.AIN1.read(device);
                break;
            case 2:
                PCF8591.AIN2.read(device);
                break;
            case 3:
                PCF8591.AIN3.read(device);
                break;
            }
            I2CUtils.I2Cdelay(1000);        // allow command processing delay
            I2CUtils.read(device);          // skip the latched value
            value = I2CUtils.read(device);  // read a new value
        }
        
        return value;
    }
    
    public void analogWrite(int pwm) throws IOException {
        PCF8591.AOUT.write(device, pwm);
        currentAinPin = -1;     // reset the analog input pin saved
    }
}
