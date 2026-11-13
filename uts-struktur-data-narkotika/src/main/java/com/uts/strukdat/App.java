package com.uts.strukdat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        System.out.println("Memuat data putusan pengadilan narkotika...");
        List<String> errorLog = new ArrayList<>();
        
        // Load dataset dari folder data/putusan-50.csv
        MyQueue<PutusanNarkotika> queue = CsvParser.parseCsv("C:\\A-Code\\Semester3\\Strukdat\\UTS\\uts-struktur-data-narkotika-kelas-7\\uts-struktur-data-narkotika\\data\\putusan-50.csv", errorLog);
        
        if (!errorLog.isEmpty()) {
            System.out.println("Peringatan saat parsing CSV:");
            for (String err : errorLog) {
                System.out.println(" - " + err);
            }
        }

        System.out.println("Berhasil memuat " + queue.size() + " data putusan ke dalam Antrean (Queue).\n");

        PutusanService service = new PutusanService(queue);
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("==========================================");
            System.out.println("   APLIKASI MANAJEMEN PUTUSAN NARKOTIKA   ");
            System.out.println("==========================================");
            System.out.println("1. Tampilkan Semua Putusan");
            System.out.println("2. Cari Putusan (CaseID / Nomor Perkara)");
            System.out.println("3. Tambah Putusan Baru");
            System.out.println("4. Hapus Putusan (Berdasarkan Posisi)");
            System.out.println("5. Filter Berdasarkan Jenis Narkotika");
            System.out.println("6. Tampilkan Statistik & Agregasi");
            System.out.println("0. Keluar");
            System.out.print("Pilih menu (1-7): ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    service.tampilkanSemua();
                    break;

                case "2":
                    System.out.print("Masukkan kata kunci (contoh: P001 atau nomor perkara): ");
                    String keyword = scanner.nextLine();
                    service.cariPutusan(keyword);
                    break;

                case "3":
                    System.out.println("\n--- Tambah Putusan Baru ---");
                    System.out.print("CaseID (misal: P051): ");
                    String caseId = scanner.nextLine();
                    System.out.print("Nomor Perkara: ");
                    String nomorPerkara = scanner.nextLine();
                    System.out.print("Pengadilan: ");
                    String pengadilan = scanner.nextLine();
                    System.out.print("Tanggal Putusan (yyyy-MM-dd): ");
                    String tgl = scanner.nextLine();
                    System.out.print("Kode Terdakwa: ");
                    String terdakwa = scanner.nextLine();
                    System.out.print("Jenis Narkotika: ");
                    String jenis = scanner.nextLine();
                    System.out.print("Berat Barang Bukti (gram): ");
                    double berat = Double.parseDouble(scanner.nextLine().replace(",", "."));
                    System.out.print("Pasal Dakwaan: ");
                    String dakwaan = scanner.nextLine();
                    System.out.print("Pasal Terbukti: ");
                    String terbukti = scanner.nextLine();
                    System.out.print("Tuntutan (bulan): ");
                    int tuntutan = Integer.parseInt(scanner.nextLine());
                    System.out.print("Vonis (bulan): ");
                    int vonis = Integer.parseInt(scanner.nextLine());
                    System.out.print("Denda (Rupiah): ");
                    long denda = Long.parseLong(scanner.nextLine());

                    PutusanNarkotika baru = new PutusanNarkotika(
                        caseId, nomorPerkara, pengadilan, tgl, terdakwa, jenis, berat,
                        dakwaan, terbukti, tuntutan, vonis, denda, "Barang bukti uji",
                        "Memberatkan", "Meringankan", "1"
                    );
                    service.tambahPutusan(baru);
                    break;

                case "4":
                    System.out.print("Masukkan posisi indeks yang ingin dihapus (0 s.d " + (queue.size() - 1) + "): ");
                    try {
                        int pos = Integer.parseInt(scanner.nextLine());
                        service.hapusPutusan(pos);
                    } catch (NumberFormatException e) {
                        System.out.println("Masukan harus berupa angka.");
                    }
                    break;

                case "5":
                    boolean filterRunning = true;
                    while (filterRunning) {
                        System.out.println("\n=== MENU FILTER BERDASARKAN ===");
                        System.out.println("1. Jenis Narkotika");
                        System.out.println("2. Rentang Berat Barang Bukti (Gram)");
                        System.out.println("3. Pasal Terbukti");
                        System.out.println("4. Rentang Vonis (Bulan)");
                        System.out.println("5. Kembali ke Menu Utama");
                        System.out.print("Pilih kriteria filter (1-5): ");
                        
                        String fChoice = scanner.nextLine().trim();
                        switch (fChoice) {
                            case "1":
                                System.out.print("Masukkan jenis narkotika (Sabu / Ekstasi): ");
                                service.filterBerdasarkanJenis(scanner.nextLine());
                                break;
                            case "2":
                                System.out.print("Masukkan berat minimum (gram): ");
                                double minB = Double.parseDouble(scanner.nextLine().replace(",", "."));
                                System.out.print("Masukkan berat maksimum (gram): ");
                                double maxB = Double.parseDouble(scanner.nextLine().replace(",", "."));
                                service.filterBerdasarkanRentangBerat(minB, maxB);
                                break;
                            case "3":
                                System.out.print("Masukkan nomor pasal (contoh: Pasal 114): ");
                                service.filterBerdasarkanPasal(scanner.nextLine());
                                break;
                            case "4":
                                System.out.print("Masukkan vonis minimum (bulan): ");
                                int minV = Integer.parseInt(scanner.nextLine());
                                System.out.print("Masukkan vonis maksimum (bulan): ");
                                int maxV = Integer.parseInt(scanner.nextLine());
                                service.filterBerdasarkanRentangVonis(minV, maxV);
                                break;
                            case "5":
                                filterRunning = false;
                                break;
                            default:
                                System.out.println("Pilihan tidak valid.");
                        }
                    }
                    break;

                case "6":
                    service.tampilkanStatistik();
                    break;

                case "0":
                    running = false;
                    System.out.println("Terima kasih telah menggunakan aplikasi ini.");
                    break;

                default:
                    System.out.println("Pilihan tidak valid. Silakan pilih 1-7.");
            }
            System.out.println();
        }
        scanner.close();
    }
}