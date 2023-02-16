
import java.io.*;
import java.net.*;
import java.util.*;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

public class AppController implements Initializable {

    public static MediaPlayer mp;
    FileChooser chooser;
    public static boolean isPresent = false, isPlaylist = false, isClicked = false;
    public static HashMap<Integer, Song> songs = new HashMap<>();
    public static int volume = 100, index = 0;
    Random generator;
    BooleanProperty isShuffle = new SimpleBooleanProperty(false);
    BooleanProperty isRepeating = new SimpleBooleanProperty(false);
    String id = "Button";

    @FXML
    private Text titleTxt;
    @FXML
    private Text artistTxt;
    @FXML
    private ImageView playBtn;

    @FXML
    private HBox topBar;
    @FXML
    private Slider progress;
    @FXML
    private Text start;
    @FXML
    private Text end;

    @FXML
    private Text vText;
    @FXML
    private VBox appPanel;
    @FXML
    private VBox playlistPanel;
    @FXML
    private StackPane panel;
    private Text vTextExtra;
    @FXML
    private VBox lister;
    @FXML
    private Slider volumeSlider;
    @FXML
    private AnchorPane shufflePanel;
    @FXML

    private AnchorPane loopPanel;
    @FXML
    private Text shuffleStatus;
    @FXML
    private Text loopStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chooser = API.MusicFileChooser();
        generator = new Random();
        playlistPanel.setVisible(false);
        menuAction();
        shuffleAction();
        loopAction();
    }

    @FXML
    private void closeAction(ActionEvent event) {
        Stage stage = (Stage) panel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void playListAction(ActionEvent event) throws IOException {
        API.Help();
    }

    @FXML
    private void minimiseAction(ActionEvent event) {
        Stage stage = (Stage) panel.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void prevAction(MouseEvent event) {
        isClicked = true;
        prev();
    }

    @FXML
    private void playAction(MouseEvent event) {
        API.PlayPause(mp, progress, start, end);
        if (isPresent) {
            if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                playBtn.setImage(API.play.getImage());
            } else {
                playBtn.setImage(API.pause.getImage());
            }
        }
    }

    @FXML
    private void nextAction(MouseEvent event) {
        isClicked = true;
        next();
    }

    @FXML
    private void moveAction(MouseEvent event) {
        Stage stage = (Stage) panel.getScene().getWindow();
        API.Move(stage, event, panel);
    }

    public void play() {
        if (songs.isEmpty()) {
            return;
        }
        API.Stop(mp);
        mp = new MediaPlayer(songs.get(index).getMedia());
        isPresent = true;
        mp.setBalance(songs.get(index).getPanning());
        mp.setVolume(songs.get(index).getVolume());
        API.BindVolume(volumeSlider, mp);
        mp.setRate(songs.get(index).getRate());
        mp.play();
        playBtn.setImage(API.pause.getImage());
        API.TAI(mp, titleTxt, artistTxt);
        mp.setOnReady(() -> API.UpdateValues(progress, mp, start, end));
        mp.currentTimeProperty().addListener((observable) -> API.UpdateValues(progress, mp, start, end));
        progress.valueProperty().addListener((observable) -> {
            if (progress.isValueChanging()) {
                mp.seek(mp.getMedia().getDuration().multiply(progress.getValue() / 100.0));
            }
        });
        mp.setOnEndOfMedia(() -> next());
    }

    public void next() {
        API.StopFade(mp, volumeSlider, songs);
        if (isClicked) {
            index++;
            isClicked = false;
        } else {
            if (isShuffle.get() == false && isRepeating.get() == true) {
                index += 0;
            } else if (isShuffle.get() == true && isRepeating.get() == true) {
                index += 0;
            } else if (isShuffle.get() == true && isRepeating.get() == false) {
                int min = 0, max = (songs.size() - 1);
                index = generator.nextInt(max - min + 1) + min;
            } else if (isShuffle.get() == false && isRepeating.get() == false) {
                index++;
            }
            if (index
                    >= songs.size()) {
                index = 0;
            }
        }

        play();
    }

    public void prev() {
        API.StopFade(mp, volumeSlider, songs);
        API.StopFade(mp, volumeSlider, songs);

        if (isClicked) {
            index--;

            isClicked = false;
        } else {
            if (isShuffle.get() == false && isRepeating.get() == true) {
                index += 0;
            } else if (isShuffle.get() == true && isRepeating.get() == true) {
                index += 0;

            } else if (isShuffle.get() == true && isRepeating.get() == false) {
                int min = 0, max = (songs.size() - 1);
                index = generator.nextInt(max - min + 1) + min;
            } else if (isShuffle.get() == false && isRepeating.get() == false) {
                index--;
            }

            if (index
                    >= songs.size()) {
                index = 0;
            }
        }
        if (index < 0) {
            index = songs.size() - 1;
        }
        play();
    }

    @FXML
    private void loadAction(ActionEvent event) {
        if (API.PLAYLISTFILE.exists()) {
            if (!lister.getChildren().isEmpty()) {
                lister.getChildren().clear();
                songs.clear();
            }
            API.LoadPlaylist(songs);
            File file = null;
            for (Map.Entry<Integer, Song> entry : songs.entrySet()) {
                Integer key = entry.getKey();
                Song song = entry.getValue();
                file = new File(song.getSource());
                Button btn = new Button(song.getName());
                btn.setFocusTraversable(false);
                btn.setOnAction(er -> {
                    index = key;
                    play();
                    hide();
                });
                btn.setId(id);
                lister.getChildren().add(btn);
            }
            API.LastDir(API.FileFixer(file), chooser);
            API.Stop(mp);
            API.Reset(mp, songs);
            index = 0;
            play();
        } else {
            API.Message("NO SAVED PLAYLIST FOUND");
        }

    }

    @FXML
    private void saveAction(ActionEvent event) {
        API.SavePlaylist(songs);
    }

    @FXML
    private void dropAction(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean isSuccessful = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            final int size = songs.size();
            int num = 0;
            for (int i = 0; i < files.size(); i++) {
                for (String audio : API.AUDIOS_FORMATES) {
                    if (audio.endsWith(API.GetExtension(files.get(i)))) {
                        String path = new File(files.get(i).getAbsolutePath()).toURI().toString();
                        Media media = new Media(path);
                        String name = API.GetFileName(files.get(i), false);
                        Song song = new Song(media, name);
                        int position = size + num;
                        songs.put(position, song);
                        Button btn = new Button(name);
                        btn.setFocusTraversable(false);
                        btn.setOnAction(e -> {
                            index = position;
                            play();
                            hide();
                        });
                        btn.setId(id);
                        btn.setWrapText(true);
                        lister.getChildren().add(btn);
                        num++;
                    }
                }
            }
            isSuccessful = true;
            API.LastDir(files.get(0), chooser);
        }
        event.setDropCompleted(isSuccessful);
        event.consume();
        if (isPresent) {
            if (mp.getStatus() == MediaPlayer.Status.STOPPED) {
                index = 0;
                play();
            } else if (mp.getStatus() == MediaPlayer.Status.PAUSED) {
                mp.play();
            }
        } else {
            play();
        }
        show();
    }

    @FXML
    private void overAction(DragEvent event) {
        if (event.getGestureSource() != panel && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.LINK);
        }
        event.consume();
    }

    @FXML
    private void openAction(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (isPlaylist) {
                appPanel.setVisible(true);
                playlistPanel.setVisible(false);
            } else {
                appPanel.setVisible(false);
                playlistPanel.setVisible(true);
                Timeline line = new Timeline(new KeyFrame(Duration.seconds(30), ex -> {
                    appPanel.setVisible(true);
                    playlistPanel.setVisible(false);
                }));
                line.setOnFinished((even) -> {
                    isPlaylist = !isPlaylist;
                });
                line.play();
            }
            isPlaylist = !isPlaylist;
        }
    }

    @FXML
    private void deleteAction(ActionEvent event) {
        API.DeletePlaylist();
    }

    @FXML
    private void clearAction(ActionEvent event) {
        if (isPresent) {
            mp.stop();
            isPresent = false;
            lister.getChildren().clear();
            songs.clear();
        }

    }

    private void shuffleAction() {
        int height = 20, width = 40;
        Rectangle rec = new Rectangle(width, height, Color.WHITE);
        rec.setArcHeight(height);
        rec.setArcWidth(height);
        rec.setStroke(Color.DARKGRAY);
        rec.setStrokeWidth(1);
        Circle circle = new Circle(height / 2, height / 2, height / 2, Color.WHITE);
        circle.setStroke(Color.DARKGRAY);
        circle.setStrokeWidth(2);
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), circle);
        FillTransition fill = new FillTransition(Duration.seconds(0.5), rec, Color.WHITE, Color.LIME);
        ParallelTransition parallel = new ParallelTransition(transition, fill);
        isShuffle.addListener((observable, oldValue, newValue) -> {
            transition.setToX(newValue ? height : 0);
            fill.setToValue(newValue ? Color.LIME : Color.WHITE);
            fill.setFromValue(newValue ? Color.WHITE : Color.LIME);

            parallel.play();
            parallel.setOnFinished((event) -> {
                if (isShuffle.get() == false) {
                    shuffleStatus.setText("SHUFFLE: OFF");
                } else if (isShuffle.get() == true) {
                    shuffleStatus.setText("SHUFFLE: ON");
                }
            });
        });
        shufflePanel.setOnMouseClicked((event) -> {
            isShuffle.set(!isShuffle.get());
        });
        Text text = new Text("ON  OFF");
        text.setLayoutX(1);
        text.setLayoutY(height - 6);
        text.setFont(Font.font(10));
        shufflePanel.getChildren().add(rec);
        shufflePanel.getChildren().add(text);
        shufflePanel.getChildren().add(circle);
    }

    private void loopAction() {
        int height = 20, width = 40;
        Rectangle rec = new Rectangle(width, height, Color.WHITE);
        rec.setArcHeight(height);
        rec.setArcWidth(height);
        rec.setStroke(Color.DARKGRAY);
        rec.setStrokeWidth(1);
        Circle circle = new Circle(height / 2, height / 2, height / 2, Color.WHITE);
        circle.setStroke(Color.DARKGRAY);
        circle.setStrokeWidth(2);
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), circle);
        FillTransition fill = new FillTransition(Duration.seconds(0.5), rec, Color.WHITE, Color.LIME);
        ParallelTransition parallel = new ParallelTransition(transition, fill);
        isRepeating.addListener((observable, oldValue, newValue) -> {
            transition.setToX(newValue ? height : 0);
            fill.setToValue(newValue ? Color.LIME : Color.WHITE);
            fill.setFromValue(newValue ? Color.WHITE : Color.LIME);
            parallel.play();
        });
        loopPanel.setOnMouseClicked((event) -> {
            isRepeating.set(!isRepeating.get());
        });
        parallel.setOnFinished((event) -> {
            if (isRepeating.get() == true) {
                loopStatus.setText("LOOP_ONE_SONG: ON");
            } else if (isRepeating.get() == false) {
                loopStatus.setText("LOOP_ONE_SONG: OFF");
            }
        });
        Text text = new Text("ON  OFF");
        text.setLayoutX(1);
        text.setLayoutY(height - 6);
        text.setFont(Font.font(10));
        loopPanel.getChildren().add(rec);
        loopPanel.getChildren().add(text);
        loopPanel.getChildren().add(circle);
    }

    private void menuAction() {
        MenuBar bar = new MenuBar();
        bar.setStyle("-fx-background-color:transparent;");
        Menu menu = new Menu();
        bar.getMenus().add(menu);
        MenuItem[] items = new MenuItem[API.shotcuts.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = new MenuItem();
            items[i].setAccelerator(KeyCombination.keyCombination(API.shotcuts[i]));
        }
        menu.getItems().addAll(items);
        topBar.getChildren().add(bar);
        items[0].setOnAction((event) -> {
            Stage window = (Stage) panel.getScene().getWindow();
            List<File> files = chooser.showOpenMultipleDialog(window);
            if (files != null) {
                if (!lister.getChildren().isEmpty()) {
                    lister.getChildren().clear();
                    songs.clear();
                }
                for (int i = 0; i < files.size(); i++) {
                    String path = new File(files.get(i).getAbsolutePath()).toURI().toString();
                    Media media = new Media(path);
                    String name = API.GetFileName(files.get(i), false);
                    Song song = new Song(media, name);
                    songs.put(i, song);
                    Button btn = new Button(name);
                    btn.setFocusTraversable(false);
                    final int x = i;
                    btn.setOnAction(e -> {
                        index = x;
                        play();
                        hide();
                    });
                    btn.setId(id);
                    btn.setWrapText(true);
                    lister.getChildren().add(btn);
                }
                API.LastDir(files.get(0), chooser);
                API.Stop(mp);
                API.Reset(mp, songs);
                index = 0;
                play();
            }
        });
        items[1].setOnAction(e -> {
            API.PlayPause(mp, progress, start, end);
            if (isPresent) {
                if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                    playBtn.setImage(API.play.getImage());
                } else {
                    playBtn.setImage(API.pause.getImage());
                }
            }
        });
        items[2].setOnAction(e -> API.Stop(mp));
        items[3].setOnAction(e -> API.Reset(mp, songs));
        items[4].setOnAction(e -> API.Mute(mp, songs));
        items[5].setOnAction(e -> API.PanLeft(mp, songs));
        items[6].setOnAction(e -> API.PanRight(mp, songs));
        items[7].setOnAction(e -> API.RateSlowAuto(mp, songs));
        items[8].setOnAction(e -> API.RateFastAuto(mp, songs));
        items[9].setOnAction(e -> {
            volume += 1;
            if (volume > 100) {
                volume = 100;
            }
            vText.setText(volume + "");
            vTextExtra.setText(volume + "");
            API.VolumeUp(mp, volumeSlider, songs);
        });
        items[10].setOnAction(e -> {
            volume -= 1;
            if (volume < 0) {
                volume = 0;
            }
            vText.setText(volume + "");
            vTextExtra.setText(volume + "");
            API.VolumeDown(mp, volumeSlider, songs);
        });
        items[11].setOnAction(e -> isRepeating.set(!isRepeating.get()));
        items[12].setOnAction(e -> {
            if (API.PLAYLISTFILE.exists()) {
                if (!lister.getChildren().isEmpty()) {
                    lister.getChildren().clear();
                    songs.clear();
                }
                API.LoadPlaylist(songs);
                File file = null;
                for (Map.Entry<Integer, Song> entry : songs.entrySet()) {
                    Integer key = entry.getKey();
                    Song song = entry.getValue();
                    file = new File(song.getSource());
                    Button btn = new Button(song.getName());
                    btn.setFocusTraversable(false);
                    btn.setOnAction(er -> {
                        index = key;
                        play();
                        hide();
                    });
                    btn.setId(id);
                    lister.getChildren().add(btn);
                }
                API.Stop(mp);
                API.LastDir(API.FileFixer(file), chooser);
                API.Reset(mp, songs);
                index = 0;
                play();
            } else {
                API.Message("NO SAVED PLAYLIST FOUND");
            }
        });
        items[13].setOnAction(e -> {
            API.SavePlaylist(songs);
        });
        items[14].setOnAction(e -> {
            API.DeletePlaylist();
        });
        items[15].setOnAction(e -> {
            if (isPresent) {
                mp.stop();
                isPresent = false;
                lister.getChildren().clear();
                songs.clear();
            } else {
                API.Message("NO  PLAYLIST FOUND TO CLEAR");
            }
        });
        items[16].setOnAction(e -> isShuffle.set(!isShuffle.get()));
        progress.setOnMouseClicked(e -> {
            if (isPresent) {
                if (mp.getStatus() == MediaPlayer.Status.PLAYING) {
                    API.Pause(mp);
                    API.ClickSeek(progress, e);
                    API.Play(mp);
                } else {
                    API.ClickSeek(progress, e);
                }
            }
        });

    }

    private void show() {
        isPlaylist = true;
        appPanel.setVisible(false);
        playlistPanel.setVisible(true);
        Timeline line = new Timeline(new KeyFrame(Duration.seconds(30), ex -> {
            appPanel.setVisible(true);
            playlistPanel.setVisible(false);
        }));
        line.setOnFinished((event) -> {
            isPlaylist = !isPlaylist;
        });
        line.play();
    }

    @FXML
    private void appendAction(ActionEvent event) {
        Stage window = (Stage) panel.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);
        if (files != null) {
            final int size = songs.size();
            for (int i = 0; i < files.size(); i++) {
                String path = new File(files.get(i).getAbsolutePath()).toURI().toString();
                Media media = new Media(path);
                String name = API.GetFileName(files.get(i), false);
                Song song = new Song(media, name);
                int position = size + i;
                songs.put(position, song);
                Button btn = new Button(name);
                btn.setFocusTraversable(false);
                btn.setOnAction(e -> {
                    index = position;
                    play();
                });
                btn.setId(id);
                btn.setWrapText(true);
                lister.getChildren().add(btn);
            }
            API.LastDir(files.get(0), chooser);
            if (isPresent) {

            } else {
                play();
            }
        }
    }

    private void hide() {
        isPlaylist = false;
        appPanel.setVisible(true);
        playlistPanel.setVisible(false);
    }
}
