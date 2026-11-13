package com.uts.strukdat;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvParserTest {

    @Test
    public void testLoad50Records() {
        List<String> errorLog = new ArrayList<>();
        // Jalankan parser membaca file dari direktori data/putusan-50.csv
        MyQueue<PutusanNarkotika> queue = CsvParser.parseCsv("data/putusan-50.csv", errorLog);

        // Cetak log error jika ada untuk debugging
        if (!errorLog.isEmpty()) {
            System.out.println("Error log parser: " + errorLog);
        }

        // Pastikan tidak ada error parsing yang fatal
        assertTrue(errorLog.isEmpty(), "Seharusnya tidak ada error saat parsing CSV.");

        // Skenario UTS 1: Memuat 50 record dan buktikan size bernilai 50
        assertEquals(50, queue.size(), "Jumlah record di Queue harus tepat 50.");
        assertFalse(queue.isEmpty(), "Queue tidak boleh kosong.");
        
        // Verifikasi elemen pertama (P001)
        PutusanNarkotika first = queue.get(0);
        assertEquals("P001", first.getCaseId());
        assertEquals("PN Banjarmasin", first.getPengadilan());
    }
}