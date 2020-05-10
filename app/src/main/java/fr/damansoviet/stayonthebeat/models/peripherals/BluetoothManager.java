package fr.damansoviet.stayonthebeat.models.peripherals;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

public class BluetoothManager {

    private static final String TAG = "Bluetooth Manager";
    private static final int REQUEST_ENABLE_BT = 10;
    private BluetoothAdapter mBluetoothAdapter;

    // some handy return codes
    public interface  BluetoothReturnCodes {
        public static final int BLUETOOTH_SUCCESS= 0;
        public static final int BLUETOOTH_FAIL = -1;
        public static final int BLUETOOTH_NOT_ENABLED= -2;
    }

    public BluetoothManager() {

    }

    // sets up the bluetooth. Must be called first
    public int setup() {
        // check first if phone has bluetooth and if user needs to enable it
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) return BluetoothReturnCodes.BLUETOOTH_FAIL;
        if(!bluetoothAdapter.isEnabled()) return BluetoothReturnCodes.BLUETOOTH_NOT_ENABLED;
        return BluetoothReturnCodes.BLUETOOTH_SUCCESS;
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

}
