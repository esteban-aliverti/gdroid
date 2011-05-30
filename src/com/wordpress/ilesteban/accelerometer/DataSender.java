/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wordpress.ilesteban.accelerometer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author esteban
 */
public class DataSender implements Runnable{

    private final String id;
    private final InetAddress ip;
    private final int port;
    private final CoordinateDataSource dataSource;
    private boolean stop;
    private int sleepTime;
    private ExceptionListener exceptionListener;

    public DataSender(String id, int sleepTime, String ip, int port, CoordinateDataSource dataSource) throws UnknownHostException {
        this.id = id;
        this.sleepTime = sleepTime;
        this.ip = InetAddress.getByName(ip);
        this.port = port;
        this.dataSource = dataSource;
    }
    
    public void run() {
        
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(DataSender.class.getName()).log(Level.SEVERE, null, ex);
            notifyException(ex);
            return;
        }
        
        while (!stop){
            float x = this.dataSource.getX();
            float y = this.dataSource.getY();
            float z = this.dataSource.getZ();
            
            String data = id+","+x+","+y+","+z;
            
            DatagramPacket datagramPacket = new DatagramPacket(data.getBytes(), data.getBytes().length, ip, port);
            try {
                socket.send(datagramPacket);
            } catch (IOException ex) {
                Logger.getLogger(DataSender.class.getName()).log(Level.SEVERE, null, ex);
                notifyException(ex);
            }
            try {
                Thread.sleep(this.sleepTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(DataSender.class.getName()).log(Level.SEVERE, null, ex);
                notifyException(ex);
            }
        }
        
        socket.close();
        
    }
    
    public void stop(){
        this.stop = true;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public ExceptionListener getExceptionListener() {
        return exceptionListener;
    }

    public void setExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }
    
    public void notifyException(Exception ex){
        if (this.exceptionListener != null){
            this.exceptionListener.onException(ex);
        }
    }
}
