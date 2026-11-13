package com.strukdat.putusan.service;

import com.strukdat.putusan.io.CsvParser;
import com.strukdat.putusan.io.EksportirPutusan;
import com.strukdat.putusan.io.HasilImpor;
import com.strukdat.putusan.io.ImporterPutusan;
import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.struktur.AntrianGenerik;
import com.strukdat.putusan.struktur.HasilCari;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class PutusanService {

    private final AntrianGenerik<Putusan> data = new AntrianGenerik<>();
    private final LogAudit audit = new LogAudit();

    public AntrianGenerik<Putusan> getData() { return data; }

    public LogAudit getAudit() { return audit; }

    // impor dan tampil

    public HasilImpor imporDariFile(Path file) throws IOException {
        return imporDariTeks(Files.readString(file, StandardCharsets.UTF_8), file.toString());
    }

    public HasilImpor imporDariTeks(String teks, String sumber) {
        data.clear();
        HasilImpor h = ImporterPutusan.impor(teks, data);
        data.validateInvariant();
        audit.catat("IMPOR", sumber, "dibaca=" + h.getDibaca() + ", diterima=" + h.getDiterima()
                + ", ditolak=" + h.getDitolak());
        return h;
    }

    public String tampil() {
        StringBuilder sb = new StringBuilder("Putusan (urutan antrian, depan ke belakang): " + data.size() + " record\n");
        data.untukSetiap((p, i) -> sb.append(String.format("%3d. %s%n", i + 1, p.ringkas())));
        audit.catat("TAMPIL", "semua", data.size() + " record");
        return sb.toString().stripTrailing();
    }

    // tambah dan hapus

    // @return null bila berhasil
    public String tambahDariBarisCsv(String barisCsv) {
        AntrianGenerik<String[]> baris;
        try {
            baris = CsvParser.parse(barisCsv);
        } catch (IllegalArgumentException e) {
            return tolakTambah("baris CSV tidak dapat dibaca: " + e.getMessage());
        }
        if (baris.size() != 1) {
            return tolakTambah("harus tepat satu baris CSV, ditemukan " + baris.size());
        }
        StringBuilder masalah = new StringBuilder();
        Putusan p = ImporterPutusan.buatDariField(baris.peek(), data, masalah);
        if (p == null) {
            return tolakTambah(masalah.toString());
        }
        data.enqueue(p);
        data.validateInvariant();
        audit.catat("TAMBAH", p.getCaseId(), "berhasil, size=" + data.size());
        return null;
    }

    // Menambah objek langsung. melempar IllegalArgumentException bila duplikat
    public void tambah(Putusan p) {
        StringBuilder alasan = new StringBuilder();
        if (data.rotateSearch(x -> x.getCaseId().equals(p.getCaseId())).isDitemukan()) {
            alasan.append("caseId ").append(p.getCaseId()).append(" duplikat; ");
        }
        if (data.rotateSearch(x -> x.getNomorPerkara().equalsIgnoreCase(p.getNomorPerkara())).isDitemukan()) {
            alasan.append("nomorPerkara ").append(p.getNomorPerkara()).append(" duplikat; ");
        }
        for (String k : p.getKodeTerdakwa()) {
            if (data.rotateSearch(x -> x.memuatKodeTerdakwa(k)).isDitemukan()) {
                alasan.append("terdakwaKode ").append(k).append(" duplikat; ");
            }
        }
        if (alasan.length() > 0) {
            throw new IllegalArgumentException(tolakTambah(alasan.toString().trim()));
        }
        data.enqueue(p);
        data.validateInvariant();
        audit.catat("TAMBAH", p.getCaseId(), "berhasil, size=" + data.size());
    }

    private String tolakTambah(String pesan) {
        audit.catat("TAMBAH", "ditolak", pesan);
        return pesan;
    }

    public Putusan hapusPosisi(int posisi) {
        Putusan p = data.remove(posisi);
        data.validateInvariant();
        audit.catat("HAPUS", "posisi " + posisi, p.getCaseId() + " dihapus, size=" + data.size());
        return p;
    }

    // @return record yang dihapus, null bila caseId tidak ditemukan
    public Putusan hapusCaseId(String caseId) {
        HasilCari<Putusan> h = data.rotateSearch(p -> p.getCaseId().equalsIgnoreCase(caseId.trim()));
        if (!h.isDitemukan()) {
            audit.catat("HAPUS", "caseId " + caseId, "tidak ditemukan");
            return null;
        }
        return hapusPosisi(h.getPosisi());
    }

    // ---------------------------------------------------------------- cari, filter, agregasi, urutan

    public HasilCari<Putusan> cariCaseId(String caseId) {
        String kunci = caseId.trim();
        HasilCari<Putusan> h = data.rotateSearch(p -> p.getCaseId().equalsIgnoreCase(kunci));
        audit.catat("CARI", "caseId " + kunci, h.toString());
        return h;
    }

    public HasilCari<Putusan> cariNomor(String nomor) {
        String kunci = nomor.trim();
        HasilCari<Putusan> h = data.rotateSearch(p -> p.getNomorPerkara().equalsIgnoreCase(kunci));
        audit.catat("CARI", "nomorPerkara " + kunci, h.toString());
        return h;
    }

    // Hasil berupa antrian baru; antrian utama tidak berubah
    public AntrianGenerik<Putusan> filter(FilterPutusan filter) {
        AntrianGenerik<Putusan> hasil = new AntrianGenerik<>();
        data.untukSetiap((p, i) -> {
            if (filter.cocok(p)) {
                hasil.enqueue(p);
            }
        });
        audit.catat("FILTER", filter.toString(), hasil.size() + " record");
        return hasil;
    }

    public Agregasi agregasi() {
        Agregasi a = Agregasi.hitung(data);
        audit.catat("AGREGASI", "seluruh data", "perkara=" + a.getTotalPerkara());
        return a;
    }

    //  Tampilan terurut menurut vonisBulan
    //  antrian utama tidak berubah. Vonis 0 (tidak terukur) selalu di paling akhir
    public AntrianGenerik<Putusan> urutVonis(boolean naik) {
        Putusan[] arr = new Putusan[data.size()];
        data.untukSetiap((p, i) -> arr[i] = p);
        urutGabung(arr, new Putusan[arr.length], 0, arr.length, naik);
        AntrianGenerik<Putusan> hasil = new AntrianGenerik<>();
        for (Putusan p : arr) {
            hasil.enqueue(p);
        }
        audit.catat("URUT", "vonis " + (naik ? "naik" : "turun"), hasil.size() + " record (salinan)");
        return hasil;
    }

    private static int bandingkan(Putusan a, Putusan b, boolean naik) {
        boolean za = a.getVonisBulan() == 0;
        boolean zb = b.getVonisBulan() == 0;
        if (za || zb) {
            return za == zb ? 0 : (za ? 1 : -1);
        }
        int c = Integer.compare(a.getVonisBulan(), b.getVonisBulan());
        return naik ? c : -c;
    }

    private static void urutGabung(Putusan[] a, Putusan[] bantu, int kiri, int kanan, boolean naik) {
        if (kanan - kiri < 2) {
            return;
        }
        int tengah = (kiri + kanan) >>> 1;
        urutGabung(a, bantu, kiri, tengah, naik);
        urutGabung(a, bantu, tengah, kanan, naik);
        int i = kiri;
        int j = tengah;
        int k = kiri;
        while (i < tengah && j < kanan) {
            bantu[k++] = bandingkan(a[i], a[j], naik) <= 0 ? a[i++] : a[j++];
        }
        while (i < tengah) {
            bantu[k++] = a[i++];
        }
        while (j < kanan) {
            bantu[k++] = a[j++];
        }
        System.arraycopy(bantu, kiri, a, kiri, kanan - kiri);
    }

    // ---------------------------------------------------------------- ekspor

    public void eksporCsv(AntrianGenerik<Putusan> hasil, Path tujuan) throws IOException {
        EksportirPutusan.tulis(tujuan, EksportirPutusan.keCsv(hasil));
        audit.catat("EKSPOR", "CSV " + tujuan, hasil.size() + " record");
    }

    public void eksporTeks(String isi, Path tujuan) throws IOException {
        EksportirPutusan.tulis(tujuan, isi + "\n");
        audit.catat("EKSPOR", "TXT " + tujuan, isi.length() + " karakter");
    }
}
