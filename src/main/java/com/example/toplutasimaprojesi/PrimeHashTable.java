package com.example.toplutasimaprojesi;

import java.util.ArrayList;

public class PrimeHashTable {
    private long[] hashKeys;
    private ArrayList<String>[] values;
    private int capacity;
    private int size;  // Eklenmiş size takibi

    public PrimeHashTable() {
        this.capacity = 1000;  // 200'den 1000'e çıkarıldı
        this.hashKeys = new long[capacity];
        this.values = new ArrayList[capacity];
        this.size = 0;
    }

    public void put(long hashKey, String durak) {
        // Kapasite kontrolü
        if (size >= capacity * 0.7) {
            System.out.println("UYARI: Hash table kapasitesi %70'e ulaştı!");
            return;
        }

        int index = findSlot(hashKey);
        if (index == -1) {
            System.out.println("HATA: Hash table dolu, ekleme yapılamadı!");
            return;
        }

        if (values[index] == null) {
            hashKeys[index] = hashKey;
            values[index] = new ArrayList<>();
            size++;
        }

        if (!values[index].contains(durak)) {
            values[index].add(durak);
        }
    }

    public ArrayList<String> get(long hashKey) {
        int index = findSlot(hashKey);
        if (index == -1) {
            return null;
        }
        return values[index];
    }

    private int findSlot(long hashKey) {
        int index = (int)(Math.abs(hashKey) % capacity);
        int startIndex = index;  // Sonsuz döngü kontrolü için

        while (hashKeys[index] != 0 && hashKeys[index] != hashKey) {
            index = (index + 1) % capacity;

            // Sonsuz döngü kontrolü
            if (index == startIndex) {
                System.out.println("HATA: Hash table tamamen dolu!");
                return -1;
            }
        }
        return index;
    }

    // Debug için bilgi metodu
    public void printStats() {
        System.out.println("Hash Table İstatistikleri:");
        System.out.println("Kapasite: " + capacity);
        System.out.println("Kullanılan slot: " + size);
        System.out.println("Doluluk oranı: %" + (size * 100 / capacity));

        int toplamDurak = 0;
        for (int i = 0; i < capacity; i++) {
            if (values[i] != null) {
                toplamDurak += values[i].size();
            }
        }
        System.out.println("Toplam durak referansı: " + toplamDurak);
    }
}