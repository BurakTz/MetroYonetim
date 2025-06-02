# 🚇 ROTALA - İstanbul Metro Rota Planlama Sistemi

## 📋 Proje Hakkında

**ROTALA**, İstanbul'un metro hatları (M4, M5, M8, Marmaray) için geliştirilmiş kapsamlı bir rota planlama uygulamasıdır. Bu proje, **Veri Yapıları ve Algoritmalar** dersi kapsamında geliştirilmiştir ve gerçek metro verilerini kullanarak kullanıcılara en optimal yolculuk rotalarını sunar.

## ✨ Özellikler

### 🗺️ Ana Özellikler
- **İnteraktif Harita**: Leaflet.js kullanılarak geliştirilmiş gerçek zamanlı metro haritası
- **Çoklu Durak Rotası**: Ara duraklar ekleyerek karmaşık rotalar planlayabilme
- **Konum Tabanlı Servis**: IP bazlı konum tespiti ve en yakın durak önerisi
- **Gerçek Zamanlı Sefer Bilgileri**: Metro gelme saatleri ve bekleme süreleri
- **Otomatik Başlangıç Seçimi**: Konumunuza göre otomatik durak önerisi

### 🛠️ Teknik Özellikler
- **Graf Algoritmaları**: Dijkstra algoritması ile en kısa yol bulma
- **Hash Table**: Prime hash ile hızlı durak arama
- **Grid Sistemi**: Coğrafi koordinatlar için optimize edilmiş arama
- **Custom Data Structures**: Kendi HashMap ve LinkedList implementasyonları

## 🏗️ Sistem Mimarisi

### 📁 Proje Yapısı
```
src/main/java/com/example/toplutasimaprojesi/
├── Main.java                      # Ana uygulama başlatıcı
├── KarsilamaEkraniController.java  # Karşılama ekranı kontrolcüsü
├── MapController.java              # Ana harita kontrolcüsü
├── MetroAgi.java                   # Metro ağı yönetimi (Singleton)
├── Durak.java                      # Durak veri yapısı
├── Hat.java                        # Metro hattı veri yapısı
├── BaglantiDurak.java             # Duraklar arası bağlantı
├── PrimeHashTable.java            # Prime hash table implementasyonu
├── MyHashMap.java                 # Custom HashMap implementasyonu
├── LocationService.java           # Konum servisleri
└── MetroBeklemeSistemi.java       # Sefer zamanları hesaplama

src/main/resources/
├── map.html                       # Leaflet.js harita
├── map.fxml                       # Ana UI layout
├── karsilama_ekrani.fxml          # Karşılama ekranı
└── styles.css                     # UI stilleri
```

## 🧠 Kullanılan Veri Yapıları ve Algoritmalar

### 1. 📊 Graf Yapısı (Metro Ağı)
```java
// MetroAgi.java - Singleton Pattern
public class MetroAgi {
    private Durak[] duraklar;           // Düğümler (Nodes)
    private Hat[] hatlar;               // Kenarlar (Edges)
    
    // Dijkstra Algoritması ile en kısa yol
    public void enKisaYoluBul(String baslangic, String bitis) {
        // O(V log V + E) karmaşıklığı
    }
}
```

**Açıklama**: Her durak bir düğüm, duraklar arası bağlantılar kenar olarak modellenmiştir.

### 2. 🔗 Bağlı Liste (Linked List)
```java
// BaglantiDurak.java
public class BaglantiDurak {
    private Durak durak;
    public BaglantiDurak sonraki;    // LinkedList yapısı
    private String hatIsmi;
    private int gecenSure;
}
```

**Açıklama**: Her durağın komşu durakları bağlı liste ile saklanır.

### 3. 🗂️ Hash Table (Prime Hash)
```java
// PrimeHashTable.java
public class PrimeHashTable {
    private long[] hashKeys;
    private ArrayList<String>[] values;
    
    // Prime sayılar ile hash hesaplama
    private long primeHash(String str) {
        long hash = 1;
        for (char c : str.toCharArray()) {
            hash *= primeSayilar[c - 'a'];  // Prime multiplication
        }
        return hash;
    }
}
```

**Açıklama**: Durak arama işlemlerini O(1) ortalama karmaşıklığa indirir.

### 4. 🌐 Grid Sistem (Spatial Indexing)
```java
// LocationService.java
private static Map<String, List<Durak>> stationGrid;
private static final double GRID_SIZE = 0.01; // ~1km grid

private static String getGridKey(double lat, double lon) {
    int gridX = (int) (lat / GRID_SIZE);
    int gridY = (int) (lon / GRID_SIZE);
    return gridX + "," + gridY;
}
```

**Açıklama**: Coğrafi koordinatları grid'lere bölerek yakın durak aramayı hızlandırır.

### 5. 🎯 Priority Queue (Dijkstra İçin)
```java
PriorityQueue<Integer> pq = new PriorityQueue<>(
    Comparator.comparingDouble(i -> mesafeler[i])
);
```

**Açıklama**: En kısa yol algoritmasında minimum mesafeli düğümü seçer.

## 🚀 Kullanım Kılavuzu

### 1. Uygulama Başlatma
```bash
mvn javafx:run
# veya
java -jar rotala.jar
```

### 2. Temel Rota Planlama
1. **Başlangıç Seçimi**: 
   - Manuel olarak durak yazın
   - Veya "📍 Konum Seç" ile otomatik seçim
2. **Bitiş Durağı**: Hedef durağınızı seçin
3. **Ara Duraklar** (opsiyonel): Uğramak istediğiniz durakları ekleyin
4. **Zaman Seçimi**: Yolculuk zamanınızı belirleyin
5. **Rota Bul**: Optimal rotanızı görüntüleyin

### 3. Gelişmiş Özellikler
- **Hat Bilgileri**: Seçilen hattın tüm duraklarını görün
- **Durak Arama**: Hızlı arama ile durak bulun
- **Konum Servisi**: GPS/IP ile otomatik konum tespiti

## ⚙️ Teknik Detaylar

### Algoritma Karmaşıklıkları
- **Dijkstra**: O(V log V + E) - V: durak sayısı, E: bağlantı sayısı
- **Hash Arama**: O(1) ortalama, O(n) en kötü
- **Grid Arama**: O(1) - sabit grid boyutu
- **Prime Hash**: O(k) - k: string uzunluğu

### Veri Yapısı Seçim Nedenleri

#### 🔍 Neden Graf?
Metro sistemi doğal olarak bir graf yapısıdır:
- **Düğümler**: Metro durakları
- **Kenarlar**: Duraklar arası bağlantılar
- **Ağırlıklar**: Seyahat süreleri

#### 🔍 Neden Hash Table?
```java
// O(n) lineer arama yerine O(1) hash arama
for (int i = 0; i < duraklar.length; i++) {    // O(n)
    if (duraklar[i].getIsim().equals(aranan)) {
        return duraklar[i];
    }
}

// Bunun yerine:
return hashTable.get(hashKey);                  // O(1)
```

#### 🔍 Neden Grid Sistemi?
Coğrafi arama optimizasyonu:
```java
// Tüm durakları kontrol etmek: O(n)
for (Durak durak : tumDuraklar) {
    double mesafe = hesaplaMesafe(konum, durak);
}

// Grid ile: O(1) - sadece yakın grid'leri kontrol
List<Durak> yakinDuraklar = grid.get(gridKey);
```

## 🎯 Öğrenme Hedefleri

Bu projede aşağıdaki konuları öğrenebilirsiniz:

### 1. Veri Yapıları
- **Array ve Dynamic Array**: Durak ve hat listelerinde
- **Linked List**: Durak bağlantılarında
- **Hash Table**: Hızlı arama işlemlerinde
- **Priority Queue**: Dijkstra algoritmasında
- **Graf**: Metro ağı modellemesinde

### 2. Algoritmalar
- **Dijkstra**: En kısa yol bulma
- **Hash Fonksiyonları**: Prime multiplication hash
- **Spatial Indexing**: Grid-based arama
- **String Matching**: Durak ismi arama

### 3. Tasarım Desenleri
- **Singleton**: MetroAgi tek instance
- **MVC**: Model-View-Controller ayrımı
- **Observer**: JavaFX event handling

### 4. Optimizasyon Teknikleri
- **Memoization**: Hesaplanan rotaları cache'leme
- **Lazy Loading**: İhtiyaç duyulduğunda yükleme
- **Spatial Indexing**: Coğrafi veri optimizasyonu

## 🔧 Sistem Gereksinimleri

- **Java**: 11 veya üzeri
- **JavaFX**: 11+
- **Maven**: 3.6+
- **RAM**: Minimum 4GB
- **İnternet**: Harita ve konum servisleri için

## 📦 Bağımlılıklar

```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>11.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20210307</version>
    </dependency>
</dependencies>
```

## 🐛 Bilinen Sınırlamalar

1. **Veri Kapsamı**: Sadece 4 metro hattı (M4, M5, M8, Marmaray)
2. **Gerçek Zamanlı Veri**: Sefer saatleri statik olarak tanımlanmış
3. **Konum Servisi**: IP bazlı konum tespiti (GPS değil)
4. **Offline Kullanım**: İnternet bağlantısı gerekli

## 🔮 Gelecek Geliştirmeler

- [ ] Tüm İstanbul metro hatlarının eklenmesi
- [ ] Gerçek zamanlı sefer API entegrasyonu
- [ ] GPS tabanlı konum servisi
- [ ] Offline harita desteği
- [ ] Favoriler ve geçmiş rotalar
- [ ] Engelli erişimi bilgileri

## 👥 Geliştirici Notları

### Debug Özellikler
- Console çıktıları ile detaylı loglama
- Hash table istatistikleri
- Grid sistem performans metrikleri
- Algoritma execution time ölçümü

### Test Senaryoları
1. **Basit Rota**: Tek hatlı yolculuk
2. **Aktarmalı Rota**: Çoklu hat kullanımı
3. **Çoklu Durak**: 3+ ara durak ile
4. **Edge Cases**: Mevcut olmayan duraklar

## 📚 Öğretici Kaynaklar

### Veri Yapıları
- [Graf Teorisi Temelleri](https://en.wikipedia.org/wiki/Graph_theory)
- [Hash Table Implementasyonu](https://en.wikipedia.org/wiki/Hash_table)
- [Dijkstra Algoritması](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)

### JavaFX
- [JavaFX Resmi Dokümantasyon](https://openjfx.io/)
- [FXML Kullanımı](https://docs.oracle.com/javafx/2/fxml_get_started/jfxpub-fxml_get_started.htm)

---

## 📄 Lisans

Bu proje eğitim amaçlı geliştirilmiştir. Ticari kullanım için izin gereklidir.

## 📞 İletişim

Proje ile ilgili sorularınız için GitHub Issues kullanabilirsiniz.

---

**Geliştirici**: Veri Yapıları ve Algoritmalar Dersi Grup Projesi  
**Versiyon**: 1.0.0  
**Son Güncelleme**: 2024
