package com.example.aleksandarx.foodfinder.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by EuroPATC on 9/7/2016.
 */
public class BluetoothConnection extends Thread {


    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler handler;
    private int packageSize = 9;
    public static String START_BLOCK = "ST";
    public static String END_BLOCK = "EN";
    private IBParent parent;

    public BluetoothConnection(BluetoothSocket socket, IBParent parent) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.handler = parent.getHandler();
        this.parent = parent;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
        int counter = 0;
        int offset = 0;

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                bytes = mmInStream.read(buffer, offset, 1);
                Log.d("BR", "" + bytes);

                // Reader read more then 0 bytes
                if(bytes > 0) {
                    counter++;
                    offset++;
                    offset %= 1024;
                }

                if(counter == packageSize){
                    byte[] start = Arrays.copyOfRange(buffer, 0, 2);
                    byte[] data = Arrays.copyOfRange(buffer, 2, packageSize-2);
                    byte[] end = Arrays.copyOfRange(buffer, packageSize-2, packageSize);

                    if(new String(start, "UTF8").equals(START_BLOCK) && new String(end, "UTF8").equals(END_BLOCK)){
                        Message message = handler.obtainMessage(16, data);
                        message.sendToTarget();
                        offset = 0;
                        counter = 0;
                    }
                }

                // End of reading
                if(bytes == -1){
                    offset = 0;
                    counter = 0;
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
            this.interrupt();
        } catch (IOException e) { }
    }
}
