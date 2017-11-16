package kltn.musicapplication.models;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by UITCV on 15/09/2017.
 */
public class Bluetooth{
    public static final int REQUEST_ENABLE_BLT = 0;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_SNACKBAR = 4;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_ERROR = 1;
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final String SNACKBAR = "snackbar";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice device, devicePair;
    private BufferedReader input;
    private OutputStream out;

    private int State;

    private Handler myHandler;

    private boolean connected=false;
    private CommunicationCallback communicationCallback=null;

    private DiscoveryCallback discoveryCallback=null;

    private Activity activity;

    public Bluetooth(Activity activity){
        this.activity=activity;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public Bluetooth(Activity activity, Handler handler){
        this.State = STATE_NONE;
        this.activity = activity;
        this.myHandler = handler;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public DiscoveryCallback getDiscoveryCallback() {
        return discoveryCallback;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }
    public void disableBluetooth(){
        if(bluetoothAdapter!=null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }
    public void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLT);
    }
    public void connectToAddress(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        new ConnectThread(device).start();
    }

    public void connectToName(String name) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToAddress(blueDevice.getAddress());
                return;
            }
        }
    }

    public synchronized void connectToDevice(BluetoothDevice device){
        ConnectThread connectThread = new ConnectThread(device);
        setState(STATE_CONNECTING);

        connectThread.start();
    }
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            if(communicationCallback!=null)
                communicationCallback.onError(e.getMessage());
        }
    }
    public boolean isConnected(){
        return connected;
    }

    public void send(String msg){
        try {
            out.write(msg.getBytes());
        } catch (IOException e) {
            connected=false;
            if(communicationCallback!=null)
                communicationCallback.onDisconnect(device, e.getMessage());
        }
    }

    private class ReceiveThread extends Thread implements Runnable{
        public void run(){
            String msg;
            try {
                while ((msg = input.readLine()) != null) {
                    if (communicationCallback != null)
                        communicationCallback.onMessage(msg);
                }
            } catch (IOException e) {
                connected=false;
                if (communicationCallback != null)
                    communicationCallback.onDisconnect(device, e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        public ConnectThread(BluetoothDevice device) {
            Bluetooth.this.device = device;
            try {
                Bluetooth.this.socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                if(communicationCallback!=null)
                    communicationCallback.onError(e.getMessage());
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
                out = socket.getOutputStream();
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected=true;
                setState(STATE_CONNECTED);
                new ReceiveThread().start();

                if(communicationCallback!=null)
                    communicationCallback.onConnect(device);
            } catch (IOException e) {
                if(communicationCallback!=null)
                    communicationCallback.onConnectError(device, e.getMessage());

                try {
                    socket.close();
                } catch (IOException closeException) {
                    if (communicationCallback != null)
                        communicationCallback.onError(closeException.getMessage());
                }
                connectionFailed();
            }
        }
    }

    public List<BluetoothDevice> getPairedDevices(){
        List<BluetoothDevice> devices = new ArrayList<>();
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            devices.add(blueDevice);
        }
        return devices;
    }

    public BluetoothSocket getSocket(){
        return socket;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public Boolean scanDevices(){
        return bluetoothAdapter.startDiscovery();

    }

    public void pair(BluetoothDevice device){
        activity.registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        devicePair=device;
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            if(discoveryCallback!=null)
                discoveryCallback.onError(e.getMessage());
        }
    }

    public void unpair(BluetoothDevice device) {
        devicePair=device;
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            if(discoveryCallback!=null)
                discoveryCallback.onError(e.getMessage());
        }
    }

//    private BroadcastReceiver mReceiverScan = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            switch (action) {
//                case BluetoothAdapter.ACTION_STATE_CHANGED:
//                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
//                    if (state == BluetoothAdapter.STATE_OFF) {
//                        if (discoveryCallback != null)
//                            discoveryCallback.onError("Bluetooth turned off");
//                    }
//                    break;
//                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
//                    context.unregisterReceiver(mReceiverScan);
//                    if (discoveryCallback != null)
//                        discoveryCallback.onFinish();
//                    break;
//                case BluetoothDevice.ACTION_FOUND:
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//                    if (discoveryCallback != null)
//                        discoveryCallback.onDevice(device);
//                    break;
//            }
//        }
//    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    context.unregisterReceiver(mPairReceiver);
                    if(discoveryCallback!=null)
                        discoveryCallback.onPair(devicePair);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    context.unregisterReceiver(mPairReceiver);
                    if(discoveryCallback!=null)
                        discoveryCallback.onUnpair(devicePair);
                }
            }
        }
    };

    public interface CommunicationCallback{
        void onConnect(BluetoothDevice device);
        void onDisconnect(BluetoothDevice device, String message);
        void onMessage(String message);
        void onError(String message);
        void onConnectError(BluetoothDevice device, String message);
    }

    public void setCommunicationCallback(CommunicationCallback communicationCallback) {
        this.communicationCallback = communicationCallback;
    }

    public void removeCommunicationCallback(){
        this.communicationCallback = null;
    }

    public interface DiscoveryCallback{
        void onFinish();
        void onDevice(BluetoothDevice device);
        void onPair(BluetoothDevice device);
        void onUnpair(BluetoothDevice device);
        void onError(String message);
    }

    public void setDiscoveryCallback(DiscoveryCallback discoveryCallback){
        this.discoveryCallback=discoveryCallback;
    }

    public void removeDiscoveryCallback(){
        this.discoveryCallback=null;
    }
    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }
    private void connectionFailed() {
        // Send a failure item_message back to the Activity
        Message msg = myHandler.obtainMessage(MESSAGE_SNACKBAR);
        Bundle bundle = new Bundle();
        bundle.putString(SNACKBAR, "Unable to connect");
        msg.setData(bundle);
        myHandler.sendMessage(msg);
        setState(STATE_ERROR);
    }
    public int getState() {
        return State;
    }

    public synchronized void setState(int state) {
        this.State = state;
        myHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }


}


