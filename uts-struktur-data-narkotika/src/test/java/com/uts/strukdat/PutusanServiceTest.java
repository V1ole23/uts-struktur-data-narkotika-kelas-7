package com.uts.strukdat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class PutusanServiceTest {
    private MyQueue<PutusanNarkotika> queue;
    private PutusanService service;

    @BeforeEach
    public void setUp() {
        queue = CsvParser.parseCsv("data/putusan-50.csv", new ArrayList<>());
        service = new PutusanService(queue);
    }



    @Test
    public void testHapusPertamaTengahAkhir() {
        int initialSize = queue.size(); // Harus 50
        assertEquals(50, initialSize);

        // Hapus elemen pertama (posisi 0)
        PutusanNarkotika removedFirst = service.hapusPutusan(0);
        assertNotNull(removedFirst);
        assertEquals(49, queue.size());

        // Hapus elemen tengah (posisi 24)
        PutusanNarkotika removedMid = service.hapusPutusan(24);
        assertNotNull(removedMid);
        assertEquals(48, queue.size());

        // Hapus elemen terakhir (posisi size - 1)
        int lastIndex = queue.size() - 1;
        PutusanNarkotika removedLast = service.hapusPutusan(lastIndex);
        assertNotNull(removedLast);
        assertEquals(47, queue.size());
    }



    @Test
    public void testTambahRecordBaru() {
        int initialSize = queue.size();
        
        PutusanNarkotika recordBaru = new PutusanNarkotika(
            "P999", "999/Pid.Sus/2026/PN Bjm", "PN Banjarmasin", "2026-01-01", 
            "T999", "Sabu", 10.0, "Pasal 114", "Pasal 114", 60, 60, 
            500000000L, "Barang bukti uji", "Memberatkan", "Meringankan", "1"
        );

        service.tambahPutusan(recordBaru);
        assertEquals(initialSize + 1, queue.size());
        assertEquals("P999", queue.get(queue.size() - 1).getCaseId());
    }

    

    @Test
    public void testCariPutusanNonDestruktif() {
        int sizeBefore = queue.size();
        String firstCaseBefore = queue.get(0).getCaseId();

        // Cari data yang ada (misal P001)
        PutusanNarkotika hasilAda = service.cariPutusan("P001");
        assertNotNull(hasilAda);
        assertEquals("P001", hasilAda.getCaseId());

        // Cari data yang tidak ada
        PutusanNarkotika hasilTidakAda = service.cariPutusan("P9999");
        assertNull(hasilTidakAda);

        // Pastikan ukuran dan urutan antrean kembali seperti semula (non-destruktif)
        assertEquals(sizeBefore, queue.size());
        assertEquals(firstCaseBefore, queue.get(0).getCaseId());
    }

    @Test
    public void testFilterDanStatistik() {
        // Pastikan service tidak null dan data termuat
        assertNotNull(service);
        assertEquals(50, queue.size());

        // Pengujian fitur filter tidak mengubah ukuran queue (non-destruktif)
        int initialSize = queue.size();
        
        // Panggil method filter dan statistik untuk memastikan tidak ada exception/error runtime
        assertDoesNotThrow(() -> {
            service.filterBerdasarkanJenis("Sabu");
            service.filterBerdasarkanRentangBerat(0.0, 10.0);
            service.filterBerdasarkanPasal("114");
            service.filterBerdasarkanRentangVonis(12, 120);
            service.tampilkanStatistik();
        });

        // Verifikasi struktur antrean tetap utuh setelah operasi filter/statistik
        assertEquals(initialSize, queue.size());
    }
}