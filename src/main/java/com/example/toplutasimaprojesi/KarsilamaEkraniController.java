package com.example.toplutasimaprojesi;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;

public class KarsilamaEkraniController {

    @FXML
    private Button baslayinButton;

    private Stage mainStage;

    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }

    @FXML
    private void baslayinButtonAction(ActionEvent event) {
        try {
            // Ana harita ekranını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/map.fxml"));
            Parent root = loader.load();

            // Ekran boyutunu al
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();

            // Sahne oluşturma - ekran boyutu kadar
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // CSS ekleme
            try {
                URL cssUrl = getClass().getResource("/styles.css");
                if (cssUrl != null) {
                    String cssString = cssUrl.toExternalForm();
                    scene.getStylesheets().add(cssString);
                }
            } catch (Exception e) {
                System.out.println("CSS yüklenirken hata: " + e.getMessage());
            }

            // Sahneyi göster
            mainStage.setTitle("Rotala");
            mainStage.setScene(scene);

            // Ekranı tamamen doldur ama tam ekran modu olmasın
            mainStage.setX(screenBounds.getMinX());
            mainStage.setY(screenBounds.getMinY());
            mainStage.setWidth(screenBounds.getWidth());
            mainStage.setHeight(screenBounds.getHeight());

            mainStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}