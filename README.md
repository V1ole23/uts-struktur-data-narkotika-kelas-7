# UTS Struktur Data: Putusan Narkotika (Queue FIFO)

**Kelompok:** 7
**Anggota:** Muhammad Qhoirul / 202510370110245

**Mata kuliah:** Struktur Data, Universitas Muhammadiyah Malang, Semester Ganjil 2026/2027

## Struktur data pilihan

**Queue (FIFO) buatan sendiri**, `AntrianGenerik<T>`,

berbasis simpul berantai (`head`/`tail`).
Operasi: `enqueue`, `dequeue`, `peek`, `rotateSearch`, `restore`, serta kontrak `StrukturData<T>`
(`add`, `get`, `remove`, `size`, `isEmpty`, `contains`, `clear`) dan `validateInvariant()`.

Struktur sekunder (riwayat audit) memakai Queue yang sama. Alasan dan pemetaan operasi ada di `docs/adr/`.
Tidak ada koleksi `java.util` (ArrayList, LinkedList, Stack, Queue, Deque, List, Map, Set), iterator, atau stream.

## Build, test, dan menjalankan

Prasyarat: JDK 17+ dan Maven 3.8+.

```bash
mvn clean test                                   # 23 tes otomatis (JUnit 5)
mvn clean package                                # target/strukdat-putusan-1.0.jar
java -jar target/strukdat-putusan-1.0.jar data/putusan-50.csv
mvn compile exec:java                            # alternatif tanpa jar
```

Hanya JDK (tanpa Maven): `javac -d out $(find src/main -name '*.java')` lalu
`java -cp out com.strukdat.putusan.app.Main data/putusan-50.csv`.

**Penting:** path CSV dihitung dari folder kerja. Jalankan dari folder proyek ini. Jika program melapor
"File tidak terbaca", pilih menu 1 dan ketik path lengkap file CSV.

Menu: 1 Impor, 2 Tampil, 3 Tambah (tempel satu baris CSV), 4 Hapus, 5 Cari, 6 Filter, 7 Agregasi,
8 Urut vonis, 9 Audit, 10 Ekspor, 0 Keluar.

## Struktur repository

```
data/putusan-50.csv          dataset terstruktur (tanpa PDF sumber)
docs/data-provenance.csv     satu baris per atribut per putusan
docs/adr/                    ADR-001 struktur, ADR-002 pencarian/pengurutan, ADR-003 audit
docs/trace/TRACE.md          trace manual
docs/demo/demo-skenario.txt  keluaran 7 skenario pada data nyata
src/main/java, src/test/java kode dan tes
AI_USAGE.md, CONTRIBUTIONS.md
```

Membuat ulang demo: `mvn -q test-compile exec:java -Dexec.classpathScope=test
-Dexec.mainClass=com.strukdat.putusan.DemoSkenario > docs/demo/demo-skenario.txt`

## Aturan data

- Satu baris = satu putusan (file). Perkara dengan banyak terdakwa memakai kode `T001 | T002`, dan
  nilai tuntutan/vonis/denda per terdakwa dipisah `|` dengan urutan yang sama.
- Terdakwa dianonimkan dengan kode `Tnnn`. Nilai yang tidak ditemukan ditulis 0 (angka) atau null (teks), tidak diperkirakan.
- `tuntutanBulan`/`vonisBulan` pada objek = **nilai tertinggi** di antara terdakwa; `dendaRupiah` = **jumlah** denda semua terdakwa.
- Vonis 0 berarti **tidak terukur** (seumur hidup/mati/nihil): dikecualikan dari rata-rata vonis, tidak lolos
  filter vonis, dan diurutkan paling akhir.
- **Aturan berat (`beratBarangBuktiGram`):** berat bersih (netto) dijumlahkan untuk seluruh barang bukti narkotika pada
  satu putusan; bila hanya berat kotor yang tertulis, berat kotor dipakai; bila tidak ada, 0. Berat 0 tidak lolos filter berat.
  Daftar perkara yang memakai berat kotor: [ISI setelah verifikasi manual].
- Distribusi narkotika: perkara bertipe ganda (mis. Sabu | Ekstasi) dihitung pada tiap jenisnya.
- Format CSV: pemisah koma, UTF-8; koma desimal (`5,39`) dan titik desimal diterima.

## Validasi impor

Record ditolak (impor berlanjut) bila: jumlah kolom bukan 16; `caseId` bukan `Pnnn`; nomor perkara bukan
`nomor/Pid.Sus/tahun/PN Kota`; tanggal bukan `yyyy-MM-dd` yang valid; kode terdakwa bukan `Tnnn`; angka tidak valid;
jumlah nilai tuntutan/vonis/denda tidak sama dengan jumlah terdakwa; atau ada duplikat `caseId`, nomor perkara,
maupun kode terdakwa.

## Pengujian wajib (`PengujianWajibTest`)

| No  | Pengujian            | Bukti                                                                            |
| --- | -------------------- | -------------------------------------------------------------------------------- |
| 1   | Memuat data          | 50 baris dibaca, `size()` = jumlah valid, galat dicatat                          |
| 2   | Pencarian            | ada, tidak ada, posisi pertama/terakhir, pencacah langkah                        |
| 3   | Penghapusan          | elemen pertama, tengah, terakhir                                                 |
| 4   | Penambahan           | tambah kembali 3 record, `validateInvariant()`, urutan                           |
| 5   | Filter gabungan      | dibandingkan dengan perhitungan independen dari `data-provenance.csv`            |
| 6   | Pencarian pada Queue | urutan akhir tidak berubah (termasuk `rotateSearch` tanpa pemulihan + `restore`) |
| 7   | Data tidak valid     | pesan error muncul, struktur tetap konsisten                                     |

## Analisis kompleksitas

| Operasi                                                  | Waktu      | Ruang tambahan                    |
| -------------------------------------------------------- | ---------- | --------------------------------- |
| `enqueue`, `dequeue`, `peek`, `size`, `isEmpty`, `clear` | O(1)       | O(1)                              |
| `get`, `contains`, `validateInvariant`, tampil, filter   | O(n)       | O(1) (filter: O(k) untuk k hasil) |
| `remove(pos)`, `rotateSearch`, `restore`                 | O(n)       | O(1)                              |
| Urut vonis (merge sort stabil)                           | O(n log n) | O(n)                              |
| Agregasi                                                 | O(n)       | O(j) untuk j jenis narkotika      |
