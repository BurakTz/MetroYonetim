package com.example.toplutasimaprojesi;

import java.util.ArrayList;
import java.util.List;

// Durak sınıfı (Graf düğümü)
public class Durak {
    private String isim;
    public int key;
    private boolean aktarmaNoktasi;
    private double xKoordinat;  // X koordinatı (enlem)
    private double yKoordinat;  // Y koordinatı (boylam)
    private BaglantiDurak baglantiListesi;  // Bağlantılı duraklar listesi

    // ⭐ YENİ: Bu durağın hangi hatlara ait olduğu
    private List<String> hatlar;

    public Durak(String isim,int key, double xKoordinat, double yKoordinat) {
        this.isim = isim;
        this.key=key;
        this.xKoordinat = xKoordinat;
        this.yKoordinat = yKoordinat;
        this.aktarmaNoktasi = false;
        this.baglantiListesi = null;
        this.hatlar = new ArrayList<>(); // ⭐ Boş hat listesi
    }

    // Getter metodları
    public String getIsim() {
        return isim;
    }

    public int getKey(){
        return key;
    }

    public double getXKoordinat() {
        return xKoordinat;
    }

    public double getYKoordinat() {
        return yKoordinat;
    }

    public boolean isAktarmaNoktasi() {
        return aktarmaNoktasi;
    }

    public void setAktarmaNoktasi(boolean aktarmaNoktasi) {
        this.aktarmaNoktasi = aktarmaNoktasi;
    }

    public BaglantiDurak getBaglantiListesi() {
        return baglantiListesi;
    }

    // Hat ekleme ve otomatik aktarma tespiti
    public void hatEkle(String hatIsmi) {
        // Manuel duplicate kontrolü
        if (!hatlar.contains(hatIsmi)) {
            hatlar.add(hatIsmi);

            // AKTARMA TESPİTİ!
            // 2 veya daha fazla hatta aitse otomatik aktarma noktası ol!
            if (hatlar.size() > 1) {
                this.setAktarmaNoktasi(true);
            }
        }
    }

    // Hat bilgileri için yardımcı metodlar
    public List<String> getHatlar() {
        return hatlar;
    }

    public boolean hatVarMi(String hatIsmi) {
        return hatlar.contains(hatIsmi);
    }

    public int hatSayisi() {
        return hatlar.size();
    }

    // Hat isimlerini güzel String olarak döndür
    public String getHatlarStr() {
        if (hatlar.isEmpty()) {
            return "Hiç hat yok";
        }
        return String.join(", ", hatlar);
    }

    // Bağlantılı durak ekleme
    public void baglantiEkle(Durak durak, String hatIsmi, int zaman) {
        BaglantiDurak yeniBaglanti = new BaglantiDurak(durak, hatIsmi, zaman);

        // İlk bağlantı ekleniyorsa
        if (baglantiListesi == null) {
            baglantiListesi = yeniBaglanti;
        } else {
            // Listeye ekle
            BaglantiDurak temp = baglantiListesi;
            while (temp.sonraki != null) {
                temp = temp.sonraki;
            }
            temp.sonraki = yeniBaglanti;
        }
    }

    // İki durak arasındaki mesafeyi hesapla (Öklid mesafesi)
    public double mesafeHesapla(Durak digerDurak) {
        double xFark = this.xKoordinat - digerDurak.getXKoordinat();
        double yFark = this.yKoordinat - digerDurak.getYKoordinat();
        return Math.sqrt(xFark * xFark + yFark * yFark);
    }
}