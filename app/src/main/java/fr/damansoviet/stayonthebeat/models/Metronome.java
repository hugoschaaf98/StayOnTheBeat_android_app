package fr.damansoviet.stayonthebeat.models;

import android.bluetooth.BluetoothSocket;

public class Metronome {
    /*** This class is aimed to handle the metronome work ***/

    //*** attributes ***//
    private static final String TAG = "MetronomeSingleton";
    private int mMinBpm = 50;
    private int mMaxBpm = 350;
    private int mBpm = mMinBpm;
    private boolean mState = false; // false => stopped - true => running
    private BluetoothSocket mBtSocket;

    public Metronome() { }

    public Metronome(BluetoothSocket btSocket) {
        mBtSocket = btSocket;
    }

    //*** Getters and setters ***//

    public int getBpm() {
        return mBpm;
    }

    public void setBpm(int bpm) {
        this.mBpm = (bpm> mMaxBpm ? mMaxBpm :(bpm< mMinBpm ? mMinBpm :bpm));
    }

    public void setBpmPercent(float percentage)
    {
        float tmpBpm = percentage*(float)(mMaxBpm - mMinBpm)/100f+(float) mMinBpm;
        setBpm(Math.round(tmpBpm));
    }

    public boolean getState() {
        return mState;
    }

    public void setState(boolean state) {
        this.mState = state;
    }

    public BluetoothSocket getBtSocket() {
        return mBtSocket;
    }

    public void setBtSocket(BluetoothSocket mBtSocket) {
        this.mBtSocket = mBtSocket;
    }
}
