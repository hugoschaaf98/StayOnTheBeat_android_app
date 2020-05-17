package fr.damansoviet.stayonthebeat.ui;

import android.Manifest;
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
import androidx.core.app.ActivityCompat;

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
        permission ();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            default:
                Log.v(TAG, "Unknown requestCode.");
        }
    }


    /**
     * Fonction permettant de gérer la partie de la bar de navigation
     */
    private void NavBar()
    {

        //initialisation de notre bande comprenant differents elements
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById ( R.id.bottom_navigation );

        //sélection de l'élément correspondant à notre page
        bottomNavigationView.setSelectedItemId ( R.id.maison);

        // gérer les redirections en fonction de ce que va faire l'utilisateur
        bottomNavigationView.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId ()) {
                    case R.id.maison:
                        return true;

                    case R.id.settings:
                        startActivity ( new Intent ( getApplicationContext (), SettingsActivity.class ) );
                        overridePendingTransition ( R.anim.slide_in_right,R.anim.slide_out_left );
                        return true;

                    case R.id.sms:
                        startActivity ( new Intent ( getApplicationContext (),SmsActivity.class ) );
                        overridePendingTransition (  R.anim.slide_in_left,R.anim.slide_out_right);
                        return true;
                }
                return false;
            }
        } );
    }

    public static String[] add(String[] originalArray, String newItem)
    {
        int currentSize = originalArray.length;
        int newSize = currentSize + 1;
        String[] tempArray = new String[ newSize ];
        for (int i=0; i < currentSize; i++)
        {
            tempArray[i] = originalArray [i];
        }
        tempArray[newSize- 1] = newItem;
        return tempArray;
    }
    /**
     * Cette fonction va permettre de vérifier quel permission a été autorisé si ce n'est pas le cas
     * ajout d'une fenetre pop up demandant l'autorisation de cette denière
     */
    public void permission()
    {

        // nous allons avoir besoin de demander la permission d'acces pour les contacts
        // pour les sms
        // pour avoir acces au bluetooth

        // occupons nous des sms en premier

        // dans un premier temps nous allons initialiser un tableau permettant de receuillir les
        // permissions dont nous avons besoin

        // initialisation de notre tableau



        String[] mypermission = {};

        // occupons nous de verifier les sms
        if(!ActivityCompat.shouldShowRequestPermissionRationale ( ControlActivity.this,
                Manifest.permission.SEND_SMS))
        {
            mypermission = add(mypermission,Manifest.permission.SEND_SMS);
        }

        // bluetooth
        if(!ActivityCompat.shouldShowRequestPermissionRationale ( ControlActivity.this,
                Manifest.permission.BLUETOOTH))
        {
            mypermission = add(mypermission,Manifest.permission.BLUETOOTH);

        }

        // Contact
        if(!ActivityCompat.shouldShowRequestPermissionRationale ( ControlActivity.this,
                Manifest.permission.READ_CONTACTS))
        {
            mypermission = add(mypermission,Manifest.permission.READ_CONTACTS);

        }



        for(String element:mypermission)
        {
            Log.d("check_permission", element);
        }

        ActivityCompat.requestPermissions ( ControlActivity.this, mypermission,2);


    }
}
