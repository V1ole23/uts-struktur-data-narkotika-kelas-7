package com.strukdat.putusan.struktur;

/** Syarat pencocokan elemen (pengganti Predicate bawaan). */
@FunctionalInterface
public interface Kriteria<T> {
    boolean cocok(T elemen);
}
