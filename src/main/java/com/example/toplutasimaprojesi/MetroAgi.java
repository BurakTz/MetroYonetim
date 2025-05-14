package com.example.toplutasimaprojesi;
import java.util.List;
import java.util.ArrayList;

public class MetroAgi {
    // 1. Singleton instance
    private static final MetroAgi instance = new MetroAgi(200, 20);

    private Durak[] duraklar;
    private Hat[] hatlar;
    private int durakSayisi;
    private int hatSayisi;
    private int maksDurakSayisi;
    private int maksHatSayisi;

    // 2. Constructor'ı private yapın
    private MetroAgi(int maksDurakSayisi, int maksHatSayisi) {
        this.maksDurakSayisi = maksDurakSayisi;
        this.maksHatSayisi = maksHatSayisi;
        this.duraklar = new Durak[maksDurakSayisi];
        this.hatlar = new Hat[maksHatSayisi];
        this.durakSayisi = 0;
        this.hatSayisi = 0;
    }

    // 3. getInstance metodu ekleyin
    public static MetroAgi getInstance() {
        return instance;
    }

    // Diğer tüm metotlar değişmeden kalır
    public int getDurakSayisi() {
        return durakSayisi;
    }

    public int getHatSayisi() {
        return hatSayisi;
    }

    public Durak getDurakIndex(int index) {
        return duraklar[index];
    }

    public Hat getHatIndex(int index) {
        return hatlar[index];
    }

    // Yeni durak ekleme
    public Durak durakEkle(String isim, double xKoordinat, double yKoordinat) {
        if (durakSayisi >= maksDurakSayisi) {
            System.out.println("Maksimum durak sayısına ulaşıldı!");
            return null;
        }

        // Durak daha önce eklenmiş mi kontrol et
        for (int i = 0; i < durakSayisi; i++) {
            if (duraklar[i].getIsim().equals(isim)) {
                return duraklar[i];  // Varsa mevcut olanı döndür
            }
        }

        // Yeni durak ekle
        Durak yeniDurak = new Durak(isim, xKoordinat, yKoordinat);
        duraklar[durakSayisi] = yeniDurak;
        durakSayisi++;
        return yeniDurak;
    }

    // Yeni hat ekleme
    public Hat hatEkle(String isim) {
        if (hatSayisi >= maksHatSayisi) {
            System.out.println("Maksimum hat sayısına ulaşıldı!");
            return null;
        }

        // Hat daha önce eklenmiş mi kontrol et
        for (int i = 0; i < hatSayisi; i++) {
            if (hatlar[i].getIsim().equals(isim)) {
                return hatlar[i];  // Varsa mevcut olanı döndür
            }
        }

        // Yeni hat ekle
        Hat yeniHat = new Hat(isim);
        hatlar[hatSayisi] = yeniHat;
        hatSayisi++;
        return yeniHat;
    }

    // İsimle durak bulma
    public Durak durakBul(String isim) {
        for (int i = 0; i < durakSayisi; i++) {
            if (duraklar[i].getIsim().equals(isim)) {
                return duraklar[i];
            }
        }
        return null;  // Bulunamadı
    }

    // İsimle hat bulma
    public Hat hatBul(String isim) {
        for (int i = 0; i < hatSayisi; i++) {
            if (hatlar[i].getIsim().equals(isim)) {
                return hatlar[i];
            }
        }
        return null;  // Bulunamadı
    }

    // İki durak arasında bağlantı oluşturma
    public void baglantiEkle(String durak1Isim, String durak2Isim, String hatIsmi) {
        Durak drk1 = durakBul(durak1Isim);
        Durak drk2 = durakBul(durak2Isim);

        if (drk1 == null || drk2 == null) {
            System.out.println("Duraklardan biri bulunamadı!");
            return;
        }

        // Çift yönlü bağlantı oluştur
        drk1.baglantiEkle(drk2, hatIsmi);
        drk2.baglantiEkle(drk1, hatIsmi);
    }

    // Hat oluşturma ve duraklarını sırayla ekleme
    public void hatOlustur(String hatIsmi, String[] durakIsimleri, double[][] koordinatlar) {
        if (durakIsimleri.length != koordinatlar.length) {
            System.out.println("Durak isimleri ve koordinat sayıları eşit olmalıdır!");
            return;
        }

        Hat hat = hatEkle(hatIsmi);

        if (hat == null) {
            return;
        }

        // Önceki durak referansı
        Durak oncekiDurak = null;

        // Hat üzerindeki her durağı ekle
        for (int i = 0; i < durakIsimleri.length; i++) {
            String durakIsmi = durakIsimleri[i];
            double xKoordinat = koordinatlar[i][0];
            double yKoordinat = koordinatlar[i][1];

            Durak durak = durakBul(durakIsmi);

            // Durak yoksa oluştur
            if (durak == null) {
                durak = durakEkle(durakIsmi, xKoordinat, yKoordinat);
            }

            // Hatta ekle
            hat.durakEkle(durak);

            // Önceki durakla bağlantı oluştur
            if (oncekiDurak != null) {
                baglantiEkle(oncekiDurak.getIsim(), durak.getIsim(), hatIsmi);
            }

            oncekiDurak = durak;
        }
    }

    // Aktarma noktalarını işaretle (birden fazla hatta yer alan duraklar)
    public void aktarmaNoktalariniIsaretle() {
        // Her durak için bağlı olduğu hat sayısını bul
        for (int i = 0; i < durakSayisi; i++) {
            Durak durak = duraklar[i];
            // Hangi hatlara ait olduğunu bulmak için set kullanımını simüle edelim
            String[] baglananHatlar = new String[hatSayisi];
            int hatSayaci = 0;

            // Bağlantılarını kontrol et
            BaglantiDurak baglanti = durak.getBaglantiListesi();
            while (baglanti != null) {
                // Bu hat daha önce eklendi mi kontrol et
                boolean hatEklendi = false;
                for (int j = 0; j < hatSayaci; j++) {
                    if (baglananHatlar[j].equals(baglanti.getHatIsmi())) {
                        hatEklendi = true;
                        break;
                    }
                }

                if (!hatEklendi) {
                    baglananHatlar[hatSayaci] = baglanti.getHatIsmi();
                    hatSayaci++;
                }

                baglanti = baglanti.sonraki;
            }

            // Birden fazla hatta bağlıysa aktarma noktası olarak işaretle
            if (hatSayaci > 1) {
                durak.setAktarmaNoktasi(true);
            }
        }
    }

    // İki durak arasındaki en kısa yolu bul (BFS algoritması)
    public void enKisaYoluBul(String baslangicIsmi, String bitisIsmi,
                              List<String> rotaBilgileri,
                              List<Object[]> rotaKoordinatlari) {
        Durak baslangic = durakBul(baslangicIsmi);
        Durak bitis = durakBul(bitisIsmi);

        if (baslangic == null || bitis == null) {
            rotaBilgileri.add("Başlangıç veya bitiş durağı bulunamadı!");
            return;
        }

        boolean[] ziyaretEdildi = new boolean[durakSayisi];
        Durak[] ebeveyn = new Durak[durakSayisi];
        String[] kullanilanHat = new String[durakSayisi];
        Durak[] kuyruk = new Durak[durakSayisi];
        int bas = 0, son = 0;

        for (int i = 0; i < durakSayisi; i++) {
            if (duraklar[i] == baslangic) {
                ziyaretEdildi[i] = true;
                break;
            }
        }
        kuyruk[son++] = baslangic;

        boolean yolBulundu = false;
        while (bas < son) {
            Durak mevcutDurak = kuyruk[bas++];

            if (mevcutDurak == bitis) {
                yolBulundu = true;
                break;
            }

            BaglantiDurak baglanti = mevcutDurak.getBaglantiListesi();
            while (baglanti != null) {
                Durak baglantiDurak = baglanti.getDurak();

                int baglantiIndeks = -1;
                for (int i = 0; i < durakSayisi; i++) {
                    if (duraklar[i] == baglantiDurak) {
                        baglantiIndeks = i;
                        break;
                    }
                }

                if (baglantiIndeks != -1 && !ziyaretEdildi[baglantiIndeks]) {
                    ziyaretEdildi[baglantiIndeks] = true;
                    ebeveyn[baglantiIndeks] = mevcutDurak;
                    kullanilanHat[baglantiIndeks] = baglanti.getHatIsmi();
                    kuyruk[son++] = baglantiDurak;
                }

                baglanti = baglanti.sonraki;
            }
        }

        if (!yolBulundu) {
            rotaBilgileri.add("Bu iki durak arasında yol bulunamadı!");
            return;
        }

        Durak[] yol = new Durak[durakSayisi];
        String[] hatlar = new String[durakSayisi];
        int yolUzunlugu = 0;

        Durak mevcutDurak = bitis;
        while (mevcutDurak != baslangic) {
            yol[yolUzunlugu] = mevcutDurak;

            int mevcutIndeks = -1;
            for (int i = 0; i < durakSayisi; i++) {
                if (duraklar[i] == mevcutDurak) {
                    mevcutIndeks = i;
                    break;
                }
            }

            hatlar[yolUzunlugu] = kullanilanHat[mevcutIndeks];
            mevcutDurak = ebeveyn[mevcutIndeks];
            yolUzunlugu++;
        }

        yol[yolUzunlugu++] = baslangic;

        rotaBilgileri.add("En kısa yol (" + baslangicIsmi + " -> " + bitisIsmi + "):");

        String aktifHat = null;
        for (int i = yolUzunlugu - 1; i >= 0; i--) {
            if (i < yolUzunlugu - 1) {
                String hat = hatlar[i];
                if (aktifHat == null || !aktifHat.equals(hat)) {
                    aktifHat = hat;
                    rotaBilgileri.add("  [" + hat + " hattına geç]");
                }
            }

            Durak d = yol[i];
            rotaBilgileri.add("  " + (yolUzunlugu - i) + ". " + d.getIsim() +
                    (d.isAktarmaNoktasi() ? " (Aktarma Noktası)" : "") +
                    " [" + d.getXKoordinat() + ", " + d.getYKoordinat() + "]");

            rotaKoordinatlari.add(new Object[]{d.getXKoordinat(), d.getYKoordinat()});
        }

        rotaBilgileri.add("Toplam " + (yolUzunlugu - 1) + " durak.");

        double toplamMesafe = 0.0;
        for (int i = yolUzunlugu - 1; i > 0; i--) {
            toplamMesafe += yol[i].mesafeHesapla(yol[i - 1]);
        }

        rotaBilgileri.add("Toplam mesafe: " + String.format("%.2f", toplamMesafe) + " birim.");
    }


    // Tüm hatları ve durakları yazdır
    public void bilgileriYazdir() {
        System.out.println("METRO AĞI BİLGİLERİ");
        System.out.println("===================");

        System.out.println("\nTÜM HATLAR (" + hatSayisi + "):");
        for (int i = 0; i < hatSayisi; i++) {
            System.out.println("\n" + (i + 1) + ". " + hatlar[i].getIsim());
            hatlar[i].duraklariYazdir();
        }

        System.out.println("\nTÜM DURAKLAR (" + durakSayisi + "):");
        for (int i = 0; i < durakSayisi; i++) {
            System.out.print((i + 1) + ". " + duraklar[i].getIsim());
            System.out.print(" [" + duraklar[i].getXKoordinat() + ", " + duraklar[i].getYKoordinat() + "]");
            if (duraklar[i].isAktarmaNoktasi()) {
                System.out.print(" (Aktarma Noktası)");
            }
            System.out.println();

            // Durağın bağlantılarını yazdır
            BaglantiDurak baglanti = duraklar[i].getBaglantiListesi();
            if (baglanti != null) {
                System.out.println("   Bağlantılar:");
                while (baglanti != null) {
                    System.out.println("   - " + baglanti.getDurak().getIsim() +
                            " (" + baglanti.getHatIsmi() + " hattı)" +
                            " [" + baglanti.getDurak().getXKoordinat() + ", " + baglanti.getDurak().getYKoordinat() + "]" +
                            " Mesafe: " + String.format("%.2f", duraklar[i].mesafeHesapla(baglanti.getDurak())) + " birim");
                    baglanti = baglanti.sonraki;
                }
            }
        }
    }
}