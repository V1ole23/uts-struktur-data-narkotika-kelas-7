package com.uts.strukdat;

import java.util.ArrayList;
import java.util.List;

public class PutusanService {
    private MyQueue<PutusanNarkotika> queue;

    public PutusanService(MyQueue<PutusanNarkotika> queue) {
        this.queue = queue;
    }



    // Fitur Tampil: Menampilkan ringkasan seluruh putusan dalam urutan queue
    public void tampilkanSemua() {
            if (queue.isEmpty()) {
                System.out.println("Belum ada data putusan dalam antrean.");
                return;
            }

            System.out.println("\n============================================= DAFTAR PUTUSAN NARKOTIKA (Total: " + queue.size() + " perkara) ==============================================");
            // Lebar kolom Jenis Narkotika diperlebar ke %-32s agar aman untuk nama obat panjang
            System.out.printf("%-6s | %-28s | %-32s | %-12s | %-10s | %-15s\n",  "CaseID", "Nomor Perkara", "Jenis Narkotika", "Berat(g)", "Vonis(Bln)", "Denda");
            System.out.println("=========================================================================================================================================");

            for (int i = 0; i < queue.size(); i++) {
                PutusanNarkotika p = queue.get(i);
                System.out.printf("%-6s | %-28s | %-32s | %-12.2f | %-10d | %-15d\n",
                        p.getCaseId(),
                        p.getNomorPerkara(),
                        p.getJenisNarkotika(),
                        p.getBeratBarangBuktiGram(),
                        p.getVonisBulan(),
                        p.getDendaRupiah()
                );
            }
            System.out.println("=========================================================================================================================================\r\n" + //
                                "\n");
        }

    

    // Fitur Tambah: Menambahkan putusan baru ke antrean (rear)
    public void tambahPutusan(PutusanNarkotika putusanBaru) {
        queue.add(putusanBaru);
        System.out.println("Berhasil menambahkan " + putusanBaru.getCaseId() + " ke dalam antrean.");
    }



    // Fitur Hapus: Menghapus putusan berdasarkan posisi indeks
    public PutusanNarkotika hapusPutusan(int posisi) {
        try {
            PutusanNarkotika removed = queue.remove(posisi);
            System.out.println("Berhasil menghapus data pada posisi ke-" + posisi + " (CaseID: " + removed.getCaseId() + ").");
            return removed;
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Gagal menghapus: Indeks " + posisi + " tidak valid.");
            return null;
        }
    }

    public MyQueue<PutusanNarkotika> getQueue() {
        return queue;
    }


    
    // Fitur Cari: Mencari berdasarkan caseId atau nomorPerkara menggunakan rotasi Queue,
    public PutusanNarkotika cariPutusan(String keyword) {
        if (queue.isEmpty()) {
            System.out.println("Antrean kosong.");
            return null;
        }

        int n = queue.size();
        int langkah = 0;
        PutusanNarkotika hasil = null;
        String kw = keyword.toLowerCase().trim();

        // Putar queue untuk mencari data tanpa merusak urutan
        for (int i = 0; i < n; i++) {
            langkah++;
            PutusanNarkotika current = queue.remove(0); // Ambil dari depan
            
            // Cek kecocokan caseId atau nomorPerkara
            if (hasil == null) {
                if (current.getCaseId().toLowerCase().contains(kw) || 
                    current.getNomorPerkara().toLowerCase().contains(kw)) {
                    hasil = current; // Ditemukan, tapi rotasi harus diselesaikan agar urutan pulih
                }
            }
            
            queue.add(current); // Kembalikan ke belakang antrean
        }

        if (hasil != null) {
            System.out.println("-> Data ditemukan dalam " + langkah + " langkah pencarian.");
            System.out.println("   [CaseID: " + hasil.getCaseId() + " | Perkara: " + hasil.getNomorPerkara() + " | Pengadilan: " + hasil.getPengadilan() + "]");
        } else {
            System.out.println("-> Data dengan keyword \"" + keyword + "\" tidak ditemukan setelah " + langkah + " langkah.");
        }

        return hasil;
    }



    //  Fitur Filter
    public void filterBerdasarkanJenis(String jenisTarget) {
        if (queue.isEmpty()) { System.out.println("Antrean kosong."); return; }

        System.out.println("\n=== FILTER: JENIS NARKOTIKA (\"" + jenisTarget.toUpperCase() + "\") ===");
        cetakHeaderTabel();
        int count = 0;
        int n = queue.size();
        for (int i = 0; i < n; i++) {
            PutusanNarkotika p = queue.remove(0);
            if (p.getJenisNarkotika().toLowerCase().contains(jenisTarget.toLowerCase())) {
                count++;
                cetakBarisTabel(p);
            }
            queue.add(p);
        }
        cetakFooterTabel(count);
    }

    // Filter Berdasarkan Rentang Berat Barang Bukti (Gram)
    public void filterBerdasarkanRentangBerat(double minBerat, double maxBerat) {
        if (queue.isEmpty()) { System.out.println("Antrean kosong."); return; }

        System.out.println("\n=== FILTER: RENTANG BERAT (" + minBerat + "g - " + maxBerat + "g) ===");
        cetakHeaderTabel();
        int count = 0;
        int n = queue.size();
        for (int i = 0; i < n; i++) {
            PutusanNarkotika p = queue.remove(0);
            double berat = p.getBeratBarangBuktiGram();
            if (berat >= minBerat && berat <= maxBerat) {
                count++;
                cetakBarisTabel(p);
            }
            queue.add(p);
        }
        cetakFooterTabel(count);
    }

    // Filter Berdasarkan Pasal Terbukti
    public void filterBerdasarkanPasal(String pasalTarget) {
        if (queue.isEmpty()) { System.out.println("Antrean kosong."); return; }

        System.out.println("\n=== FILTER: PASAL TERBUKTI (\"" + pasalTarget.toUpperCase() + "\") ===");
        cetakHeaderTabel();
        int count = 0;
        int n = queue.size();
        for (int i = 0; i < n; i++) {
            PutusanNarkotika p = queue.remove(0);
            if (p.getPasalTerbukti().toLowerCase().contains(pasalTarget.toLowerCase())) {
                count++;
                cetakBarisTabel(p);
            }
            queue.add(p);
        }
        cetakFooterTabel(count);
    }

    // Filter Berdasarkan Rentang Vonis (Bulan)
    public void filterBerdasarkanRentangVonis(int minBulan, int maxBulan) {
        if (queue.isEmpty()) { System.out.println("Antrean kosong."); return; }

        System.out.println("\n=== FILTER: RENTANG VONIS (" + minBulan + " - " + maxBulan + " bulan) ===");
        cetakHeaderTabel();
        int count = 0;
        int n = queue.size();
        for (int i = 0; i < n; i++) {
            PutusanNarkotika p = queue.remove(0);
            int vonis = p.getVonisBulan();
            if (vonis >= minBulan && vonis <= maxBulan) {
                count++;
                cetakBarisTabel(p);
            }
            queue.add(p);
        }
        cetakFooterTabel(count);
    }

    // --- Helper untuk merapikan cetak tabel agar kodenya bersih ---
    private void cetakHeaderTabel() {
        System.out.printf("%-6s | %-28s | %-32s | %-12s | %-10s | %-15s\n", 
                "CaseID", "Nomor Perkara", "Jenis Narkotika", "Berat(g)", "Vonis(Bln)", "Denda");
        System.out.println("=========================================================================================================================================");
    }

    private void cetakBarisTabel(PutusanNarkotika p) {
        System.out.printf("%-6s | %-28s | %-32s | %-12.2f | %-10d | %-15d\n",
                p.getCaseId(), p.getNomorPerkara(), p.getJenisNarkotika(),
                p.getBeratBarangBuktiGram(), p.getVonisBulan(), p.getDendaRupiah());
    }

    private void cetakFooterTabel(int count) {
        System.out.println("=========================================================================================================================================");
        System.out.println("Ditemukan " + count + " perkara yang sesuai dengan filter.\n");
    }



    // Fitur Agregasi Statistik: Menghitung total, rata-rata vonis, dan total denda dari seluruh antrean.
    public void tampilkanStatistik() {
        if (queue.isEmpty()) {
            System.out.println("Antrean kosong, tidak ada statistik untuk ditampilkan.");
            return;
        }

        int totalPerkara = queue.size();
        long totalVonisBulan = 0;
        long totalDenda = 0;
        double totalBeratGram = 0;

        // Struktur sederhana untuk mencatat frekuensi jenis narkotika
        List<String> daftarJenis = new ArrayList<>();
        List<Integer> daftarCount = new ArrayList<>();

        int n = queue.size();
        for (int i = 0; i < n; i++) {
            PutusanNarkotika p = queue.remove(0);
            
            totalVonisBulan += p.getVonisBulan();
            totalDenda += p.getDendaRupiah();
            totalBeratGram += p.getBeratBarangBuktiGram();

            // Hitung distribusi jenis narkotika
            String jenis = p.getJenisNarkotika().trim();
            if (jenis.isEmpty()) jenis = "Tidak Diketahui";
            
            int index = daftarJenis.indexOf(jenis);
            if (index != -1) {
                daftarCount.set(index, daftarCount.get(index) + 1);
            } else {
                daftarJenis.add(jenis);
                daftarCount.add(1);
            }
            
            queue.add(p); // Rotasi untuk mengembalikan antrean ke posisi semula
        }

        double rataRataVonis = (double) totalVonisBulan / totalPerkara;

        System.out.println("\n==========================================");
        System.out.println("       STATISTIK PUTUSAN NARKOTIKA        ");
        System.out.println("==========================================");
        System.out.println(" Total Perkara Diperiksa : " + totalPerkara);
        System.out.println(" Rata-rata Masa Vonis    : " + String.format("%.2f bulan", rataRataVonis));
        System.out.println(" Akumulasi Total Denda   : Rp " + String.format("%,d", totalDenda).replace(',', '.'));
        System.out.println(" Total Berat Barang Bukti: " + String.format("%.2f gram", totalBeratGram));
        System.out.println("--------------------------------------------------");
        System.out.println(" Distribusi Jenis Narkotika :");
        
        for (int i = 0; i < daftarJenis.size(); i++) {
            System.out.printf(" - %-28s : %d perkara\n", daftarJenis.get(i), daftarCount.get(i));
        }
        System.out.println("==================================================\n");
    }

        
}