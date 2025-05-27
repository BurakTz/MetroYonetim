package com.example.toplutasimaprojesi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Karşılama ekranını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/karsilama_ekrani.fxml"));
            Parent root = loader.load();

            // ✅ DOĞRU: KarsilamaEkraniController olarak cast et
            KarsilamaEkraniController controller = loader.getController();
            controller.setMainStage(stage);

            // Sahne oluşturma
            Scene scene = new Scene(root);

            // CSS ekleme
            try {
                URL cssUrl = getClass().getResource("/styles.css");
                if (cssUrl != null) {
                    String cssString = cssUrl.toExternalForm();
                    System.out.println("CSS URL: " + cssString);
                    scene.getStylesheets().add(cssString);
                } else {
                    System.out.println("CSS dosyası bulunamadı!");
                }
            } catch (Exception e) {
                System.out.println("CSS yüklenirken hata: " + e.getMessage());
            }
            stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/icon.png")));


            // Sahneyi göster
            stage.setTitle("Rotala");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}