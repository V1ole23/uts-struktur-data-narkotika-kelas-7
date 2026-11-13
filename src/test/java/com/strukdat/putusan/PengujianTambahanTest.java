package com.strukdat.putusan;

import com.strukdat.putusan.io.EksportirPutusan;
import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.service.Agregasi;
import com.strukdat.putusan.service.FilterPutusan;
import com.strukdat.putusan.service.PutusanService;
import com.strukdat.putusan.struktur.AntrianGenerik;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Agregasi, urutan, ekspor, dan audit. */
class PengujianTambahanTest {

    @Test
    @DisplayName("Agregasi sama dengan perhitungan independen dari provenance")
    void agregasiSesuaiProvenance() {
        PutusanService s = DataUji.layananTermuat();
        String[][] pv = DataUji.provenance();
        String[] ids = DataUji.daftarCaseId(pv);
        long denda = 0;
        long jumlahVonis = 0;
        int denganVonis = 0;
        int sabu = 0;
        int penyebutan = 0;
        for (String id : ids) {
            denda += DataUji.dendaJumlah(pv, id);
            int v = DataUji.vonisMaks(pv, id);
            if (v > 0) {
                jumlahVonis += v;
                denganVonis++;
            }
            for (String j : DataUji.pecah(DataUji.nilai(pv, id, "jenisNarkotika"))) {
                penyebutan++;
                if (j.equalsIgnoreCase("sabu")) {
                    sabu++;
                }
            }
        }
        Agregasi a = s.agregasi();
        assertEquals(ids.length, a.getTotalPerkara());
        assertEquals(denda, a.getTotalDenda());
        assertEquals(denganVonis, a.getPerkaraDenganVonis());
        assertEquals((double) jumlahVonis / denganVonis, a.getRataRataVonis(), 1e-9);
        assertEquals(sabu, a.jumlahJenis("Sabu"));
        assertEquals(penyebutan, a.totalPenyebutanJenis());
        assertTrue(a.laporan().contains("Total perkara"));
    }

    @Test
    @DisplayName("Urutan vonis: salinan terurut, antrian utama tidak berubah, vonis 0 di akhir")
    void urutanVonis() {
        PutusanService s = DataUji.layananTermuat();
        String awal = DataUji.idGabung(s.getData());
        for (boolean naik : new boolean[] {true, false}) {
            AntrianGenerik<Putusan> u = s.urutVonis(naik);
            assertEquals(s.getData().size(), u.size());
            Putusan[] arr = new Putusan[u.size()];
            u.untukSetiap((p, i) -> arr[i] = p);
            boolean nolMulai = false;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].getVonisBulan() == 0) {
                    nolMulai = true;
                    continue;
                }
                assertFalse(nolMulai, "vonis 0 harus di akhir");
                if (i > 0 && arr[i - 1].getVonisBulan() != 0) {
                    int d = arr[i - 1].getVonisBulan() - arr[i].getVonisBulan();
                    assertTrue(naik ? d <= 0 : d >= 0, "urutan salah pada indeks " + i);
                }
            }
            u.validateInvariant();
        }
        assertEquals(awal, DataUji.idGabung(s.getData()));
    }

    @Test
    @DisplayName("Ekspor CSV lalu impor kembali menghasilkan data identik")
    void eksporBolakBalik() throws IOException {
        PutusanService s = DataUji.layananTermuat();
        Path tmp = Files.createTempDirectory("strukdat-uji");
        Path csv = tmp.resolve("keluar.csv");
        s.eksporCsv(s.getData(), csv);
        s.eksporTeks(s.agregasi().laporan(), tmp.resolve("agregasi.txt"));
        assertTrue(Files.readString(tmp.resolve("agregasi.txt")).startsWith("=== AGREGASI"));

        PutusanService s2 = new PutusanService();
        s2.imporDariFile(csv);
        assertEquals(s.getData().size(), s2.getData().size());
        for (int i = 0; i < s.getData().size(); i++) {
            assertEquals(s.getData().get(i).detail(), s2.getData().get(i).detail());
        }
        assertEquals(EksportirPutusan.keCsv(s.getData()), EksportirPutusan.keCsv(s2.getData()));
    }

    @Test
    @DisplayName("Audit mencatat setiap operasi sesuai urutan kejadian")
    void audit() {
        PutusanService s = DataUji.layananTermuat();
        s.cariCaseId("P002");
        s.filter(new FilterPutusan().jenis("Sabu"));
        s.agregasi();
        s.hapusPosisi(0);
        AntrianGenerik<String> r = s.getAudit().getRiwayat();
        assertEquals(5, r.size());
        String[] urutan = {"IMPOR", "CARI", "FILTER", "AGREGASI", "HAPUS"};
        for (int i = 0; i < urutan.length; i++) {
            assertTrue(r.get(i).contains(" | " + urutan[i] + " | "), r.get(i));
        }
    }
}
