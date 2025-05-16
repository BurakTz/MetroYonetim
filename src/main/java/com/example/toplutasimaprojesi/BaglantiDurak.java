package com.example.toplutasimaprojesi;

// Bağlantılı durak bilgilerini tutan sınıf (Edge)
//BaglantiDurak dediğin yapı aslında dümdüz bir LinkedList:
public class BaglantiDurak {
    private Durak durak;      // Bağlantılı durak
    private String hatIsmi;   // Hangi hatta bağlı olduğu
    public BaglantiDurak sonraki;// Bağlı liste yapısı için
    private int gecenSure;

    public BaglantiDurak(Durak durak, String hatIsmi,int gecenSure) {
        this.durak = durak;
        this.hatIsmi = hatIsmi;
        this.sonraki = null;
        this.gecenSure=gecenSure;
    }

    public Durak getDurak() {
        return durak;
    }

    public String getHatIsmi() {
        return hatIsmi;
    }

    public int getGecenSure() {
        return gecenSure;
    }

    public void setGecenSure(int gecenSure) {
        this.gecenSure = gecenSure;
    }
}