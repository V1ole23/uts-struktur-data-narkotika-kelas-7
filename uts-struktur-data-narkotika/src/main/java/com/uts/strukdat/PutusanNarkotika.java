package com.uts.strukdat;

public class PutusanNarkotika {
    private String caseId;
    private String nomorPerkara;
    private String pengadilan;
    private String tanggalPutusan;
    private String terdakwaKode;
    private String jenisNarkotika;
    private double beratBarangBuktiGram;
    private String pasalDakwaan;
    private String pasalTerbukti;
    private int tuntutanBulan;
    private int vonisBulan;
    private long dendaRupiah;
    private String barangBukti;
    private String keadaanMemberatkan;
    private String keadaanMeringankan;
    private String sourcePages;

    // Konstruktor
    public PutusanNarkotika(String caseId, String nomorPerkara, String pengadilan, String tanggalPutusan,
                            String terdakwaKode, String jenisNarkotika, double beratBarangBuktiGram,
                            String pasalDakwaan, String pasalTerbukti, int tuntutanBulan, int vonisBulan,
                            long dendaRupiah, String barangBukti, String keadaanMemberatkan,
                            String keadaanMeringankan, String sourcePages) {
        this.caseId = caseId;
        this.nomorPerkara = nomorPerkara;
        this.pengadilan = pengadilan;
        this.tanggalPutusan = tanggalPutusan;
        this.terdakwaKode = terdakwaKode;
        this.jenisNarkotika = jenisNarkotika;
        this.beratBarangBuktiGram = beratBarangBuktiGram;
        this.pasalDakwaan = pasalDakwaan;
        this.pasalTerbukti = pasalTerbukti;
        this.tuntutanBulan = tuntutanBulan;
        this.vonisBulan = vonisBulan;
        this.dendaRupiah = dendaRupiah;
        this.barangBukti = barangBukti;
        this.keadaanMemberatkan = keadaanMemberatkan;
        this.keadaanMeringankan = keadaanMeringankan;
        this.sourcePages = sourcePages;
    }

    // Getter dan Setter
    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getNomorPerkara() { return nomorPerkara; }
    public void setNomorPerkara(String nomorPerkara) { this.nomorPerkara = nomorPerkara; }

    public String getPengadilan() { return pengadilan; }
    public void setPengadilan(String pengadilan) { this.pengadilan = pengadilan; }

    public String getTanggalPutusan() { return tanggalPutusan; }
    public void setTanggalPutusan(String tanggalPutusan) { this.tanggalPutusan = tanggalPutusan; }

    public String getTerdakwaKode() { return terdakwaKode; }
    public void setTerdakwaKode(String terdakwaKode) { this.terdakwaKode = terdakwaKode; }

    public String getJenisNarkotika() { return jenisNarkotika; }
    public void setJenisNarkotika(String jenisNarkotika) { this.jenisNarkotika = jenisNarkotika; }

    public double getBeratBarangBuktiGram() { return beratBarangBuktiGram; }
    public void setBeratBarangBuktiGram(double beratBarangBuktiGram) { this.beratBarangBuktiGram = beratBarangBuktiGram; }

    public String getPasalDakwaan() { return pasalDakwaan; }
    public void setPasalDakwaan(String pasalDakwaan) { this.pasalDakwaan = pasalDakwaan; }

    public String getPasalTerbukti() { return pasalTerbukti; }
    public void setPasalTerbukti(String pasalTerbukti) { this.pasalTerbukti = pasalTerbukti; }

    public int getTuntutanBulan() { return tuntutanBulan; }
    public void setTuntutanBulan(int tuntutanBulan) { this.tuntutanBulan = tuntutanBulan; }

    public int getVonisBulan() { return vonisBulan; }
    public void setVonisBulan(int vonisBulan) { this.vonisBulan = vonisBulan; }

    public long getDendaRupiah() { return dendaRupiah; }
    public void setDendaRupiah(long dendaRupiah) { this.dendaRupiah = dendaRupiah; }

    public String getBarangBukti() { return barangBukti; }
    public void setBarangBukti(String barangBukti) { this.barangBukti = barangBukti; }

    public String getKeadaanMemberatkan() { return keadaanMemberatkan; }
    public void setKeadaanMemberatkan(String keadaanMemberatkan) { this.keadaanMemberatkan = keadaanMemberatkan; }

    public String getKeadaanMeringankan() { return keadaanMeringankan; }
    public void setKeadaanMeringankan(String keadaanMeringankan) { this.keadaanMeringankan = keadaanMeringankan; }

    public String getSourcePages() { return sourcePages; }
    public void setSourcePages(String sourcePages) { this.sourcePages = sourcePages; }

    @Override
    public String toString() {
        return "PutusanNarkotika{" +
                "caseId='" + caseId + '\'' +
                ", nomorPerkara='" + nomorPerkara + '\'' +
                ", jenisNarkotika='" + jenisNarkotika + '\'' +
                ", vonisBulan=" + vonisBulan +
                '}';
    }
}