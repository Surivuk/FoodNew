package com.example.aleksandarx.foodfinder.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleksandarx on 8/21/16.
 */
public class BluetoothClient extends Thread implements IBParent {

    private final BluetoothSocket socket;
    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnection con;
    private final Handler handler;
    private Context context;
    private Command command;

    private class Command{
        public char flag;
        public int id;

        public Command(char flag, int id) {
            this.flag = flag;
            this.id = id;
        }
    }

    public BluetoothClient(BluetoothDevice device,Handler handler, Context context) {
        BluetoothSocket tmp = null;
        this.context = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            tmp = device.createRfcommSocketToServiceRecord(BluetoothServer.SPP_UUID);
        } catch (IOException e) {
        }
        socket = tmp;
        this.handler = handler;
    }

    public void setCommand(char flag, int id){
        command = new Command(flag, id);
    }


    @Override
    public void run() {
        mBluetoothAdapter.cancelDiscovery();
        try {
            boolean isFirst = true;
            while(mBluetoothAdapter.isDiscovering()){
                if(isFirst) {
                    isFirst = false;
                    System.out.println("Bluetooth waiting cancelDiscovery()!");
                }
            }
            System.out.println("Bluetooth Discovering is cancel!");

            if(!socket.isConnected())
                socket.connect();

            isFirst = true;
            while(!socket.isConnected()){
                if(isFirst) {
                    isFirst = false;
                    System.out.println("Bluetooth waiting isConnected()!");
                }
            }
            System.out.println("Bluetooth is connected()!");

            if(con == null)
                con = new BluetoothConnection(socket, this);
            con.start();


            send(command.flag, command.id);

        } catch (IOException connectException) {
            connectException.printStackTrace();
            sendError("EXCEPTION (run() function) CLIENT");
            cancel();
        }
    }


    public void send(char c, int id){
        if(socket.isConnected()){
            if(con == null)
                con = new BluetoothConnection(socket, this);
            ByteBuffer buff = ByteBuffer.allocate(9);
            buff.put("ST".getBytes());
            buff.put((byte) c);
            buff.put(ByteBuffer.allocate(4).putInt(0, id));
            buff.put("EN".getBytes());
            con.write(buff.array());
            System.out.println("Bluetooth message send successfully!");

        }
        else {
            while(!socket.isConnected()){

            }
            send(c, id);
            System.out.println("Bluetooth message send error!");
        }
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            socket.close();
            if (con != null) {
                con.interrupt();
            }
            this.interrupt();
        } catch (IOException e) { }
    }

    private void sendError(String errorText){
        Intent intent = new Intent("bluetooth-error");
        intent.putExtra("message", errorText);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        System.out.println("EMMIT");
    }


    @Override
    public Handler getHandler(){
        return handler;
    }
}
