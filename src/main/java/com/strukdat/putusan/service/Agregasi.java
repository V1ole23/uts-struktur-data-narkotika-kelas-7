package com.strukdat.putusan.service;

import com.strukdat.putusan.io.FormatAngka;
import com.strukdat.putusan.model.Putusan;
import com.strukdat.putusan.struktur.AntrianGenerik;

public final class Agregasi {
    private int totalPerkara;
    private int totalTerdakwa;
    private int perkaraDenganVonis;
    private long jumlahVonis;
    private long totalDenda;
    private String[] namaJenis = new String[4];
    private int[] jumlahJenis = new int[4];
    private int banyakJenis;

    private Agregasi() {
    }

    public static Agregasi hitung(AntrianGenerik<Putusan> data) {
        Agregasi a = new Agregasi();
        data.untukSetiap((p, i) -> {
            a.totalPerkara++;
            a.totalTerdakwa += p.getJumlahTerdakwa();
            if (p.getVonisBulan() > 0) {
                a.perkaraDenganVonis++;
                a.jumlahVonis += p.getVonisBulan();
            }
            a.totalDenda += p.getDendaRupiah();
            for (String j : p.getJenisArray()) {
                a.tambahJenis(j);
            }
        });
        return a;
    }

    private void tambahJenis(String jenis) {
        for (int i = 0; i < banyakJenis; i++) {
            if (namaJenis[i].equalsIgnoreCase(jenis)) {
                jumlahJenis[i]++;
                return;
            }
        }
        if (banyakJenis == namaJenis.length) {
            String[] n2 = new String[banyakJenis * 2];
            int[] j2 = new int[banyakJenis * 2];
            System.arraycopy(namaJenis, 0, n2, 0, banyakJenis);
            System.arraycopy(jumlahJenis, 0, j2, 0, banyakJenis);
            namaJenis = n2;
            jumlahJenis = j2;
        }
        namaJenis[banyakJenis] = jenis;
        jumlahJenis[banyakJenis++] = 1;
    }

    public int getTotalPerkara() { return totalPerkara; }
    public int getTotalTerdakwa() { return totalTerdakwa; }
    public int getPerkaraDenganVonis() { return perkaraDenganVonis; }
    public int getPerkaraDikecualikan() { return totalPerkara - perkaraDenganVonis; }
    public double getRataRataVonis() { return perkaraDenganVonis == 0 ? 0 : (double) jumlahVonis / perkaraDenganVonis; }
    public long getTotalDenda() { return totalDenda; }

    public int jumlahJenis(String jenis) {
        for (int i = 0; i < banyakJenis; i++) {
            if (namaJenis[i].equalsIgnoreCase(jenis)) {
                return jumlahJenis[i];
            }
        }
        return 0;
    }

    public int totalPenyebutanJenis() {
        int s = 0;
        for (int i = 0; i < banyakJenis; i++) {
            s += jumlahJenis[i];
        }
        return s;
    }

    public String laporan() {
        StringBuilder sb = new StringBuilder("=== AGREGASI ===\n");
        sb.append("Total perkara   : ").append(totalPerkara).append('\n');
        sb.append("Total terdakwa  : ").append(totalTerdakwa).append('\n');
        sb.append("Rata-rata vonis : ").append(String.format(java.util.Locale.ROOT, "%.2f", getRataRataVonis()))
          .append(" bulan (").append(perkaraDenganVonis).append(" perkara dihitung, ")
          .append(getPerkaraDikecualikan()).append(" vonis 0 dikecualikan)\n");
        sb.append("Total denda     : ").append(FormatAngka.formatRupiah(totalDenda)).append('\n');
        sb.append("Distribusi narkotika:\n");
        for (int i = 0; i < banyakJenis; i++) {
            sb.append("  - ").append(namaJenis[i]).append(": ").append(jumlahJenis[i]).append('\n');
        }
        int sabu = jumlahJenis("Sabu");
        int ganja = jumlahJenis("Ganja");
        sb.append("Ringkasan       : Sabu ").append(sabu).append(", Ganja ").append(ganja)
          .append(", Lainnya ").append(totalPenyebutanJenis() - sabu - ganja);
        return sb.toString();
    }
}
