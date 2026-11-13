package com.strukdat.putusan.struktur;

/** Aksi untuk setiap elemen saat penelusuran (posisi berbasis 0). */
@FunctionalInterface
public interface Pengunjung<T> {
    void kunjungi(T elemen, int posisi);
}
