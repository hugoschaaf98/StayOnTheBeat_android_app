package fr.damansoviet.stayonthebeat;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

public class MetronomeSingleton {
    /*** This class is aimed to handle the metronome work ***/

    //*** attributes ***//
    private static final String TAG = "MetronomeSingleton";
    private int minBpm = 50;
    private int maxBpm = 350;
    private int bpm = minBpm;
    private boolean state = false; // false => stopped - true => running
    // android relative
    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    /*** Singleton specific stuffs ***/

    private MetronomeSingleton() {    }

    private static class MetronomeSingletonHolder {
        private static final MetronomeSingleton INSTANCE = new MetronomeSingleton();
    }


    public static MetronomeSingleton getInstance() {
        return MetronomeSingletonHolder.INSTANCE;
    }

    //*** Getters and setters ***//

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = (bpm>maxBpm?maxBpm:(bpm<minBpm?minBpm:bpm));
    }

    public void setBpmPercent(float percentage)
    {
        float tmpBpm = percentage*(float)(maxBpm-minBpm)/100f+(float)minBpm;
        setBpm(Math.round(tmpBpm));
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    //*** other methods ***//
    /**
     * configure peripherals used by the metronome ie bluetooth for now
     * @param bluetoothAdapter the bluetoothAdapter to use
     */
    public void configure(BluetoothAdapter bluetoothAdapter)
    {
        this.bluetoothAdapter = bluetoothAdapter;
    }
}
