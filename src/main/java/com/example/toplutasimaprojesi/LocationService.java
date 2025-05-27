package com.example.toplutasimaprojesi;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.*;

public class LocationService {

    // Grid sistemi için
    private static Map<String, List<Durak>> stationGrid;
    private static final double GRID_SIZE = 0.01; // ~1km grid boyutu
    private static boolean gridInitialized = false;

    public static class LocationInfo {
        public double latitude;
        public double longitude;
        public String city;
        public String country;
        public String address;

        public LocationInfo(double lat, double lon, String city, String country) {
            this.latitude = lat;
            this.longitude = lon;
            this.city = city;
            this.country = country;
        }

        @Override
        public String toString() {
            return String.format("Konum: %.6f, %.6f (%s, %s)",
                    latitude, longitude, city, country);
        }
    }

    public interface LocationCallback {
        void onLocationReceived(LocationInfo location);
        void onError(String error);
    }

    // Grid sistemini başlat
    public static void initializeGrid(MetroAgi metroAgi) {
        if (gridInitialized) return;

        stationGrid = new HashMap<>();

        // Tüm durakları grid'e yerleştir
        for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
            Durak durak = metroAgi.getDurakIndex(i);
            String gridKey = getGridKey(durak.getXKoordinat(), durak.getYKoordinat());

            stationGrid.computeIfAbsent(gridKey, k -> new ArrayList<>()).add(durak);
        }

        gridInitialized = true;
        System.out.println("🔥 Grid sistemi hazırlandı - Toplam cell: " + stationGrid.size());

        // Grid istatistikleri
        printGridStats();
    }

    // Koordinatı grid key'e çevir
    private static String getGridKey(double lat, double lon) {
        int gridX = (int) (lat / GRID_SIZE);
        int gridY = (int) (lon / GRID_SIZE);
        return gridX + "," + gridY;
    }

    // IP tabanlı konum belirleme
    public static void getCurrentLocationByIP(LocationCallback callback) {
        Task<LocationInfo> task = new Task<LocationInfo>() {
            @Override
            protected LocationInfo call() throws Exception {
                try {
                    // ip-api.com servisi (ücretsiz, günde 1000 istek limiti)
                    URL url = new URL("http://ip-api.com/json/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "JavaMetroApp/1.0");
                    conn.setConnectTimeout(5000); // 5 saniye timeout
                    conn.setReadTimeout(5000);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());

                    if ("success".equals(json.getString("status"))) {
                        double lat = json.getDouble("lat");
                        double lon = json.getDouble("lon");
                        String city = json.getString("city");
                        String country = json.getString("country");

                        System.out.println("🌍 Konum alındı: " + city + ", " + country +
                                " (" + lat + ", " + lon + ")");

                        return new LocationInfo(lat, lon, city, country);
                    } else {
                        throw new Exception("Konum alınamadı: " + json.getString("message"));
                    }

                } catch (Exception e) {
                    // Fallback: ipinfo.io servisi dene
                    System.out.println("⚠️ Ana servis başarısız, fallback deneniyor...");
                    return tryFallbackService();
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    callback.onLocationReceived(getValue());
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    callback.onError(getException().getMessage());
                });
            }
        };

        new Thread(task).start();
    }

    // Alternatif servis (ipinfo.io)
    private static LocationInfo tryFallbackService() throws Exception {
        try {
            URL url = new URL("https://ipinfo.io/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "JavaMetroApp/1.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());

            String[] latLon = json.getString("loc").split(",");
            double lat = Double.parseDouble(latLon[0]);
            double lon = Double.parseDouble(latLon[1]);
            String city = json.optString("city", "Bilinmiyor");
            String country = json.optString("country", "Bilinmiyor");

            System.out.println("🔄 Fallback servis başarılı: " + city + ", " + country);

            return new LocationInfo(lat, lon, city, country);

        } catch (Exception e) {
            throw new Exception("Tüm konum servisleri başarısız: " + e.getMessage());
        }
    }

    // GRID-BASED en yakın durak bulma (SÜPER HIZLI!)
    public static NearestStationResult findNearestStationOptimized(LocationInfo userLocation, MetroAgi metroAgi) {
        if (!gridInitialized) {
            initializeGrid(metroAgi);
        }

        return findNearestStationGrid(userLocation.latitude, userLocation.longitude);
    }

    // Grid-based arama
    private static NearestStationResult findNearestStationGrid(double userLat, double userLon) {
        String userGridKey = getGridKey(userLat, userLon);

        // Önce aynı grid cell'e bak
        List<Durak> sameGridStations = stationGrid.get(userGridKey);
        if (sameGridStations != null && !sameGridStations.isEmpty()) {
            return findNearestInList(sameGridStations, userLat, userLon);
        }

        // Bulamazsa komşu grid'lere bak
        return searchNeighborGrids(userLat, userLon);
    }

    // Komşu grid'lerde ara
    private static NearestStationResult searchNeighborGrids(double userLat, double userLon) {
        int userGridX = (int) (userLat / GRID_SIZE);
        int userGridY = (int) (userLon / GRID_SIZE);

        Durak nearest = null;
        double minDistance = Double.MAX_VALUE;

        // 3x3 komşu grid'leri kontrol et (9 cell)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                String neighborKey = (userGridX + dx) + "," + (userGridY + dy);
                List<Durak> neighbors = stationGrid.get(neighborKey);

                if (neighbors != null) {
                    for (Durak station : neighbors) {
                        double distance = calculateDistanceKm(userLat, userLon,
                                station.getXKoordinat(),
                                station.getYKoordinat());

                        if (distance < minDistance) {
                            minDistance = distance;
                            nearest = station;
                        }
                    }
                }
            }
        }

        return new NearestStationResult(nearest, minDistance);
    }

    // Liste içinde en yakını bul
    private static NearestStationResult findNearestInList(List<Durak> stations, double userLat, double userLon) {
        Durak nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Durak station : stations) {
            double distance = calculateDistanceKm(userLat, userLon,
                    station.getXKoordinat(),
                    station.getYKoordinat());

            if (distance < minDistance) {
                minDistance = distance;
                nearest = station;
            }
        }

        return new NearestStationResult(nearest, minDistance);
    }

    // En yakın durak sonucu
    public static class NearestStationResult {
        public final Durak station;
        public final double distanceKm;

        public NearestStationResult(Durak station, double distanceKm) {
            this.station = station;
            this.distanceKm = distanceKm;
        }

        public String getFormattedDistance() {
            if (distanceKm < 1.0) {
                return String.format("%.0f m", distanceKm * 1000);
            } else {
                return String.format("%.1f km", distanceKm);
            }
        }

        public boolean isTooFar() {
            return distanceKm > 15.0; // 15km'den uzaksa çok uzak
        }

        public boolean needsConfirmation() {
            return distanceKm > 5.0 && distanceKm <= 15.0; // 5-15km arası onay iste
        }
    }

    // Haversine formülü - gerçek mesafe hesabı (km)
    private static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Dünya yarıçapı (km)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // km cinsinden
    }

    // Grid istatistikleri yazdır
    private static void printGridStats() {
        System.out.println("=== 📊 GRID İSTATİSTİKLERİ ===");
        System.out.println("Grid boyutu: " + GRID_SIZE + "° (~" +
                String.format("%.1f", GRID_SIZE * 111) + " km)");
        System.out.println("Toplam dolu grid cell: " + stationGrid.size());

        // En kalabalık cell
        String mostCrowded = stationGrid.entrySet().stream()
                .max(Map.Entry.comparingByValue((a, b) -> a.size() - b.size()))
                .map(Map.Entry::getKey)
                .orElse("Yok");

        if (stationGrid.containsKey(mostCrowded)) {
            int maxStations = stationGrid.get(mostCrowded).size();
            System.out.println("En kalabalık cell: " + mostCrowded + " (" + maxStations + " durak)");
        }

        // Ortalama durak sayısı
        double avgStations = stationGrid.values().stream()
                .mapToInt(List::size)
                .average()
                .orElse(0);

        System.out.println("Cell başına ortalama durak: " + String.format("%.1f", avgStations));
        System.out.println("===============================");
    }

    // En yakın 3 durağı bul (alternatif öneriler için)
    public static List<NearestStationResult> findNearestStations(LocationInfo userLocation, MetroAgi metroAgi, int count) {
        if (!gridInitialized) {
            initializeGrid(metroAgi);
        }

        List<NearestStationResult> allResults = new ArrayList<>();

        // Tüm durakları kontrol et ve mesafeleri hesapla
        for (int i = 0; i < metroAgi.getDurakSayisi(); i++) {
            Durak durak = metroAgi.getDurakIndex(i);
            double distance = calculateDistanceKm(userLocation.latitude, userLocation.longitude,
                    durak.getXKoordinat(), durak.getYKoordinat());
            allResults.add(new NearestStationResult(durak, distance));
        }

        // Mesafeye göre sırala ve ilk 'count' tanesini al
        return allResults.stream()
                .sorted((a, b) -> Double.compare(a.distanceKm, b.distanceKm))
                .limit(count)
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
}