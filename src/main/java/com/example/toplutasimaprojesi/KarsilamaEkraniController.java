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

            // Sahne oluşturma
            Scene scene = new Scene(root);

            // CSS ekleme - aynı CSS'i burada da ekliyoruz
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
            mainStage.setTitle("Toplu Taşıma Rota Planlayıcı");
            mainStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}