
import javafx.animation.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.stage.*;
import javafx.util.Duration;

public class SMusic extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root1 = FXMLLoader.load(getClass().getResource("splash.fxml"));
        Parent root2 = FXMLLoader.load(getClass().getResource("app.fxml"));
        root2.getStylesheets().add(getClass().getResource("style.css").toString());
        Scene scene1 = new Scene(root1, 330, 580, Color.TRANSPARENT);
        Scene scene2 = new Scene(root2, 330, 580, Color.TRANSPARENT);
        Image logo = new Image(getClass().getResource("Images/icon.jpg").toString());
        stage.getIcons().add(logo);
        stage.setScene(scene1);
        stage.centerOnScreen();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setTitle("SRZ_MUSIC");
        stage.show();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            stage.setScene(scene2);
        }));
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
