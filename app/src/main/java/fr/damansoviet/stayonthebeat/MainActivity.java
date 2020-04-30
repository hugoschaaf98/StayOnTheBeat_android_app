package fr.damansoviet.stayonthebeat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout cl_main;
    private LinearLayout ll_main;
    private ImageButton ib_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInit();
    }

    private void mInit() {
        cl_main = (ConstraintLayout) findViewById(R.id.cl_main);
        ll_main = (LinearLayout) findViewById(R.id.ll_main);
        ib_settings = (ImageButton) findViewById(R.id.ib_settings);

        //setContentView(ll_main);
    }

    private void drawRotaryKnob() {

    }

    //*** Slots ***//
    public void openSettingsActivity(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}
