# ADR-002: Strategi pencarian, filter, dan pengurutan

- Status: diterima

## Pencarian: rotateSearch
Pada setiap langkah elemen depan dibandingkan dengan kriteria. Bila tidak cocok, elemen itu dipindah ke belakang
(`dequeue` lalu `enqueue`). Berhenti saat cocok atau setelah satu putaran penuh. Lalu `restore()` memutar sisa
langkah sehingga urutan kembali seperti semula.

| Kasus | langkah banding | rotasi | pemulihan |
|---|---|---|---|
| elemen posisi `p` | `p + 1` | `p` | `size - p` (0 bila `p = 0`) |
| tidak ditemukan | `size` | `size` | 0 (satu putaran penuh sudah mengembalikan urutan) |

`HasilCari` melaporkan ketiga pencacah ini (fitur Cari wajib "melaporkan jumlah langkah").
Kompleksitas waktu O(n), ruang O(1).

## Filter
Penelusuran linear (`untukSetiap`) O(n) yang meng-`enqueue` elemen cocok ke antrian hasil baru; antrian utama
tidak dimodifikasi. Kriteria digabung dengan AND. Berat 0 (tidak ditemukan) dan vonis 0 (tidak terukur)
tidak lolos filter rentang karena nilainya bukan pengukuran.

## Pengurutan berdasarkan vonis
Elemen disalin ke array, diurutkan dengan merge sort stabil (O(n log n), ruang O(n)), lalu di-`enqueue` ke antrian
baru. Antrian utama tidak berubah sehingga invariant terjaga. Vonis 0 selalu ditempatkan paling akhir pada urutan
naik maupun turun karena 0 menandai pidana seumur hidup/mati/nihil, bukan 0 bulan.

## Alasan
- Mengurutkan antrian di tempat tidak dimungkinkan hanya dengan operasi FIFO tanpa biaya besar.
- Array biasa tidak termasuk koleksi terlarang dan hanya dipakai sebagai penampung sementara, bukan penyimpanan inti.
- Stabilitas menjaga urutan masuk untuk vonis yang sama.

## Konsekuensi
Pencarian per `caseId` atau nomor perkara tetap O(n); untuk 50 record dapat diterima.
