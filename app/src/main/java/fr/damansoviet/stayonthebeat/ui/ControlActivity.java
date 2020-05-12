package fr.damansoviet.stayonthebeat.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fr.damansoviet.stayonthebeat.AppContainer;
import fr.damansoviet.stayonthebeat.R;
import fr.damansoviet.stayonthebeat.StayOnTheBeatApplication;
import fr.damansoviet.stayonthebeat.Utils;
import fr.damansoviet.stayonthebeat.ui.RoundKnobButton.RoundKnobButtonListener;
import fr.damansoviet.stayonthebeat.viewmodels.ControlViewModel;


public class ControlActivity extends AppCompatActivity {

    private static final String TAG = "ControlActivity";
    // graphical components
    private RelativeLayout mRlControlZone;
    private ImageButton mIbSettings;
    private TextView mTvBpm;

    // ViewModels
    @Nullable
    private ControlViewModel mControlViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        // data
        AppContainer appContainer = ((StayOnTheBeatApplication)getApplication()).appContainer;
        mControlViewModel = new ControlViewModel(appContainer.metronome);

        NavBar();
        init();
    }

    private void init() {
        // graphical components
        mRlControlZone = (RelativeLayout) findViewById(R.id.rl_control_zone);
        //mIbSettings = (ImageButton) findViewById(R.id.ib_settings);
        setupAndDrawBpmControl();
    }

    private void setupAndDrawBpmControl() {
        // instantiate it
        RoundKnobButton rv = new RoundKnobButton(this, R.drawable.stator, R.drawable.rotoron, R.drawable.rotoroff, 600, 600);
        rv.setRotorPercentage(50f);
        rv.setId(Utils.generateViewId());

        // and draw it on screen
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRlControlZone.addView(rv, lp);

        // place the value monitor above
        mTvBpm = new TextView(this);
        mTvBpm.setTextColor(Color.BLACK);
        mTvBpm.setGravity(Gravity.CENTER_HORIZONTAL);
        mTvBpm.setTextSize(18f);
        mTvBpm.setHint("-- BPM");
        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ABOVE,rv.getId());
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRlControlZone.addView(mTvBpm, lp);

        // set initial position and register listener to the rotary knob
        rv.setListener(new RoundKnobButtonListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            public void onStateChange(boolean newState) {
                Toast.makeText(ControlActivity.this,  "Metronome "+(newState?"started":"paused"),  Toast.LENGTH_SHORT).show();
                mControlViewModel.getMetronome().setState(newState);
                StayOnTheBeatApplication.vibrator.vibrate(100);
            }
            public void onRotate(float percentage) {
                mControlViewModel.getMetronome().setBpmPercent(percentage);
                mTvBpm.setText(String.format("%d BPM", mControlViewModel.getMetronome().getBpm()));
            }
        });
    }

    /**
     *
     * @param v
     */
    /**
    public void openSmsActivity(View v) {
        Intent iOpenSmsPart = new Intent(this, SmsActivity.class);
        startActivity(iOpenSmsPart);
    }
     **/

  
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            default:
                Log.v(TAG, "Unknown requestCode.");
        }
    }

    private void NavBar()
    {

        //initialisation de notre bande comprenant differents elements
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById ( R.id.bottom_navigation );
        // appel de notre fonction dinitialisation

        //cette fonction s'occupera de la fonctionnalite de notre bar de navigation

        // premiere etape sera de choisir le premier element de la navbar de sélectionné
        bottomNavigationView.setSelectedItemId ( R.id.maison);

        // nous allons a present ecouter ce que va faire l'utilisateur
        bottomNavigationView.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId ()) {
                    case R.id.maison:
                        return true;

                    case R.id.settings:
                        startActivity ( new Intent ( getApplicationContext (),Settings.class ) );
                        overridePendingTransition ( 0,0 );
                        return true;

                    case R.id.sms:
                        startActivity ( new Intent ( getApplicationContext (),SmsActivity.class ) );
                        overridePendingTransition ( 0,0 );
                        return true;
                }
                return false;
            }
        } );
    }
}
