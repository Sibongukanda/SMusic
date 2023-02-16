
import java.io.*;
import java.text.*;
import java.util.*;
import javafx.animation.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

public class API {

    public static ImageView pause = new ImageView(new Image(API.class.getResource("Images/pause.png").toString()));
    public static ImageView play = new ImageView(new Image(API.class.getResource("Images/play.png").toString()));
    public static ImageView warn = new ImageView(new Image(API.class.getResource("Images/warn.jpg").toString()));
    public static final File PLAYLISTFILE = new File(System.getProperty("user.home").replace("\\", "\\\\") + "\\\\srzPlay.srz");
    public static final String[] AUDIOS_FORMATES = {".mp3", ".wav", ".aiff", ".m3u8", ".aif", ".aac", ".mp4"};
    private static double xDrage, yDrage;
    public static final String[] shotcuts = {"Ctrl+O", "SPACE", "Ctrl+SPACE", "Ctrl+R", "M", "LEFT", "RIGHT", "Ctrl+DOWN", "Ctrl+UP", "UP", "DOWN", "L", "U", "Ctrl+S", "D", "X", "S"};

    public static void LastDir(File file, FileChooser chooser) {
        chooser.setInitialDirectory(file.getParentFile());
    }

    public static void SetDir(File file, FileChooser chooser) {
        if (file.exists()) {
            chooser.setInitialDirectory(file);
        }
    }

    public static String GetExtension(File file) {
        String name = GetFileName(file);
        int start = name.lastIndexOf(".") + 1;
        int end = name.length();
        String ext = name.substring(start, end);
        return ext;
    }

    public static void Move(Stage window, MouseEvent event, Node box) {
        xDrage = event.getSceneX();
        yDrage = event.getSceneY();
        box.setOnMouseDragged(e -> {
            window.setX(e.getScreenX() - xDrage);
            window.setY(e.getScreenY() - yDrage);
        });
    }

    public static FileChooser MusicFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audios", "*.mp3", "*.wav", "*.aiff", "*.m3u8", "*.aif", "*.aac", "*.mp4"));
        chooser.setTitle("SRZ_MUSIC");
        String music = System.getProperty("user.home") + File.separator;
        music = music.replace("\\", "\\\\") + "Music";
        SetDir(new File(music), chooser);
        return chooser;
    }

    private static String Title(MediaPlayer mp) {
        String name;
        try {
            name = (String) mp.getMedia().getMetadata().get("title");
            if (name.isEmpty() || name.equals("")) {
                name = mp.getMedia().getSource();
                name = name.substring(name.lastIndexOf("/") + 1).replaceAll("%20", " ");
                name = name.substring(0, name.length() - 4).toUpperCase();
            } else {
                name = (String) mp.getMedia().getMetadata().get("title");
                name = name.toUpperCase();
            }
            return name;
        } catch (Exception e) {
            name = mp.getMedia().getSource();
            name = name.substring(name.lastIndexOf("/") + 1).replaceAll("%20", " ");
            name = name.substring(0, name.length() - 4).toUpperCase();
            return name;
        }

    }

    private static String Artist(MediaPlayer mp) {
        String name;
        try {
            name = (String) mp.getMedia().getMetadata().get("artist");
            if (name.isEmpty() || name.equals("")) {
                name = "UNKNOWN";
            } else {
                name = (String) mp.getMedia().getMetadata().get("artist");
            }
            return name;
        } catch (Exception e) {
            name = "UNKNOWN";
            return name;
        }
    }

    public static void TAI(MediaPlayer mp, Text title, Text artist) {
        title.setText(Title(mp));
        artist.setText(Artist(mp));
        Media media = mp.getMedia();
        media.getMetadata().addListener((MapChangeListener.Change<? extends String, ? extends Object> change) -> {
            if (change.wasAdded()) {
                API.Data(change.getKey(), change.getValueAdded(), title, artist);
            }
        });
    }

    private static void Data(String key, Object value, Text title, Text artist) {
        switch (key) {
            case "title":
                title.setText(value.toString().toUpperCase());
                break;
            case "artist":
                artist.setText(value.toString());
                break;
            default:
                break;
        }
    }

    public static void Stop(MediaPlayer mp) {
        if (AppController.isPresent) {
            mp.stop();
        }
    }

    public static void ClickSeek(Slider slider, MouseEvent e) {
        slider.setValueChanging(true);
        double v = e.getX() / slider.getWidth() * slider.getMax();
        slider.setValue(v);
        slider.setValueChanging(false);
    }

    public static void UpdateValues(Slider slider, MediaPlayer mp, Text cTime, Text tTime) {
        if (slider != null) {
            Platform.runLater(() -> {
                Duration duration = mp.getMedia().getDuration();
                Duration currentTime = mp.getCurrentTime();
                String[] times = FormatDuration(currentTime, duration).split("/");
                if (times[1].substring(0, 2).equals("00")) {
                    times[1] = times[1].substring(3);
                    times[0] = times[0].substring(3);
                }
                cTime.setText(times[0]);
                tTime.setText(times[1]);
                slider.setDisable(duration.isUnknown());
                if (!slider.isDisabled() && duration.greaterThan(Duration.ZERO) && !slider.isValueChanging()) {
                    slider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }
            });
        }
    }

    private static String FormatDuration(Duration dur1, Duration dur2) {
        long d1 = (int) dur1.toMillis();
        long d2 = (int) dur2.toMillis();
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = f.format(new Date(d1)) + "/" + f.format(new Date(d2));
        return time;
    }

    public static void StopFade(MediaPlayer mp, Slider volume, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            double vol = volume.getValue();
            double ori = volume.getValue();
            while (vol != 0) {
                VolumeDown(mp, volume, songs);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {

                }
                vol = volume.getValue();
            }
            mp.stop();
            volume.setValue(ori);
        }
    }

    public static void PlayPause(MediaPlayer mp, Slider progress, Text cTime, Text tTime) {
        if (AppController.isPresent) {
            if (null != mp.getStatus()) {
                switch (mp.getStatus()) {
                    case PLAYING:
                        Pause(mp);
                        break;
                    case STOPPED:
                        mp.seek(mp.getStartTime());
                        Play(mp);
                        mp.setOnReady(() -> {
                            UpdateValues(progress, mp, cTime, tTime);
                        });
                        mp.currentTimeProperty().addListener((observable) -> {
                            UpdateValues(progress, mp, cTime, tTime);
                        });
                        progress.valueProperty().addListener(observable -> {
                            if (progress.isValueChanging()) {
                                mp.seek(mp.getMedia().getDuration().multiply(progress.getValue() / 100.0));
                            }
                        });
                        break;
                    case PAUSED:
                        Play(mp);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static void Pause(MediaPlayer mp) {
        if (AppController.isPresent) {
            mp.pause();
        }
    }

    public static void Play(MediaPlayer mp) {
        if (AppController.isPresent) {
            mp.play();
        }
    }

    public static void BindVolume(Slider slider, MediaPlayer mp) {
        mp.volumeProperty().bind(slider.valueProperty().divide(100));
        if (!slider.isValueChanging()) {
            slider.setValue((int) Math.round(mp.getVolume() * 100));
        }
    }

    public static String GetFileName(File file) {
        return file.getName();
    }

    public static String GetFileName(File file, boolean ext) {
        String name = GetFileName(file);
        if (!ext) {
            int last = name.lastIndexOf(".");
            name = name.substring(0, last);
        }
        return name;
    }

    public static void VolumeDown(MediaPlayer mp, Slider volume, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            BindVolume(volume, mp);
            volume.setValue(volume.getValue() - 1);
            AppController.songs.entrySet().forEach((entry) -> {
                entry.getValue().setVolume(volume.getValue());
            });
        }
    }

    public static void VolumeUp(MediaPlayer mp, Slider volume, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            BindVolume(volume, mp);
            volume.setValue(volume.getValue() + 1);
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setVolume(volume.getValue());
            });
        }
    }

    public static void Mute(MediaPlayer mp, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            if (mp.isMute()) {
                mp.setMute(false);
            } else {
                mp.setMute(true);
            }
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setMute(mp.isMute());
            });
        }
    }

    public static void MediaConfig(MediaPlayer mp, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            Song song = songs.get(0);
            mp.setBalance(song.getPanning());
            mp.setRate(song.getRate());
            mp.setMute(song.isMute());
            mp.setVolume(song.getVolume());
        }
    }

    public static void PanLeft(MediaPlayer mp, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            double balance = mp.getBalance();
            if (balance == 0.0 || balance == 1.0) {
                mp.setBalance(-1.0);
            } else {
                mp.setBalance(0.0);
            }
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setPanning(mp.getBalance());
            });
        }
    }

    public static void PanLeft(MediaPlayer mp, MediaPlayer mp1, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            double balance = mp.getBalance();
            if (balance == 0.0 || balance == 1.0) {
                mp.setBalance(-1.0);
                mp1.setBalance(-1.0);
            } else {
                mp.setBalance(0.0);
                mp1.setBalance(0.0);
            }
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setPanning(mp.getBalance());
            });
        }
    }

    public static void PanRight(MediaPlayer mp, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            double balance = mp.getBalance();
            if (balance == 0.0 || balance == -1.0) {
                mp.setBalance(1.0);
            } else {
                mp.setBalance(0.0);
            }
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setPanning(mp.getBalance());
            });
        }

    }

    public static void RateSlowAuto(MediaPlayer mp, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            double rate = mp.getRate();
            if (rate == 1.0 || rate == 1.3) {
                mp.setRate(0.8);
            } else {
                mp.setRate(1.0);
            }
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setRate(mp.getRate());
            });
        }
    }

    public static void RateFastAuto(MediaPlayer mp, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            double rate = mp.getRate();
            if (rate == 1.0 || rate == 0.8) {
                mp.setRate(1.3);
            } else {
                mp.setRate(1.0);
            }
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setRate(mp.getRate());
            });
        }
    }

    public static void Reset(MediaPlayer mp, HashMap<Integer, Song> songs) {
        if (AppController.isPresent) {
            mp.setMute(false);
            mp.setRate(1);
            mp.setBalance(0);
            songs.entrySet().forEach((entry) -> {
                entry.getValue().setRate(mp.getRate());
                entry.getValue().setMute(mp.isMute());
                entry.getValue().setPanning(mp.getBalance());
            });
        }
    }

    public static void Message(String message) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        Text text = new Text(message.toUpperCase());
        text.setWrappingWidth(150);
        text.setStyle("fx-font:bold 16pt monospace;-fx-text-alignment:center;");
        ImageView view = warn;
        view.setPreserveRatio(false);
        view.setFitHeight(30);
        view.setFitWidth(30);
        VBox b = new VBox(text);
        b.setAlignment(Pos.CENTER);
        b.setPrefSize(150, 100);
        HBox.setHgrow(b, Priority.ALWAYS);
        HBox box = new HBox(view, b);
        VBox pane = new VBox(5, box);
        pane.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.seconds(1), pane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_BOTH);
        fade.setOnFinished((event) -> {
            Timeline line = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                fade.setFromValue(1);
                fade.setToValue(0);
                fade.play();
            }));
            fade.setOnFinished((ev) -> {
                stage.close();
            });
            line.play();
        });
        fade.play();
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: white;-fx-background-radius:3%");
        box.setPrefSize(250, 100);
        box.setAlignment(Pos.CENTER);
        Scene scene = new Scene(pane, Color.TRANSPARENT);
        pane.setOnMouseClicked((event) -> {
            Move(stage, event, pane);
        });
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public static void Help() {
        String[] actions = {"open file chooser", "play/pause", "stop song", "reset", "Mute", "pan left", "pan right", "low rate", "fast rate", "volume up", "volume down", "Loop one song on/off", "Upload playlist", "save playlist", "Delete playlist", "clear playlist", "Shuffle on/off"};
        Button okButton = new Button("close");
        okButton.setStyle(" -fx-text-fill: white;-fx-background-color: black;");
        okButton.setPrefSize(80, 20);
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        okButton.setOnAction((event) -> {
            stage.close();
        });
        VBox b = new VBox();
        for (int i = 0; i < shotcuts.length; i++) {
            Text text = new Text(shotcuts[i].toUpperCase());
            text.setWrappingWidth(80);
            Text textEx = new Text(" : " + actions[i].toUpperCase());
            HBox hb = new HBox(5, text, textEx);
            hb.setAlignment(Pos.CENTER_LEFT);
            hb.setPrefWidth(150);
            b.getChildren().add(hb);
        }
        b.setAlignment(Pos.CENTER);
        b.setPadding(new Insets(0, 0, 0, 10));
        b.setPrefSize(150, 100);
        HBox.setHgrow(b, Priority.ALWAYS);
        VBox pane = new VBox(5, b, okButton);
        pane.setAlignment(Pos.CENTER);
        pane.setOnMouseClicked((event) -> {
            Move(stage, event, pane);
        });
        pane.setStyle("-fx-background-color: white;-fx-background-radius:3%");
        b.setPrefSize(250, 320);
        Scene scene = new Scene(pane, Color.TRANSPARENT);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public static void LoadPlaylist(HashMap<Integer, Song> songs) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(PLAYLISTFILE));
            String value;
            while ((value = reader.readLine()) != null) {
                String[] data = value.split("@");
                int key = Integer.parseInt(data[0]);
                String[] info = data[1].split("#");
                Song song = new Song(new Media(info[0]), info[1]);
                songs.put(key, song);
            }
            reader.close();
        } catch (IOException e) {
            Message(e.getMessage());
        }

    }

    public static void SavePlaylist(HashMap<Integer, Song> songs) {
        if (!songs.isEmpty()) {
            String[] strings = new String[songs.size()];
            songs.entrySet().forEach((song) -> {
                Integer i = song.getKey();
                Song s = song.getValue();
                strings[i] = i + "@" + s.toString();
            });
            WriteFile(PLAYLISTFILE, strings, false);
            Message("playlist saved");
        } else {
            Message("No song list found");
        }
    }

    /*public static void SavePlaylist(HashMap<Integer, Song> songs) {
        if (!songs.isEmpty()) {
            String[] strings = new String[songs.size()];
            for (int i = 0; i < strings.length; i++) {
                Song song = songs.get(i);
                strings[i] = i + "@" + song.toString();
            }
            WriteFile(PLAYLISTFILE, strings, false);
            Message("playlist saved");
        } else {
            Message("No song list found");
        }
    }*/
    public static void WriteFile(File file, String[] strings, boolean append) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file, append));
            for (String string : strings) {
                writer.println(string);
            }
            writer.close();
        } catch (IOException ex) {
            Message(ex.getMessage());
        }
    }

    public static void DeletePlaylist() {
        if (PLAYLISTFILE.exists()) {
            PLAYLISTFILE.delete();
            Message("Playlist Deleted Successful");
        } else {
            Message("No Saved Playlist Found");
        }

    }

    public static File FileFixer(File file) {
        String f = file.toString().substring(6);
        f = f.replace("\\", "\\\\");
        f = f.replace("%20", " ");
        File result = new File(f);
        return result;
    }

}
