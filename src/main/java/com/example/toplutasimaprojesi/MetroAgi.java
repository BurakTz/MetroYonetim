package com.example.toplutasimaprojesi;
import java.util.*;

public class MetroAgi {
    // 1. Singleton instance
    private static final MetroAgi instance = new MetroAgi(200, 100);

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
    public Durak durakEkle(String isim,int key, double xKoordinat, double yKoordinat) {
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
        Durak yeniDurak = new Durak(isim,key,xKoordinat, yKoordinat);
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
    public void baglantiEkle(String durak1Isim, String durak2Isim, String hatIsmi,int zaman) {
        Durak drk1 = durakBul(durak1Isim);
        Durak drk2 = durakBul(durak2Isim);

        if (drk1 == null || drk2 == null) {
            System.out.println("Duraklardan biri bulunamadı!");
            return;
        }

        // Çift yönlü bağlantı oluştur
        drk1.baglantiEkle(drk2, hatIsmi,zaman);
        drk2.baglantiEkle(drk1, hatIsmi,zaman);
    }

    // Hat oluşturma ve duraklarını sırayla ekleme
    public void hatOlustur(String hatIsmi, String[] durakIsimleri, double[][] koordinatlar,int [] gecenSureler) {
        if (durakIsimleri.length != koordinatlar.length && koordinatlar.length != gecenSureler.length) {
            System.out.println("Durak isimleri , koordinat sayıları ve gecen sureler eşit olmalıdır!");
            return;
        }

        Hat hat = hatEkle(hatIsmi);

        if (hat == null) {
            return;
        }

        // Önceki durak referansı
        Durak oncekiDurak = null;
        int y=0;

        // Hat üzerindeki her durağı ekle
        for (int i = 0; i < durakIsimleri.length; i++) {
            String durakIsmi = durakIsimleri[i];
            double xKoordinat = koordinatlar[i][0];
            double yKoordinat = koordinatlar[i][1];
            int  gecenSure  = gecenSureler[y];


            Durak durak = durakBul(durakIsmi);

            // Durak yoksa oluştur
            if (durak == null) {
                durak = durakEkle(durakIsmi, gecenSure, xKoordinat, yKoordinat);
            }

            durak.hatEkle(hatIsmi);

            // Hatta ekle
            hat.durakEkle(durak);

            // Önceki durakla bağlantı oluştur
            if (oncekiDurak != null) {
                y++;
                baglantiEkle(oncekiDurak.getIsim(), durak.getIsim(), hatIsmi,gecenSure);
            }

            oncekiDurak = durak;
        }
    }


    // İki durak arasındaki en kısa yolu bul (Dijkstra algoritması)

    public void enKisaYoluBul(String baslangicIsmi, String bitisIsmi,
                              List<String> rotaBilgileri,
                              List<Object[]> rotaKoordinatlari) {
        Durak baslangic = durakBul(baslangicIsmi);
        Durak bitis = durakBul(bitisIsmi);

        if (baslangic == null || bitis == null) {
            rotaBilgileri.add("Başlangıç veya bitiş durağı bulunamadı!");
            return;
        }

        double[] mesafeler = new double[durakSayisi];
        Durak[] ebeveyn = new Durak[durakSayisi];
        String[] kullanilanHat = new String[durakSayisi];
        boolean[] ziyaretEdildi = new boolean[durakSayisi];

        Arrays.fill(mesafeler, Double.MAX_VALUE);

        int baslangicIndeks = -1;
        for (int i = 0; i < durakSayisi; i++) {
            if (duraklar[i] == baslangic) {
                mesafeler[i] = 0;
                baslangicIndeks = i;
                break;
            }
        }

        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(i -> mesafeler[i]));
        pq.add(baslangicIndeks);

        while (!pq.isEmpty()) {
            int mevcutIndeks = pq.poll();

            if (ziyaretEdildi[mevcutIndeks]) continue;
            ziyaretEdildi[mevcutIndeks] = true;

            Durak mevcutDurak = duraklar[mevcutIndeks];
            BaglantiDurak baglanti = mevcutDurak.getBaglantiListesi();

            while (baglanti != null) {
                Durak komsuDurak = baglanti.getDurak();

                int komsuIndeks = -1;
                for (int i = 0; i < durakSayisi; i++) {
                    if (duraklar[i] == komsuDurak) {
                        komsuIndeks = i;
                        break;
                    }
                }

                if (komsuIndeks != -1) {
                    double agirlik = Math.abs(mevcutDurak.getKey() - komsuDurak.getKey()); // süre gibi
                    double yeniMesafe = mesafeler[mevcutIndeks] + agirlik;

                    if (yeniMesafe < mesafeler[komsuIndeks]) {
                        mesafeler[komsuIndeks] = yeniMesafe;
                        ebeveyn[komsuIndeks] = mevcutDurak;
                        kullanilanHat[komsuIndeks] = baglanti.getHatIsmi();
                        pq.add(komsuIndeks);
                    }
                }

                baglanti = baglanti.sonraki;
            }
        }

        int bitisIndeks = -1;
        for (int i = 0; i < durakSayisi; i++) {
            if (duraklar[i] == bitis) {
                bitisIndeks = i;
                break;
            }
        }

        if (mesafeler[bitisIndeks] == Double.MAX_VALUE) {
            rotaBilgileri.add("Bu iki durak arasında yol bulunamadı!");
            return;
        }

        // Geriye doğru yolu kur
        List<Durak> yol = new ArrayList<>();
        List<String> hatlar = new ArrayList<>();
        Durak mevcutDurak = bitis;

// Baştan indeks al:
        int mevcutIndeks = -1;
        for (int i = 0; i < durakSayisi; i++) {
            if (duraklar[i] == mevcutDurak) {
                mevcutIndeks = i;
                break;
            }
        }

// Geri izleme: başlangıca kadar
        while (mevcutDurak != null) {
            yol.add(mevcutDurak);
            hatlar.add(kullanilanHat[mevcutIndeks]);

            // Ebeveyni al
            mevcutDurak = ebeveyn[mevcutIndeks];

            // Yeni indeks bulun
            if (mevcutDurak != null) {
                for (int i = 0; i < durakSayisi; i++) {
                    if (duraklar[i] == mevcutDurak) {
                        mevcutIndeks = i;
                        break;
                    }
                }
            }
        }

        Collections.reverse(yol);
        Collections.reverse(hatlar);

        rotaBilgileri.add("En kısa yol (" + baslangicIsmi + " -> " + bitisIsmi + "):");

        String aktifHat = null;
        for (int i = 0; i < yol.size(); i++) {
            Durak d = yol.get(i);

            if (i > 0) {
                String hat = hatlar.get(i);
                if (hat != null && (aktifHat == null || !aktifHat.equals(hat))) {
                    aktifHat = hat;
                    rotaBilgileri.add("  [" + hat + " hattına geç]");
                }
            }

            rotaBilgileri.add("  " + (i + 1) + ". " + d.getIsim() +
                    (d.isAktarmaNoktasi() ? " (Aktarma Noktası)" : "") +
                    " [" + d.getXKoordinat() + ", " + d.getYKoordinat() + "]");
            rotaKoordinatlari.add(new Object[]{d.getXKoordinat(), d.getYKoordinat()});
        }

        rotaBilgileri.add("Toplam " + (yol.size() - 1) + " durak.");
        rotaBilgileri.add("Toplam süre (key farkları): " +
                String.format("%.0f", mesafeler[bitisIndeks]+2) + " dakika.");
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