package id.ac.ukdw.rplbo.wacanalibrary;

import id.ac.ukdw.rplbo.wacanalibrary.utils.DatabaseHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Inisialisasi semua tabel database SQLite saat aplikasi mulai
        DatabaseHelper.initializeDatabase();

        // Mengubah awal mulai ke layar Login
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/Homepage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Wacana Library - Login Sistem");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
