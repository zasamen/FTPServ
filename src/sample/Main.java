package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(Main.class.getResource("./new.fxml"));
            BorderPane root=loader.load();
            TextArea textArea=(TextArea)root.getCenter();
            FTPServerThread ftpServerThread = new FTPServerThread();
            textArea.appendText("START");
            ftpServerThread.start();
            Scene scene=new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

            /*TextArea textArea = new TextArea();
            BorderPane root = new BorderPane(textArea);
            FTPServerThread ftpServerThread = new FTPServerThread(textArea);
            ftpServerThread.start();
            primaryStage.setTitle("Hello World");
            primaryStage.setScene(new Scene(root, 500, 600));
            primaryStage.show();*/
        }catch (Exception e){
            System.err.println("1 "+e);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
