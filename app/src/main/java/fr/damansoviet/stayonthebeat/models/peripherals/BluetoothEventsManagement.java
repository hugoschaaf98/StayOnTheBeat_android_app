package fr.damansoviet.stayonthebeat.models.peripherals;

import android.os.Handler;

public interface BluetoothEventsManagement {
    public void registerBluetoothEventsHandler(Handler handler);
    public void unregisterBluetoothEventsHandler();
}
