package com.strukdat.putusan;

import com.strukdat.putusan.io.CsvParser;
import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.service.PutusanService;
import com.strukdat.putusan.struktur.AntrianGenerik;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Pembantu pengujian: memuat resource, konfigurasi, dan data provenance. */
final class DataUji {

    private DataUji() {
    }

    /** Membaca file relatif terhadap folder proyek (Maven menjalankan tes dari folder proyek). */
    static String baca(String path) {
        try {
            return java.nio.file.Files.readString(java.nio.file.Path.of(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Tidak dapat membaca " + path, e);
        }
    }

    private static String bacaResource(String nama) {
        try (InputStream in = DataUji.class.getResourceAsStream("/" + nama)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Membaca baris "kunci=angka" dari uji.config tanpa memakai koleksi bawaan. */
    static int konfig(String kunci) {
        for (String baris : bacaResource("uji.config").split("\\R")) {
            String b = baris.trim();
            if (b.startsWith(kunci + "=")) {
                return Integer.parseInt(b.substring(kunci.length() + 1).trim());
            }
        }
        throw new IllegalStateException("Kunci konfigurasi tidak ada: " + kunci);
    }

    static PutusanService layananTermuat() {
        PutusanService s = new PutusanService();
        s.imporDariTeks(baca("data/putusan-50.csv"), "data/putusan-50.csv");
        return s;
    }

    static String[] idUrut(AntrianGenerik<Putusan> q) {
        String[] hasil = new String[q.size()];
        q.untukSetiap((p, i) -> hasil[i] = p.getCaseId());
        return hasil;
    }

    static String idGabung(AntrianGenerik<Putusan> q) {
        return String.join(",", idUrut(q));
    }

    // -------------------------------------------------------------- provenance

    /** Baris provenance tanpa header: [case_id, field_name, value, source_file, source_page, extractor, reviewer, status]. */
    static String[][] provenance() {
        AntrianGenerik<String[]> q = CsvParser.parse(baca("docs/data-provenance.csv"));
        q.dequeue(); // header
        String[][] hasil = new String[q.size()][];
        int i = 0;
        while (!q.isEmpty()) {
            hasil[i++] = q.dequeue();
        }
        return hasil;
    }

    static String nilai(String[][] pv, String caseId, String field) {
        for (String[] r : pv) {
            if (r[0].equals(caseId) && r[1].equals(field)) {
                return r[2];
            }
        }
        throw new IllegalStateException("Provenance tidak punya " + caseId + "/" + field);
    }

    /** Urutan caseId sesuai kemunculan baris nomorPerkara pada provenance. */
    static String[] daftarCaseId(String[][] pv) {
        int n = 0;
        for (String[] r : pv) {
            if (r[1].equals("nomorPerkara")) {
                n++;
            }
        }
        String[] ids = new String[n];
        int i = 0;
        for (String[] r : pv) {
            if (r[1].equals("nomorPerkara")) {
                ids[i++] = r[0];
            }
        }
        return ids;
    }

    static String[] pecah(String nilai) {
        String[] mentah = nilai.split("\\|");
        String[] hasil = new String[mentah.length];
        for (int i = 0; i < mentah.length; i++) {
            hasil[i] = mentah[i].trim();
        }
        return hasil;
    }

    static double angka(String s) {
        return Double.parseDouble(s.trim().replace(',', '.'));
    }

    static int vonisMaks(String[][] pv, String id) {
        int m = 0;
        for (String x : pecah(nilai(pv, id, "vonisBulan"))) {
            m = Math.max(m, Integer.parseInt(x));
        }
        return m;
    }

    static long dendaJumlah(String[][] pv, String id) {
        long s = 0;
        for (String x : pecah(nilai(pv, id, "dendaRupiah"))) {
            s += Long.parseLong(x);
        }
        return s;
    }
}
