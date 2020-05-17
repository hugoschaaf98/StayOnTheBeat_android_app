package fr.damansoviet.stayonthebeat.models.peripherals;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Set;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = "Bluetooth Manager";
    private static final int REQUEST_ENABLE_BT = 10;
    private static final UUID mUuid = UUID.fromString("f8ebed5e-ac31-4a6a-9f1b-2c7d60f73e8c");

    // some handy return codes
    public interface  BluetoothReturnCodes {
        public static final int BLUETOOTH_SUCCESS= 0;
        public static final int BLUETOOTH_FAIL = -1;
        public static final int BLUETOOTH_NOT_ENABLED= -2;
    }

    @Nullable
    private BluetoothAdapter mBluetoothAdapter = null;
    @Nullable
    private BluetoothDevice mBluetoothDevice = null;
    @Nullable
    private BluetoothService mBluetoothService = null;
    @Nullable
    private Handler mHandler = null;

    public BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            mBluetoothService = new BluetoothService(mBluetoothAdapter);
        }
        else {
            Log.e(TAG, "Your device doesn't support Bluetooth");
        }
    }

    @Nullable
    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public UUID getUUID() {
        return mUuid;
    }

    @Nullable
    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
    }

    public int findDevice() {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
        return BluetoothReturnCodes.BLUETOOTH_SUCCESS;
    }

    /**
     * Register a handler to receive back bluetooth events and data
     * @param handler
     */
    public void registerBluetoothEventsHandler(Handler handler) {
        mHandler = handler;
    }

    public void unregisterBluetoothEventsHandler() {
        mHandler = null;
    }

    public void connectAndStartClient() {
        mBluetoothService.startConnectThread(mBluetoothDevice, mUuid);
    }

    public void shutdownClient() {
        mBluetoothService.stopConnectThread();
        mBluetoothService.stopConnectedThread();
    }

}
