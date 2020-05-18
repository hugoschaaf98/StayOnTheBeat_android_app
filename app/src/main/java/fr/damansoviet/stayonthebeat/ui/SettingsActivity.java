package fr.damansoviet.stayonthebeat.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Set;

import fr.damansoviet.stayonthebeat.AppContainer;
import fr.damansoviet.stayonthebeat.R;
import fr.damansoviet.stayonthebeat.StayOnTheBeatApplication;
import fr.damansoviet.stayonthebeat.models.peripherals.BluetoothManager;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = "SettingsActivity";

    private BluetoothAdapter mBluetoothAdapter ;
    private BluetoothManager mBluetoothManager;

    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>() ;
    private DeviceListAdapter mDeviceListAdapter ;
    // graphical components
    private Switch mBluetoothSwitch;
    private ListView mLvNewDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_settings );
        NavBar();
        init();
    }

    /**
     * Destructeur
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy ();

        Log.d ( TAG,"onDestroy: called" );
        unregisterReceiver ( mBroadcastReceiver1 );
        unregisterReceiver ( mBroadcastReceiver2 );
        unregisterReceiver ( mBroadcastReceiver3 );
    }

    /**
     * Initialisation des paramètres graphiques
     */
    private void init()
    {
        AppContainer appContainer = ((StayOnTheBeatApplication)getApplication()).appContainer;
        mBluetoothManager = appContainer.bluetoothManager;
        mBluetoothAdapter = mBluetoothManager.getBluetoothAdapter();
        // setup the devices list
        mLvNewDevices = (ListView) findViewById ( R.id.LvNewDevices );
        mLvNewDevices.setOnItemClickListener ( (AdapterView.OnItemClickListener) SettingsActivity.this );
        // initialize the bluetooth switch position
        mBluetoothSwitch = findViewById(R.id.sw_bluetooth);
        mBluetoothSwitch.setChecked(mBluetoothAdapter.isEnabled());
    }

    /*** SLOTS ***/

    /**
     * Cette fonction va permettre d'activer/désativer le bluetooth de votre téléphone
     *
     */
    public void bluetoothSwitch(View v)
    {
        // trois possibilités
        // dans le cas où le téléphone n'a pas de bluetooth
        if(mBluetoothAdapter == null)
        {
            Log.d (TAG,"enableDisable : doesnt have BT compatobilities");
        }

        // Si le bluetooth n'est pas activé
        if(!mBluetoothAdapter.isEnabled ())
        {
            Log.d(TAG,"enableDisable : enabling BT");
            //Activation du bluetooth
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity ( enableBTIntent );


            // Maintenant pour avoir une visibilité de ce qu'on fait nous allons attraper le flux c'est a dire appeler
            // broadcast1 permettant de catch les differents case de notre bluetooth afin d'aider au debug
            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver ( mBroadcastReceiver1,BTintent );
        }
        if(mBluetoothAdapter.isEnabled ())
        {
            Log.d(TAG,"enableDisable : disabling BT");

            // desactivation du bluetooth
            mBluetoothAdapter.disable ();


            // Maintenant pour avoir une visibilité de ce qu'on fait nous allons attraper le flux c'est a dire appeler
            // broadcast1 permettant de catch les differents case de notre bluetooth afin d'aider au debug
            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver ( mBroadcastReceiver1,BTintent );
        }
    }


    /**
     *
     * cette fonction permettra a lappareil d'etre visible pendant quelques secondes par d'autres appareils bluetooth
     *
     */
    public void enableDiscoverability(View v)
    {
        Log.d(TAG,"50 secondes pour être vu par d'autres périphériques");

        Intent discoverable = new Intent ( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
        discoverable.putExtra ( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,50 );
        startActivity ( discoverable );

        // Pour aider au debug nous allons intercepter le flux donc nous allons créer un broadcast2
        // afin de voir si des changements ont été appercus
        IntentFilter intentFilter = new IntentFilter ( mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED );
        registerReceiver ( mBroadcastReceiver2,intentFilter);
    }


    /*** SLOTS ***
     *
     * Le but va etre de détecter les appareils bluetooth
     *
     *
     */
    public void scanDevices(View v)
    {
        Log.d(TAG , "btnDiscover : Looking for unpaired devices");
        if(mBluetoothAdapter.isDiscovering ())
        {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG,"btnDiscover : Cancelling discovery");

            //nous allons checker si il y a permission
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();

            // ici, cela ne va pas nous aider qu'à débuguer, nous allons aussi intégrer ce que nous avvons réceptionné dans une liste
            // généré par notre broadcast3
            IntentFilter discoverDevicesIntent = new IntentFilter ( BluetoothDevice.ACTION_FOUND );
            registerReceiver ( mBroadcastReceiver3 , discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering())
        {
            //on va checker les permissions
            checkBTPermissions ();
            Toast.makeText (SettingsActivity.this,"Recherche d'appareils en cours ...",Toast.LENGTH_LONG).show ();
            // on lance la recherche
            mBluetoothAdapter.startDiscovery () ;
            IntentFilter discoverDevicesIntent = new IntentFilter ( BluetoothDevice.ACTION_FOUND );
            registerReceiver ( mBroadcastReceiver3 , discoverDevicesIntent);
        }
    }

    /**
     * Simple fonction permettant d'appeler la permission de localisation, cette permission est nécessaire pour certaines versions d'android
     * Sinon la connection bluetooth ne peut pas se faire
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions()
    {
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
//        {
//            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                int permissionCheck = checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
//                permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
//
//                if(permissionCheck != 0) {
//                    requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION} ,1001);
//                }
//                else {
//                     Log.d ( TAG,"Pas besoin de checker les permissions" );
//                }
//            }
//        }
    }

    /**
     * ici nous récupérons l'évènement lorsque l'utilisateur intéragis avec notre list dans le but de récupérer le nom du device et son adresse
     * dans un premier temps et pour finit par un apparaillage de cette deniere.
     *
     */
    @Override
    public  void onItemClick(AdapterView<?> adapterView,View v ,int i ,long l)
    {
        BluetoothDevice tmp;
        //nous devons tout dabord supprimer la recherche car cela demande beaucoup de memoire
        mBluetoothAdapter.cancelDiscovery ();
        Log.d(TAG , "onItemCLick : you clicked on a device");
        //nous allons recuperer les infos que vous avez selectionné
        tmp = mBTDevices.get(i);
        String deviceName = tmp.getName ();
        String deviceAddress = tmp.getAddress ();
        Log.d(TAG , "onItemClick : deviceName = " + deviceName);
        Log.d(TAG , "onItemClick : deviceAddress = " + deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            // check if we need to pair else start the client directly
            if(tmp.getBondState () != BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "Tentative d'apparaillage " + deviceName);
                tmp.createBond();
            }
            else {
                mBluetoothManager.setBluetoothDevice(tmp);
                mBluetoothManager.connectAndStartClient();
            }
            IntentFilter discoverDevicesIntent = new IntentFilter ( BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver ( mBroadcastReceiver4 , discoverDevicesIntent);
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
        bottomNavigationView.setSelectedItemId ( R.id.settings );

        // gérer les redirections en fonction de ce que va faire l'utilisateur
        bottomNavigationView.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId ()) {
                    case R.id.maison:
                        startActivity ( new Intent ( getApplicationContext (),ControlActivity.class ) );
                        overridePendingTransition ( R.anim.slide_in_left,R.anim.slide_out_right );
                        return true;

                    case R.id.settings:
                        return true;

                    case R.id.sms:
                        startActivity ( new Intent ( getApplicationContext (),SmsActivity.class ) );
                        overridePendingTransition ( R.anim.slide_in_left,R.anim.slide_out_right);
                        return true;
                }
                return false;
            }
        } );
    }


    /*******************************************************************************************************************************/
    /*****                                                  LES BROADCASTS                                                  ********/
    /*******************************************************************************************************************************/


    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver () {
        public void onReceive(Context context,Intent intent)
        {
            String action = intent.getAction();
            //evenement quand on decouvre un equipement
            if(action.equals ( mBluetoothAdapter.ACTION_STATE_CHANGED ))
            {
                final int state = intent.getIntExtra ( BluetoothAdapter.EXTRA_STATE,mBluetoothAdapter.ERROR );

                // application des differents etats du boutton
                switch(state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d ( TAG,"onReceive: STATE OFF" );
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d ( TAG,"mBroadcast: STATE_TURNING_ON" );
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d ( TAG,"mBroadcast: STATE_TURNING_OFF" );
                        break;

                }

            }
        }
    };



    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver () {
        @Override
        public void onReceive(Context context,Intent intent)
        {
            String action = intent.getAction();
            //evenement quand on decouvre un equipement
            if(action.equals ( mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED ))
            {
                int mode = intent.getIntExtra ( BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR );

                // Receptions des differents etats du bouton
                switch(mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d ( TAG,"onReceive: Discoverable enabled" );
                        break;

                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d ( TAG,"mBroadcast: tentative de connection" );
                        break;

                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d ( TAG,"mBroadcast: impossible detablir une connection" );
                        break;

                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d ( TAG,"mBroadcast: Connection en cours ..." );
                        break;

                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d ( TAG,"mBroadcast: Connection success" );
                        break;

                }

            }
        }
    };


    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver ()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            Log.d(TAG , "onreceive : ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                Toast.makeText (SettingsActivity.this,"Appreil trouvé !",Toast.LENGTH_SHORT).show ();
                BluetoothDevice device = intent.getParcelableExtra ( BluetoothDevice.EXTRA_DEVICE );
                //ajout des devices trouvés dnas notre liste
                mBTDevices.add(device);
                // partie des elements qui ont deja ete rencontré
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if(pairedDevices.size () > 0 )
                {
                    for(BluetoothDevice periph : pairedDevices)
                    {
                        mBTDevices.add(periph);
                    }
                }
                Log.d(TAG , "onReceives " + device.getName () + " : " + device.getAddress());

                //appel du constructeur de la classe DeviceListAdapter
                mDeviceListAdapter = new DeviceListAdapter ( context , R.layout.device_list , mBTDevices );
                //intégration de la vue mDeviceListAdapter dans notre ListView
                mLvNewDevices.setAdapter ( mDeviceListAdapter );
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver ()
    {
        @Override
        public void onReceive(Context context , Intent intent)
        {
            final String action = intent.getAction () ;
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // on fait face à trois cas
                // si le composant est deja lié
                if(mDevice.getBondState () == BluetoothDevice.BOND_BONDED)
                {
                    //dans ce cas nous naurons qua nous logger directement
                    mBluetoothManager.setBluetoothDevice(mDevice);
                    mBluetoothManager.connectAndStartClient();

                    Toast.makeText (SettingsActivity.this,"Connexion success",Toast.LENGTH_SHORT).show ();
                    Log.d(TAG, "Broadcast : BOND_BOUNDED");
                }

                // si cest une nouvelle liaison
                if(mDevice.getBondState () == BluetoothDevice.BOND_BONDING)
                {
                    Log.d(TAG, "Broadcast : BOND_BOUNDING");
                    Toast.makeText (SettingsActivity.this,"Tentative de Connexion avec l'appareil ...",Toast.LENGTH_SHORT).show ();
                }

                // si la liaison est cassé
                if(mDevice.getBondState () == BluetoothDevice.BOND_NONE)
                {
                    Log.d(TAG, "Broadcast : BOND_NONE");
                    Toast.makeText (SettingsActivity.this,"Impossible de se connecter avec l'appareil",Toast.LENGTH_SHORT).show ();
                    mBluetoothManager.shutdownClient();
                }

            }
        }
    };
}
