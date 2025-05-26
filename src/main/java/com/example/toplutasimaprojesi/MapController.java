package com.example.toplutasimaprojesi;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.time.LocalTime;

public class MapController implements Initializable {

    @FXML
    private WebView mapView;

    // YENİ: TextField ve ListView'lar ComboBox yerine
    @FXML
    private TextField baslangicTextField;

    @FXML
    private ListView<String> baslangicListView;

    @FXML
    private TextField bitisTextField;

    @FXML
    private ListView<String> bitisListView;

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
    @FXML private Button rotaBulButton;

    // Ara duraklar için yeni alanlar
    @FXML
    private VBox araDuraklarContainer;

    @FXML
    private Button durakEkleButton;

    @FXML
    private ScrollPane araDurakScrollPane;

    @FXML
    private Label araDurakSayisiLabel;

    @FXML
    private Label rotaUzunlukLabel;

    @FXML
    private Label rotaSureLabel;

    @FXML
    private Label rotaHatlarLabel;

    @FXML
    private TextField yolcuSaatText;

    @FXML
    private TextField yolcuDakikaText;

    @FXML
    private CheckBox simdiCheckBox;



    // Aktif olarak seçilen hat
    private String selectedLine = "ALL";

    // YENİ: Rota görünürlük kontrolü
    private boolean isRouteVisible = false;

    private WebEngine webEngine;
    private MetroAgi metroAgi;
    private HashMap<String, String> hatRenkleri;

    // YENİ: Hash table ve utility fonksiyonları
    private PrimeHashTable primeHashTable;
    private int[] primeSayilar = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101};

    // Ara duraklar için
    private List<AraDurakBileseni> araDuraklar = new ArrayList<>();
    private int araDurakSayaci = 1;

    // Inner class - AraDurakBileseni
    private static class AraDurakBileseni {
        TextField textField;
        ListView<String> listView;
        VBox container;
        Button silButton;
        int durakNo;

        AraDurakBileseni(int no) {
            this.durakNo = no;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialize başlıyor...");

            // Metro ağını oluştur
            metroAgi = MetroAgi.getInstance();
            hatRenkleriOlustur();
            metroHatlariniOlustur();
            System.out.println("Metro ağı oluşturuldu");

            // WebView ayarları
            webEngine = mapView.getEngine();
            webEngine.load(getClass().getResource("/map.html").toExternalForm());
            System.out.println("WebView yüklendi");

            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState.toString().equals("SUCCEEDED")) {
                    initJavaScriptBridge();
                    Platform.runLater(this::haritayiDoldur);
                }
            });

            // Butonların tıklama olaylarını başlat
            butonlariBaslat();
            System.out.println("Butonlar başlatıldı");

            // YENİ: Hash table hazırla
            System.out.println("Hash table hazırlanıyor...");
            hashTableHazirla();
            System.out.println("Hash table hazırlandı");

            // YENİ: TextField listener'ları kur
            System.out.println("TextField listener'ları kuruluyor...");
            textFieldListenersKur();
            System.out.println("TextField listener'ları kuruldu");

            beklemeSistemi = new MetroBeklemeSistemi(this);

            // Ara durak sayısı label'ını başlat
            araDurakSayisiniGuncelle();

            if (rotaUzunlukLabel != null) rotaUzunlukLabel.setText("🚇 Toplam Durak: -");
            if (rotaSureLabel != null) rotaSureLabel.setText("⏱️ Tahmini Süre: -");
            if (rotaHatlarLabel != null) rotaHatlarLabel.setText("🚊 Kullanılan Hatlar: -");

            System.out.println("Initialize tamamlandı!");

        } catch (Exception e) {
            System.out.println("HATA: Initialize'da exception!");
            e.printStackTrace();
        }
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

            ArrayList<Durak> duraklar = hat.getDuraklar();
            for (Durak durak : duraklar) {
                String durakIsmi = durak.getIsim();
                webEngine.executeScript("addStationToLine('" + hatIsmi + "', '" + durakIsmi + "')");
            }
        }

        // Haritayı merkeze al
        if (metroAgi.getDurakSayisi() > 0) {
            Durak ilkDurak = metroAgi.getDurakIndex(0);
            webEngine.executeScript("centerMap(" + ilkDurak.getXKoordinat() +
                    ", " + ilkDurak.getYKoordinat() + ", 12)");
        }
    }

    // Hat renkleri için HashMap oluştur
    private void hatRenkleriOlustur() {
        hatRenkleri = new HashMap<>();
        hatRenkleri.put("M1", "#E32017");
        hatRenkleri.put("M2", "#00853F");
        hatRenkleri.put("M3", "#0098D4");
        hatRenkleri.put("M4", "#9B0058");
        hatRenkleri.put("M5", "#954F9E");
        hatRenkleri.put("M6", "#F0D77A");
        hatRenkleri.put("M7", "#EE7C0E");
        hatRenkleri.put("M8", "#876129");
        hatRenkleri.put("M9", "#DA9100");
        hatRenkleri.put("M10", "#00AFAD");
        hatRenkleri.put("Marmaray", "#0075C9");
    }
    private String[] marmarayDuraklari;
    private int[] marmaraykeys;
    private String[] m4Duraklari;
    private int[] m4keys;
    private String[] m8Duraklari;
    private int[] m8keys;
    private String[] m5Duraklari;
    private int[] m5keys;

    // Metro hatlarını oluştur
    private void metroHatlariniOlustur() {
        {
            marmarayDuraklari =new String[] {
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

            marmaraykeys =new int[] {
                    5, 8, 10, 12, 14, 17, 20, 22, 25, 27, 30, 33, 35, 37, 39, 42, 46, 48, 51, 53,
                    55, 57, 60, 62, 64, 67, 69, 71, 73, 76, 79, 82, 85, 88, 90, 92, 94, 96, 99, 102, 104, 107,109
            };


            metroAgi.hatOlustur("Marmaray", marmarayDuraklari, marmarayKoordinatlari, marmaraykeys);

            m4Duraklari =new String[] {
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

            m4keys =new int[] {
                     22, 25, 28, 31, 33, 35, 37, 40, 42, 45, 47, 49, 51, 53, 56, 59, 61, 63, 65, 67, 69, 71, 75
            };

            metroAgi.hatOlustur("M4", m4Duraklari, m4Koordinatlari, m4keys);

            m8Duraklari =new String[] {
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

            m8keys =new int[] {
                    53, 55, 56, 59, 61, 64, 67, 69, 71, 73, 75, 77
            };

            metroAgi.hatOlustur("M8", m8Duraklari, m8Koordinatlari, m8keys);

            m5Duraklari =new String[] {
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

            m5keys =new int[] {
                    71, 73, 75, 72, 70, 73, 76, 74, 72, 69, 67, 69, 71, 73, 77, 79, 82, 85, 87, 89, 91
            };

            metroAgi.hatOlustur("M5", m5Duraklari, m5Koordinatlari, m5keys);

        }
    }

    // GÜNCELLENEN: Hat seçildiğinde çağrılacak metot
    private void hatSecAction(String hatIsmi) {
        // Eğer rota görünürse, gizle
        if (isRouteVisible) {
            isRouteVisible = false;
            webEngine.executeScript("toggleRoute(false)");
            updateRotaButton();
        }

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

    // Hat durakları göster
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

    // Ara durak sayısını güncelleme metodu
    private void araDurakSayisiniGuncelle() {
        if (araDurakSayisiLabel != null) {
            int sayi = araDuraklar.size();
            if (sayi == 0) {
                araDurakSayisiLabel.setText("0 ara durak");
                araDurakSayisiLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
            } else {
                araDurakSayisiLabel.setText(sayi + " ara durak eklendi");
                araDurakSayisiLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
            }
        }
    }
    private MetroBeklemeSistemi beklemeSistemi;

    private void hesaplaBeklemeSuresiAction() {
        if (!simdiCheckBox.isSelected()) {
            // Checkbox işaretli DEĞİLSE yapılacaklar buraya
            String saatSt = yolcuSaatText.getText().trim();
            String dakikaSt = yolcuDakikaText.getText().trim();

            // Seçilen hat
            String secilenHat = selectedLine;

            // ListView'den seçilen durak
            String secilenDurakBilgi = hatDuraklariListView.getSelectionModel().getSelectedItem();
            if (secilenDurakBilgi == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Lütfen bir durak seçiniz.");
                alert.showAndWait();
                return;
            }

            // "1. Kadıköy (Aktarma Noktası)" -> "Kadıköy"
            String secilenDurak = secilenDurakBilgi.replaceAll("^\\d+\\.\\s*", "")
                    .replaceAll("\\s*\\(.*\\)$", "");

            if (secilenHat == null || secilenHat.equals("ALL")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Lütfen önce bir hat seçiniz.");
                alert.showAndWait();
                return;
            }

            beklemeSistemi.hesaplaBeklemeSuresi(secilenHat, secilenDurak, saatSt, dakikaSt,simdiCheckBox);
        }
        else{
            LocalTime simdi = LocalTime.now();
            String saatStr = Integer.toString(simdi.getHour());
            String dakikaStr = Integer.toString(simdi.getMinute());

            // Seçilen hat
            String secilenHat = selectedLine;

            // ListView'den seçilen durak
            String secilenDurakBilgi = hatDuraklariListView.getSelectionModel().getSelectedItem();
            if (secilenDurakBilgi == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Lütfen bir durak seçiniz.");
                alert.showAndWait();
                return;
            }

            // "1. Kadıköy (Aktarma Noktası)" -> "Kadıköy"
            String secilenDurak = secilenDurakBilgi.replaceAll("^\\d+\\.\\s*", "")
                    .replaceAll("\\s*\\(.*\\)$", "");

            if (secilenHat == null || secilenHat.equals("ALL")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Lütfen önce bir hat seçiniz.");
                alert.showAndWait();
                return;
            }

            beklemeSistemi.hesaplaBeklemeSuresi(secilenHat, secilenDurak, saatStr, dakikaStr,simdiCheckBox);


        }

    }



    public int getDurakKey(String hatIsmi, String durakIsmi) {
        return switch (hatIsmi) {
            case "Marmaray" -> getMarmarayDurakKey(durakIsmi);
            case "M4" -> getM4DurakKey(durakIsmi);
            case "M5" -> getM5DurakKey(durakIsmi);
            case "M8" -> getM8DurakKey(durakIsmi);
            default -> -1;
        };
    }
    public int getMarmarayDurakKey(String durakIsmi) {
        for (int i = 0; i < marmarayDuraklari.length; i++) {
            if (marmarayDuraklari[i].equals(durakIsmi)) {
                return marmaraykeys[i];
            }
        }
        return -1;
    }

    public int getM4DurakKey(String durakIsmi) {
        for (int i = 0; i < m4Duraklari.length; i++) {
            if (m4Duraklari[i].equals(durakIsmi)) {
                return m4keys[i];
            }
        }
        return -1;
    }

    public int getM8DurakKey(String durakIsmi) {
        for (int i = 0; i < m8Duraklari.length; i++) {
            if (m8Duraklari[i].equals(durakIsmi)) {
                return m8keys[i];
            }
        }
        return -1;
    }

    public int getM5DurakKey(String durakIsmi) {
        for (int i = 0; i < m5Duraklari.length; i++) {
            if (m5Duraklari[i].equals(durakIsmi)) {
                return m5keys[i];
            }
        }
        return -1;
    }



    // Ara durak ekleme butonu action
    @FXML
    private void durakEkleButtonAction(ActionEvent event) {
        System.out.println("DEBUG: Ara durak ekleniyor. Mevcut sayı: " + araDuraklar.size());

        if (araDuraklar.size() >= 8) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText("Maksimum Durak Sayısı");
            alert.setContentText("En fazla 8 ara durak ekleyebilirsiniz.\n\nDaha fazla durak eklemek için önce mevcut durakları silin.");
            alert.showAndWait();
            return;
        }

        AraDurakBileseni yeniDurak = araDurakBileseniOlustur();
        araDuraklar.add(yeniDurak);
        araDuraklarContainer.getChildren().add(yeniDurak.container);

        // Sayı label'ını güncelle
        araDurakSayisiniGuncelle();

        // ScrollPane'i en alta kaydır
        Platform.runLater(() -> {
            araDurakScrollPane.setVvalue(1.0);
        });

        System.out.println("DEBUG: Ara durak eklendi. Yeni sayı: " + araDuraklar.size());
    }

    // Ara durak bileşeni oluşturma
    private AraDurakBileseni araDurakBileseniOlustur() {
        AraDurakBileseni bilesen = new AraDurakBileseni(araDurakSayaci++);

        // TextField oluştur - BÜYÜTÜLMÜŞ
        bilesen.textField = new TextField();
        bilesen.textField.setPromptText("🔍 Ara durak " + bilesen.durakNo + " adını yazın...");
        bilesen.textField.setPrefHeight(40.0); // 35'ten 40'a
        bilesen.textField.setPrefWidth(250.0); // Genişlik eklendi
        bilesen.textField.setStyle("-fx-font-size: 14px; -fx-background-radius: 12px; " +
                "-fx-border-radius: 12px; -fx-border-color: #ffc107; " +
                "-fx-border-width: 2px; -fx-background-color: #fffef7; " +
                "-fx-prompt-text-fill: #6c757d;");

        // ListView oluştur - BÜYÜTÜLMÜŞ
        bilesen.listView = new ListView<>();
        bilesen.listView.setPrefHeight(100); // 80'den 100'e
        bilesen.listView.setStyle("-fx-background-radius: 12px; -fx-border-radius: 12px; " +
                "-fx-border-color: #ffc107; -fx-border-width: 2px; " +
                "-fx-background-color: #fffef7; -fx-font-size: 13px;");

        // Sil butonu - GELİŞTİRİLMİŞ
        bilesen.silButton = new Button("🗑️ Sil");
        bilesen.silButton.setPrefHeight(40.0); // 35'ten 40'a
        bilesen.silButton.setPrefWidth(90.0); // 80'den 90'a
        bilesen.silButton.setStyle("-fx-background-color: linear-gradient(to bottom, #f8d7da, #f1aeb5); " +
                "-fx-text-fill: #721c24; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12px; -fx-border-radius: 12px; " +
                "-fx-border-color: #e2a8a8; -fx-border-width: 1px;");
        bilesen.silButton.setOnAction(e -> araDurakSil(bilesen));

        // Container oluştur - GELİŞTİRİLMİŞ
        bilesen.container = new VBox(8); // spacing 5'ten 8'e
        bilesen.container.setStyle("-fx-background-color: #fff; -fx-background-radius: 10px; " +
                "-fx-border-radius: 10px; -fx-border-color: #dee2e6; " +
                "-fx-border-width: 1px; -fx-padding: 10px;");

        // Label - GELİŞTİRİLMİŞ
        Label durakLabel = new Label("📍 Ara Durak " + bilesen.durakNo);
        durakLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff8c00;");

        // TextField ve Sil butonu için HBox
        HBox inputBox = new HBox(10); // spacing 8'den 10'a
        inputBox.setAlignment(Pos.CENTER_LEFT);
        inputBox.getChildren().addAll(bilesen.textField, bilesen.silButton);

        bilesen.container.getChildren().addAll(durakLabel, inputBox, bilesen.listView);

        // TextField listener kur
        setupAraDurakListener(bilesen);

        // ListView click listener
        bilesen.listView.setOnMouseClicked(e -> {
            String selected = bilesen.listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                bilesen.textField.setText(selected);
                bilesen.listView.getItems().clear();
            }
        });

        return bilesen;
    }

    // Ara durak silme
    private void araDurakSil(AraDurakBileseni bilesen) {
        araDuraklarContainer.getChildren().remove(bilesen.container);
        araDuraklar.remove(bilesen);

        // Sayı label'ını güncelle
        araDurakSayisiniGuncelle();

        // Durak numaralarını yeniden düzenle
        araDurakSayaci = 1;
        for (AraDurakBileseni aradurak : araDuraklar) {
            aradurak.durakNo = araDurakSayaci++;
            // Label'ı güncelle
            VBox container = aradurak.container;
            Label label = (Label) container.getChildren().get(0);
            label.setText("📍 Ara Durak " + aradurak.durakNo);

            // TextField placeholder'ını güncelle
            aradurak.textField.setPromptText("🔍 Ara durak " + aradurak.durakNo + " adını yazın...");
        }
    }

    // Ara durak için arama listener'ı kurma
    private void setupAraDurakListener(AraDurakBileseni bilesen) {
        bilesen.textField.textProperty().addListener((obs, oldText, newText) -> {
            ArrayList<String> sonuclar = twoPhaseSearch(newText);
            bilesen.listView.getItems().clear();
            if (!sonuclar.isEmpty()) {
                bilesen.listView.getItems().addAll(sonuclar);
            }
        });
    }

    // Tüm durak listesini alma (rota için)
    private List<String> tumRotaDuraklariniAl() {
        List<String> rotaDuraklari = new ArrayList<>();

        // Başlangıç durağı
        String baslangic = baslangicTextField.getText().trim();
        if (!baslangic.isEmpty()) {
            rotaDuraklari.add(baslangic);
            System.out.println("DEBUG: Başlangıç durağı: " + baslangic);
        }

        // Ara duraklar
        for (int i = 0; i < araDuraklar.size(); i++) {
            AraDurakBileseni ara = araDuraklar.get(i);
            String araDurak = ara.textField.getText().trim();
            if (!araDurak.isEmpty()) {
                rotaDuraklari.add(araDurak);
                System.out.println("DEBUG: Ara durak " + (i+1) + ": " + araDurak);
            }
        }

        // Bitiş durağı
        String bitis = bitisTextField.getText().trim();
        if (!bitis.isEmpty()) {
            rotaDuraklari.add(bitis);
            System.out.println("DEBUG: Bitiş durağı: " + bitis);
        }

        System.out.println("DEBUG: Toplam durak sayısı: " + rotaDuraklari.size());
        System.out.println("DEBUG: Durak listesi: " + rotaDuraklari);

        return rotaDuraklari;
    }

    // GÜNCELLENEN: Rota bul button action
    @FXML
    private void rotaBulButtonAction(ActionEvent event) {
        // Eğer rota mevcut değilse, normal rota bulma işlemi
        Boolean routeExists = (Boolean) webEngine.executeScript("window.currentRoute != null");
        String baslangic=baslangicTextField.getText();
        String durak = "";
        if (routeExists == null || !routeExists) {
            // Normal rota bulma işlemi
            performRouteSearch();
        } else {
            if (getMarmarayDurakKey(baslangic) != -1){
                durak= "Marmaray";
            }
            if (getM4DurakKey(baslangic) != -1){
                durak= "M4";
            }
            if (getM5DurakKey(baslangic) != -1){
                durak= "M5";
            }
            if (getM8DurakKey(baslangic) != -1){
                durak= "M8";
            }
            beklemeSistemi.hesaplaBeklemeSuresi(durak,baslangicTextField.getText().trim(),yolcuSaatText.getText().trim(),yolcuDakikaText.getText().trim(),simdiCheckBox);
            // Rota toggle işlemi
            toggleRouteVisibility();
        }
    }

    // Gelişmiş validasyon metodu
    private boolean rotaValidasyonu() {
        List<String> tumDuraklar = tumRotaDuraklariniAl();

        if (tumDuraklar.size() < 2) {
            showAlert("Uyarı", "Durak Seçimi Eksik",
                    "En az başlangıç ve bitiş durağını seçin.");
            return false;
        }

        // Tekrarlanan durak kontrolü
        Set<String> benzersizDuraklar = new HashSet<>(tumDuraklar);
        if (benzersizDuraklar.size() != tumDuraklar.size()) {
            showAlert("Uyarı", "Tekrarlanan Durak",
                    "Aynı durağı birden fazla kez seçtiniz. Lütfen kontrol edin.");
            return false;
        }

        // Durakların var olup olmadığını kontrol et
        for (String durakIsmi : tumDuraklar) {
            if (metroAgi.durakBul(durakIsmi) == null) {
                showAlert("Hata", "Durak Bulunamadı",
                        "'" + durakIsmi + "' durağı bulunamadı.");
                return false;
            }
        }

        return true;
    }

    // Alert helper metodu
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // YENİ: Normal rota bulma işlemi
    private void performRouteSearch() {
        if (!rotaValidasyonu()) {
            return;
        }

        List<String> tumDuraklar = tumRotaDuraklariniAl();
        List<String> rotaBilgileri = new ArrayList<>();
        List<Object[]> rotaKoordinatlari = new ArrayList<>();

        try {
            if (tumDuraklar.size() == 2) {
                // Normal tek segment rota
                System.out.println("DEBUG: Tek segment rota hesaplanıyor");
                metroAgi.enKisaYoluBul(tumDuraklar.get(0), tumDuraklar.get(1),
                        rotaBilgileri, rotaKoordinatlari);
            } else {
                // Çoklu durak rotası
                System.out.println("DEBUG: Çoklu durak rotası hesaplanıyor: " + tumDuraklar.size() + " durak");
                metroAgi.cokluDurakRotasi(tumDuraklar, rotaBilgileri, rotaKoordinatlari);
            }

            if (rotaKoordinatlari.isEmpty()) {
                showAlert("Hata", "Rota Bulunamadı",
                        "Seçilen duraklar arasında geçerli bir rota bulunamadı.");
                return;
            }

            // UI güncelleme
            updateUIWithRoute(rotaBilgileri, rotaKoordinatlari);

        } catch (Exception e) {
            System.err.println("HATA: Rota hesaplama sırasında hata: " + e.getMessage());
            e.printStackTrace();
            showAlert("Hata", "Rota Hesaplama Hatası",
                    "Rota hesaplanırken bir hata oluştu: " + e.getMessage());
        }
    }

    // UI güncelleme metodu
    private void updateUIWithRoute(List<String> rotaBilgileri, List<Object[]> rotaKoordinatlari) {
        // Sonuçları göster
        rotaListView.setItems(FXCollections.observableArrayList(rotaBilgileri));

        updateRotaOzeti(rotaBilgileri, rotaKoordinatlari);

        // Rota durak isimlerini çıkar
        List<String> rotaDuraklari = extractRouteStationNames(rotaBilgileri);

        // JavaScript'e gönder
        sendRouteToMap(rotaKoordinatlari, rotaDuraklari);

        // UI durumunu güncelle
        updateRotaButton();
        webEngine.executeScript("showOnlyLine('ALL')");
        selectedLine = "ALL";
        hatDuraklariListView.setItems(FXCollections.observableArrayList());

        System.out.println("DEBUG: Rota başarıyla hesaplandı ve UI güncellendi");
    }

    // Haritaya rota gönderme metodu
    private void sendRouteToMap(List<Object[]> rotaKoordinatlari, List<String> rotaDuraklari) {
        System.out.println("=== HARITA DEBUG ===");
        System.out.println("Toplam koordinat sayısı: " + rotaKoordinatlari.size());
        System.out.println("Ana durak sayısı: " + rotaDuraklari.size());
        System.out.println("Ana duraklar: " + rotaDuraklari);

        // Koordinat kontrolü
        if (rotaKoordinatlari.isEmpty()) {
            System.out.println("HATA: Koordinat listesi boş!");
            return;
        }

        if (rotaDuraklari.isEmpty()) {
            System.out.println("HATA: Ana durak listesi boş!");
            return;
        }

        // JavaScript'e gönderilecek ana durakları yazdır
        System.out.println("Haritada gösterilecek duraklar: " + rotaDuraklari);

        // Koordinatları JavaScript array'ine dönüştür
        StringBuilder routePointsJs = new StringBuilder("[");
        for (Object[] koordinat : rotaKoordinatlari) {
            routePointsJs.append("[").append(koordinat[0]).append(",").append(koordinat[1]).append("],");
        }
        if (routePointsJs.length() > 1 && routePointsJs.charAt(routePointsJs.length() - 1) == ',') {
            routePointsJs.deleteCharAt(routePointsJs.length() - 1);
        }
        routePointsJs.append("]");

        // Durak isimlerini JavaScript array'ine dönüştür
        StringBuilder stationNamesJs = new StringBuilder("[");
        for (String durakIsmi : rotaDuraklari) {
            stationNamesJs.append("'").append(durakIsmi).append("',");
        }
        if (stationNamesJs.length() > 1 && stationNamesJs.charAt(stationNamesJs.length() - 1) == ',') {
            stationNamesJs.deleteCharAt(stationNamesJs.length() - 1);
        }
        stationNamesJs.append("]");

        System.out.println("JavaScript çağrısı yapılıyor...");

        // JavaScript fonksiyonlarını çağır
        webEngine.executeScript("showRoute(" + routePointsJs + ")");
        webEngine.executeScript("showRouteStations(" + stationNamesJs + ")");

        System.out.println("JavaScript çağrısı tamamlandı");
        System.out.println("=== DEBUG BİTTİ ===");
    }

    // YENİ: Rota bilgilerinden durak isimlerini çıkar
    private List<String> extractRouteStationNames(List<String> rotaBilgileri) {
        List<String> durakIsimleri = new ArrayList<>();

        for (String satir : rotaBilgileri) {
            // Çoklu durak rotası için özel durum
            if (satir.contains("Ziyaret sırası:")) {
                // "Ziyaret sırası: Kartal → Göztepe → Sirkeci" formatından durakları çıkar
                String[] parcalar = satir.split(":");
                if (parcalar.length > 1) {
                    String durakKismi = parcalar[1].trim();
                    String[] duraklar = durakKismi.split(" → ");
                    for (String durak : duraklar) {
                        String temizDurak = durak.trim();
                        if (!temizDurak.isEmpty() && !durakIsimleri.contains(temizDurak)) {
                            durakIsimleri.add(temizDurak);
                        }
                    }
                    // Çoklu durak rotasında ana durakları bulduk, return et
                    return durakIsimleri;
                }
            }

            // Normal tek segment rota için (eski mantık)
            if (satir.trim().matches("\\s*\\d+\\..*")) {
                String temp = satir.replaceFirst("\\s*\\d+\\.\\s*", "");
                temp = temp.replaceAll("\\s*\\(.*?\\)", "");
                temp = temp.replaceAll("\\s*\\[.*?\\]", "");

                String durakIsmi = temp.trim();
                if (!durakIsmi.isEmpty() && !durakIsimleri.contains(durakIsmi)) {
                    durakIsimleri.add(durakIsmi);
                }
            }
        }

        return durakIsimleri;
    }

    private void toggleRouteVisibility() {
        System.out.println("=== TOGGLE BAŞLADI ===");
        System.out.println("Önceki isRouteVisible: " + isRouteVisible);

        isRouteVisible = !isRouteVisible;

        System.out.println("Yeni isRouteVisible: " + isRouteVisible);

        webEngine.executeScript("toggleRoute(" + isRouteVisible + ")");

        if (isRouteVisible) {
            System.out.println("DALLANMA: ROTA GÖSTERİLİYOR");
            webEngine.executeScript("hideAllLines()");

            // ✅ BU KISMI EKLE - Rota durakları tekrar göster
            List<String> rotaDuraklari = extractRouteStationNames(rotaListView.getItems());
            if (!rotaDuraklari.isEmpty()) {
                System.out.println("DEBUG: Rota durakları tekrar gösteriliyor: " + rotaDuraklari);
                sendRouteStationsToMap(rotaDuraklari);
            } else {
                System.out.println("HATA: Rota durakları bulunamadı!");
            }

            selectedLine = "ROUTE_MODE";
            hatDuraklariListView.setItems(FXCollections.observableArrayList());
        } else {
            System.out.println("DALLANMA: ROTA GİZLENİYOR");
            webEngine.executeScript("clearRouteStations()");
            webEngine.executeScript("showOnlyLine('ALL')");
            selectedLine = "ALL";
            hatDuraklariListView.setItems(FXCollections.observableArrayList());
        }

        updateRotaButton();
        System.out.println("=== TOGGLE BİTTİ ===");
    }

    private void sendRouteStationsToMap(List<String> rotaDuraklari) {
        // Durak isimlerini JavaScript array'ine dönüştür
        StringBuilder stationNamesJs = new StringBuilder("[");
        for (String durakIsmi : rotaDuraklari) {
            stationNamesJs.append("'").append(durakIsmi).append("',");
        }
        if (stationNamesJs.length() > 1 && stationNamesJs.charAt(stationNamesJs.length() - 1) == ',') {
            stationNamesJs.deleteCharAt(stationNamesJs.length() - 1);
        }
        stationNamesJs.append("]");

        System.out.println("DEBUG: Duraklar JavaScript'e tekrar gönderiliyor: " + stationNamesJs);
        webEngine.executeScript("showRouteStations(" + stationNamesJs + ")");
    }
    // YENİ: Rota buton durumunu güncelle
    private void updateRotaButton() {
        if (isRouteVisible) {
            rotaBulButton.setText("👁️ Rotayı Gizle");
            rotaBulButton.setStyle("-fx-background-color: linear-gradient(to bottom, #f8b5b5, #f0a3a3); -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 15px; -fx-border-radius: 15px; -fx-border-color: #e3999b; -fx-border-width: 1px;");
        } else {
            Boolean routeExists = (Boolean) webEngine.executeScript("window.currentRoute != null");
            if (routeExists != null && routeExists) {
                rotaBulButton.setText("👁️ Rotayı Göster");
            } else {
                rotaBulButton.setText("🔍 Rota Bul");
            }
            rotaBulButton.setStyle("-fx-background-color: linear-gradient(to bottom, #a8d5f2, #87ceeb); -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 15px; -fx-border-radius: 15px; -fx-border-color: #7fb3d3; -fx-border-width: 1px;");
        }
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

    // GÜNCELLENEN: Rota temizle
    @FXML
    private void btnrotaTemizleButtonAction(ActionEvent event) {
        // UI temizle
        rotaListView.getItems().clear();
        baslangicTextField.clear();
        bitisTextField.clear();
        baslangicListView.getItems().clear();
        bitisListView.getItems().clear();

        rotaUzunlukLabel.setText("🚇 Toplam Durak: -");
        rotaSureLabel.setText("⏱️ Tahmini Süre: -");
        rotaHatlarLabel.setText("🚊 Kullanılan Hatlar: -");

        // Ara durakları temizle
        araDuraklarContainer.getChildren().clear();
        araDuraklar.clear();
        araDurakSayaci = 1;

        // Sayı label'ını güncelle
        araDurakSayisiniGuncelle();

        // Rota durumunu sıfırla
        isRouteVisible = false;
        webEngine.executeScript("clearRoute()");
        webEngine.executeScript("clearRouteStations()");
        webEngine.executeScript("showOnlyLine('ALL')");

        // Buton metnini sıfırla
        rotaBulButton.setText("🔍 Rota Bul");
        rotaBulButton.setStyle("-fx-background-color: linear-gradient(to bottom, #a8d5f2, #87ceeb); " +
                "-fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 15px; -fx-border-radius: 15px; " +
                "-fx-border-color: #7fb3d3; -fx-border-width: 1px;");

        selectedLine = "ALL";
        hatDuraklariListView.setItems(FXCollections.observableArrayList());
    }

    // ✅ YENİ METOT - MapController sınıfının içine ekle
    private void updateRotaOzeti(List<String> rotaBilgileri, List<Object[]> rotaKoordinatlari) {
        try {
            int toplamDurak = rotaKoordinatlari.size();
            double tahminiSure = 0.0;
            Set<String> kullanilanHatlar = new HashSet<>();

            // Rota bilgilerinden verileri çıkar
            for (String satir : rotaBilgileri) {

                // Toplam süre bilgisini bul
                if (satir.contains("Toplam süre") || satir.contains("Tahmini toplam süre")) {
                    try {
                        String[] parcalar = satir.split(":");
                        if (parcalar.length > 1) {
                            String sureParcasi = parcalar[1].trim();
                            String sayiStr = sureParcasi.replaceAll("[^0-9.]", "");
                            if (!sayiStr.isEmpty()) {
                                tahminiSure += Double.parseDouble(sayiStr);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Süre parse hatası: " + satir);
                    }
                }

                // Hat bilgilerini bul
                if (satir.contains("hattına geç") || satir.contains("hattı")) {
                    if (satir.contains("[") && satir.contains("hattına geç")) {
                        String hatIsmi = satir.substring(satir.indexOf("[") + 1, satir.indexOf(" hattına geç"));
                        kullanilanHatlar.add(hatIsmi.trim());
                    }
                    else if (satir.contains("(") && satir.contains(" hattı)")) {
                        String hatIsmi = satir.substring(satir.indexOf("(") + 1, satir.indexOf(" hattı)"));
                        kullanilanHatlar.add(hatIsmi.trim());
                    }
                }
            }

            // Eğer süre bulunamadıysa tahmini hesapla
            if (tahminiSure == 0.0 && toplamDurak > 1) {
                tahminiSure = (toplamDurak - 1) * 2.5;
            }

            // Eğer hat bulunamadıysa durak bazlı kontrol yap
            if (kullanilanHatlar.isEmpty()) {
                List<String> rotaDuraklari = extractRouteStationNames(rotaBilgileri);
                for (String durakIsmi : rotaDuraklari) {
                    Durak durak = metroAgi.durakBul(durakIsmi);
                    if (durak != null) {
                        List<String> durakHatlari = durak.getHatlar();
                        kullanilanHatlar.addAll(durakHatlari);
                    }
                }
            }

            // ✅ FINAL değişkenler oluştur lambda için
            final int finalToplamDurak = toplamDurak;
            final double finalTahminiSure = tahminiSure;
            final Set<String> finalKullanilanHatlar = new HashSet<>(kullanilanHatlar);

            // Label'ları güncelle
            Platform.runLater(() -> {
                if (finalToplamDurak > 1) {
                    rotaUzunlukLabel.setText("🚇 Toplam Durak: " + (finalToplamDurak - 1) + " geçiş");
                } else {
                    rotaUzunlukLabel.setText("🚇 Toplam Durak: -");
                }

                if (finalTahminiSure > 0) {
                    rotaSureLabel.setText("⏱️ Tahmini Süre: " + String.format("%.0f", finalTahminiSure) + " dakika");
                } else {
                    rotaSureLabel.setText("⏱️ Tahmini Süre: -");
                }

                if (!finalKullanilanHatlar.isEmpty()) {
                    String hatlarStr = String.join(", ", finalKullanilanHatlar);
                    if (hatlarStr.length() > 25) {
                        hatlarStr = hatlarStr.substring(0, 22) + "...";
                    }
                    rotaHatlarLabel.setText("🚊 Kullanılan Hatlar: " + hatlarStr);
                } else {
                    rotaHatlarLabel.setText("🚊 Kullanılan Hatlar: -");
                }
            });

        } catch (Exception e) {
            System.err.println("HATA: Özet güncelleme sırasında hata: " + e.getMessage());
            Platform.runLater(() -> {
                rotaUzunlukLabel.setText("🚇 Toplam Durak: Hesaplanamadı");
                rotaSureLabel.setText("⏱️ Tahmini Süre: Hesaplanamadı");
                rotaHatlarLabel.setText("🚊 Kullanılan Hatlar: Hesaplanamadı");
            });
        }
    }

    // YENİ: Hash table hazırlama
    private void hashTableHazirla() {
        primeHashTable = new PrimeHashTable();

        for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
            String durak = metroAgi.getDurakIndex(i).getIsim();
            String[] kelimeler = manuelSplit(durak, ' ');

            for (String kelime : kelimeler) {
                String normKelime = normalizeEt(kelime);

                // 1,2,3,4 karakterlik başlangıçları hash'le
                for (int len = 1; len <= Math.min(4, normKelime.length()); len++) {
                    String parca = manuelSubstring(normKelime, 0, len);
                    long hash = primeHash(parca);
                    primeHashTable.put(hash, durak);
                }
            }
        }
    }

    // YENİ: TextField listener'ları kur
    private void textFieldListenersKur() {
        baslangicTextField.textProperty().addListener((obs, oldText, newText) -> {
            ArrayList<String> sonuclar = twoPhaseSearch(newText);
            baslangicListView.getItems().clear();
            if (!sonuclar.isEmpty()) {
                baslangicListView.getItems().addAll(sonuclar);
            }
        });

        bitisTextField.textProperty().addListener((obs, oldText, newText) -> {
            ArrayList<String> sonuclar = twoPhaseSearch(newText);
            bitisListView.getItems().clear();
            if (!sonuclar.isEmpty()) {
                bitisListView.getItems().addAll(sonuclar);
            }
        });

        // ListView'den seçim yapıldığında TextField'ı güncelle
        baslangicListView.setOnMouseClicked(e -> {
            String selected = baslangicListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                baslangicTextField.setText(selected);
                baslangicListView.getItems().clear();
            }
        });

        bitisListView.setOnMouseClicked(e -> {
            String selected = bitisListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                bitisTextField.setText(selected);
                bitisListView.getItems().clear();
            }
        });
    }

    // YENİ: Two-phase search
    private ArrayList<String> twoPhaseSearch(String aramaMetni) {
        ArrayList<String> sonuclar = new ArrayList<>();

        if (aramaMetni == null || aramaMetni.trim().isEmpty()) {
            return sonuclar;
        }

        String normalized = normalizeEt(aramaMetni.trim());

        // Phase 1: Hash table lookup
        if (normalized.length() >= 1) {
            long aramaHash = primeHash(normalized);
            ArrayList<String> adaylar = primeHashTable.get(aramaHash);

            if (adaylar != null) {
                for (String aday : adaylar) {
                    if (normalizeEt(aday).contains(normalized) && !sonuclar.contains(aday)) {
                        sonuclar.add(aday);
                    }
                }
            }
        }

        // Phase 2: Linear search (fallback)
        if (sonuclar.isEmpty()) {
            for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
                String durak = metroAgi.getDurakIndex(i).getIsim();
                if (normalizeEt(durak).contains(normalized) && !sonuclar.contains(durak)) {
                    sonuclar.add(durak);
                }
            }
        }

        return sonuclar;
    }

    // YENİ: String utility fonksiyonları
    private String normalizeEt(String metin) {
        if (metin == null) return "";
        return metin.toLowerCase()
                .replace('ç', 'c')
                .replace('ğ', 'g')
                .replace('ı', 'i')
                .replace('ö', 'o')
                .replace('ş', 's')
                .replace('ü', 'u');
    }

    private String[] manuelSplit(String str, char ayirici) {
        if (str == null || str.isEmpty()) return new String[0];

        int kelimeSayisi = 1;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ayirici) kelimeSayisi++;
        }

        String[] kelimeler = new String[kelimeSayisi];
        int startIndex = 0;
        int currentIndex = 0;

        for (int i = 0; i <= str.length(); i++) {
            if (i == str.length() || str.charAt(i) == ayirici) {
                kelimeler[currentIndex] = str.substring(startIndex, i);
                startIndex = i + 1;
                currentIndex++;
            }
        }
        return kelimeler;
    }

    private String manuelSubstring(String str, int start, int end) {
        if (str == null || start < 0 || end > str.length() || start >= end) {
            return "";
        }
        char[] result = new char[end - start];
        for (int i = start; i < end; i++) {
            result[i - start] = str.charAt(i);
        }
        return new String(result);
    }

    private long primeHash(String str) {
        if (str == null || str.isEmpty()) return 0;
        long hash = 1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z') {
                hash *= primeSayilar[c - 'a'];
            }
        }
        return hash;
    }

    // JavaScript'ten çağrılabilecek metotlar
    public void logFromJS(String message) {
        System.out.println("JavaScript Log: " + message);
    }
}