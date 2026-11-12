package com.uts.strukdat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    /**
     * Membaca file CSV putusan dan memuatnya ke dalam MyQueue.
     * Mengembalikan jumlah baris yang berhasil dimuat serta mencatat error jika ada.
     */
    public static MyQueue<PutusanNarkotika> parseCsv(String filePath, List<String> errorLog) {
        MyQueue<PutusanNarkotika> queue = new MyQueue<>();
        List<String> seenCaseIds = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Baca header
            if (line == null) {
                errorLog.add("File CSV kosong.");
                return queue;
            }

            int rowNumber = 1;
            while ((line = br.readLine()) != null) {
                rowNumber++;
                List<String> tokens = parseCsvLine(line);

                if (tokens.size() != 16) {
                    errorLog.add("Baris " + rowNumber + ": Jumlah kolom tidak valid (" + tokens.size() + " dari 16).");
                    continue;
                }

                try {
                    String caseId = tokens.get(0).trim();
                    String nomorPerkara = tokens.get(1).trim();
                    String pengadilan = tokens.get(2).trim();
                    String tanggalPutusan = tokens.get(3).trim();
                    String terdakwaKode = tokens.get(4).trim();
                    String jenisNarkotika = tokens.get(5).trim();
                    
                    // Konversi berat (menangani koma desimal seperti "5,39")
                    String beratStr = tokens.get(6).trim().replace(",", ".");
                    double beratBarangBuktiGram = beratStr.isEmpty() ? 0.0 : Double.parseDouble(beratStr);
                    
                    String pasalDakwaan = tokens.get(7).trim();
                    String pasalTerbukti = tokens.get(8).trim();
                    
                    // Penanganan multi-terdakwa (ambil nilai tertinggi untuk bulan)
                    int tuntutanBulan = parseMaxInt(tokens.get(9).trim());
                    int vonisBulan = parseMaxInt(tokens.get(10).trim());
                    
                    // Penanganan multi-terdakwa (jumlahkan denda)
                    long dendaRupiah = parseSumLong(tokens.get(11).trim());
                    
                    String barangBukti = tokens.get(12).trim();
                    String keadaanMemberatkan = tokens.get(13).trim();
                    String keadaanMeringankan = tokens.get(14).trim();
                    String sourcePages = tokens.get(15).trim();

                    // Validasi caseId
                    if (!caseId.matches("P\\d{3}")) {
                        errorLog.add("Baris " + rowNumber + ": Format caseId tidak valid (" + caseId + ").");
                        continue;
                    }
                    if (seenCaseIds.contains(caseId)) {
                        errorLog.add("Baris " + rowNumber + ": caseId duplikat (" + caseId + ").");
                        continue;
                    }

                    seenCaseIds.add(caseId);

                    PutusanNarkotika putusan = new PutusanNarkotika(
                        caseId, nomorPerkara, pengadilan, tanggalPutusan, terdakwaKode,
                        jenisNarkotika, beratBarangBuktiGram, pasalDakwaan, pasalTerbukti,
                        tuntutanBulan, vonisBulan, dendaRupiah, barangBukti,
                        keadaanMemberatkan, keadaanMeringankan, sourcePages
                    );

                    queue.add(putusan);

                } catch (Exception e) {
                    errorLog.add("Baris " + rowNumber + ": Gagal parsing data (" + e.getMessage() + ").");
                }
            }

        } catch (IOException e) {
            errorLog.add("Gagal membaca file: " + e.getMessage());
        }

        return queue;
    }

    /**
     * Helper untuk memecah baris CSV dengan dukungan tanda kutip koma di dalam sel.
     */
    private static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes; // toggle status kutip
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().replaceAll("^\"|\"$", "").trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().replaceAll("^\"|\"$", "").trim());
        return tokens;
    }

    /**
     * Mengambil nilai maksimum jika ada beberapa nilai dipisah " | " (untuk bulan tuntutan/vonis)
     */
    private static int parseMaxInt(String raw) {
        if (raw == null || raw.isEmpty()) return 0;
        if (raw.contains("|")) {
            String[] parts = raw.split("\\|");
            int max = 0;
            for (String p : parts) {
                try {
                    int val = Integer.parseInt(p.trim());
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
            return max;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Menjumlahkan nilai jika ada beberapa denda dipisah " | "
     */
    private static long parseSumLong(String raw) {
        if (raw == null || raw.isEmpty()) return 0L;
        if (raw.contains("|")) {
            String[] parts = raw.split("\\|");
            long sum = 0L;
            for (String p : parts) {
                try {
                    String clean = p.trim().replace(".", "").replace(",", "");
                    if (!clean.isEmpty()) {
                        sum += Long.parseLong(clean);
                    }
                } catch (NumberFormatException ignored) {}
            }
            return sum;
        }
        try {
            String clean = raw.trim().replace(".", "").replace(",", "");
            return clean.isEmpty() ? 0L : Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}