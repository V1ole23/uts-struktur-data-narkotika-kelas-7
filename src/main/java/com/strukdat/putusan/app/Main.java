package com.strukdat.putusan.app;

import com.strukdat.putusan.io.FormatAngka;
import com.strukdat.putusan.io.HasilImpor;
import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.service.FilterPutusan;
import com.strukdat.putusan.service.PutusanService;
import com.strukdat.putusan.struktur.AntrianGenerik;
import com.strukdat.putusan.struktur.HasilCari;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

// Menu terminal. Penggunaan: java -jar target/strukdat-putusan-1.0.jar [path-csv] (bawaan: data/putusan-50.csv)
public final class Main {

    private final PutusanService service = new PutusanService();
    private final Scanner in = new Scanner(System.in, "UTF-8");
    private AntrianGenerik<Putusan> hasilTerakhir = new AntrianGenerik<>();

    public static void main(String[] args) {
        Main app = new Main();
        String path = args.length > 0 ? args[0] : "data/putusan-50.csv";    // Path default bila tidak ada argumen
        System.out.println("\n=== Putusan Narkotika (Queue FIFO) ===");
        app.imporFile(path);
        app.jalankan(path);
    }

    private void jalankan(String pathAwal) {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("   APLIKASI MANAJEMEN PUTUSAN NARKOTIKA   ");
            System.out.println("==========================================");
            System.out.println("1. Import CSV File");
            System.out.println("2. Tampilkan Semua Putusan");
            System.out.println("3. Tambah Putusan Baru");
            System.out.println("4. Hapus Putusan");
            System.out.println("5. Cari Putusan (CaseID / Nomor Perkara)");
            System.out.println("6. Filter Berdasarkan Jenis Narkotika");
            System.out.println("7. Tampilkan Statistik & Agregasi");
            System.out.println("8. Urutkan Berdasarkan Vonis");
            System.out.println("9. Tampilkan Log Audit");
            System.out.println("10. Ekspor Data ke CSV / TXT");
            System.out.println("0. Keluar");
            System.out.print("Pilih menu (1-10): ");
            String pilih = baca("Pilih");
            System.out.println("");
            if (pilih == null || pilih.trim().equals("0")) {
                return;
            }
            try {
                switch (pilih.trim()) {
                    case "1": imporFile(isi(baca("Path CSV [" + pathAwal + "]"), pathAwal)); break;
                    case "2":
                        cetakTabel(service.getData(), "Total " + service.getData().size()
                                + " perkara. Perkara >1 terdakwa: Vonis = tertinggi, Denda = jumlah semua terdakwa.");
                        service.getAudit().catat("TAMPIL", "semua", service.getData().size() + " record");
                        break;
                    case "3": menuTambah(); break;
                    case "4": menuHapus(); break;
                    case "5": menuCari(); break;
                    case "6": menuFilter(); break;
                    case "7": System.out.println(service.agregasi().laporan()); break;
                    case "8": menuUrut(); break;
                    case "9": System.out.println(service.getAudit().tampil()); break;
                    case "10": menuEkspor(); break;
                    default: System.out.println("Menu tidak dikenal.");
                }
            } catch (RuntimeException | IOException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private void imporFile(String path) {
        try {
            HasilImpor h = service.imporDariFile(Path.of(path));
            System.out.println(h.ringkasan());
        } catch (IOException e) {
            System.out.println("[ERROR] File '" + path + "' tidak terbaca. Folder kerja saat ini: "
                    + Path.of("").toAbsolutePath() + "\n        Gunakan menu 1 dan ketik path lengkap.");
        }
    }

    private void menuTambah() {
        System.out.println("Tempel SATU baris CSV 16 kolom (urutan sama dengan header data/putusan-50.csv):");
        String galat = service.tambahDariBarisCsv(isi(baca("Baris"), ""));
        System.out.println(galat == null ? "Berhasil. size=" + service.getData().size() : "[DITOLAK] " + galat);
    }

    private void menuHapus() {
        if (isi(baca("Hapus berdasarkan 1=caseId, 2=posisi (mulai 0)"), "1").trim().equals("2")) {
            Putusan p = service.hapusPosisi(Integer.parseInt(baca("Posisi").trim()));
            System.out.println("Dihapus: " + p.getCaseId() + ", size=" + service.getData().size());
        } else {
            Putusan p = service.hapusCaseId(isi(baca("caseId"), ""));
            System.out.println(p == null ? "caseId tidak ditemukan." : "Dihapus: " + p.getCaseId()
                    + ", size=" + service.getData().size());
        }
    }

    private void menuCari() {
        boolean nomor = isi(baca("Cari berdasarkan 1=caseId, 2=nomorPerkara"), "1").trim().equals("2");
        String kunci = isi(baca(nomor ? "nomorPerkara" : "caseId"), "");
        HasilCari<Putusan> h = nomor ? service.cariNomor(kunci) : service.cariCaseId(kunci);
        System.out.println(h);
        hasilTerakhir = new AntrianGenerik<>();
        if (h.isDitemukan()) {
            System.out.println(h.getElemen().detail());
            hasilTerakhir.enqueue(h.getElemen());
        }
    }

    private void menuFilter() {
        FilterPutusan f = new FilterPutusan();   // kriteria menumpuk (AND) selama sub-menu aktif
        while (true) {
            System.out.println();
            System.out.println("=== MENU FILTER BERDASARKAN ===");
            System.out.println("1. Jenis Narkotika");
            System.out.println("2. Rentang Berat Barang Bukti (Gram)");
            System.out.println("3. Pasal Terbukti");
            System.out.println("4. Rentang Vonis (Bulan)");
            System.out.println("0. Kembali ke Menu Utama");
            String pilih = baca("Pilih kriteria filter (1-4)");
            if (pilih == null || pilih.trim().equals("0")) {
                return;
            }
            try {
                switch (pilih.trim()) {
                    case "1":
                        f.jenis(baca("Jenis narkotika (contoh: Sabu)"));
                        break;
                    case "2":
                        f.beratMin(desimal(baca("Berat minimum (gram, kosong = lewati)")));
                        f.beratMax(desimal(baca("Berat maksimum (gram, kosong = lewati)")));
                        break;
                    case "3":
                        f.pasalTerbukti(baca("Pasal terbukti (contoh: 114)"));
                        break;
                    case "4":
                        f.vonisMin(bulat(baca("Vonis minimum (bulan, kosong = lewati)")));
                        f.vonisMax(bulat(baca("Vonis maksimum (bulan, kosong = lewati)")));
                        break;
                    default:
                        System.out.println("Pilihan tidak valid, pilih 1-4.");
                        continue;
                }
            } catch (RuntimeException e) {
                System.out.println("[ERROR] Input tidak valid: " + e.getMessage());
                continue;
            }
            hasilTerakhir = service.filter(f);
            System.out.println("\nKriteria aktif : " + f);
            System.out.println("Hasil          : " + hasilTerakhir.size() + " putusan");
            cetakTabel(hasilTerakhir, "Ditemukan " + hasilTerakhir.size() + " perkara yang sesuai dengan filter.");
        }
    }

    private void menuUrut() {
        boolean naik = !isi(baca("Urutan 1=vonis naik, 2=vonis turun"), "1").trim().equals("2");
        hasilTerakhir = service.urutVonis(naik);
        cetak(hasilTerakhir);
        System.out.println("(antrian utama tidak berubah, size=" + service.getData().size() + ")");
    }

    private void menuEkspor() throws IOException {
        System.out.println("Ekspor 1=hasil terakhir (CSV), 2=agregasi (TXT), 3=audit (TXT), 4=semua data (CSV)");
        String apa = isi(baca("Pilih"), "1").trim();
        String nama = apa.equals("2") ? "agregasi.txt" : apa.equals("3") ? "audit.txt"
                : apa.equals("4") ? "data-penuh.csv" : "hasil-terakhir.csv";
        Path tujuan = Path.of(isi(baca("Path tujuan [output/" + nama + "]"), "output/" + nama));
        switch (apa) {
            case "2": service.eksporTeks(service.agregasi().laporan(), tujuan); break;
            case "3": service.eksporTeks(service.getAudit().tampil(), tujuan); break;
            case "4": service.eksporCsv(service.getData(), tujuan); break;
            default: service.eksporCsv(hasilTerakhir, tujuan);
        }
        System.out.println("Tersimpan: " + tujuan.toAbsolutePath());
    }



    private static final String FORMAT_TABEL = "%3s | %-6s | %-26s | %-26s | %10s | %10s | %17s%n";

    // Menampilkan tabel dengan header, garis pemisah, dan footer
    private static void cetakTabel(AntrianGenerik<Putusan> q, String footer) {
        String header = String.format(FORMAT_TABEL, "No", "CaseID", "Nomor Perkara", "Jenis Narkotika", "Berat(g)", "Vonis(Bln)", "Denda");
        String garis = "=".repeat(header.stripTrailing().length());
        System.out.println(garis);
        System.out.print(header);
        System.out.println(garis);
        if (q.isEmpty()) {
            System.out.println("(tidak ada data yang cocok)");
        }
        q.untukSetiap((p, i) -> System.out.printf(FORMAT_TABEL,
                String.valueOf(i + 1), p.getCaseId(), p.getNomorPerkara(), p.getJenisNarkotika(),
                FormatAngka.formatBerat(p.getBeratBarangBuktiGram()),
                String.valueOf(p.getVonisBulan()),
                FormatAngka.formatRupiah(p.getDendaRupiah())));
        System.out.println(garis);
        System.out.println(footer + "\n");
    }

    private static void cetak(AntrianGenerik<Putusan> q) {
        cetakTabel(q, "Total " + q.size() + " perkara.");
    }



    private String baca(String prompt) {
        System.out.print(prompt + ": ");
        System.out.flush();
        return in.hasNextLine() ? in.nextLine() : null;
    }

    // Teks kosong atau null diganti nilai bawaan
    private static String isi(String s, String bawaan) {
        return s == null || s.trim().isEmpty() ? bawaan : s.trim();
    }

    private static Double desimal(String s) {
        return s == null || s.trim().isEmpty() ? null : FormatAngka.parseBerat(s);
    }

    private static Integer bulat(String s) {
        return s == null || s.trim().isEmpty() ? null : (int) FormatAngka.parseBulat(s);
    }
}
