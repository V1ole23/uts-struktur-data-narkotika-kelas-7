package com.strukdat.putusan.io;

import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.struktur.AntrianGenerik;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Ekspor ke CSV atau teks (UTF-8). CSV hasil ekspor dapat diimpor kembali
public final class EksportirPutusan {

    private EksportirPutusan() {
    }

    public static String keCsv(AntrianGenerik<Putusan> data) {
        StringBuilder sb = new StringBuilder(CsvParser.gabungBaris(ImporterPutusan.HEADER)).append('\n');
        data.untukSetiap((p, i) -> sb.append(CsvParser.gabungBaris(p.keFieldCsv())).append('\n'));
        return sb.toString();
    }

    public static void tulis(Path tujuan, String isi) throws IOException {
        Path induk = tujuan.toAbsolutePath().getParent();
        if (induk != null) {
            Files.createDirectories(induk);
        }
        Files.writeString(tujuan, isi, StandardCharsets.UTF_8);
    }
}
