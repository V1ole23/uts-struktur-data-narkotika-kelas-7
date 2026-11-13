package com.strukdat.putusan;

import com.strukdat.putusan.io.CsvParser;
import com.strukdat.putusan.io.HasilImpor;
import com.strukdat.putusan.io.ImporterPutusan;
import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.service.FilterPutusan;
import com.strukdat.putusan.service.PutusanService;
import com.strukdat.putusan.struktur.AntrianGenerik;
import com.strukdat.putusan.struktur.HasilCari;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tujuh pengujian wajib. Jumlah valid yang diharapkan dibaca dari src/test/resources/uji.config. */
class PengujianWajibTest {

    private final int valid = DataUji.konfig("jumlahValid");
    private final int dibaca = DataUji.konfig("jumlahDibaca");

    private String idKe(int n) {
        return String.format("P%03d", n);
    }

    // ---------------------------------------------------------------- 1
    @Test
    @DisplayName("1. Memuat data: 50 record dibaca, size() = jumlah valid, record tidak valid ditolak")
    void memuatData() {
        PutusanService s = new PutusanService();
        HasilImpor h = s.imporDariTeks(DataUji.baca("data/putusan-50.csv"), "uji");
        assertEquals(dibaca, h.getDibaca(), "tepat 50 record harus dibaca");
        assertEquals(valid, h.getDiterima());
        assertEquals(valid, s.getData().size());
        assertEquals(dibaca - valid, h.getDitolak());
        assertEquals(h.getDitolak(), h.getGalat().size());
        assertDoesNotThrow(() -> s.getData().validateInvariant());
        assertEquals("P001", s.getData().get(0).getCaseId());
        assertEquals(idKe(valid), s.getData().get(valid - 1).getCaseId());
        if (valid < dibaca) {
            assertTrue(h.getGalat().peek().contains("P050"), "pesan galat harus menyebut record yang ditolak");
        }
    }

    // ---------------------------------------------------------------- 2
    @Test
    @DisplayName("2. Pencarian: ditemukan, tidak ditemukan, dan posisi batas (pertama/terakhir)")
    void pencarian() {
        PutusanService s = DataUji.layananTermuat();
        int n = s.getData().size();
        String sebelum = DataUji.idGabung(s.getData());

        HasilCari<Putusan> tengah = s.cariCaseId("P025");
        assertTrue(tengah.isDitemukan());
        assertEquals(24, tengah.getPosisi());
        assertEquals(25, tengah.getLangkahBanding());
        assertEquals("P025", tengah.getElemen().getCaseId());

        HasilCari<Putusan> tidak = s.cariCaseId("P999");
        assertFalse(tidak.isDitemukan());
        assertEquals(-1, tidak.getPosisi());
        assertEquals(n, tidak.getLangkahBanding(), "tidak ditemukan harus memeriksa seluruh elemen");

        HasilCari<Putusan> pertama = s.cariCaseId("P001");
        assertEquals(0, pertama.getPosisi());
        assertEquals(1, pertama.getLangkahBanding());
        assertEquals(0, pertama.getLangkahRotasi());

        HasilCari<Putusan> terakhir = s.cariCaseId(idKe(n));
        assertEquals(n - 1, terakhir.getPosisi());
        assertEquals(n, terakhir.getLangkahBanding());

        HasilCari<Putusan> noPerkara = s.cariNomor("934/Pid.Sus/2024/PN Bjm");
        assertTrue(noPerkara.isDitemukan());
        assertEquals("P001", noPerkara.getElemen().getCaseId());
        assertFalse(s.cariNomor("1/Pid.Sus/1999/PN Bjm").isDitemukan());

        assertEquals(sebelum, DataUji.idGabung(s.getData()), "pencarian tidak boleh mengubah urutan");
    }

    // ---------------------------------------------------------------- 3
    @Test
    @DisplayName("3. Penghapusan: elemen pertama, tengah, dan terakhir")
    void penghapusan() {
        PutusanService s = DataUji.layananTermuat();
        AntrianGenerik<Putusan> q = s.getData();
        int n = q.size();

        Putusan pertama = s.hapusPosisi(0);
        assertEquals("P001", pertama.getCaseId());
        assertEquals(n - 1, q.size());
        assertEquals("P002", q.get(0).getCaseId());
        assertFalse(q.contains(pertama));
        q.validateInvariant();

        int posTengah = q.size() / 2;
        String idTengah = q.get(posTengah).getCaseId();
        String idSebelum = q.get(posTengah - 1).getCaseId();
        String idSesudah = q.get(posTengah + 1).getCaseId();
        Putusan tengah = s.hapusPosisi(posTengah);
        assertEquals(idTengah, tengah.getCaseId());
        assertEquals(n - 2, q.size());
        assertEquals(idSebelum, q.get(posTengah - 1).getCaseId());
        assertEquals(idSesudah, q.get(posTengah).getCaseId());
        q.validateInvariant();

        Putusan terakhir = s.hapusPosisi(q.size() - 1);
        assertEquals(idKe(valid), terakhir.getCaseId());
        assertEquals(n - 3, q.size());
        assertNotEquals(terakhir.getCaseId(), q.get(q.size() - 1).getCaseId());
        q.validateInvariant();

        assertNull(s.hapusCaseId("P999"));
        assertEquals(n - 3, q.size());
    }

    // ---------------------------------------------------------------- 4
    @Test
    @DisplayName("4. Penambahan: tambah kembali 3 record dan periksa invariant")
    void penambahan() {
        PutusanService s = DataUji.layananTermuat();
        AntrianGenerik<Putusan> q = s.getData();
        int n = q.size();
        Putusan a = s.hapusPosisi(0);
        Putusan b = s.hapusPosisi(q.size() / 2);
        Putusan c = s.hapusPosisi(q.size() - 1);
        assertEquals(n - 3, q.size());

        s.tambah(a);
        q.validateInvariant();
        s.tambah(b);
        q.validateInvariant();
        s.tambah(c);
        q.validateInvariant();

        assertEquals(n, q.size());
        assertSame(a, q.get(n - 3));
        assertSame(b, q.get(n - 2));
        assertSame(c, q.get(n - 1));
        assertTrue(q.contains(a) && q.contains(b) && q.contains(c));
        assertSame(q.get(0), q.peek());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> s.tambah(a));
        assertTrue(e.getMessage().contains("duplikat"));
        assertEquals(n, q.size(), "penambahan duplikat tidak boleh mengubah struktur");
        q.validateInvariant();
    }

    // ---------------------------------------------------------------- 5
    @Test
    @DisplayName("5. Filter gabungan: hasil sama dengan perhitungan independen dari data-provenance.csv")
    void filterGabunganSesuaiProvenance() {
        PutusanService s = DataUji.layananTermuat();
        String[][] pv = DataUji.provenance();
        String[] ids = DataUji.daftarCaseId(pv);

        // Kombinasi A: Sabu, berat >= 5 g, pasal terbukti memuat 114, vonis 72..96 bulan
        FilterPutusan fa = new FilterPutusan().jenis("sabu").beratMin(5.0)
                .pasalTerbukti("114").vonisMin(72).vonisMax(96);
        StringBuilder harapA = new StringBuilder();
        for (String id : ids) {
            boolean sabu = false;
            for (String j : DataUji.pecah(DataUji.nilai(pv, id, "jenisNarkotika"))) {
                sabu |= j.equalsIgnoreCase("sabu");
            }
            double berat = DataUji.angka(DataUji.nilai(pv, id, "beratBarangBuktiGram"));
            int vonis = DataUji.vonisMaks(pv, id);
            boolean cocok = sabu && berat > 0 && berat >= 5.0
                    && DataUji.nilai(pv, id, "pasalTerbukti").contains("114")
                    && vonis > 0 && vonis >= 72 && vonis <= 96;
            if (cocok) {
                harapA.append(harapA.length() == 0 ? "" : ",").append(id);
            }
        }
        AntrianGenerik<Putusan> hasilA = s.filter(fa);
        assertTrue(hasilA.size() > 0, "kombinasi A harus menghasilkan data agar uji bermakna");
        assertEquals(harapA.toString(), DataUji.idGabung(hasilA));

        // Kombinasi B: Ekstasi saja
        StringBuilder harapB = new StringBuilder();
        for (String id : ids) {
            for (String j : DataUji.pecah(DataUji.nilai(pv, id, "jenisNarkotika"))) {
                if (j.equalsIgnoreCase("ekstasi")) {
                    harapB.append(harapB.length() == 0 ? "" : ",").append(id);
                    break;
                }
            }
        }
        assertEquals(harapB.toString(), DataUji.idGabung(s.filter(new FilterPutusan().jenis("Ekstasi"))));

        // Kombinasi C: pasal terbukti memuat 112, berat <= 1 g (berat 0 = tidak ditemukan, dikecualikan)
        StringBuilder harapC = new StringBuilder();
        for (String id : ids) {
            double berat = DataUji.angka(DataUji.nilai(pv, id, "beratBarangBuktiGram"));
            boolean pasal = DataUji.nilai(pv, id, "pasalTerbukti").contains("112");
            if (pasal && berat > 0 && berat <= 1.0) {
                harapC.append(harapC.length() == 0 ? "" : ",").append(id);
            }
        }
        AntrianGenerik<Putusan> hasilC = s.filter(new FilterPutusan().beratMax(1.0)
                .pasalTerbukti("112"));
        assertEquals(harapC.toString(), DataUji.idGabung(hasilC));

        // Setiap field di struktur harus sama dengan provenance
        for (String id : ids) {
            Putusan p = s.cariCaseId(id).getElemen();
            assertNotNull(p, id);
            assertEquals(DataUji.nilai(pv, id, "nomorPerkara"), p.getNomorPerkara(), id);
            assertEquals(DataUji.nilai(pv, id, "tanggalPutusan"), p.getTanggalPutusan().toString(), id);
            assertEquals(DataUji.vonisMaks(pv, id), p.getVonisBulan(), id);
            assertEquals(DataUji.dendaJumlah(pv, id), p.getDendaRupiah(), id);
            assertEquals(DataUji.angka(DataUji.nilai(pv, id, "beratBarangBuktiGram")),
                    p.getBeratBarangBuktiGram(), 1e-9, id);
        }
    }

    // ---------------------------------------------------------------- 6
    @Test
    @DisplayName("6. Pencarian pada Queue: urutan akhir tidak berubah")
    void urutanTidakBerubahSetelahPencarian() {
        PutusanService s = DataUji.layananTermuat();
        AntrianGenerik<Putusan> q = s.getData();
        String awal = DataUji.idGabung(q);

        s.cariCaseId("P010");
        assertEquals(awal, DataUji.idGabung(q));
        s.cariCaseId("TIDAK-ADA");
        assertEquals(awal, DataUji.idGabung(q));

        // rotateSearch tanpa pemulihan otomatis: urutan terputar, restore() mengembalikannya
        HasilCari<Putusan> h = q.rotateSearch(p -> p.getCaseId().equals("P030"), false);
        assertTrue(h.isDitemukan());
        assertEquals(29, h.getPosisi());
        assertEquals(29, q.getOffsetRotasi());
        assertEquals(0, h.getLangkahPemulihan());
        int langkah = q.restore();
        assertEquals(q.size() - 29, langkah);
        assertEquals(0, q.getOffsetRotasi());
        assertEquals(awal, DataUji.idGabung(q));

        s.filter(new FilterPutusan().jenis("Sabu"));
        s.urutVonis(true);
        s.agregasi();
        s.tampil();
        assertEquals(awal, DataUji.idGabung(q));
        q.validateInvariant();
    }

    // ---------------------------------------------------------------- 7
    @Test
    @DisplayName("7. Data tidak valid: pesan error muncul dan struktur tetap konsisten")
    void dataTidakValid() {
        String[] header = ImporterPutusan.HEADER;
        AntrianGenerik<String[]> baris = CsvParser.parse(DataUji.baca("data/putusan-50.csv"));
        baris.dequeue();
        String[] valid1 = baris.dequeue(); // P001

        String[] tglSalah = valid1.clone(); tglSalah[0] = "P101"; tglSalah[1] = "101/Pid.Sus/2025/PN Bjm"; tglSalah[3] = "2025-2-5"; tglSalah[4] = "T901";
        String[] tglTakAda = valid1.clone(); tglTakAda[0] = "P102"; tglTakAda[1] = "102/Pid.Sus/2025/PN Bjm"; tglTakAda[3] = "2025-02-30"; tglTakAda[4] = "T902";
        String[] beratBuruk = valid1.clone(); beratBuruk[0] = "P103"; beratBuruk[1] = "103/Pid.Sus/2025/PN Bjm"; beratBuruk[6] = "abc"; beratBuruk[4] = "T903";
        String[] vonisNegatif = valid1.clone(); vonisNegatif[0] = "P104"; vonisNegatif[1] = "104/Pid.Sus/2025/PN Bjm"; vonisNegatif[10] = "-5"; vonisNegatif[4] = "T904";
        String[] jumlahTakSama = valid1.clone(); jumlahTakSama[0] = "P105"; jumlahTakSama[1] = "105/Pid.Sus/2025/PN Bjm"; jumlahTakSama[4] = "T905 | T906";
        String[] kolomKurang = new String[15]; System.arraycopy(valid1, 0, kolomKurang, 0, 15);
        String[] caseIdDuplikat = valid1.clone(); caseIdDuplikat[1] = "106/Pid.Sus/2025/PN Bjm"; caseIdDuplikat[4] = "T907";
        String[] kodeFormat = valid1.clone(); kodeFormat[0] = "P107"; kodeFormat[1] = "107/Pid.Sus/2025/PN Bjm"; kodeFormat[4] = "X1";

        StringBuilder teks = new StringBuilder(CsvParser.gabungBaris(header)).append('\n');
        String[][] semua = {valid1, tglSalah, tglTakAda, beratBuruk, vonisNegatif, jumlahTakSama, kolomKurang,
                caseIdDuplikat, kodeFormat};
        for (String[] r : semua) {
            teks.append(CsvParser.gabungBaris(r)).append('\n');
        }

        PutusanService s = new PutusanService();
        HasilImpor h = s.imporDariTeks(teks.toString(), "uji-tidak-valid");
        assertEquals(semua.length, h.getDibaca());
        assertEquals(1, h.getDiterima(), "hanya baris pertama yang valid");
        assertEquals(semua.length - 1, h.getGalat().size());
        assertEquals(1, s.getData().size());
        s.getData().validateInvariant();

        StringBuilder semuaPesan = new StringBuilder();
        h.getGalat().untukSetiap((m, i) -> semuaPesan.append(m).append('\n'));
        String pesan = semuaPesan.toString();
        assertTrue(pesan.contains("tanggalPutusan '2025-2-5' tidak berformat yyyy-MM-dd"), pesan);
        assertTrue(pesan.contains("bukan tanggal yang valid"), pesan);
        assertTrue(pesan.contains("beratBarangBuktiGram 'abc'"), pesan);
        assertTrue(pesan.contains("vonisBulan '-5'"), pesan);
        assertTrue(pesan.contains("tidak sama dengan jumlah terdakwa"), pesan);
        assertTrue(pesan.contains("jumlah kolom 15"), pesan);
        assertTrue(pesan.contains("caseId P001 duplikat"), pesan);
        assertTrue(pesan.contains("terdakwaKode 'X1' harus berformat Tnnn"), pesan);

        // Menambah satu baris tidak valid lewat service: ditolak, struktur tidak berubah
        int sebelum = s.getData().size();
        String galat = s.tambahDariBarisCsv(CsvParser.gabungBaris(tglSalah));
        assertNotNull(galat);
        assertTrue(galat.contains("tanggalPutusan"));
        assertEquals(sebelum, s.getData().size());
        s.getData().validateInvariant();

        // File rusak: kutip tidak ditutup dan header salah, program tidak berhenti
        HasilImpor rusak = s.imporDariTeks("caseId,nomorPerkara\n\"belum ditutup", "uji-rusak");
        assertEquals(0, rusak.getDiterima());
        assertTrue(rusak.getGalat().size() > 0);
        assertEquals(0, s.getData().size());
        s.getData().validateInvariant();
    }
}
