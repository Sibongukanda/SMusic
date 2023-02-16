
import javafx.scene.media.*;

public final class Song {

    private final Media media;
    private final String source;
    private final String name;
    private double panning, volume, rate;
    private boolean mute;

    public Song(Media media, String name) {
        this.media = media;
        this.source = media.getSource();
        this.name = name;
        setMute(false);
        setPanning(0);
        setVolume(100);
        setRate(1);
    }

    public double getPanning() {
        return panning;
    }

    public void setPanning(double panning) {
        this.panning = panning;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public Media getMedia() {
        return media;
    }

    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return source + "#" + name;
    }

}
