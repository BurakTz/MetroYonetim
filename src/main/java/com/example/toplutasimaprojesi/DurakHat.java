package com.example.toplutasimaprojesi;

// Hat üzerindeki durakları tutan sınıf (çift yönlü bağlı liste)
public class DurakHat {
    private Durak durak;
    public DurakHat onceki;   // Önceki durak
    public DurakHat sonraki;  // Sonraki durak

    public DurakHat(Durak durak) {
        this.durak = durak;
        this.onceki = null;
        this.sonraki = null;
    }

    public Durak getDurak() {
        return durak;
    }
}