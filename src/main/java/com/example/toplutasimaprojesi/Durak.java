package com.example.toplutasimaprojesi;

// Durak sınıfı (Graf düğümü)
public class Durak {
    private String isim;
    private boolean aktarmaNoktasi;
    private double xKoordinat;  // X koordinatı (enlem)
    private double yKoordinat;  // Y koordinatı (boylam)
    private BaglantiDurak baglantiListesi;  // Bağlantılı duraklar listesi

    public Durak(String isim, double xKoordinat, double yKoordinat) {
        this.isim = isim;
        this.xKoordinat = xKoordinat;
        this.yKoordinat = yKoordinat;
        this.aktarmaNoktasi = false;
        this.baglantiListesi = null;
    }

    public String getIsim() {
        return isim;
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

    // Bağlantılı durak ekleme
    public void baglantiEkle(Durak durak, String hatIsmi,int zaman) {
        BaglantiDurak yeniBaglanti = new BaglantiDurak(durak, hatIsmi, zaman);

        // İlk bağlantı ekleniyorsa
        if (baglantiListesi == null) {
            baglantiListesi = yeniBaglanti;
            yeniBaglanti.setGecenSure(zaman);
        } else {
            // Listeye ekle
            BaglantiDurak temp = baglantiListesi;
            while (temp.sonraki != null) {
                temp = temp.sonraki;
            }
            temp.sonraki = yeniBaglanti;
            yeniBaglanti.setGecenSure(zaman);
        }
    }

    // Bağlantıları getir
    public BaglantiDurak getBaglantiListesi() {
        return baglantiListesi;
    }

    // İki durak arasındaki mesafeyi hesapla (Öklid mesafesi)
    public double mesafeHesapla(Durak digerDurak) {
        double xFark = this.xKoordinat - digerDurak.getXKoordinat();
        double yFark = this.yKoordinat - digerDurak.getYKoordinat();
        return Math.sqrt(xFark * xFark + yFark * yFark);
    }
}