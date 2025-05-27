package com.example.toplutasimaprojesi;

public class MyHashMap<K, V> {

    // İç sınıf - Key-Value çiftini tutmak için
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next; // Çakışma durumu için linked list

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private Entry<K, V>[] buckets; // Ana dizi
    private int capacity; // Dizi boyutu
    private int size; // Mevcut eleman sayısı
    private static final int DEFAULT_CAPACITY = 16;

    // Constructor - varsayılan kapasiteyle
    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    // Constructor - özel kapasiteyle
    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity) {
        this.capacity = capacity;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    // Hash fonksiyonu
    private int hash(K key) {
        if (key == null) return 0;
        return Math.abs(key.hashCode() % capacity);
    }

    // Eleman ekleme
    public V put(K key, V value) {
        int index = hash(key);
        Entry<K, V> head = buckets[index];

        // Eğer bu index boşsa, yeni entry oluştur
        if (head == null) {
            buckets[index] = new Entry<>(key, value);
            size++;
            return null;
        }

        // Aynı key varsa değeri güncelle
        Entry<K, V> current = head;
        while (current != null) {
            if (current.key != null && current.key.equals(key)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            if (current.next == null) break;
            current = current.next;
        }

        // Yeni entry'yi zincirin sonuna ekle
        current.next = new Entry<>(key, value);
        size++;
        return null;
    }

    // Eleman getirme
    public V get(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if (current.key != null && current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }

        return null; // Bulunamadı
    }


    public V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return (value != null) ? value : defaultValue;
    }

    // Key var mı kontrol et
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    // Eleman silme
    public V remove(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        Entry<K, V> prev = null;

        while (current != null) {
            if (current.key != null && current.key.equals(key)) {
                if (prev == null) {
                    // İlk eleman silinecek
                    buckets[index] = current.next;
                } else {
                    // Ortadaki veya son eleman silinecek
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }

        return null; // Bulunamadı
    }

    // HashMap boş mu?
    public boolean isEmpty() {
        return size == 0;
    }

    // Eleman sayısı
    public int size() {
        return size;
    }

    // Tüm elemanları temizle
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buckets[i] = null;
        }
        size = 0;
    }

    // Debug için - içeriği yazdır
    public void printAll() {
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            if (current != null) {
                System.out.print("Bucket " + i + ": ");
                while (current != null) {
                    System.out.print("[" + current.key + "=" + current.value + "] ");
                    current = current.next;
                }
                System.out.println();
            }
        }
    }

    // Tüm key'leri döndür
    public java.util.Set<K> keySet() {
        java.util.Set<K> keys = new java.util.HashSet<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                keys.add(current.key);
                current = current.next;
            }
        }
        return keys;
    }
}