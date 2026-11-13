package com.strukdat.putusan.io;

import com.strukdat.putusan.struktur.AntrianGenerik;

// Ringkasan impor: jumlah dibaca, diterima, dan pesan galat per record yang ditolak
public final class HasilImpor {
    private final int dibaca;
    private final int diterima;
    private final AntrianGenerik<String> galat;

    public HasilImpor(int dibaca, int diterima, AntrianGenerik<String> galat) {
        this.dibaca = dibaca;
        this.diterima = diterima;
        this.galat = galat;
    }

    public int getDibaca() { return dibaca; }
    public int getDiterima() { return diterima; }
    public int getDitolak() { return dibaca - diterima; }
    public AntrianGenerik<String> getGalat() { return galat; }

    public String ringkasan() {
        StringBuilder sb = new StringBuilder("Dibaca " + dibaca + " record, diterima " + diterima
                + ", ditolak " + getDitolak() + ".");
        galat.untukSetiap((pesan, i) -> sb.append("\n  [ERROR] ").append(pesan));
        return sb.toString();
    }
}
