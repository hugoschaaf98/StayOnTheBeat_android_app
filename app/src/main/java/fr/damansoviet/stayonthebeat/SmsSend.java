package fr.damansoviet.stayonthebeat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import android.os.Bundle;

public class SmsSend extends AppCompatActivity {

    //on definit nos propriété
    // pour editText on va recuperer ce que lon inject sur les elements dont le type est edittext (a voir sur les elements activity_main)
    private EditText phonetxt ;
    private EditText message ;
    private Button envoi ;
    private LinearLayout LaySMS ;
    private EditText txtContacts ;
    private Button contact ;
    private static int PICK_CONTACT = 1 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_sms_send );
        initActivity ();

    }



    // initialisation
    private void initActivity()
    {
        // recuperation des objets graphiques
        phonetxt = (EditText)findViewById( R.id.TxtPhone );
        message = (EditText)findViewById( R.id.TxtMessage );
        envoi = (Button)findViewById( R.id.btnEnvoi );
        LaySMS = (LinearLayout)findViewById ( R.id.laySMS );
        contact = (Button)findViewById ( R.id.Contact );
        String pub = "Hello venez donc télécharger lapplication StayOnTheBeat c'est trop cool ! ";
        message.setText ( pub );


        //recupContacts ();
        //affcontact();
        //gestion de levenement click sur boutton envoie
    }

    public void onClickSend(View v)
    {
        // on va faire un controle si nous avons les compatibilite
        if(ActivityCompat.checkSelfPermission ( SmsSend.this,Manifest.permission.SEND_SMS ) ==
                PackageManager.PERMISSION_GRANTED)
        {
            //pour envoyer notre sms il faut quon recupere ce qui a ete tapé au préalable
            String txt = phonetxt.getText ().toString ();
            String msg = message.getText ().toString ();

            // on a plus qua injecter nos infos dans la fonction permettant lenvoi de sms.
            SmsManager.getDefault ().sendTextMessage ( txt,null,msg,null,null );
            //message pour dire que sms est parti
            Toast.makeText ( SmsSend.this,"SMS ENVOYE AVEC SUCCES",Toast.LENGTH_SHORT ).show ();
            //vider le texte
            //message.setText ( " " );
        }
        else
        {
            //demande une fois de donner la permission
            // si il y a refus on lui fera un autre type de message
            if(!ActivityCompat.shouldShowRequestPermissionRationale ( SmsSend.this,
                    Manifest.permission.SEND_SMS))
            {
                //je nai pas la permission donc on va faire un tableau des permissions quon va demander
                String[] permissions = {Manifest.permission.SEND_SMS};
                // afficher la demande de permission
                //request 2 est le numero quon va controler dans le cas ou on va reverifier le type de permission
                // qui a ete affecte
                //ici cest affichage de la fenetre de permission
                ActivityCompat.requestPermissions ( SmsSend.this, permissions,2);
            }
            else
            {
                // on est dans le cas ou on a eu un refus de permission la premiere fois et la on va faire un message expliquant que
                // cest obligatoire
                messagePermissionObligatoire ();
            }
        }
    }

    // message pour informer l'user que la permission est obligatoire
    private void messagePermissionObligatoire()
    {
        //le setaction va preciser ce quil doit faire
        Snackbar.make ( LaySMS,"Permission SMS obligatoire",Snackbar.LENGTH_LONG).
                setAction ( "Paramètres",new View.OnClickListener ()
                {
                    @Override
                    public void onClick(View view)
                    {
                        final Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        final Uri uri = Uri.fromParts ( "package" , SmsSend.this.getPackageName(),null);
                        intent.setData ( uri );
                        startActivity ( intent );
                    }

                }).show ();
    }





    private void demande()
    {
        // cette fonction n'a pour but que de demander lacces aux contacts du telephone avant dafficher les contacts
        // on va faire un controle si nous avons les compatibilite
        if(ActivityCompat.checkSelfPermission ( SmsSend.this,Manifest.permission.READ_CONTACTS ) ==
                PackageManager.PERMISSION_GRANTED)
        {
            initActivity ();
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_CONTACTS};
            ActivityCompat.requestPermissions ( SmsSend.this, permissions,2);
            if(ActivityCompat.checkSelfPermission ( SmsSend.this,Manifest.permission.SEND_SMS ) ==
                    PackageManager.PERMISSION_GRANTED)
            {
                initActivity ();
            }

        }

    }


    public void callContacts(View v)
    {
        // cette fonction sactive des que le bouton de lapp est appuye
        Intent intent = new Intent ( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
        //on lance le prog donc on definit le intent pour preciser ou lancer
        // et comme deuxieme argument on doit donner une valeur > 0 pour retourner un onActivityResult()
        startActivityForResult( intent,PICK_CONTACT );
    }

    @Override
    protected void onActivityResult(int reqCode , int resultCode , Intent data)
    {
        super.onActivityResult ( reqCode , resultCode, data);

        //on verifie si reqcode correspond toujours a pickcode
        if(reqCode == PICK_CONTACT)
        {
            if(resultCode == AppCompatActivity.RESULT_OK)
            {
                Uri contactData = data.getData ();
                //ContentResolver contentResolver = this.getContentResolver ();
                Cursor c = getContentResolver ().query (contactData,null, null ,null,null );

                if(c.moveToFirst ())
                {
                    String name = c.getString ( c.getColumnIndex (ContactsContract.Contacts.DISPLAY_NAME) );
                    checkNumber(name);
                    c.close ();
                }
            }
        }
    }

    public void checkNumber(String name)
    {
        //le but est de regénérer un cursor comprenant les numeros et les noms et si un des noms est coherant
        // avec largument injecte on affichera le numero

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
                //maintenant quon a lle nom et le phone on regarde si cest pareil et hop si cest le cas on affiche le numero
                Log.d("urgence", nom + " " + name);
                if(nom.equals ( name ))
                {
                    Log.d("GG", "IM HERE");
                    //Toast.makeText (this , "You've picked" + phone , Toast.LENGTH_LONG).show();
                    phonetxt.setText ( phone );
                }

            }
            //fermer le cursor
            cursor.close ();
        }

    }



}