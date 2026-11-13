package com.strukdat.putusan.struktur;

/** Kontrak umum struktur data (posisi berbasis 0; 0 = elemen paling depan). */
public interface StrukturData<T> {
    void add(T value);
    T get(int position);
    T remove(int position);
    int size();
    boolean isEmpty();
    boolean contains(T value);
    void clear();
    /** Memeriksa konsistensi internal; melempar IllegalStateException bila rusak. */
    void validateInvariant();
}
