package com.strukdat.putusan.struktur;

/** Hasil rotateSearch beserta pencacah langkahnya. */
public final class HasilCari<T> {
    private final boolean ditemukan;
    private final int posisi;          // posisi pada urutan asli, -1 bila tidak ada
    private final int langkahBanding;  // jumlah elemen yang dibandingkan
    private final int langkahRotasi;   // dequeue+enqueue saat mencari
    private final int langkahPemulihan; // dequeue+enqueue saat restore
    private final T elemen;

    public HasilCari(boolean ditemukan, int posisi, int langkahBanding, int langkahRotasi,
                     int langkahPemulihan, T elemen) {
        this.ditemukan = ditemukan;
        this.posisi = posisi;
        this.langkahBanding = langkahBanding;
        this.langkahRotasi = langkahRotasi;
        this.langkahPemulihan = langkahPemulihan;
        this.elemen = elemen;
    }

    public boolean isDitemukan() { return ditemukan; }
    public int getPosisi() { return posisi; }
    public int getLangkahBanding() { return langkahBanding; }
    public int getLangkahRotasi() { return langkahRotasi; }
    public int getLangkahPemulihan() { return langkahPemulihan; }
    public T getElemen() { return elemen; }

    @Override
    public String toString() {
        if (!ditemukan) {
            return "TIDAK DITEMUKAN (" + langkahBanding + " langkah)";
        }
        return "DITEMUKAN di posisi " + posisi + " (" + langkahBanding + " langkah banding, "
                + langkahRotasi + " rotasi, " + langkahPemulihan + " pemulihan)";
    }
}
