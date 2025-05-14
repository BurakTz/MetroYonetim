package com.example.toplutasimaprojesi;

// Hat sınıfı (Belirli bir metro hattı)
public class Hat {
    private String isim;        // Hat adı (örneğin: "Kırmızı Hat", "M1", vb.)
    private DurakHat baslangic;  // Hat üzerindeki durakların sıralı listesi

    public Hat(String isim) {
        this.isim = isim;
        this.baslangic = null;
    }

    public String getIsim() {
        return isim;
    }

    // Hatta durak ekleme (sıralı)
    public void durakEkle(Durak durak) {
        DurakHat yeniDurak = new DurakHat(durak);

        // İlk durak ekleniyorsa
        if (baslangic == null) {
            baslangic = yeniDurak;
        } else {
            // Listenin sonuna ekle
            DurakHat temp = baslangic;
            while (temp.sonraki != null) {
                temp = temp.sonraki;
            }
            temp.sonraki = yeniDurak;
            yeniDurak.onceki = temp;  // Çift yönlü bağlantı
        }
    }

    // Hattaki durakları sırayla yazdır
    public void duraklariYazdir() {
        System.out.println(isim + " durakları:");
        DurakHat temp = baslangic;
        int sayac = 1;

        while (temp != null) {
            System.out.println(sayac + ". " + temp.getDurak().getIsim() +
                    (temp.getDurak().isAktarmaNoktasi() ? " (Aktarma Noktası)" : "") +
                    " [" + temp.getDurak().getXKoordinat() + ", " + temp.getDurak().getYKoordinat() + "]");
            temp = temp.sonraki;
            sayac++;
        }
    }

    // Hattın başlangıç durağını döndür
    public DurakHat getBaslangic() {
        return baslangic;
    }
}