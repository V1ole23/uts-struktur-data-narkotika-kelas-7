package com.strukdat.putusan.io;

import com.strukdat.putusan.struktur.AntrianGenerik;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    @Test
    @DisplayName("baris sederhana, CRLF, dan baris kosong dilewati")
    void sederhana() {
        AntrianGenerik<String[]> r = CsvParser.parse("a,b,c\r\n\r\n1,2,3\n");
        assertEquals(2, r.size());
        assertArrayEquals(new String[] {"a", "b", "c"}, r.dequeue());
        assertArrayEquals(new String[] {"1", "2", "3"}, r.dequeue());
    }

    @Test
    @DisplayName("koma dan baris baru di dalam tanda kutip, serta kutip ganda ")
    void berkutip() {
        AntrianGenerik<String[]> r = CsvParser.parse("\"x,y\",\"dia berkata \"\"ya\"\"\",\"baris1\nbaris2\"\n");
        String[] f = r.dequeue();
        assertEquals("x,y", f[0]);
        assertEquals("dia berkata \"ya\"", f[1]);
        assertEquals("baris1\nbaris2", f[2]);
    }

    @Test
    @DisplayName("tanda kutip tidak ditutup melempar exception")
    void kutipTidakDitutup() {
        assertThrows(IllegalArgumentException.class, () -> CsvParser.parse("a,\"b\n"));
    }

    @Test
    @DisplayName("escape dan gabungBaris dapat dibaca kembali tanpa perubahan")
    void bolakBalik() {
        String[] asli = {"biasa", "ada,koma", "ada \"kutip\"", "dua\nbaris", ""};
        String[] kembali = CsvParser.parse(CsvParser.gabungBaris(asli)).dequeue();
        assertArrayEquals(asli, kembali);
    }

    @Test
    @DisplayName("FormatAngka: koma desimal, titik ribuan, dan format berat")
    void angka() {
        assertEquals(5.39, FormatAngka.parseBerat("5,39"), 1e-12);
        assertEquals(5.39, FormatAngka.parseBerat("5.39"), 1e-12);
        assertEquals(1003.83, FormatAngka.parseBerat("1.003,83"), 1e-12);
        assertEquals(5.0, FormatAngka.parseBerat("5"), 1e-12);
        assertThrows(IllegalArgumentException.class, () -> FormatAngka.parseBerat("-1"));
        assertThrows(IllegalArgumentException.class, () -> FormatAngka.parseBerat("1,2,3"));
        assertEquals(1000000000L, FormatAngka.parseBulat("1.000.000.000"));
        assertThrows(IllegalArgumentException.class, () -> FormatAngka.parseBulat("1.5"));
        assertEquals("2.50", FormatAngka.formatBerat(2.5));
        assertEquals("344.8561", FormatAngka.formatBerat(344.8561));
        assertEquals("Rp1.000.000.000", FormatAngka.formatRupiah(1000000000L));
        assertEquals("Rp0", FormatAngka.formatRupiah(0));
    }
}
