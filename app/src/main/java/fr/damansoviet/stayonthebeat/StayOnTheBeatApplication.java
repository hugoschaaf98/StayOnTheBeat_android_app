package fr.damansoviet.stayonthebeat;

import android.app.Application;
import android.os.Vibrator;

import fr.damansoviet.stayonthebeat.AppContainer;

public class StayOnTheBeatApplication extends Application {

    // here making it public doesn't cause any issue since application will
    // be instantiated before any call to vibrator
    public static Vibrator vibrator = null;
    public AppContainer appContainer = new AppContainer();

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

}
