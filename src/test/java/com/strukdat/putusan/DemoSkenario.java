package com.strukdat.putusan;

import com.strukdat.putusan.io.CsvParser;
import com.strukdat.putusan.io.HasilImpor;
import com.strukdat.putusan.io.ImporterPutusan;
import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.service.FilterPutusan;
import com.strukdat.putusan.service.PutusanService;
import com.strukdat.putusan.struktur.AntrianGenerik;
import com.strukdat.putusan.struktur.HasilCari;

/**
 * Menjalankan 7 skenario 4.1 pada data nyata dan mencetak buktinya.
 * Jalankan dari folder proyek; simpan keluaran ke docs/demo/demo-skenario.txt.
 */
public final class DemoSkenario {

    private static String id(AntrianGenerik<Putusan> q, int pos) {
        return pos < 0 || pos >= q.size() ? "null" : q.get(pos).getCaseId();
    }

    private static void rantai(String label, AntrianGenerik<Putusan> q, int pos) {
        System.out.println("  " + label + ": ... -> " + id(q, pos - 1) + " -> " + id(q, pos) + " -> "
                + id(q, pos + 1) + " -> ...");
    }

    public static void main(String[] args) {
        PutusanService s = new PutusanService();
        AntrianGenerik<Putusan> q = s.getData();

        System.out.println("=== SKENARIO 1: Muat data ===");
        HasilImpor h = s.imporDariTeks(DataUji.baca("data/putusan-50.csv"), "data/putusan-50.csv");
        System.out.println(h.ringkasan());
        q.validateInvariant();
        System.out.println("size() = " + q.size() + " (dibaca " + h.getDibaca() + ", valid " + h.getDiterima() + ")");
        System.out.println("validateInvariant(): OK");

        System.out.println("\n=== SKENARIO 2: Pencarian ===");
        int n = q.size();
        String[] kunci = {"P025", "P999", "P001", String.format("P%03d", n)};
        String[] ket = {"ada (tengah)", "tidak ada", "posisi batas: pertama", "posisi batas: terakhir"};
        for (int i = 0; i < kunci.length; i++) {
            System.out.println(String.format("  cari %s [%s] -> %s", kunci[i], ket[i], s.cariCaseId(kunci[i])));
        }
        System.out.println("  cari nomor 934/Pid.Sus/2024/PN Bjm -> " + s.cariNomor("934/Pid.Sus/2024/PN Bjm"));

        System.out.println("\n=== SKENARIO 3: Hapus pertama, tengah, terakhir (perubahan pointer) ===");
        Putusan[] dihapus = new Putusan[3];
        System.out.println("Awal: head=" + id(q, 0) + ", tail=" + id(q, q.size() - 1) + ", size=" + q.size());
        rantai("sebelum hapus pertama (posisi 0)", q, 0);
        dihapus[0] = s.hapusPosisi(0);
        System.out.println("  dihapus " + dihapus[0].getCaseId() + " -> head sekarang " + id(q, 0)
                + ", size=" + q.size());
        int tengah = q.size() / 2;
        rantai("sebelum hapus tengah (posisi " + tengah + ")", q, tengah);
        String tetanggaKiri = id(q, tengah - 1);
        dihapus[1] = s.hapusPosisi(tengah);
        System.out.println("  dihapus " + dihapus[1].getCaseId() + " -> next(" + tetanggaKiri + ") kini "
                + id(q, tengah) + ", size=" + q.size());
        rantai("sebelum hapus terakhir (posisi " + (q.size() - 1) + ")", q, q.size() - 1);
        dihapus[2] = s.hapusPosisi(q.size() - 1);
        System.out.println("  dihapus " + dihapus[2].getCaseId() + " -> tail sekarang " + id(q, q.size() - 1)
                + ", next(tail)=null, size=" + q.size());
        q.validateInvariant();
        System.out.println("validateInvariant(): OK");

        System.out.println("\n=== SKENARIO 4: Tambah kembali 3 record ===");
        for (Putusan p : dihapus) {
            s.tambah(p);
            q.validateInvariant();
            System.out.println("  tambah " + p.getCaseId() + " -> size=" + q.size() + ", tail=" + id(q, q.size() - 1)
                    + ", invariant OK");
        }
        System.out.println("  urutan 3 terakhir: " + id(q, q.size() - 3) + ", " + id(q, q.size() - 2) + ", "
                + id(q, q.size() - 1) + " (urutan penambahan; elemen baru selalu masuk di belakang)");

        System.out.println("\n=== SKENARIO 5: Filter gabungan vs provenance ===");
        System.out.println("  (memakai data segar hasil impor ulang, karena langkah 3-4 memindahkan 3 record ke belakang)");
        PutusanService segar = DataUji.layananTermuat();
        FilterPutusan f = new FilterPutusan().jenis("Sabu").beratMin(5.0).pasalTerbukti("114").vonisMin(72).vonisMax(96);
        AntrianGenerik<Putusan> hasil = segar.filter(f);
        String[][] pv = DataUji.provenance();
        StringBuilder harap = new StringBuilder();
        for (String id : DataUji.daftarCaseId(pv)) {
            boolean sabu = false;
            for (String j : DataUji.pecah(DataUji.nilai(pv, id, "jenisNarkotika"))) {
                sabu |= j.equalsIgnoreCase("sabu");
            }
            double berat = DataUji.angka(DataUji.nilai(pv, id, "beratBarangBuktiGram"));
            int v = DataUji.vonisMaks(pv, id);
            if (sabu && berat >= 5.0 && DataUji.nilai(pv, id, "pasalTerbukti").contains("114") && v >= 72 && v <= 96) {
                harap.append(harap.length() == 0 ? "" : ",").append(id);
            }
        }
        System.out.println("  kriteria        : " + f);
        System.out.println("  hasil program   : " + DataUji.idGabung(hasil) + " (" + hasil.size() + " record)");
        System.out.println("  hasil provenance: " + harap);
        System.out.println("  COCOK: " + harap.toString().equals(DataUji.idGabung(hasil)));

        System.out.println("\n=== SKENARIO 6: Pencarian tidak mengubah urutan ===");
        String sebelum = DataUji.idGabung(q);
        s.cariCaseId("P030");
        s.cariCaseId("TIDAK-ADA");
        System.out.println("  setelah rotateSearch otomatis, urutan sama: " + sebelum.equals(DataUji.idGabung(q)));
        HasilCari<Putusan> sebagian = q.rotateSearch(p -> p.getCaseId().equals("P030"), false);
        String fisik = q.toString();
        System.out.println("  rotateSearch tanpa pemulihan: " + sebagian);
        System.out.println("  offsetRotasi=" + q.getOffsetRotasi() + ", urutan fisik sementara dimulai: "
                + fisik.substring(0, 33) + "...");
        System.out.println("  restore() -> " + q.restore() + " langkah, offsetRotasi=" + q.getOffsetRotasi()
                + ", urutan dimulai: " + q.toString().substring(0, 33) + "...");
        System.out.println("  urutan akhir sama dengan awal: " + sebelum.equals(DataUji.idGabung(q)));
        q.validateInvariant();

        System.out.println("\n=== SKENARIO 7: Record tidak valid ===");
        AntrianGenerik<String[]> baris = CsvParser.parse(DataUji.baca("data/putusan-50.csv"));
        baris.dequeue();
        String[] contoh = baris.dequeue();
        String[] buruk = contoh.clone();
        buruk[0] = "P900";
        buruk[1] = "900/Pid.Sus/2025/PN Bjm";
        buruk[3] = "2025-2-5";
        buruk[6] = "abc";
        buruk[4] = "T900";
        int sizeSebelum = q.size();
        String galat = s.tambahDariBarisCsv(CsvParser.gabungBaris(buruk));
        System.out.println("  pesan: " + galat);
        System.out.println("  size sebelum=" + sizeSebelum + ", sesudah=" + q.size() + " (tidak berubah)");
        q.validateInvariant();
        System.out.println("  validateInvariant(): OK");
        System.out.println(ImporterPutusan.HEADER.length + " kolom wajib; file rusak: "
                + s.imporDariTeks("a,b\n\"x", "uji").getGalat().peek());
    }
}
