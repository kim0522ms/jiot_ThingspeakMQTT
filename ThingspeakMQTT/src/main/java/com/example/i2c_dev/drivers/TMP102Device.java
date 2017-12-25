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
import com.example.i2c_dev.TMP102;

@Component
public class TMP102Device extends I2CRPi {
    private static final int TMP102_ADDR = 0x49;    // jump ADD0 to 
 
    public static final int TEMP_REG = 0x00;
    public static final int CONFIG_REG = 0x01;
    public static final int LOW_LIMIT_REG = 0x02;
    public static final int HIGH_LIMIT_REG = 0x03;
    
    public static final float TEMP_RESOLUTION = 0.0625F;    
    
    public static final int OS_MASK = 0x08000;
    public static final int RS_MASK = 0x06000;
    public static final int FQ_MASK = 0x01800;
    public static final int POL_MASK = 0x00400;
    public static final int TM_MASK = 0x00200;
    public static final int SD_MASK = 0x00100;
    public static final int CR_MASK = 0x000C0;
    public static final int AL_MASK = 0x00020;
    public static final int EM_MASK = 0x00010;
    
    public TMP102Device() throws IOException {
        super(TMP102_ADDR);
    }
  
    public int read(int reg) {
        int value = 0;
        
        switch (reg) {
            case 0:
                value = TMP102.TERMPERATURE.read(device);
                break;
            case 1:
                value = TMP102.CONFIG.read(device);
                break;
            case 2:
                value = TMP102.LOW_LIMIT.read(device);
                break;
            case 3:
                value = TMP102.HIGH_LIMIT.read(device);
                break;
        }
        
        return value;
    }
    
    public void write(int reg, int value) {
        switch (reg) {
            case 1:
                TMP102.CONFIG.write(device, value);
                break;
            case 2:
                TMP102.LOW_LIMIT.write(device, value);
                break;
            case 3:
                TMP102.HIGH_LIMIT.write(device, value);
                break;
        }
    }
    
    public float readTempC() {
        int regValue = read(TEMP_REG);
        
        if ((regValue & 0x01) != 0) {
            // 13-bit extended mode
            regValue >>= 3;
        }
        else {
            // 12-bit normal mode
            regValue >>= 4;
        }
        return regValue * TEMP_RESOLUTION;
    }
    
    public double readTempF() {
        return (readTempC() * 9.0 / 5.0) + 32.0;
    }
    
    public void setLowLimitTempC(double temp) {
        if (temp > 150.0)  temp = 150.0;
        else if (temp < -55.0)  temp = -55.0;
        
        int regValue = (int)(temp / TEMP_RESOLUTION);
        
        if (isExtendedMode()) {
            regValue <<= 3;
        }
        else {
            regValue <<= 4;
        }
        
        write(LOW_LIMIT_REG, regValue);
    }
    
    public void setHighLimitTempC(double temp) {
        if (temp > 150.0)  temp = 150.0;
        else if (temp < -55.0)  temp = -55.0;
        
        int regValue = (int)(temp / TEMP_RESOLUTION);
        
        if (isExtendedMode()) {
            regValue <<= 3;
        }
        else {
            regValue <<= 4;
        }
        
        write(HIGH_LIMIT_REG, regValue);
    }

    public void setLowTempF(double temp) {
        temp = (temp - 32.0) * 5.0 /9.0;
        setLowLimitTempC(temp);
    }

    public void setHighTempF(double temp) {
        temp = (temp - 32.0) * 5.0 /9.0;
        setHighLimitTempC(temp);
    }
    
    public double readLowLimitTempC() {
        int regValue = read(LOW_LIMIT_REG);
        boolean bEM = isExtendedMode();
        
        if (bEM) {
            regValue >>= 3;
        }
        else {
            regValue >>= 4;
        }
        return regValue * TEMP_RESOLUTION;
    }
    
    public double readHighLimitTempC() {
        int regValue = read(HIGH_LIMIT_REG);
        boolean bEM = isExtendedMode();
        
        if (bEM) {
            regValue >>= 3;
        }
        else {
            regValue >>= 4;
        }
        return regValue * TEMP_RESOLUTION;
    }
    
    public double readLowLimitTempF() {
        return (readLowLimitTempC() * 9.0 / 5.0) + 32.0;
    }
    
    public double readHighLimitTempF() {
        return (readHighLimitTempC() * 9.0 / 5.0) + 32.0;
    }
    
    public boolean isExtendedMode() {
        return ((read(CONFIG_REG) & EM_MASK) != 0) ? true : false;
    }
    
    public boolean isAlert() {
        int config = read(CONFIG_REG);
        boolean alert = false;
        
        if ((config & POL_MASK) != 0) {
            alert = (read(CONFIG_REG) & AL_MASK) != 0 ? true : false;
        }
        else {
            alert = (read(CONFIG_REG) & AL_MASK) == 0 ? true : false;
        }
        return alert;
    }
    
    private void setConfigBit(int mask, boolean mode) {
        int regValue = read(CONFIG_REG);
        if (mode) {
            regValue |= mask;
        }
        else {
            regValue &= (~mask);
        }
        write(CONFIG_REG, regValue);
    }
    
    public void sleep() {
        setConfigBit(SD_MASK, true);
    }
    
    public void wakeup() {
        setConfigBit(SD_MASK, false);
    }
    
    public void setAlertMode(boolean mode) {
        setConfigBit(TM_MASK, mode);
    }

    public void setAlertPolarity(boolean polarity) {
        setConfigBit(POL_MASK, polarity);
    }
    
    public void setFaultQueue(int faults) {
        int regValue = read(CONFIG_REG);

        faults &= 0x03;        
        regValue &= (~FQ_MASK);
        regValue |= (faults << 11);
        
        write(CONFIG_REG, regValue);        
    }

    public void setConverterResolution(int resolution) {
        int regValue = read(CONFIG_REG);

        resolution &= 0x03;        
        regValue &= (~RS_MASK);
        regValue |= (resolution << 13);
        
        write(CONFIG_REG, regValue);        
    }

    public void setOneShot(boolean mode) {
        setConfigBit(OS_MASK, mode);
    }
        
    public void setExtendedMode(boolean mode) {
        setConfigBit(EM_MASK, mode);
        I2CUtils.I2Cdelay(500);
    }
    
    public void setConversionRate(int rate) {
        int regValue = read(CONFIG_REG);

        rate &= 0x03;        
        regValue &= (~CR_MASK);
        regValue |= (rate << 6);
        
        write(CONFIG_REG, regValue);        
    }

}
