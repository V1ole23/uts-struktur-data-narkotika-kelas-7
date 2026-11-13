package com.strukdat.putusan.service;

import com.strukdat.putusan.struktur.AntrianGenerik;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Riwayat operasi pengguna
public final class LogAudit {
    private static final DateTimeFormatter WAKTU = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AntrianGenerik<String> riwayat = new AntrianGenerik<>();

    public void catat(String operasi, String detail, String hasil) {
        riwayat.enqueue(LocalDateTime.now().format(WAKTU) + " | " + operasi + " | " + detail + " | " + hasil);
    }

    public AntrianGenerik<String> getRiwayat() { return riwayat; }

    public String tampil() {
        if (riwayat.isEmpty()) {
            return "(riwayat audit kosong)";
        }
        StringBuilder sb = new StringBuilder("RIWAYAT AUDIT (" + riwayat.size() + " operasi)\n");
        riwayat.untukSetiap((e, i) -> sb.append(String.format("%3d. %s%n", i + 1, e)));
        return sb.toString().stripTrailing();
    }
}
