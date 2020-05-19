package fr.damansoviet.stayonthebeat;

import fr.damansoviet.stayonthebeat.models.Metronome;
import fr.damansoviet.stayonthebeat.models.peripherals.BluetoothManager;

public class AppContainer {
    public BluetoothManager bluetoothManager = new BluetoothManager();
    public Metronome metronome = new Metronome();
}
