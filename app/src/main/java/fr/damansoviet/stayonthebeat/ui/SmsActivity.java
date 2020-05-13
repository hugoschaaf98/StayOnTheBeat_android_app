package fr.damansoviet.stayonthebeat.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import fr.damansoviet.stayonthebeat.R;

public class SmsActivity extends AppCompatActivity {

    //on definit nos propriété
    private EditText phonetxt ;
    private EditText message ;
    private Button envoi ;
    private LinearLayout LaySMS ;
    private EditText txtContacts ;
    private Button contact ;
    private static int PICK_CONTACT = 1 ;
    private int verif = 0 ;
    // initialisation de la partie vibreur
    Vibrator vibrator ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_sms);

        // Appel Des Fonctions
        NavBar();
        init();
    }


    /**
     * Fonction d'initialisation
     */
    private void init()
    {
        // recuperation des objets graphiques
        phonetxt = (EditText)findViewById( R.id.TxtPhone );
        message = (EditText)findViewById( R.id.TxtMessage );
        envoi = (Button)findViewById( R.id.btnEnvoi );
        LaySMS = (LinearLayout)findViewById ( R.id.laySMS );
        contact = (Button)findViewById ( R.id.Contact );
        String pub = "Hello,\nvenez télécharger l'application StayOnTheBeat c'est super utile ! :)";
        message.setText ( pub );
        vibrator = (Vibrator) getSystemService ( VIBRATOR_SERVICE );
    }

    /** SLOTS **/
    public void onClickSend(View v)
    {
        // on va faire un controle si nous avons les compatibilite
        if(ActivityCompat.checkSelfPermission ( SmsActivity.this,Manifest.permission.SEND_SMS ) ==
                PackageManager.PERMISSION_GRANTED)
        {
            //pour envoyer notre sms il faut quon recupere ce qui a ete tapé au préalable
            String txt = phonetxt.getText ().toString ();
            String msg = message.getText ().toString ();

            // on a plus qua injecter nos infos dans la fonction permettant lenvoi de sms.
            SmsManager.getDefault ().sendTextMessage ( txt,null,msg,null,null );
            //message pour dire que sms est parti
            fnctVibration ( "Message envoyé",350 );
            //vider le texte
            //message.setText ( " " );
        }
        else
        {
            //demande une fois de donner la permission
            // si il y a refus on lui fera un autre type de message
            /**
            if(!ActivityCompat.shouldShowRequestPermissionRationale ( SmsActivity.this,
                    Manifest.permission.SEND_SMS))
            {
                //je nai pas la permission donc on va faire un tableau des permissions quon va demander

                // Pas de permission donc ajout de cette demande dans un tableau
                String[] permissions = {Manifest.permission.SEND_SMS};
                // afficher la demande de permission
                //request 2 est le numero quon va controler dans le cas ou on va reverifier le type de permission
                // qui a ete affecte
                //ici cest affichage de la fenetre de permission
                ActivityCompat.requestPermissions ( SmsActivity.this, permissions,2);
            }
            else
            {
                // on est dans le cas ou on a eu un refus de permission la premiere fois et la on va faire un message expliquant que
                // cest obligatoire
                messagePermissionObligatoire ();
            }
             **/
            messagePermissionObligatoire ();
        }
    }



    /**
     * Cette fonction fait simplement apparaitre un snackbar.
     * Cette dernière ne s'active que dans le cas où l'utilisateur à fait un refus pour donner la permission
     * de pouvoir envoyer des sms.
     */
    private void messagePermissionObligatoire()
    {
        Snackbar.make ( LaySMS,"Permission SMS obligatoire",Snackbar.LENGTH_LONG).
                //ici nous allons générer dans la snackbar un raccourci permettant à l'utilisateur d'accéder à ses paramètres de permission
                setAction ( "Paramètres",new View.OnClickListener ()
                {
                    @Override
                    public void onClick(View view)
                    {
                        final Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        final Uri uri = Uri.fromParts ( "package" , SmsActivity.this.getPackageName(),null);
                        intent.setData ( uri );
                        startActivity ( intent );
                    }

                }).show ();
    }





    private void demande()
    {
        // cette fonction n'a pour but que de demander lacces aux contacts du telephone avant dafficher les contacts
        // on va faire un controle si nous avons les compatibilite
        if(ActivityCompat.checkSelfPermission ( SmsActivity.this,Manifest.permission.READ_CONTACTS ) ==
                PackageManager.PERMISSION_GRANTED)
        {
            init();
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_CONTACTS};
            ActivityCompat.requestPermissions ( SmsActivity.this, permissions,2);
            if(ActivityCompat.checkSelfPermission ( SmsActivity.this,Manifest.permission.SEND_SMS ) ==
                    PackageManager.PERMISSION_GRANTED)
            {
                init();
            }

        }

    }

    /**
     *
     * @param name le message à afficher
     * @param vib  le tps en ms de vibration
     */
    public void fnctVibration( String name, int vib)
    {
        vibrator.vibrate(vib);// le nbr est le nbr de fois ou je repete sachant que 0 est infini
        Toast.makeText ( SmsActivity.this,name,Toast.LENGTH_SHORT ).show ();
    }




    /** SLOTS **/
    public void callContacts(View v)
    {
        Intent intent = new Intent ( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
        startActivityForResult( intent,PICK_CONTACT );
    }


    /**
     * Récupération de l'information sélectionné par l'utilisateur (après avoir cliqué sur callcontacts)
     */
    @Override
    protected void onActivityResult(int reqCode , int resultCode , Intent data)
    {

        super.onActivityResult ( reqCode , resultCode, data);

        if(reqCode == PICK_CONTACT)
        {
            if(resultCode == AppCompatActivity.RESULT_OK)
            {
                Uri contactData = data.getData ();
                Cursor c = getContentResolver ().query (contactData,null, null ,null,null );

                if(c.moveToFirst ())
                {
                    String name = c.getString ( c.getColumnIndex (ContactsContract.Contacts.DISPLAY_NAME) );
                    // Problème rencontré, impossible de récupérer le numéro, appel d'une fonction qui permet en fonction du nom
                    // de retrouver le numéro à partir de la liste de ses contacts
                    checkNumber(name);
                    c.close ();
                }
            }
        }
    }

    /**
     * la fonction permet de regénérer un cursor comprenant les numeros et les noms de ses contacts,
     * si un des noms est coherant avec largument name, on affichera le numero qui lui correspond
     *
     * @param name : nom du contact
     */
    public void checkNumber(String name)
    {
        // recupération du contenu du téléphone
        ContentResolver contentResolver = this.getContentResolver ();
        Cursor cursor = contentResolver.query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        // je veux recuperer le nom et le numero des contacts
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_ALTERNATIVE,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },null,null,null);
        if (cursor == null)
        {
            //affichage dans la console
            Log.d("recup", "################## ERROR CURSOR #########");
        }
        else
        {
            //parcours des contacts
            while(cursor.moveToNext ())
            {
                //dans mon cursor, il y a  deux colonnes , dans une colonne il y a displaynamealternative
                //dans la deuxieme il y a number
                String nom = cursor.getString ( cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_ALTERNATIVE) );
                String phone = cursor.getString ( cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER) );
                //maintenant quon a lle nom et le phone on regarde si cest pareil
                Log.d("urgence", nom + " " + name);
                if(nom.equals ( name ))
                {
                    Log.d("GG", "IM HERE");
                    //Toast.makeText (this , "You've picked" + phone , Toast.LENGTH_LONG).show();
                    verif = 1 ;
                    phonetxt.setText ( phone );
                }

            }
            // ajout d'un message d'erreur dans le cas où la fonction n'a pas pu trouver un nom correspondant
            if (verif == 0)
            {
                Toast.makeText ( this,"Error : le numéro n'a pas pu être injecté",Toast.LENGTH_SHORT ).show ();

            }
            else
            {
                verif = 0 ;
            }
            //fermer le cursor
            cursor.close ();
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
        bottomNavigationView.setSelectedItemId ( R.id.sms );

        // gérer les redirections en fonction de ce que va faire l'utilisateur
        bottomNavigationView.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId ()) {
                    case R.id.maison:
                        startActivity ( new Intent ( getApplicationContext (),ControlActivity.class ) );
                        overridePendingTransition ( 0,0 );
                        return true;

                    case R.id.settings:
                        startActivity ( new Intent ( getApplicationContext (),Settings.class ) );
                        overridePendingTransition ( 0,0 );
                        return true;

                    case R.id.sms:
                        return true;
                }
                return false;
            }
        } );
    }



}