package com.strukdat.putusan.struktur;

/** Simpul tunggal berantai. Field berakses paket agar tes dapat merusak struktur dengan sengaja. */
final class NodeAntrian<T> {
    final T data;
    NodeAntrian<T> next;

    NodeAntrian(T data) {
        this.data = data;
    }
}
