# Trace Manual Queue (FIFO)

Notasi `[depan, ..., belakang]`. Tabel di bawah diverifikasi oleh tes
`AntrianGenerikTest.traceManual` sehingga selalu sama dengan perilaku kode.

## 1. Aturan inti

| Operasi | Efek | Biaya |
|---|---|---|
| `enqueue(x)` | x masuk di belakang (`tail`) | O(1) |
| `dequeue()` | elemen depan (`head`) keluar | O(1) |
| `peek()` | membaca depan | O(1) |
| `rotateSearch(k)` | depan tidak cocok: `dequeue` lalu `enqueue`; ulangi sampai cocok / satu putaran | O(n) |
| `restore()` | memutar `size - offsetRotasi` kali agar urutan asli kembali | O(n) |
| `remove(pos)` | satu putaran penuh `dequeue`+`enqueue`, elemen `pos` tidak di-enqueue lagi | O(n) |

`offsetRotasi` = jumlah rotasi bersih sejak urutan asli. Nilainya 0 saat urutan asli.

## 2. rotateSearch("A") pada `[B, C, D, A]`, tanpa pemulihan otomatis

| Langkah | Depan | Cocok? | Aksi | Antrian sesudah | offsetRotasi |
|---|---|---|---|---|---|
| 1 | B | tidak | rotasi | [C, D, A, B] | 1 |
| 2 | C | tidak | rotasi | [D, A, B, C] | 2 |
| 3 | D | tidak | rotasi | [A, B, C, D] | 3 |
| 4 | A | ya | berhenti | [A, B, C, D] | 3 |

Hasil: posisi asli 3, 4 langkah banding, 3 rotasi. `restore()` memutar `4 - 3 = 1` langkah:
`[A, B, C, D]` menjadi `[B, C, D, A]`, offset 0.

Rumus umum: elemen di posisi `p` perlu `p + 1` langkah banding, `p` rotasi, `size - p` langkah pemulihan.
Bila tidak ditemukan: `size` langkah banding dan `size` rotasi; urutan kembali sendiri.

## 3. remove(1) pada `[B, C, D, A]` (size = 4, empat putaran)

| i | dequeue | i == 1? | Aksi | Antrian |
|---|---|---|---|---|
| 0 | B | tidak | enqueue(B) | [C, D, A, B] |
| 1 | C | ya | disimpan sebagai hasil, tidak di-enqueue | [D, A, B] |
| 2 | D | tidak | enqueue(D) | [A, B, D] |
| 3 | A | tidak | enqueue(A) | [B, D, A] |

Hasil: C terhapus dan urutan sisa `[B, D, A]` sama dengan urutan awal tanpa C.

## 4. Invariant (`validateInvariant`)
1. `size >= 0`; antrian kosong: `head == tail == null` dan `offsetRotasi == 0`.
2. Antrian tidak kosong: `head` dan `tail` tidak null.
3. Jumlah simpul dari `head` sama dengan `size` (menangkap siklus); tidak ada elemen null.
4. Simpul terakhir adalah `tail`; `tail.next == null`.
5. `0 <= offsetRotasi < size`.

Tes `invariantMendeteksiKerusakan` merusak struktur (siklus, rantai terputus) dan memastikan pelanggaran terdeteksi.

## 5. Bukti pada data nyata
`docs/demo/demo-skenario.txt` memuat keluaran 7 skenario pada `data/putusan-50.csv`, termasuk perubahan pointer
saat menghapus elemen pertama, tengah, dan terakhir.
