package com.example.toplutasimaprojesi;

import java.util.ArrayList;

// Hat sınıfı (Belirli bir metro hattı)
public class Hat {
    private String isim;        // Hat adı (örneğin: "M1", "M4", "Marmaray")
    private String renk;        // 🎨 YENİ: Hat rengi (hex kod olarak)
    private ArrayList<Durak> duraklar;  // Hat üzerindeki durakların sıralı listesi

    // Renk ile constructor
    public Hat(String isim, String renk) {
        this.isim = isim;
        this.renk = renk;
        this.duraklar = new ArrayList<>();
    }

    // Eski constructor (geriye dönük uyumluluk için)
    public Hat(String isim) {
        this.isim = isim;
        this.renk = "#3388ff"; // Varsayılan mavi renk
        this.duraklar = new ArrayList<>();
    }

    public String getIsim() {
        return isim;
    }

    //  Renk getter
    public String getRenk() {
        return renk;
    }

    // Renk setter
    public void setRenk(String renk) {
        this.renk = renk;
    }

    // Hatta durak ekleme (sıralı)
    public void durakEkle(Durak durak) {
        duraklar.add(durak);
    }

    // Hattaki durakları sırayla yazdır - 🎨 RENK BİLGİSİ EKLENDİ
    public void duraklariYazdir() {
        System.out.println(isim + " durakları (Renk: " + renk + "):");

        for (int i = 0; i < duraklar.size(); i++) {
            Durak durak = duraklar.get(i);
            System.out.println((i + 1) + ". " + durak.getIsim() +
                    (durak.isAktarmaNoktasi() ? " (Aktarma Noktası)" : "") +
                    " [" + durak.getXKoordinat() + ", " + durak.getYKoordinat() + "]");
        }
    }

    // Hattın durakları listesini döndür
    public ArrayList<Durak> getDuraklar() {
        return duraklar;
    }

    // Durak sayısını döndür
    public int getDurakSayisi() {
        return duraklar.size();
    }

    // Belirli index'teki durağı döndür
    public Durak getDurak(int index) {
        if (index >= 0 && index < duraklar.size()) {
            return duraklar.get(index);
        }
        return null;
    }

    // İlk durağı döndür
    public Durak getIlkDurak() {
        return duraklar.isEmpty() ? null : duraklar.get(0);
    }

    // Son durağı döndür
    public Durak getSonDurak() {
        return duraklar.isEmpty() ? null : duraklar.get(duraklar.size() - 1);
    }

    // 🎨 YENİ: Hat bilgilerini string olarak döndür
    @Override
    public String toString() {
        return isim + " (" + renk + ") - " + duraklar.size() + " durak";
    }
}