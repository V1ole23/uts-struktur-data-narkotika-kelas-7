# ADR-003: Riwayat audit memakai Queue yang sama

- Status: diterima (keputusan kelompok)
- Konteks: tugas meminta satu struktur sekunder sederhana untuk riwayat, antrean validasi, atau pembalikan proses.

## Keputusan
Riwayat operasi disimpan pada instance kedua dari `AntrianGenerik<String>` (kelas yang sama dengan struktur utama),
dibungkus `LogAudit`. Setiap entri berisi waktu, operasi, detail, dan hasil.

## Alasan
- FIFO sesuai riwayat: entri tertua ditampilkan lebih dulu (urutan kejadian).
- Memakai kelas yang sudah teruji berarti `validateInvariant()` dan tes yang sama berlaku untuk struktur sekunder.
- Mengurangi jumlah kode yang harus dipahami dan dipertahankan.

## Konsekuensi
- Struktur sekunder tidak menyediakan pembalikan proses (undo). Itu tidak diwajibkan.
- Riwayat tidak dibatasi ukurannya; untuk sesi pendek ini tidak bermasalah.

## Alternatif yang ditolak
Stack terpisah untuk riwayat (LIFO, mendukung undo): ditolak untuk menjaga kode tetap ringkas.
