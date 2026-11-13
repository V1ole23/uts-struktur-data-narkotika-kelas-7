package com.strukdat.putusan.io;

import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.struktur.AntrianGenerik;
import com.strukdat.putusan.struktur.HasilCari;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;


//  Membaca CSV dan memvalidasi tiap record. Record tidak valid ditolak dengan pesan
//  impor tetap berlanjut dan antrian hanya berisi record valid

public final class ImporterPutusan {

    public static final String[] HEADER = {
        "caseId", "nomorPerkara", "pengadilan", "tanggalPutusan", "terdakwaKode", "jenisNarkotika",
        "beratBarangBuktiGram", "pasalDakwaan", "pasalTerbukti", "tuntutanBulan", "vonisBulan",
        "dendaRupiah", "barangBukti", "keadaanMemberatkan", "keadaanMeringankan", "sourcePages"
    };

    private ImporterPutusan() {
    }

    public static HasilImpor impor(String teks, AntrianGenerik<Putusan> sasaran) {
        AntrianGenerik<String> galat = new AntrianGenerik<>();
        AntrianGenerik<String[]> baris;
        try {
            baris = CsvParser.parse(teks);
        } catch (IllegalArgumentException e) {
            galat.enqueue("File tidak dapat dibaca: " + e.getMessage());
            return new HasilImpor(0, 0, galat);
        }
        if (baris.isEmpty() || !headerSesuai(baris.dequeue())) {
            galat.enqueue("Header tidak sesuai: harus " + String.join(",", HEADER));
            return new HasilImpor(0, 0, galat);
        }
        int[] hitung = new int[2]; // [0] dibaca, [1] diterima
        baris.untukSetiap((f, idx) -> {
            hitung[0]++;
            StringBuilder masalah = new StringBuilder();
            Putusan p = buatDariField(f, sasaran, masalah);
            if (p == null) {
                String id = f.length > 0 && !f[0].trim().isEmpty() ? f[0].trim() : "?";
                galat.enqueue("Baris data ke-" + (idx + 1) + " [" + id + "] DITOLAK: " + masalah);
            } else {
                sasaran.enqueue(p);
                hitung[1]++;
            }
        });
        return new HasilImpor(hitung[0], hitung[1], galat);
    }

    private static boolean headerSesuai(String[] h) {
        if (h.length != HEADER.length) {
            return false;
        }
        for (int i = 0; i < HEADER.length; i++) {
            if (!HEADER[i].equals(h[i].trim())) {
                return false;
            }
        }
        return true;
    }

    //  Memvalidasi satu record. Mengembalikan Putusan bila valid. bila tidak, mengembalikan null dan
    //  mengisi {@code masalah} dengan semua alasan. Duplikat diperiksa terhadap isi {@code sasaran}.
    public static Putusan buatDariField(String[] f, AntrianGenerik<Putusan> sasaran, StringBuilder masalah) {
        if (f.length != HEADER.length) {
            catat(masalah, "jumlah kolom " + f.length + ", seharusnya " + HEADER.length);
            return null;
        }
        String caseId = f[0].trim();
        String nomor = f[1].trim();
        String pengadilan = f[2].trim();

        if (!caseId.matches("P\\d{3}")) {
            catat(masalah, "caseId '" + caseId + "' harus berformat Pnnn");
        } else if (sasaran.rotateSearch(p -> p.getCaseId().equals(caseId)).isDitemukan()) {
            catat(masalah, "caseId " + caseId + " duplikat");
        }
        if (!nomor.matches("\\d+/Pid\\.Sus/\\d{4}/PN [A-Za-z]+")) {
            catat(masalah, "nomorPerkara '" + nomor + "' tidak berformat nomor/Pid.Sus/tahun/PN Kota");
        } else {
            HasilCari<Putusan> h = sasaran.rotateSearch(p -> p.getNomorPerkara().equalsIgnoreCase(nomor));
            if (h.isDitemukan()) {
                catat(masalah, "nomorPerkara duplikat dengan " + h.getElemen().getCaseId());
            }
        }
        if (pengadilan.isEmpty()) {
            catat(masalah, "pengadilan kosong");
        }

        LocalDate tanggal = null;
        String t = f[3].trim();
        if (!t.matches("\\d{4}-\\d{2}-\\d{2}")) {
            catat(masalah, "tanggalPutusan '" + t + "' tidak berformat yyyy-MM-dd");
        } else {
            try {
                tanggal = LocalDate.parse(t);
            } catch (DateTimeParseException e) {
                catat(masalah, "tanggalPutusan '" + t + "' bukan tanggal yang valid");
            }
        }

        String[] kode = pecah(f[4]);
        boolean kodeOk = kode.length > 0;
        if (!kodeOk) {
            catat(masalah, "terdakwaKode kosong");
        }
        for (int i = 0; i < kode.length; i++) {
            String k = kode[i];
            if (!k.matches("T\\d{3}")) {
                catat(masalah, "terdakwaKode '" + k + "' harus berformat Tnnn");
                kodeOk = false;
                continue;
            }
            for (int j = 0; j < i; j++) {
                if (kode[j].equals(k)) {
                    catat(masalah, "terdakwaKode " + k + " muncul dua kali dalam satu record");
                    kodeOk = false;
                }
            }
            HasilCari<Putusan> h = sasaran.rotateSearch(p -> p.memuatKodeTerdakwa(k));
            if (h.isDitemukan()) {
                catat(masalah, "terdakwaKode " + k + " sudah dipakai " + h.getElemen().getCaseId());
            }
        }

        String[] jenis = pecah(f[5]);
        if (jenis.length == 0) {
            catat(masalah, "jenisNarkotika kosong");
        }
        for (int i = 0; i < jenis.length; i++) {
            jenis[i] = judulKata(jenis[i]);
        }

        double berat = 0;
        try {
            berat = FormatAngka.parseBerat(f[6]);
        } catch (IllegalArgumentException e) {
            catat(masalah, "beratBarangBuktiGram '" + f[6].trim() + "' tidak valid (" + e.getMessage() + ")");
        }

        String pasalDakwaan = String.join(Putusan.PEMISAH, pecah(f[7]));
        String pasalTerbukti = String.join(Putusan.PEMISAH, pecah(f[8]));
        if (pasalDakwaan.isEmpty()) {
            catat(masalah, "pasalDakwaan kosong");
        }
        if (pasalTerbukti.isEmpty()) {
            catat(masalah, "pasalTerbukti kosong");
        }

        long[] tuntutan = daftarAngka(f[9], "tuntutanBulan", kode.length, kodeOk, masalah);
        long[] vonis = daftarAngka(f[10], "vonisBulan", kode.length, kodeOk, masalah);
        long[] denda = daftarAngka(f[11], "dendaRupiah", kode.length, kodeOk, masalah);

        String[] halaman = pecah(f[15]);
        if (halaman.length == 0) {
            catat(masalah, "sourcePages kosong");
        }
        for (String h : halaman) {
            if (!h.matches("\\d+")) {
                catat(masalah, "sourcePages memuat '" + h + "' (harus nomor halaman)");
                break;
            }
        }

        if (masalah.length() > 0) {
            return null;
        }
        return new Putusan(caseId, nomor, pengadilan, tanggal, kode, jenis, berat, pasalDakwaan, pasalTerbukti,
                tuntutan, vonis, denda, teksBebas(f[12]), teksBebas(f[13]), teksBebas(f[14]),
                String.join(Putusan.PEMISAH, halaman));
    }

    // Daftar bilangan bulat tak negatif dipisah '|'; jumlahnya harus sama dengan jumlah terdakwa
    private static long[] daftarAngka(String teks, String nama, int diharapkan, boolean kodeOk,
                                      StringBuilder masalah) {
        String[] bagian = pecah(teks);
        long[] hasil = new long[bagian.length];
        boolean ok = bagian.length > 0;
        if (!ok) {
            catat(masalah, nama + " kosong");
        }
        for (int i = 0; i < bagian.length; i++) {
            try {
                hasil[i] = FormatAngka.parseBulat(bagian[i]);
                if (hasil[i] > Integer.MAX_VALUE && !nama.equals("dendaRupiah")) {
                    throw new IllegalArgumentException("terlalu besar");
                }
            } catch (IllegalArgumentException e) {
                catat(masalah, nama + " '" + bagian[i] + "' tidak valid (" + e.getMessage() + ")");
                ok = false;
            }
        }
        if (ok && kodeOk && bagian.length != diharapkan) {
            catat(masalah, "jumlah nilai " + nama + " (" + bagian.length
                    + ") tidak sama dengan jumlah terdakwa (" + diharapkan + ")");
        }
        return hasil;
    }

    // Memecah berdasarkan '|', membuang spasi berlebih dan bagian kosong
    private static String[] pecah(String teks) {
        String[] mentah = teks.split("\\|");
        int n = 0;
        for (String m : mentah) {
            if (!m.trim().isEmpty()) {
                mentah[n++] = m.trim().replaceAll("\\s+", " ");
            }
        }
        String[] hasil = new String[n];
        System.arraycopy(mentah, 0, hasil, 0, n);
        return hasil;
    }

    private static String teksBebas(String teks) {
        String s = String.join(Putusan.PEMISAH, pecah(teks));
        return s.isEmpty() ? "null" : s;
    }

    // Kapitalisasi tiap kata, misal "sabu-sabu" menjadi "Sabu-Sabu"
    private static String judulKata(String s) {
        StringBuilder sb = new StringBuilder();
        for (String kata : s.toLowerCase().split(" ")) {
            if (!kata.isEmpty()) {
                sb.append(sb.length() > 0 ? " " : "").append(Character.toUpperCase(kata.charAt(0)))
                  .append(kata.substring(1));
            }
        }
        return sb.toString();
    }

    private static void catat(StringBuilder sb, String pesan) {
        sb.append(sb.length() > 0 ? "; " : "").append(pesan);
    }
}
