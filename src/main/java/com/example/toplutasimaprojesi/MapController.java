package com.example.toplutasimaprojesi;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class MapController implements Initializable {

    @FXML
    private WebView mapView;

    @FXML
    private ComboBox<String> baslangicCombo;

    @FXML
    private ComboBox<String> bitisCombo;

    @FXML
    private ComboBox<String> hatlarCombo;

    @FXML
    private ListView<String> rotaListView;

    @FXML
    private ListView<String> hatDuraklariListView;

    @FXML
    private TextField durakAramaText;

    @FXML
    private ListView<String> durakAramaListView;

    private WebEngine webEngine;
    private MetroAgi metroAgi;
    private HashMap<String, String> hatRenkleri;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Metro ağını oluştur
        metroAgi = MetroAgi.getInstance();
        hatRenkleriOlustur();
        metroHatlariniOlustur();
        metroAgi.aktarmaNoktalariniIsaretle();

        // WebView ayarları
        webEngine = mapView.getEngine();
        webEngine.load(getClass().getResource("/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState.toString().equals("SUCCEEDED")) {
                initJavaScriptBridge();
                Platform.runLater(this::haritayiDoldur);
            }
        });

        // ComboBox ve ListView ayarları
        durakComboBoxlariniDoldur();
        hatComboBoxDoldur();

        hatlarCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                hatDuraklariniGoster(newVal);
            }
        });
    }

    // JavaScript köprüsünü ayarla
    private void initJavaScriptBridge() {
        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("javaConnector", this);
    }

    // Haritayı durak ve hatlarla doldur
    private void haritayiDoldur() {
        // Tüm hatları ekle
        for (int i = 0; i < metroAgi.getHatSayisi(); i++) {
            Hat hat = metroAgi.getHatIndex(i);
            String hatIsmi = hat.getIsim();
            String renk = hatRenkleri.getOrDefault(hatIsmi, "#3388ff");
            webEngine.executeScript("addLine('" + hatIsmi + "', '" + renk + "')");
        }

        // Tüm durakları ekle
        for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
            Durak durak = metroAgi.getDurakIndex(i);
            webEngine.executeScript("addStation('" +
                    durak.getIsim() + "', " +
                    durak.getXKoordinat() + ", " +
                    durak.getYKoordinat() + ", " +
                    durak.isAktarmaNoktasi() + ")");
        }

        // Hatları duraklar ile bağla
        for (int i = 0; i < metroAgi.getHatSayisi(); i++) {
            Hat hat = metroAgi.getHatIndex(i);
            String hatIsmi = hat.getIsim();

            DurakHat durakHat = hat.getBaslangic();
            while (durakHat != null) {
                String durakIsmi = durakHat.getDurak().getIsim();
                webEngine.executeScript("addStationToLine('" + hatIsmi + "', '" + durakIsmi + "')");
                durakHat = durakHat.sonraki;
            }
        }

        // Haritayı merkeze al (ilk durağın konumuna)
        if (metroAgi.getDurakSayisi() > 0) {
            Durak ilkDurak = metroAgi.getDurakIndex(0);
            webEngine.executeScript("centerMap(" + ilkDurak.getXKoordinat() +
                    ", " + ilkDurak.getYKoordinat() + ", 12)");
        }
    }

    // Hat renkleri için HashMap oluştur
    private void hatRenkleriOlustur() {
        hatRenkleri = new HashMap<>();
        hatRenkleri.put("M1", "#E32017"); // Kırmızı
        hatRenkleri.put("M2", "#00853F"); // Yeşil
        hatRenkleri.put("M3", "#0098D4"); // Mavi
        hatRenkleri.put("M4", "#9B0058"); // Mor
        hatRenkleri.put("M5", "#954F9E"); // Açık Mor
        hatRenkleri.put("M6", "#F0D77A"); // Sarı
        hatRenkleri.put("M7", "#EE7C0E"); // Turuncu
        hatRenkleri.put("M8", "#876129"); // Kahverengi
        hatRenkleri.put("M9", "#DA9100"); // Altın Sarısı
        hatRenkleri.put("M10", "#00AFAD"); // Turkuaz
    }

    // Örnek metro hatlarını oluştur
    private void metroHatlariniOlustur() {
        {
            // Marmaray
            String[] marmarayDuraklari = {
                    "Gebze", "Tuzla", "İçmeler", "Aydıntepe", "Kaynarca", "Pendik", "Güzelyalı", "Maltepe",
                    "Söğütlüçeşme", "Ayrılık Çeşmesi", "Üsküdar", "Sirkeci", "Yenikapı", "Bakırköy", "Halkalı"
            };

            double[][] marmarayKoordinatlari = {
                    {40.797, 29.430}, {40.838, 29.330}, {40.853, 29.290}, {40.860, 29.270}, {40.870, 29.250},
                    {40.880, 29.230}, {40.890, 29.210}, {40.950, 29.170}, {40.985, 29.065}, {40.998, 29.040},
                    {41.025, 29.015}, {41.015, 28.980}, {41.012, 28.950}, {40.980, 28.870}, {41.035, 28.800}
            };

            metroAgi.hatOlustur("Marmaray", marmarayDuraklari, marmarayKoordinatlari);

            // M4 Hattı: Kadıköy - Sabiha Gökçen
            String[] m4Duraklari = {
                    "Kadıköy", "Ayrılık Çeşmesi", "Acıbadem", "Ünalan", "Göztepe", "Yenisahra", "Kozyatağı",
                    "Bostancı", "Küçükyalı", "Maltepe", "Huzurevi", "Gülsuyu", "Esenkent", "Hastane-Adliye",
                    "Soğanlık", "Kartal", "Yakacık-Adnan Kahveci", "Pendik", "Tavşantepe", "Sabiha Gökçen"
            };

            double[][] m4Koordinatlari = {
                    {40.991, 29.027}, {40.998, 29.040}, {41.005, 29.055}, {41.008, 29.075}, {41.012, 29.095},
                    {41.017, 29.110}, {41.025, 29.125}, {41.035, 29.135}, {41.045, 29.150}, {41.055, 29.165},
                    {41.060, 29.180}, {41.065, 29.190}, {41.070, 29.200}, {41.075, 29.210}, {41.080, 29.220},
                    {41.085, 29.230}, {41.090, 29.240}, {41.095, 29.250}, {41.100, 29.260}, {41.120, 29.310}
            };

            metroAgi.hatOlustur("M4", m4Duraklari, m4Koordinatlari);

            // M5 Hattı: Üsküdar - Çekmeköy
            String[] m5Duraklari = {
                    "Üsküdar", "Fıstıkağacı", "Bağlarbaşı", "Altunizade", "Kısıklı", "Bulgurlu",
                    "Ümraniye", "Çarşı", "Yamanevler", "Çakmak", "İMES", "Modoko", "Dudullu", "Huzur", "Çekmeköy"
            };

            double[][] m5Koordinatlari = {
                    {41.025, 29.015}, {41.028, 29.025}, {41.030, 29.035}, {41.023, 29.048}, {41.018, 29.055},
                    {41.010, 29.065}, {41.005, 29.080}, {41.000, 29.095}, {40.995, 29.105}, {40.990, 29.115},
                    {40.985, 29.125}, {40.980, 29.135}, {40.975, 29.145}, {40.970, 29.155}, {40.965, 29.165}
            };

            metroAgi.hatOlustur("M5", m5Duraklari, m5Koordinatlari);

            // M8 Hattı: Bostancı - Dudullu
            String[] m8Duraklari = {
                    "Bostancı", "Emin Ali Paşa", "Ayşekadın", "Kozyatağı", "Küçükbakkalköy",
                    "İçerenköy", "Mevlana", "İMES", "Modoko", "Dudullu"
            };

            double[][] m8Koordinatlari = {
                    {41.035, 29.135}, {40.964, 29.082}, {40.968, 29.092}, {41.025, 29.125}, {40.975, 29.112},
                    {40.978, 29.122}, {40.982, 29.132}, {40.985, 29.125}, {40.980, 29.135}, {40.975, 29.145}
            };

            metroAgi.hatOlustur("M8", m8Duraklari, m8Koordinatlari);


    }}

    // Durak ComboBox'larını doldur
    private void durakComboBoxlariniDoldur() {
        ObservableList<String> duraklar = FXCollections.observableArrayList();

        for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
            duraklar.add(metroAgi.getDurakIndex(i).getIsim());
        }

        baslangicCombo.setItems(duraklar);
        bitisCombo.setItems(duraklar);
    }

    // Hat ComboBox'ını doldur
    private void hatComboBoxDoldur() {
        ObservableList<String> hatlar = FXCollections.observableArrayList();

        for (int i = 0; i < metroAgi.getHatSayisi(); i++) {
            hatlar.add(metroAgi.getHatIndex(i).getIsim());
        }

        hatlarCombo.setItems(hatlar);
    }

    // Hat durakları göster
    private void hatDuraklariniGoster(String hatIsmi) {
        Hat hat = metroAgi.hatBul(hatIsmi);
        ObservableList<String> duraklar = FXCollections.observableArrayList();

        if (hat != null) {
            DurakHat durakHat = hat.getBaslangic();
            int sira = 1;

            while (durakHat != null) {
                Durak durak = durakHat.getDurak();
                String durakBilgi = sira + ". " + durak.getIsim() +
                        (durak.isAktarmaNoktasi() ? " (Aktarma Noktası)" : "");
                duraklar.add(durakBilgi);
                durakHat = durakHat.sonraki;
                sira++;
            }
        }

        hatDuraklariListView.setItems(duraklar);
    }

    @FXML
    private void rotaBulButtonAction(ActionEvent event) {
        String baslangic = baslangicCombo.getValue();
        String bitis = bitisCombo.getValue();

        if (baslangic == null || bitis == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("Durak Seçimi Eksik");
            alert.setContentText("Lütfen başlangıç ve bitiş duraklarını seçin.");
            alert.showAndWait();
            return;
        }

        // Listeleri oluştur
        List<String> rotaBilgileri = new ArrayList<>();
        List<Object[]> rotaKoordinatlari = new ArrayList<>();

        // MetroAgi'den rotayı bul
        metroAgi.enKisaYoluBul(baslangic, bitis, rotaBilgileri, rotaKoordinatlari);

        // Ekrana yaz
        rotaListView.setItems(FXCollections.observableArrayList(rotaBilgileri));

        // Haritaya gönder
        StringBuilder routePointsJs = new StringBuilder("[");
        for (Object[] koordinat : rotaKoordinatlari) {
            routePointsJs.append("[").append(koordinat[0]).append(",").append(koordinat[1]).append("],");
        }
        if (routePointsJs.charAt(routePointsJs.length() - 1) == ',') {
            routePointsJs.deleteCharAt(routePointsJs.length() - 1);
        }
        routePointsJs.append("]");
        webEngine.executeScript("showRoute(" + routePointsJs + ")");
    }


    @FXML
    private void durakAraButtonAction(ActionEvent event) {
        String aramaMetni = durakAramaText.getText().toLowerCase();

        if (aramaMetni.isEmpty()) {
            return;
        }

        ObservableList<String> sonuclar = FXCollections.observableArrayList();

        for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
            Durak durak = metroAgi.getDurakIndex(i);
            if (durak.getIsim().toLowerCase().contains(aramaMetni)) {
                String durakBilgi = durak.getIsim() +
                        (durak.isAktarmaNoktasi() ? " (Aktarma Noktası)" : "");
                sonuclar.add(durakBilgi);
            }
        }

        durakAramaListView.setItems(sonuclar);

        // Arama sonucu yoksa bilgi ver
        if (sonuclar.isEmpty()) {
            durakAramaListView.setItems(FXCollections.observableArrayList("Sonuç bulunamadı."));
        }
    }

    // JavaScript'ten çağrılabilecek metotlar
    public void logFromJS(String message) {
        System.out.println("JavaScript Log: " + message);
    }
}