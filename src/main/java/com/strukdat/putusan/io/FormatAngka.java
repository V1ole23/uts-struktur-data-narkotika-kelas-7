package com.strukdat.putusan.io;

import java.math.BigDecimal;

// Parsing dan format angka yang tidak bergantung pada Locale
public final class FormatAngka {

    private FormatAngka() {
    }

    // Menerima: 5, 5.39, 5,39, 1003,83, 1.003,83. Menolak teks lain atau nilai negatif
    public static double parseBerat(String teks) {
        String s = teks == null ? "" : teks.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("kosong");
        }
        String norm;
        if (s.matches("\\d+")) {
            norm = s;
        } else if (s.matches("\\d+,\\d+")) {
            norm = s.replace(',', '.');
        } else if (s.matches("\\d+\\.\\d+")) {
            norm = s;
        } else if (s.matches("\\d{1,3}(\\.\\d{3})+(,\\d+)?")) {
            norm = s.replace(".", "").replace(',', '.');
        } else {
            throw new IllegalArgumentException("format angka desimal tidak dikenali");
        }
        return Double.parseDouble(norm);
    }

    // Bilangan bulat tak negatif, titik pemisah ribuan diperbolehkan (1.000.000)
    public static long parseBulat(String teks) {
        String s = teks == null ? "" : teks.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("kosong");
        }
        String norm;
        if (s.matches("\\d+")) {
            norm = s;
        } else if (s.matches("\\d{1,3}(\\.\\d{3})+")) {
            norm = s.replace(".", "");
        } else {
            throw new IllegalArgumentException("bukan bilangan bulat tak negatif");
        }
        if (norm.length() > 18) {
            throw new IllegalArgumentException("terlalu besar");
        }
        return Long.parseLong(norm);
    }

    // Berat dengan titik desimal, minimal 2 angka di belakang titik (5.39, 2.00)
    public static String formatBerat(double nilai) {
        BigDecimal bd = BigDecimal.valueOf(nilai).stripTrailingZeros();
        if (bd.scale() < 2) {
            bd = bd.setScale(2);
        }
        return bd.toPlainString();
    }

    // Format rupiah: 1000000000 menjadi "Rp1.000.000.000"
    public static String formatRupiah(long nilai) {
        String digit = Long.toString(Math.abs(nilai));
        StringBuilder sb = new StringBuilder();
        int hitung = 0;
        for (int i = digit.length() - 1; i >= 0; i--) {
            sb.append(digit.charAt(i));
            hitung++;
            if (hitung % 3 == 0 && i > 0) {
                sb.append('.');
            }
        }
        return (nilai < 0 ? "-Rp" : "Rp") + sb.reverse();
    }
}
