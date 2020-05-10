package fr.damansoviet.stayonthebeat.viewmodels;

import androidx.lifecycle.ViewModel;

import fr.damansoviet.stayonthebeat.models.Metronome;

public class ControlViewModel extends ViewModel {

    private Metronome mMetronome;

    public ControlViewModel(Metronome metronome) {
        mMetronome = metronome;
    }

    public Metronome getMetronome() {
        return mMetronome;
    }

    public void setMetronome(Metronome metronome) {
        this.mMetronome = metronome;
    }
}
