package com.strukdat.putusan.model;

import com.strukdat.putusan.io.FormatAngka;
import java.time.LocalDate;
import java.util.Objects;

//  Satu putusan. Perkara dengan >1 terdakwa: tuntutanBulan dan vonisBulan = nilai tertinggi
//  dendaRupiah = jumlah denda semua terdakwa. Nilai per terdakwa tetap disimpan untuk ekspor

public final class Putusan {

    public static final String PEMISAH = " | ";

    private final String caseId;
    private final String nomorPerkara;
    private final String pengadilan;
    private final LocalDate tanggalPutusan;
    private final String terdakwaKode;
    private final String jenisNarkotika;
    private final double beratBarangBuktiGram;
    private final String pasalDakwaan;
    private final String pasalTerbukti;
    private final int tuntutanBulan;
    private final int vonisBulan;
    private final long dendaRupiah;
    private final String barangBukti;
    private final String keadaanMemberatkan;
    private final String keadaanMeringankan;
    private final String sourcePages;

    private final String[] kode;
    private final String[] jenis;
    private final long[] tuntutanPer;
    private final long[] vonisPer;
    private final long[] dendaPer;

    public Putusan(String caseId, String nomorPerkara, String pengadilan, LocalDate tanggalPutusan,
                   String[] kode, String[] jenis, double berat, String pasalDakwaan, String pasalTerbukti,
                   long[] tuntutanPer, long[] vonisPer, long[] dendaPer,
                   String barangBukti, String memberatkan, String meringankan, String sourcePages) {
        if (kode.length == 0 || tuntutanPer.length != kode.length || vonisPer.length != kode.length || dendaPer.length != kode.length) {
            throw new IllegalArgumentException("Jumlah nilai per terdakwa harus sama dengan jumlah terdakwa");
        }
        this.caseId = caseId;
        this.nomorPerkara = nomorPerkara;
        this.pengadilan = pengadilan;
        this.tanggalPutusan = tanggalPutusan;
        this.kode = kode.clone();
        this.jenis = jenis.clone();
        this.terdakwaKode = String.join(PEMISAH, kode);
        this.jenisNarkotika = String.join(PEMISAH, jenis);
        this.beratBarangBuktiGram = berat;
        this.pasalDakwaan = pasalDakwaan;
        this.pasalTerbukti = pasalTerbukti;
        this.tuntutanPer = tuntutanPer.clone();
        this.vonisPer = vonisPer.clone();
        this.dendaPer = dendaPer.clone();
        this.tuntutanBulan = (int) maks(tuntutanPer);
        this.vonisBulan = (int) maks(vonisPer);
        this.dendaRupiah = jumlah(dendaPer);
        this.barangBukti = barangBukti;
        this.keadaanMemberatkan = memberatkan;
        this.keadaanMeringankan = meringankan;
        this.sourcePages = sourcePages;
    }

    private static long maks(long[] a) {
        long m = a[0];
        for (long x : a) {
            m = Math.max(m, x);
        }
        return m;
    }

    private static long jumlah(long[] a) {
        long s = 0;
        for (long x : a) {
            s += x;
        }
        return s;
    }

    private static String gabung(long[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            sb.append(i > 0 ? PEMISAH : "").append(a[i]);
        }
        return sb.toString();
    }

    public String getCaseId() { return caseId; }
    public String getNomorPerkara() { return nomorPerkara; }
    public String getPengadilan() { return pengadilan; }
    public LocalDate getTanggalPutusan() { return tanggalPutusan; }
    public String getTerdakwaKode() { return terdakwaKode; }
    public String getJenisNarkotika() { return jenisNarkotika; }
    public double getBeratBarangBuktiGram() { return beratBarangBuktiGram; }
    public String getPasalDakwaan() { return pasalDakwaan; }
    public String getPasalTerbukti() { return pasalTerbukti; }
    /// Nilai tertinggi di antara terdakwa
    public int getTuntutanBulan() { return tuntutanBulan; }
    // Nilai tertinggi di antara terdakwa. 0 = tidak terukur (seumur hidup/mati/nihil)
    public int getVonisBulan() { return vonisBulan; }
    /// Jumlah denda semua terdakwa.
    public long getDendaRupiah() { return dendaRupiah; }
    public String getBarangBukti() { return barangBukti; }
    public String getKeadaanMemberatkan() { return keadaanMemberatkan; }
    public String getKeadaanMeringankan() { return keadaanMeringankan; }
    public String getSourcePages() { return sourcePages; }
    public String[] getKodeTerdakwa() { return kode.clone(); }
    public String[] getJenisArray() { return jenis.clone(); }
    public int getJumlahTerdakwa() { return kode.length; }

    public boolean memuatKodeTerdakwa(String k) {
        for (String x : kode) {
            if (x.equals(k)) {
                return true;
            }
        }
        return false;
    }

    public boolean memuatJenis(String j) {
        for (String x : jenis) {
            if (x.equalsIgnoreCase(j)) {
                return true;
            }
        }
        return false;
    }

    /// 16 kolom untuk ekspor CSV (nilai per terdakwa dipertahankan)
    public String[] keFieldCsv() {
        return new String[] {
            caseId, nomorPerkara, pengadilan, tanggalPutusan.toString(), terdakwaKode, jenisNarkotika,
            FormatAngka.formatBerat(beratBarangBuktiGram), pasalDakwaan, pasalTerbukti,
            gabung(tuntutanPer), gabung(vonisPer), gabung(dendaPer),
            barangBukti, keadaanMemberatkan, keadaanMeringankan, sourcePages
        };
    }

    public String ringkas() {
        return String.format("%-5s | %-26s | %s | %-14s | %-18s | %10s g | tuntut %3d | vonis %3d | denda %s",
                caseId, nomorPerkara, tanggalPutusan, terdakwaKode, jenisNarkotika,
                FormatAngka.formatBerat(beratBarangBuktiGram), tuntutanBulan, vonisBulan,
                FormatAngka.formatRupiah(dendaRupiah));
    }

    public String detail() {
        String[] label = {"caseId", "nomorPerkara", "pengadilan", "tanggalPutusan", "terdakwaKode",
            "jenisNarkotika", "beratBarangBuktiGram", "pasalDakwaan", "pasalTerbukti", "tuntutanBulan",
            "vonisBulan", "dendaRupiah", "barangBukti", "keadaanMemberatkan", "keadaanMeringankan", "sourcePages"};
        String[] isi = keFieldCsv();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < label.length; i++) {
            sb.append(String.format("%-21s: %s%n", label[i], isi[i]));
        }
        return sb.toString().stripTrailing();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Putusan && caseId.equals(((Putusan) o).caseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId);
    }

    @Override
    public String toString() {
        return caseId + "(" + nomorPerkara + ")";
    }
}
