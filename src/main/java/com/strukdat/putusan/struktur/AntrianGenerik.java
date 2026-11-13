package com.strukdat.putusan.struktur;

import java.util.Objects;

/**
 * Queue (FIFO) buatan sendiri berbasis simpul berantai (head = depan, tail = belakang).
 * Hanya memakai operasi antrian: remove dan rotateSearch dikerjakan dengan dequeue lalu enqueue.
 */
public class AntrianGenerik<T> implements StrukturData<T> {

    private NodeAntrian<T> head;
    private NodeAntrian<T> tail;
    private int size;
    /** Jumlah rotasi bersih sejak urutan asli (0 <= offsetRotasi < size). */
    private int offsetRotasi;

    // ---------------------------------------------------------------- operasi Queue

    public void enqueue(T nilai) {
        Objects.requireNonNull(nilai, "Elemen null tidak diperbolehkan");
        restore();
        enqueueMentah(nilai);
    }

    public T dequeue() {
        restore();
        return dequeueMentah();
    }

    public T peek() {
        restore();
        if (head == null) {
            throw new IllegalStateException("Antrian kosong");
        }
        return head.data;
    }

    /** Mencari lalu otomatis mengembalikan urutan asli. */
    public HasilCari<T> rotateSearch(Kriteria<T> kriteria) {
        return rotateSearch(kriteria, true);
    }

    /**
     * Mencari elemen pertama yang cocok. Setiap langkah membandingkan elemen depan; bila tidak cocok,
     * elemen itu dipindah ke belakang (dequeue lalu enqueue).
     * Bila pulihkanOtomatis = false, urutan dibiarkan terputar sampai restore() dipanggil.
     */
    public HasilCari<T> rotateSearch(Kriteria<T> kriteria, boolean pulihkanOtomatis) {
        restore();
        int n = size;
        int banding = 0;
        int rotasi = 0;
        int posisi = -1;
        T ketemu = null;
        for (int i = 0; i < n; i++) {
            banding++;
            if (kriteria.cocok(head.data)) {
                posisi = i;
                ketemu = head.data;
                break;
            }
            rotasiSatu();
            rotasi++;
        }
        int pemulihan = pulihkanOtomatis ? restore() : 0;
        return new HasilCari<>(posisi >= 0, posisi, banding, rotasi, pemulihan, ketemu);
    }

    /** Memutar antrian sampai urutan kembali seperti semula. Mengembalikan jumlah langkahnya. */
    public int restore() {
        int langkah = 0;
        while (offsetRotasi != 0) {
            rotasiSatu();
            langkah++;
        }
        return langkah;
    }

    public int getOffsetRotasi() {
        return offsetRotasi;
    }

    // ---------------------------------------------------------------- StrukturData

    @Override
    public void add(T value) {
        enqueue(value);
    }

    @Override
    public T get(int position) {
        restore();
        periksaPosisi(position);
        NodeAntrian<T> cur = head;
        for (int i = 0; i < position; i++) {
            cur = cur.next;
        }
        return cur.data;
    }

    /** Satu putaran penuh dequeue+enqueue; elemen pada posisi target tidak di-enqueue lagi. */
    @Override
    public T remove(int position) {
        restore();
        periksaPosisi(position);
        int n = size;
        T dihapus = null;
        for (int i = 0; i < n; i++) {
            T x = dequeueMentah();
            if (i == position) {
                dihapus = x;
            } else {
                enqueueMentah(x);
            }
        }
        return dihapus;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(T value) {
        for (NodeAntrian<T> cur = head; cur != null; cur = cur.next) {
            if (Objects.equals(cur.data, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
        offsetRotasi = 0;
    }

    @Override
    public void validateInvariant() {
        if (size == 0) {
            if (head != null || tail != null || offsetRotasi != 0) {
                gagal("antrian kosong tetapi head/tail/offset tidak kosong");
            }
            return;
        }
        if (size < 0 || head == null || tail == null) {
            gagal("size=" + size + " tetapi head atau tail null");
        }
        int hitung = 0;
        NodeAntrian<T> terakhir = null;
        for (NodeAntrian<T> cur = head; cur != null; cur = cur.next) {
            if (++hitung > size) {
                gagal("jumlah simpul melebihi size=" + size + " (kemungkinan siklus)");
            }
            terakhir = cur;
        }
        if (hitung != size) {
            gagal("jumlah simpul " + hitung + " tidak sama dengan size " + size);
        }
        if (terakhir != tail || tail.next != null) {
            gagal("tail tidak menunjuk simpul terakhir atau tail.next tidak null");
        }
        if (offsetRotasi < 0 || offsetRotasi >= size) {
            gagal("offsetRotasi " + offsetRotasi + " di luar rentang");
        }
    }

    // ---------------------------------------------------------------- utilitas

    /** Menelusuri dari depan ke belakang tanpa mengubah antrian. */
    public void untukSetiap(Pengunjung<T> pengunjung) {
        restore();
        int i = 0;
        for (NodeAntrian<T> cur = head; cur != null; cur = cur.next) {
            pengunjung.kunjungi(cur.data, i++);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        int batas = 0;
        for (NodeAntrian<T> cur = head; cur != null && batas <= size; cur = cur.next, batas++) {
            if (batas > 0) {
                sb.append(", ");
            }
            sb.append(cur.data);
        }
        return sb.append("]").toString();
    }

    /** Hanya untuk tes (paket): akses head agar struktur bisa dirusak dengan sengaja. */
    NodeAntrian<T> headUntukUji() {
        return head;
    }

    // ---------------------------------------------------------------- privat

    private void enqueueMentah(T nilai) {
        NodeAntrian<T> baru = new NodeAntrian<>(nilai);
        if (tail == null) {
            head = baru;
        } else {
            tail.next = baru;
        }
        tail = baru;
        size++;
    }

    private T dequeueMentah() {
        if (head == null) {
            throw new IllegalStateException("Antrian kosong");
        }
        T nilai = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return nilai;
    }

    private void rotasiSatu() {
        enqueueMentah(dequeueMentah());
        offsetRotasi = (offsetRotasi + 1) % size;
    }

    private void periksaPosisi(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Posisi " + position + " di luar rentang [0," + (size - 1) + "]");
        }
    }

    private static void gagal(String pesan) {
        throw new IllegalStateException("Invariant dilanggar: " + pesan);
    }
}
