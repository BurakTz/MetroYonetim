package com.example.toplutasimaprojesi;
import java.time.LocalTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;

public class MetroBeklemeSistemi {

    // MapController referansı
    private MapController mapController;

    // Her hat için kalkış saatlerini tutan map
    private MyHashMap<String, LocalTime> hatKalkisSaatleri;

    // Her hat için sefer aralığını tutan map (dakika cinsinden)
    private MyHashMap<String, Integer> hatSeferAraliklari;

    public MetroBeklemeSistemi(MapController mapController) {
        this.mapController = mapController;
        hatKalkisSaatleriOlustur();
        hatSeferAralikOlustur();
    }

    private void hatKalkisSaatleriOlustur() {
        hatKalkisSaatleri = new MyHashMap<>();

        // Her hat için ilk kalkış saatlerini tanımla
        hatKalkisSaatleri.put("Marmaray", LocalTime.of(6, 0));  // 06:00
        hatKalkisSaatleri.put("M4", LocalTime.of(6, 15));       // 06:15
        hatKalkisSaatleri.put("M8", LocalTime.of(6, 30));       // 06:30
        hatKalkisSaatleri.put("M5", LocalTime.of(6, 10));       // 06:10
    }

    private void hatSeferAralikOlustur() {
        hatSeferAraliklari = new MyHashMap<>();

        // Her hat için sefer aralıklarını tanımla (dakika)
        hatSeferAraliklari.put("Marmaray", 6);   // 6 dakikada bir
        hatSeferAraliklari.put("M4", 4);         // 4 dakikada bir
        hatSeferAraliklari.put("M8", 5);         // 5 dakikada bir
        hatSeferAraliklari.put("M5", 4);         // 4 dakikada bir
    }

    @FXML
    public void hesaplaBeklemeSuresi(String hatIsmi, String durakIsmi, String saatStr, String dakikaStr, CheckBox simdiCheckBox) {
        int saat, dakika;

        // Eğer kullanıcı 'Şu anki saat' checkbox'ını işaretlemediyse:
        if (!simdiCheckBox.isSelected()) {
            // Saat ve dakika girişlerinin 2 basamaklı sayı olup olmadığını kontrol et
            if (!saatStr.matches("\\d{2}") || !dakikaStr.matches("\\d{2}")) {
                gosterUyari("Lütfen saat ve dakikayı 2 basamaklı sayı olarak girin. (örn: 08, 05)");
                return;
            }

            // Girilen saat ve dakikayı Integer'a çevir
            try {
                saat = Integer.parseInt(saatStr);
                dakika = Integer.parseInt(dakikaStr);
            } catch (NumberFormatException e) {
                gosterUyari("Geçersiz sayı formatı. Lütfen sadece rakam girin.");
                return;
            }

            // Saat ve dakika değerlerinin geçerli aralıkta olup olmadığını kontrol et
            if (saat < 0 || saat > 23 || dakika < 0 || dakika > 59) {
                gosterUyari("Saat 00-23 ve dakika 00-59 aralığında olmalıdır.");
                return;
            }
        }
        // Eğer checkbox işaretliyse, sistem saatini al
        else {
            LocalTime now = LocalTime.now();
            saat = now.getHour();
            dakika = now.getMinute();
        }

        // Kullanıcının belirlediği ya da sistemden alınan saat objesini oluştur
        LocalTime kullaniciSaati = LocalTime.of(saat, dakika);

        // Hat ismi sistemde tanımlı mı, kontrol et
        if (!hatKalkisSaatleri.containsKey(hatIsmi)) {
            gosterUyari("Geçersiz hat ismi: " + hatIsmi);
            return;
        }

        // Hat kalkış saatini ve sefer aralığını al
        LocalTime hatIlkKalkis = hatKalkisSaatleri.get(hatIsmi);
        int seferAraligi = hatSeferAraliklari.get(hatIsmi);

        // Belirtilen durak için geçen süreyi hesapla
        int durakGecenSure = getDurakGecenSure(hatIsmi, durakIsmi);
        if (durakGecenSure == -1) {
            gosterUyari("Durak bulunamadı: " + durakIsmi);
            return;
        }

        // Trenin bu durağa ilk geliş saatini hesapla
        LocalTime durakIlkVaris = hatIlkKalkis.plusMinutes(durakGecenSure);

        // Kullanıcının saatine göre ne kadar süre kaldığını hesapla
        long dakikaFarki = Duration.between(durakIlkVaris, kullaniciSaati).toMinutes();

        // Eğer kullanıcı belirttiği saatte tren durağa daha gelmemişse
        if (dakikaFarki < 0) {
            long beklemeSuresi = Math.abs(dakikaFarki);
            gosterBilgi(hatIsmi, durakIsmi, kullaniciSaati, durakIlkVaris, beklemeSuresi);
        }
        // Eğer tren durağa daha önce geldiyse ve sonraki tren bekleniyorsa
        else {
            long sonrakiTrenKalanDakika = seferAraligi - (dakikaFarki % seferAraligi);

            // Tam sefer zamanında denk geldiyse bekleme sıfır olur
            if (sonrakiTrenKalanDakika == seferAraligi) {
                sonrakiTrenKalanDakika = 0;
            }

            LocalTime sonrakiTrenSaati = kullaniciSaati.plusMinutes(sonrakiTrenKalanDakika);
            gosterBilgi(hatIsmi, durakIsmi, kullaniciSaati, sonrakiTrenSaati, sonrakiTrenKalanDakika);
        }
    }


    // MapController'daki durak key değer farklari
    private int getDurakGecenSure(String hatIsmi, String durakIsmi) {
        switch (hatIsmi) {
            case "Marmaray":
                return mapController.getMarmarayDurakKey(durakIsmi);
            case "M4":
                return mapController.getM4DurakKey(durakIsmi);
            case "M8":
                return mapController.getM8DurakKey(durakIsmi);
            case "M5":
                return mapController.getM5DurakKey(durakIsmi);
            default:
                return -1;
        }
    }

    private void gosterBilgi(String hatIsmi, String durakIsmi, LocalTime kullaniciSaati,
                             LocalTime trenSaati, long beklemeSuresi) {
        String mesaj = String.format(
                "%s hattında %s durağında:\n" +
                        "Şu anki saat: %s\n" +
                        "Sonraki tren: %s\n" +
                        "Bekleme süresi: %d dakika",
                hatIsmi, durakIsmi, kullaniciSaati, trenSaati, beklemeSuresi
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tren Bekleme Süresi");
        alert.setHeaderText("Tren Bilgileri");
        alert.setContentText(mesaj);
        alert.showAndWait();
    }

    private void gosterUyari(String mesaj) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uyarı");
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
    //yorum satırı
}