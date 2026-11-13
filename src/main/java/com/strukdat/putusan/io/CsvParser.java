package com.strukdat.putusan.io;

import com.strukdat.putusan.struktur.AntrianGenerik;

// Parser CSV (pemisah koma) buatan sendiri: tanda kutip, "" sebagai kutip, CRLF/LF, dan BOM
public final class CsvParser {

    private CsvParser() {
    }

    //  * @return antrian baris; tiap baris berupa array kolom. Baris yang sepenuhnya kosong dilewati
    //  * @throws IllegalArgumentException bila tanda kutip tidak ditutup

    public static AntrianGenerik<String[]> parse(String teks) {
        AntrianGenerik<String[]> hasil = new AntrianGenerik<>();
        if (teks == null || teks.isEmpty()) {
            return hasil;
        }
        if (teks.charAt(0) == '\uFEFF') {
            teks = teks.substring(1);
        }
        AntrianGenerik<String> baris = new AntrianGenerik<>();
        StringBuilder kolom = new StringBuilder();
        boolean dalamKutip = false;
        boolean kolomBerkutip = false;
        boolean adaIsiBaris = false;
        int n = teks.length();
        for (int i = 0; i < n; i++) {
            char c = teks.charAt(i);
            if (dalamKutip) {
                if (c == '"') {
                    if (i + 1 < n && teks.charAt(i + 1) == '"') {
                        kolom.append('"');
                        i++;
                    } else {
                        dalamKutip = false;
                    }
                } else {
                    kolom.append(c);
                }
                continue;
            }
            if (c == '"' && kolom.length() == 0 && !kolomBerkutip) {
                dalamKutip = true;
                kolomBerkutip = true;
                adaIsiBaris = true;
            } else if (c == ',') {
                baris.enqueue(kolom.toString());
                kolom.setLength(0);
                kolomBerkutip = false;
                adaIsiBaris = true;
            } else if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < n && teks.charAt(i + 1) == '\n') {
                    i++;
                }
                akhiriBaris(hasil, baris, kolom, adaIsiBaris);
                kolomBerkutip = false;
                adaIsiBaris = false;
            } else {
                kolom.append(c);
                adaIsiBaris = true;
            }
        }
        if (dalamKutip) {
            throw new IllegalArgumentException("Tanda kutip ganda tidak ditutup");
        }
        akhiriBaris(hasil, baris, kolom, adaIsiBaris);
        return hasil;
    }

    private static void akhiriBaris(AntrianGenerik<String[]> hasil, AntrianGenerik<String> baris,
                                    StringBuilder kolom, boolean adaIsi) {
        if (!adaIsi) {
            return;
        }
        baris.enqueue(kolom.toString());
        kolom.setLength(0);
        String[] arr = new String[baris.size()];
        int idx = 0;
        while (!baris.isEmpty()) {
            arr[idx++] = baris.dequeue();
        }
        hasil.enqueue(arr);
    }

    // Membungkus kolom dengan kutip bila mengandung koma, kutip, atau baris baru
    public static String escape(String nilai) {
        if (nilai == null) {
            return "";
        }
        boolean perlu = nilai.indexOf(',') >= 0 || nilai.indexOf('"') >= 0
                || nilai.indexOf('\n') >= 0 || nilai.indexOf('\r') >= 0;
        if (!perlu) {
            return nilai;
        }
        return "\"" + nilai.replace("\"", "\"\"") + "\"";
    }

    public static String gabungBaris(String[] kolom) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kolom.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(kolom[i]));
        }
        return sb.toString();
    }
}
