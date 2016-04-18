package com.hn.rgbstreamer;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import com.hn.rgbstreamer.RGBStreamer.*;
import java.math.*;

// RGBStreamer - Global variables to share data across activities
// Version: V01_002
// Last Mofidied: 18.04.2016
// Author: HN (Bluetooth code: http://developer.android.com/guide/topics/connectivity/bluetooth.html)



public class Globals extends Application {

    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mBluetoothDevice;
    private boolean connected = false;
    private boolean colorSet = false;
    private int noPanelRows;
    private int noPanelCol;
    private int noPixelRows;
    private int noPixelCol;
    private int colorDepth;
    private int color;
    private int rgbStreamingProtocolSize;
    private boolean ackEnabledStreaming;
    private int rgbDrawingProtocolSize;
    private boolean ackEnabledDrawing;
    private boolean useFPGA;

    public ConnectedThread getConnectedThread(){
        return mConnectedThread;
    }
    public void setConnectedThread(ConnectedThread mmConnectedThread){
        mConnectedThread = mmConnectedThread;
    }

    public ConnectThread getConnectThread(){
        return mConnectThread;
    }
    public void setConnectThread(ConnectThread mmConnectThread){
        mConnectThread = mmConnectThread;
    }

    public BluetoothSocket getBluetoothSocket(){
        return mBluetoothSocket;
    }
    public void setBluetoothSocket(BluetoothSocket mmBluetoothSocket){
        mBluetoothSocket = mmBluetoothSocket;
    }

    public BluetoothDevice getBluetoothDevice(){
        return mBluetoothDevice;
    }
    public void setBluetoothDevice(BluetoothDevice mmBluetoothDevice){
        mBluetoothDevice = mmBluetoothDevice;
    }

    public boolean getConnectionStatus(){
        return connected;
    }
    public void setConnectionStatus(boolean connectionStatus){
        connected = connectionStatus;
    }

    public int getNoPanelRows(){
        return noPanelRows;
    }
    public void setNoPanelRows(int nPanelRows){
        noPanelRows = nPanelRows;
    }

    public int getNoPanelCol(){
        return noPanelCol;
    }
    public void setNoPanelCol(int nPanelCol){
        noPanelCol = nPanelCol;
    }

    public int getNoPixelRows(){
        return noPixelRows;
    }
    public void setNoPixelRows(int nPixelRows){
        noPixelRows = nPixelRows;
    }

    public int getNoPixelCol(){
        return noPixelCol;
    }
    public void setNoPixelCol(int nPixelCol){
        noPixelCol = nPixelCol;
    }

    public int getColorDepth(){
        return colorDepth;
    }
    public void setColorDepth(int colDepth){
        colorDepth = colDepth;
    }

    public int getRGBStreamingProtocolSize(){
        return rgbStreamingProtocolSize;
    }
    public void setRGBStreamingProtocolSize(int protocolsize){
        rgbStreamingProtocolSize = protocolsize;
    }

    public void setLEDPanelDriver(boolean FPGA)
    {
        useFPGA = FPGA;
    }

    public boolean getLEDPanelDriver()
    {
        return useFPGA;
    }

    public boolean getAckEnabledStreaming(){
        return ackEnabledStreaming;
    }
    public void setAckEnabledStreaming(boolean enable){
        ackEnabledStreaming = enable;
    }

    public int getRGBDrawingProtocolSize(){
        return rgbDrawingProtocolSize;
    }
    public void setRGBDrawingProtocolSize(int protocolsize){
        rgbDrawingProtocolSize = protocolsize;
    }

    public boolean getAckEnabledDrawing(){
        return ackEnabledDrawing;
    }
    public void setAckEnabledDrawing(boolean enable){
        ackEnabledDrawing = enable;
    }

    public int getColor(){
        return color;
    }
    public void setColor(int colorin){
        color = colorin;
        colorSet = true;
    }

    public boolean isColorSet(){
        return colorSet;
    }

    public void sendRGBStreamingPacket(int x, int y, int R, int G, int B)
    {
        byte[] outputByteArray = new byte[rgbStreamingProtocolSize];
        outputByteArray[0] = (byte) (x & 0xFF);
        outputByteArray[1] = (byte) (y & 0xFF);

        if(useFPGA) {   // Consider desired color depth if FPGA is used as panel driver
            outputByteArray[2] = (byte) ((R*((int)Math.pow(2,colorDepth)-1))/255 & 0xFF);
            outputByteArray[3] = (byte) ((G*((int)Math.pow(2,colorDepth)-1))/255 & 0xFF);
            outputByteArray[4] = (byte) ((B*((int)Math.pow(2,colorDepth)-1))/255 & 0xFF);
        }
        else {
            outputByteArray[2] = (byte) (R & 0xFF);
            outputByteArray[3] = (byte) (G & 0xFF);
            outputByteArray[4] = (byte) (B & 0xFF);
        }

        if(rgbStreamingProtocolSize == 6)
            outputByteArray[5] = (byte) (outputByteArray[0] ^ outputByteArray[1] ^ outputByteArray[2] ^ outputByteArray[3] ^ outputByteArray[4]);

        mConnectedThread.write(outputByteArray);
    }

    public void sendRGBDrawingPacket(int x, int y, int R, int G, int B)
    {
        byte[] outputByteArray = new byte[rgbDrawingProtocolSize];
        outputByteArray[0] = (byte) (x & 0xFF);
        outputByteArray[1] = (byte) (y & 0xFF);

        if(useFPGA) {   // Consider desired color depth if FPGA is used as panel driver
            outputByteArray[2] = (byte) ((R*((int)Math.pow(2,colorDepth)-1))/255 & 0xFF);
            outputByteArray[3] = (byte) ((G*((int)Math.pow(2,colorDepth)-1))/255 & 0xFF);
            outputByteArray[4] = (byte) ((B*((int)Math.pow(2,colorDepth)-1))/255 & 0xFF);
        }
        else {
            outputByteArray[2] = (byte) (R & 0xFF);
            outputByteArray[3] = (byte) (G & 0xFF);
            outputByteArray[4] = (byte) (B & 0xFF);
        }

        if(rgbDrawingProtocolSize == 6)
            outputByteArray[5] = (byte) (outputByteArray[0] ^ outputByteArray[1] ^ outputByteArray[2] ^ outputByteArray[3] ^ outputByteArray[4]);

        mConnectedThread.write(outputByteArray);
    }
}