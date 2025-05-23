package com.example.toplutasimaprojesi;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
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
    private ListView<String> rotaListView;

    @FXML
    private ListView<String> hatDuraklariListView;

    @FXML
    private TextField durakAramaText;

    @FXML
    private ListView<String> durakAramaListView;

    @FXML
    private FlowPane hatButtonsPane;

    @FXML
    private Button btnrotaTemizle;

    @FXML private Button btnTumHatlar;
    @FXML private Button btnM4;
    @FXML private Button btnM5;
    @FXML private Button btnM8;
    @FXML private Button btnMarmaray;


    // Aktif olarak seçilen hat
    private String selectedLine = "ALL";

    private WebEngine webEngine;
    private MetroAgi metroAgi;
    private HashMap<String, String> hatRenkleri;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Metro ağını oluştur
        metroAgi = MetroAgi.getInstance();
        hatRenkleriOlustur();
        metroHatlariniOlustur();


        // WebView ayarları
        webEngine = mapView.getEngine();
        webEngine.load(getClass().getResource("/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState.toString().equals("SUCCEEDED")) {
                initJavaScriptBridge();
                Platform.runLater(this::haritayiDoldur);
            }
        });

        // Butonların tıklama olaylarını başlat
        butonlariBaslat();

        // ComboBox ve ListView ayarları
        durakComboBoxlariniDoldur();
    }

    // Butonların tıklama olaylarını başlat
    private void butonlariBaslat() {
        btnTumHatlar.setOnAction(e -> hatSecAction("ALL"));
        btnM4.setOnAction(e -> hatSecAction("M4"));
        btnM5.setOnAction(e -> hatSecAction("M5"));
        btnM8.setOnAction(e -> hatSecAction("M8"));
        btnMarmaray.setOnAction(e -> hatSecAction("Marmaray"));
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
            // Parametreleri doğru sırada gönder: isim, enlem (xKoordinat), boylam (yKoordinat), aktarma
            webEngine.executeScript("addStation('" +
                    durak.getIsim() + "', " +
                    durak.getXKoordinat() + ", " +  // Enlem (xKoordinat) - lat parametresi
                    durak.getYKoordinat() + ", " +  // Boylam (yKoordinat) - lon parametresi
                    durak.isAktarmaNoktasi() + ")");
        }

        // Hatları duraklar ile bağla - ArrayList kullanımı
        for (int i = 0; i < metroAgi.getHatSayisi(); i++) {
            Hat hat = metroAgi.getHatIndex(i);
            String hatIsmi = hat.getIsim();

            // ArrayList kullanarak durakları al
            ArrayList<Durak> duraklar = hat.getDuraklar();
            for (Durak durak : duraklar) {
                String durakIsmi = durak.getIsim();
                webEngine.executeScript("addStationToLine('" + hatIsmi + "', '" + durakIsmi + "')");
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
        hatRenkleri.put("Marmaray", "#0075C9"); // Deniz Mavisi
    }

    // Örnek metro hatlarını oluştur
    private void metroHatlariniOlustur() {
        {
            String[] marmarayDuraklari = {
                    "Gebze", "Darıca", "Osmangazi", "GTÜ – Fatih", "Cayırova", "Tuzla",
                    "İçmeler", "Aydıntepe", "Güzelyalı", "Tersane", "Kaynarca", "Pendik",
                    "Yunus", "Kartal", "Başak", "Atalar", "Cevizli", "Maltepe",
                    "Süreyya Plajı", "İdealtepe", "Küçükyalı", "Bostancı", "Suadiye",
                    "Erenköy", "Göztepe", "Feneryolu", "Söğütlüçeşme", "Ayrılık Çeşmesi",
                    "Üsküdar", "Sirkeci", "Yenikapı", "Kazlıçeşme",
                    "Zeytinburnu", "Yenimahalle",
                    "Bakırköy", "Ataköy", "Yeşilyurt", "Yeşilköy",
                    "Florya Akvaryum", "Florya", "Küçükçekmece", "Mustafa Kemal", "Halkalı"
            };
            double[][] marmarayKoordinatlari = {
                    {40.783559, 29.410139},
                    {40.791758, 29.391817},
                    {40.799043, 29.379582},
                    {40.807369, 29.363852},
                    {40.810286, 29.347198},
                    {40.829902, 29.322054},
                    {40.845887, 29.300290},
                    {40.852059, 29.293235},
                    {40.856890, 29.283813},
                    {40.860869, 29.273218},
                    {40.871203, 29.255940},
                    {40.880126, 29.231807},
                    {40.884327, 29.210464},
                    {40.888480, 29.191264},
                    {40.890227, 29.177175},
                    {40.898484, 29.169089},
                    {40.910345, 29.155303},
                    {40.920548, 29.133376},
                    {40.926346, 29.123875},
                    {40.937347, 29.114235},
                    {40.945725, 29.107419},
                    {40.953451, 29.095779},
                    {40.960152, 29.084483},
                    {40.971543, 29.075945},
                    {40.978912, 29.062682},
                    {40.978606, 29.048840},
                    {40.990744, 29.037569},
                    {41.000159, 29.029768},
                    {41.025386, 29.014821},
                    {41.013338, 28.976886},
                    {41.005413, 28.951358},
                    {40.992331, 28.916747},
                    {40.985834, 28.905923},
                    {40.981484, 28.880954},
                    {40.980137, 28.872699},
                    {40.980014, 28.856240},
                    {40.965290, 28.837424},
                    {40.962359, 28.824569},
                    {40.967547, 28.796528},
                    {40.972647, 28.787696},
                    {40.988512, 28.772505},
                    {41.005926, 28.774014},
                    {41.018633, 28.767911}
            };

            int[] marmaraySureleri = {
                    3, 2, 3, 2, 4, 3, 2, 2, 2, 3, 4, 3, 2, 2, 2, 3, 3, 2, 2, 2,
                    3, 2, 2, 2, 2, 3, 2, 4, 5, 4, 3, 2, 4, 2, 3, 2, 3, 3, 2, 3, 3, 4
            };

            metroAgi.hatOlustur("Marmaray", marmarayDuraklari, marmarayKoordinatlari, marmaraySureleri);

            String[] m4Duraklari = {
                    "Sabiha Gökçen", "Kurtköy", "Yayalar", "Fevzi Çakmak",
                    "Tavşantepe", "PendikM4", "Yakacık", "KartalM4", "Soğanlık",
                    "Hastane-Adliye", "Esenkent", "Gülsuyu", "Huzurevi",
                    "MaltepeM4", "KüçükyalıM4", "BostancıM4", "Kozyatağı",
                    "Yenisahra", "GöztepeM4", "Ünalan", "Acıbadem",
                    "Ayrılık Çeşmesi", "Kadıköy"
            };

            double[][] m4Koordinatlari = {
                    {40.906413, 29.310922},  // Sabiha Gökçen
                    {40.910116, 29.296293},  // Kurtköy
                    {40.904026, 29.275226},  // Yayalar
                    {40.889170, 29.262424},  // Fevzi Çakmak
                    {40.882238, 29.248492},  // Tavşantepe
                    {40.888629, 29.238392},  // Pendik
                    {40.896600, 29.226837},  // Yakacık
                    {40.906265, 29.211318},  // Kartal
                    {40.912789, 29.192750},  // Soğanlık
                    {40.916248, 29.178469},  // Hastane-Adliye
                    {40.920515, 29.166207},  // Esenkent
                    {40.923787, 29.154489},  // Gülsuyu
                    {40.929813, 29.146631},  // Huzurevi
                    {40.936024, 29.139113},  // MaltepeM4
                    {40.948938, 29.122185},  // Küçükyalı
                    {40.964719, 29.105275},  // Bostancı
                    {40.975784, 29.098797},  // Kozyatağı
                    {40.984686, 29.090444},  // Yenisahra
                    {40.993770, 29.071245},  // Göztepe
                    {40.998110, 29.060525},  // Ünalan
                    {41.002014, 29.045396},  // Acıbadem
                    {41.000381, 29.030075},  // Ayrılık Çeşmesi
                    {40.990530, 29.022147}   // Kadıköy
            };

            int[] m4Sureleri = {
                    3, 3, 3, 2, 2, 2, 3, 2, 3, 2, 2, 2, 2, 3, 3, 2, 2, 3, 2, 3, 3, 2
            };

            metroAgi.hatOlustur("M4", m4Duraklari, m4Koordinatlari, m4Sureleri);

            String[] m8Duraklari = {
                    "Bostancı",
                    "Emin Ali Paşa",
                    "Ayşekadın",
                    "Kozyatağı",
                    "Küçükbakkalköy",
                    "İçerenköy",
                    "Kayışdağı",
                    "Mevlana",
                    "İMES",
                    "MODOKO-KEYAP",
                    "Dudullu",
                    "Huzur",
                    "Parseller"
            };

            double[][] m8Koordinatlari = {
                    {40.953451, 29.095779},  // Bostancı
                    {40.960600, 29.093885},  // Emin Ali Paşa
                    {40.967009, 29.086917},  // Ayşekadın
                    {40.975784, 29.098797},  // Kozyatağı
                    {40.978809, 29.111555},  // Küçükbakkalköy
                    {40.979158, 29.126216},  // İçerenköy
                    {40.984635, 29.137775},  // Kayışdağı
                    {40.992225, 29.153342},  // Mevlana
                    {41.000271, 29.156067},  // İMES
                    {41.007514, 29.162259},  // MODOKO-KEYAP
                    {41.015608, 29.163382},  // Dudullu
                    {41.022572, 29.159687},  // Huzur
                    {41.031239, 29.152671}   // Parseller
            };

            int[] m8Sureleri = {
                    2, 2, 3, 2, 3, 3, 3, 2, 2, 2, 2, 2
            };

            metroAgi.hatOlustur("M8", m8Duraklari, m8Koordinatlari, m8Sureleri);

            String[] m5Duraklari = {
                    "Üsküdar",
                    "Fıstıkağacı",
                    "Bağlarbaşı",
                    "Altunizade",
                    "Kısıklı",
                    "Bulgurlu",
                    "Ümraniye",
                    "Çarşı",
                    "Yamanevler",
                    "Çakmak",
                    "Ihlamurkuyu",
                    "Altınşehir",
                    "İmam Hatip Lisesi",
                    "Dudullu",
                    "Necip Fazıl",
                    "Çekmeköy",
                    "Meclis",
                    "Sarıgazi",
                    "Sancaktepe Şehir Hastanesi",
                    "Sancaktepe",
                    "Samandıra Merkez"
            };

            double[][] m5Koordinatlari = {
                    {41.025386, 29.014821},  // Üsküdar
                    {41.027987, 29.028540},  // Fıstıkağacı
                    {41.021991, 29.035892},  // Bağlarbaşı
                    {41.021715, 29.048406},  // Altunizade
                    {41.022287, 29.062130},  // Kısıklı
                    {41.016355, 29.076221},  // Bulgurlu
                    {41.024708, 29.084792},  // Ümraniye
                    {41.026026, 29.097201},  // Çarşı
                    {41.024266, 29.108986},  // Yamanevler
                    {41.021482, 29.118280},  // Çakmak
                    {41.019260, 29.131191},  // Ihlamurkuyu
                    {41.016583, 29.140074},  // Altınşehir
                    {41.016084, 29.151892},  // İmam Hatip Lisesi
                    {41.015608, 29.163382},  // Dudullu
                    {41.016208, 29.179181},  // Necip Fazıl
                    {41.014494, 29.188681},  // Çekmeköy
                    {41.009596, 29.199071},  // Meclis
                    {41.010283, 29.211868},  // Sarıgazi
                    {41.002071, 29.216860},  // Sancaktepe Şehir Hastanesi
                    {40.991898, 29.228907},  // Sancaktepe
                    {40.983305, 29.230839}   // Samandıra Merkez
            };

            int[] m5Sureleri = {
                    2, 2, 3, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 3, 3, 2
            };

            metroAgi.hatOlustur("M5", m5Duraklari, m5Koordinatlari, m5Sureleri);
        }
    }
    // Hat seçildiğinde çağrılacak metot
    private void hatSecAction(String hatIsmi) {
        selectedLine = hatIsmi;

        // JavaScript'te sadece seçilen hattı göster
        webEngine.executeScript("showOnlyLine('" + hatIsmi + "')");

        // Seçilen hattın duraklarını göster (eğer "ALL" değilse)
        if (!hatIsmi.equals("ALL")) {
            hatDuraklariniGoster(hatIsmi);
        } else {
            // Tüm hatlar seçildiğinde durak listesini temizle
            hatDuraklariListView.setItems(FXCollections.observableArrayList());
        }
    }

    // Durak ComboBox'larını doldur
    private void durakComboBoxlariniDoldur() {
        ObservableList<String> duraklar = FXCollections.observableArrayList();

        for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
            duraklar.add(metroAgi.getDurakIndex(i).getIsim());
        }

        baslangicCombo.setItems(duraklar);
        bitisCombo.setItems(duraklar);
    }

    // Hat durakları göster - ArrayList kullanımı
    private void hatDuraklariniGoster(String hatIsmi) {
        Hat hat = metroAgi.hatBul(hatIsmi);
        ObservableList<String> duraklar = FXCollections.observableArrayList();

        if (hat != null) {
            ArrayList<Durak> hatDuraklari = hat.getDuraklar();

            for (int i = 0; i < hatDuraklari.size(); i++) {
                Durak durak = hatDuraklari.get(i);
                String durakBilgi = (i + 1) + ". " + durak.getIsim() +
                        (durak.isAktarmaNoktasi() ? " (Aktarma Noktası)" : "");
                duraklar.add(durakBilgi);
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

        // Rota çizildikten sonra, tüm hatları göstermeye geç
        webEngine.executeScript("showOnlyLine('ALL')");
        selectedLine = "ALL";
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
    @FXML
    private void btnrotaTemizleButtonAction(ActionEvent event) {
        // UI temizle
        rotaListView.getItems().clear();
        baslangicCombo.getSelectionModel().clearSelection();
        bitisCombo.getSelectionModel().clearSelection();

        // Harita temizle
        webEngine.executeScript("clearRoute()");
        webEngine.executeScript("showOnlyLine('ALL')");
    }

    // JavaScript'ten çağrılabilecek metotlar
    public void logFromJS(String message) {
        System.out.println("JavaScript Log: " + message);
    }
}