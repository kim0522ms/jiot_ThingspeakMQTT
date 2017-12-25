/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.i2c_dev;

import java.io.IOException;
import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
import jdk.dio.i2cbus.I2CDevice;

/**
 * Functions to read and write to I2C Raspberry Pi bus
 *
 */
public class I2CUtils {

    /**
     *
     * @param mili
     */
    public static void I2Cdelay(int mili) {
        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
        }
    }

    /**
     *
     * @param mili
     * @param nano
     */
    public static void I2CdelayNano(int mili, int nano) {
        try {
            Thread.sleep(mili, nano);
        } catch (InterruptedException ex) {
        }
    }

    /**
     *
     * @param b
     * @return byte values from -127..128 convert 128..255
     */
    public static int asInt(byte b) {
        int i = b;
        if (i < 0) {
            i += 256;
        }
        return i;
    }

    /**
     *
     * @param device Device connected to I2C bus
     * @param cmd Command send to device
     * @return read an int from a device connected to I2C bus
     */
    public static int read(I2CDevice device, int cmd) {
        ByteBuffer rxBuf = ByteBuffer.allocateDirect(1);
        try {
            device.read(cmd, 1, rxBuf);
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING,ex.getMessage());
            //ex.printStackTrace();
            System.out.println("WARNING: " + ex.getMessage());
        }
        return asInt(rxBuf.get(0));
    }

    public static int read(I2CDevice device) {
        byte value = -1;
        try {
            value = (byte)device.read();
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING,ex.getMessage());
            //ex.printStackTrace();
            System.out.println("WARNING: " + ex.getMessage());
        }
        return asInt(value);
    }
    
    /**
     *
     * @param device Device connected to I2C bus
     * @param cmd Command send to Arduino Due
     * @return read a float from Arduino Due connected to I2C bus. All bytes
     * received must be swamp order
     */
    public static float readFloatArduino(I2CDevice device, int cmd) {
        byte[] b = new byte[4];
        ByteBuffer rxBuf = ByteBuffer.allocateDirect(4);
        try {
            device.read(cmd, 4, rxBuf);
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING, ex.getMessage());
            System.out.println("WARNING: " + ex.getMessage());
        }
        rxBuf.clear();
        for (int i = 0; i < 4; i++) {
            b[i] = rxBuf.get(3 - i);
        }
        return ByteBuffer.wrap(b).getFloat();
    }

    /**
     *
     * @param device Device connected to I2C bus
     * @param cmd Command send to Arduino Due
     * @return read a short from Arduino Due connected to I2C bus. All bytes
     * received must be swamp order
     */
    public static short readShortArduino(I2CDevice device, int cmd) {
        byte[] b = new byte[2];
        ByteBuffer rxBuf = ByteBuffer.allocateDirect(2);
        try {
            device.read(cmd, 2, rxBuf);
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING, ex.getMessage());
            System.out.println("WARNING: " + ex.getMessage());            
        }
        rxBuf.clear();
        for (int i = 0; i < 2; i++) {
            b[i] = rxBuf.get(1 - i);
        }
        return ByteBuffer.wrap(b).getShort();
    }

    /**
     *
     * @param device Device connected to I2C bus
     * @param cmd Command send to a device
     * @return read a short from a device connected to I2C bus
     */
    public static short readShort(I2CDevice device, int cmd) {
        //byte[] b = new byte[2];
        ByteBuffer rxBuf = ByteBuffer.allocateDirect(2);
        try {
            device.read(cmd, 1, rxBuf);
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING, ex.getMessage());
            System.out.println("WARNING: " + ex.getMessage());            
        }
        rxBuf.clear();
        return rxBuf.getShort();
    }

    /**
     *
     * @param device Device connected to I2C bus
     * @param cmd Command send to a device
     * @param value Command value(byte) to write to device
     */
    public static void write(I2CDevice device, byte cmd, byte value) {
        ByteBuffer txBuf = ByteBuffer.allocateDirect(2);
        txBuf.put(0, cmd);
        txBuf.put(1, value);
        try {
            device.write(txBuf);
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING, ex.getMessage());
            System.out.println("WARNING: " + ex.getMessage());            
        }
    }

    /**
     *
     * @param device Device connected to I2C bus
     * @param cmd Command send to a device
     * @param value Command value(short) to write to device
     */
    public static void writeShort(I2CDevice device, byte cmd, int value) {
/*  for testing...      
        ByteBuffer txBuf = ByteBuffer.allocateDirect(2);
        txBuf.putShort((short)value);
        txBuf.rewind();
//        System.out.println(String.format("0x%04x", txBuf.getShort()));
        try {
            device.write(cmd, 1, txBuf);
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING, ex.getMessage());
            System.out.println("WARNING: " + ex.getMessage());            
        }
*/
        ByteBuffer txBuf = ByteBuffer.allocateDirect(3);
        txBuf.put(0, cmd);
        txBuf.put(1, (byte)((value >> 8) & 0x00FF));
        txBuf.put(2, (byte)(value & 0x00FF));
        try {
            device.write(txBuf);
        } catch (IOException ex) {
            //Logger.getGlobal().log(Level.WARNING, ex.getMessage());
            System.out.println("WARNING: " + ex.getMessage());            
        }
//        System.out.println(String.format("0x%02x", txBuf.get(0)));
//        System.out.println(String.format("0x%02x", txBuf.get(1)));
//        System.out.println(String.format("0x%02x", txBuf.get(2)));
    }

    /**
     * Write multiple bits in an 8-bit device register.
     *
     * @param devAddr I2C slave device address
     * @param regAddr Register regAddr to write to
     * @param bitStart First bit position to write (0-7)
     * @param length Number of bits to write (not more than 8)
     * @param data Right-aligned value to write
     */
    public static void writeBits(I2CDevice devAddr, byte regAddr, int bitStart, int length, int data) {
        //      010 value to write
        // 76543210 bit numbers
        //    xxx   args: bitStart=4, length=3
        // 00011100 mask byte
        // 10101111 original value (sample)
        // 10100011 original & ~mask
        // 10101011 masked | value
        byte b = (byte) read(devAddr, regAddr);
        if (b != 0) {
            int mask = ((1 << length) - 1) << (bitStart - length + 1);
            data <<= (bitStart - length + 1); // shift data into correct position
            data &= mask; // zero all non-important bits in data
            b &= ~(mask); // zero all important bits in existing byte
            b |= data; // combine data with existing byte
            write(devAddr, regAddr, b);
        }
    }

    /**
     * write a single bit in an 8-bit device register.
     *
     * @param devAddr I2C slave device address
     * @param regAddr Register regAddr to write to
     * @param bitNum Bit position to write (0-7)
     * @param data
     */
    public static void writeBit(I2CDevice devAddr, byte regAddr, int bitNum, int data) {
        int b= (byte) read(devAddr, regAddr);
        b = (data != 0) ? (b | (1 << bitNum)) : (b & ~(1 << bitNum));
        write(devAddr, regAddr, (byte) b);
    }

/*    
    public static byte[] int_to_bb_le(int myInteger){
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(myInteger).array();
    }

    public static int bb_to_int_le(byte [] byteBarray){
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static byte[] int_to_bb_be(int myInteger){
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
    }

    public static int bb_to_int_be(byte [] byteBarray){
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
    }
*/
}