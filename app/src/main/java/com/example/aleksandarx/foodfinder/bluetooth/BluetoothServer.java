package com.example.aleksandarx.foodfinder.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by aleksandarx on 8/21/16.
 */
public class BluetoothServer extends Thread implements IBParent{

    public static UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String nameOfConnection = "MyBluetooth";
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnection con;
    private Handler handler;

    public BluetoothServer(Handler handler) {
        BluetoothServerSocket tmp = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(nameOfConnection, SPP_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
        this.handler = handler;
    }

    @Override
    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)

                if(con == null)
                    con = new BluetoothConnection(socket, this);

                con.start();
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            if(con != null) {
                con.interrupt();
                con = null;
            }
            mmServerSocket.close();
        } catch (IOException e) { }
    }

    @Override
    public Handler getHandler() {
        return handler;
    }
}
