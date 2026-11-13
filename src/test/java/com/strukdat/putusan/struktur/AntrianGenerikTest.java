package com.strukdat.putusan.struktur;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AntrianGenerikTest {

    private AntrianGenerik<String> isi(String... nilai) {
        AntrianGenerik<String> q = new AntrianGenerik<>();
        for (String n : nilai) {
            q.enqueue(n);
        }
        return q;
    }

    @Test
    @DisplayName("FIFO, peek, add/get/contains/clear")
    void dasar() {
        AntrianGenerik<String> q = isi("a", "b", "c");
        assertEquals("a", q.peek());
        assertEquals(3, q.size());
        assertEquals("a", q.dequeue());
        assertEquals("b", q.dequeue());
        q.add("d");
        assertEquals("c", q.get(0));
        assertEquals("d", q.get(1));
        assertTrue(q.contains("d"));
        assertFalse(q.contains("a"));
        q.clear();
        assertTrue(q.isEmpty());
        q.validateInvariant();
    }

    @Test
    @DisplayName("kondisi batas: antrian kosong, posisi di luar rentang, elemen null")
    void batas() {
        AntrianGenerik<String> q = new AntrianGenerik<>();
        assertThrows(IllegalStateException.class, q::dequeue);
        assertThrows(IllegalStateException.class, q::peek);
        assertThrows(IndexOutOfBoundsException.class, () -> q.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> q.remove(0));
        q.enqueue("x");
        assertThrows(IndexOutOfBoundsException.class, () -> q.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> q.get(-1));
        assertThrows(NullPointerException.class, () -> q.enqueue(null));
        q.validateInvariant();
    }

    @Test
    @DisplayName("remove pertama, tengah, terakhir menjaga urutan sisa")
    void remove() {
        AntrianGenerik<String> q = isi("a", "b", "c", "d", "e");
        assertEquals("a", q.remove(0));
        assertEquals("[b, c, d, e]", q.toString());
        assertEquals("d", q.remove(2));
        assertEquals("[b, c, e]", q.toString());
        assertEquals("e", q.remove(2));
        assertEquals("[b, c]", q.toString());
        q.validateInvariant();
        q.remove(0);
        q.remove(0);
        assertTrue(q.isEmpty());
        q.validateInvariant();
    }

    @Test
    @DisplayName("rotateSearch: pencacah langkah untuk elemen pertama, terakhir, dan tidak ada")
    void rotateSearchLangkah() {
        AntrianGenerik<String> q = isi("1", "2", "3", "4", "5");

        HasilCari<String> depan = q.rotateSearch(x -> x.equals("1"));
        assertEquals(0, depan.getPosisi());
        assertEquals(1, depan.getLangkahBanding());
        assertEquals(0, depan.getLangkahRotasi());
        assertEquals(0, depan.getLangkahPemulihan());

        HasilCari<String> belakang = q.rotateSearch(x -> x.equals("5"));
        assertEquals(4, belakang.getPosisi());
        assertEquals(5, belakang.getLangkahBanding());
        assertEquals(4, belakang.getLangkahRotasi());
        assertEquals(1, belakang.getLangkahPemulihan());

        HasilCari<String> tidak = q.rotateSearch(x -> x.equals("9"));
        assertFalse(tidak.isDitemukan());
        assertEquals(-1, tidak.getPosisi());
        assertEquals(5, tidak.getLangkahBanding());
        assertEquals(5, tidak.getLangkahRotasi());

        assertEquals("[1, 2, 3, 4, 5]", q.toString());
        assertEquals(0, q.getOffsetRotasi());
        q.validateInvariant();
    }

    @Test
    @DisplayName("trace manual (docs/trace/TRACE.md): rotateSearch tanpa pemulihan, restore, dan remove")
    void traceManual() {
        AntrianGenerik<String> q = isi("B", "C", "D", "A");
        // baris tabel trace: setelah 1, 2, 3 rotasi
        q.rotateSearch(x -> x.equals("C"), false);
        assertEquals("[C, D, A, B]", q.toString());
        assertEquals(1, q.getOffsetRotasi());
        q.restore();
        q.rotateSearch(x -> x.equals("D"), false);
        assertEquals("[D, A, B, C]", q.toString());
        assertEquals(2, q.getOffsetRotasi());
        q.restore();
        // mencari A: ditemukan di posisi 3 setelah 3 rotasi
        HasilCari<String> h = q.rotateSearch(x -> x.equals("A"), false);
        assertEquals(3, h.getPosisi());
        assertEquals(4, h.getLangkahBanding());
        assertEquals(3, h.getLangkahRotasi());
        assertEquals("[A, B, C, D]", q.toString());
        assertEquals(3, q.getOffsetRotasi());
        assertEquals(1, q.restore());
        assertEquals("[B, C, D, A]", q.toString());
        // remove(1)
        assertEquals("C", q.remove(1));
        assertEquals("[B, D, A]", q.toString());
        q.validateInvariant();
    }

    @Test
    @DisplayName("operasi lain otomatis memulihkan urutan yang masih terputar")
    void operasiMemulihkan() {
        AntrianGenerik<String> q = isi("1", "2", "3", "4");
        q.rotateSearch(x -> x.equals("3"), false);
        assertEquals("1", q.peek());
        q.rotateSearch(x -> x.equals("4"), false);
        q.enqueue("5");
        assertEquals("[1, 2, 3, 4, 5]", q.toString());
    }

    @Test
    @DisplayName("validateInvariant mendeteksi siklus dan rantai terputus")
    void invariantMendeteksiKerusakan() {
        AntrianGenerik<String> siklus = isi("1", "2", "3");
        NodeAntrian<String> h = siklus.headUntukUji();
        h.next.next.next = h;
        assertTrue(assertThrows(IllegalStateException.class, siklus::validateInvariant)
                .getMessage().contains("Invariant dilanggar"));

        AntrianGenerik<String> putus = isi("1", "2", "3");
        putus.headUntukUji().next = null;
        assertThrows(IllegalStateException.class, putus::validateInvariant);
    }
}
