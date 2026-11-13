package com.strukdat.putusan.service;

import com.strukdat.putusan.model.Putusan;


public final class FilterPutusan {

    private String jenis;
    private Double beratMin;
    private Double beratMax;
    private String pasal;
    private Integer vonisMin;
    private Integer vonisMax;

    public FilterPutusan jenis(String nilai) { this.jenis = kosongJadiNull(nilai); return this; }
    public FilterPutusan beratMin(Double nilai) { this.beratMin = nilai; return this; }
    public FilterPutusan beratMax(Double nilai) { this.beratMax = nilai; return this; }
    public FilterPutusan pasalTerbukti(String nilai) { this.pasal = kosongJadiNull(nilai); return this; }
    public FilterPutusan vonisMin(Integer nilai) { this.vonisMin = nilai; return this; }
    public FilterPutusan vonisMax(Integer nilai) { this.vonisMax = nilai; return this; }

    public boolean cocok(Putusan p) {
        if (jenis != null && !p.memuatJenis(jenis)) {
            return false;
        }
        if (beratMin != null || beratMax != null) {
            double b = p.getBeratBarangBuktiGram();
            if (b <= 0 || (beratMin != null && b < beratMin) || (beratMax != null && b > beratMax)) {
                return false;
            }
        }
        if (pasal != null && !p.getPasalTerbukti().toLowerCase().contains(pasal.toLowerCase())) {
            return false;
        }
        if (vonisMin != null || vonisMax != null) {
            int v = p.getVonisBulan();
            if (v <= 0 || (vonisMin != null && v < vonisMin) || (vonisMax != null && v > vonisMax)) {
                return false;
            }
        }
        return true;
    }

    private static String kosongJadiNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (jenis != null) sb.append("jenis=").append(jenis).append(' ');
        if (beratMin != null) sb.append("beratMin=").append(beratMin).append(' ');
        if (beratMax != null) sb.append("beratMax=").append(beratMax).append(' ');
        if (pasal != null) sb.append("pasalTerbukti=").append(pasal).append(' ');
        if (vonisMin != null) sb.append("vonisMin=").append(vonisMin).append(' ');
        if (vonisMax != null) sb.append("vonisMax=").append(vonisMax).append(' ');
        return sb.length() == 0 ? "(tanpa kriteria)" : sb.toString().trim();
    }
}
