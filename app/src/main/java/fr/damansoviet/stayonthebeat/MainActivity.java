package fr.damansoviet.stayonthebeat;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.damansoviet.stayonthebeat.RoundKnobButton.RoundKnobButtonListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // request code for enabling bluetooth
    private static final int REQUEST_ENABLE_BT = 10;
    // graphical components
    private RelativeLayout rl_main;
    private TextView tvBpm;
    private ImageButton ib_settings;
    // peripherals
    private BluetoothAdapter bluetoothAdapter;
    // the metronome itself
    private MetronomeSingleton metronome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInit();
    }

    private void mInit() {
        // bluetooth config
        setupBluetooth();
        // initialize and configure the metronome
        metronome = MetronomeSingleton.getInstance();
        metronome.configure(bluetoothAdapter);
        // retrieve static graphical components
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);
        ib_settings = (ImageButton) findViewById(R.id.ib_settings);
        // draw graphical components
        drawBpmControl();
    }

    private void setupBluetooth() {

//        Intent iOpenBluetoothSettings = new Intent();
//        iOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
//        startActivity(iOpenBluetoothSettings);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) {
            Toast.makeText(this,  "Your phone doesn't support bluetooth.",  Toast.LENGTH_LONG).show();
        }
        else {

            if(!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void drawBpmControl() {
        // instantiate it
        RoundKnobButton rv = new RoundKnobButton(this, R.drawable.stator, R.drawable.rotoron, R.drawable.rotoroff, 600, 600);
        rv.setRotorPercentage(50f);
        rv.setId(Utils.generateViewId());

        // and draw it on screen
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        rl_main.addView(rv, lp);

        // update the bpm value of the metronome to match the rotary knob position
        metronome.setBpmPercent(50f);

        // place the value monitor above
        tvBpm = new TextView(this);
        tvBpm.setTextColor(Color.BLACK);
        tvBpm.setGravity(Gravity.CENTER_HORIZONTAL);
        tvBpm.setTextSize(18f);
        tvBpm.setText(String.format("%d BPM", metronome.getBpm()));
        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ABOVE,rv.getId());
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rl_main.addView(tvBpm, lp);

        // set initial position and register listener to the rotary knob
        rv.setListener(new RoundKnobButtonListener() {
            public void onStateChange(boolean newState) {
                Toast.makeText(MainActivity.this,  "Metronome "+(newState?"started":"paused"),  Toast.LENGTH_SHORT).show();
                metronome.setState(newState);
            }
            public void onRotate(float percentage) {
                metronome.setBpmPercent(percentage);
                tvBpm.setText(String.format("%d BPM", metronome.getBpm()));
            }
        });
    }

    //*** Slots ***//
    public void openSettingsActivity(View v) {
        Intent iOpenSettings = new Intent(this, SettingsActivity.class);
        startActivity(iOpenSettings);
    }

    public void openSmsActivity(View v) {
        Intent iOpenSmsPart = new Intent(this, SmsSend.class);
        startActivity(iOpenSmsPart);
    }
  
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    Log.i(TAG, "Bluetooth successfully enabled");
                }
                else {
                    Toast.makeText(this, "You must enable bluetooth to use this app !", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Log.v(TAG, "Unknown requestCode.");
        }
    }
}
