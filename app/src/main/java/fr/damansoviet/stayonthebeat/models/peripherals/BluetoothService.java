package fr.damansoviet.stayonthebeat.models.peripherals;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService implements BluetoothEventsManagement {

    private static final String TAG = "Bluetooth Service";

    @Nullable
    private Handler mHandler = null; // handler that gets info from Bluetooth service
    @Nullable
    private BluetoothAdapter mBluetoothAdapter = null;
    @Nullable
    private ConnectThread mConnectThread = null;
    @Nullable
    private ConnectedThread mConnectedThread = null;
    // the state of the bluetooth connection
    private int mState = Constants.STATE_NONE;

    public BluetoothService(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
        mState = Constants.STATE_NONE;
    }

    public void registerBluetoothEventsHandler(Handler handler) {
        mHandler = handler;
    }

    public void unregisterBluetoothEventsHandler() {
        mHandler = null;
    }

    /**
     * Manage the connect thread
     * startConnectThread --> creates and starts a connect thread and calls automatically startConnectedThread() if connection succeeds
     * stopConnectThread --> stops and destroys the connect thread
     */
    public synchronized void startConnectThread(BluetoothDevice bluetoothDevice, UUID uuid) {
        // check if a connect thread isn't already running or created
        if(mConnectThread == null) {
            Log.d(TAG, "create and start ConnectThread");
            mConnectThread = new ConnectThread(bluetoothDevice, uuid);
            mConnectThread.start();
        }
        else {
            Log.w(TAG, "ConnectThread already exists");
        }
    }

    public synchronized void stopConnectThread() {
        Log.d(TAG, "stopped connect thread");
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    /**
     * Manage the connected thread
     * startConnectedThread --> creates and starts the connected thread
     * stopConnectedThread --> stops and destroys the connected thread
     */
    public synchronized void startConnectedThread(BluetoothSocket socket) {
        if(mConnectedThread == null) {
            Log.d(TAG, "create and start ConnectedThread");
            mConnectedThread = new ConnectedThread(socket);
            mConnectedThread.start();
        }
        else {
            Log.w(TAG, "ConnectedThread already exists");
        }
    }

    public synchronized void stopConnectedThread() {
        Log.d(TAG, "stopped connected thread");
        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        if(mConnectedThread == null)
        {
            Log.w(TAG, "bluetooth connection not initialized !");
            return;
        }
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != Constants.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Connect as a client (we want this app to connect only as a client).
     * This threads kills itself automatically once a connection has been established.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();
            mState = Constants.STATE_CONNECTING;

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            startConnectedThread(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /**
     * Manage the connection. Provides a write function to write in the bluetooth socket and listen permanently
     * on incoming data to read
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            }
            catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = Constants.STATE_CONNECTED;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            Constants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                }
                catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                // Share the sent message with the UI activity.
                if(mHandler != null) {
                    Message writtenMsg = mHandler.obtainMessage(
                            Constants.MESSAGE_WRITE, -1, -1, mmBuffer);
                    writtenMsg.sendToTarget();
                }
                else {
                    Log.w(TAG, "Couldn't send a message. No Handler specified");
                }

            }
            catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                // Share the sent message with the UI activity.
                if(mHandler != null) {
                    // Send a failure message back to the activity.
                    Message writeErrorMsg =
                            mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast",
                            "Couldn't send data to the other device");
                    writeErrorMsg.setData(bundle);
                    mHandler.sendMessage(writeErrorMsg);
                }
                else {
                    Log.w(TAG, "Couldn't send a message. No Handler specified");
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
                mState = Constants.STATE_NONE;
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
