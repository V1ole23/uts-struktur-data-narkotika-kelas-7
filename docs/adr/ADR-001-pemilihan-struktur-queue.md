# ADR-001: Memilih Queue (FIFO) sebagai struktur data utama

- Status: diterima
- Konteks: tugas mewajibkan satu struktur utama buatan sendiri yang menyimpan seluruh objek `Putusan`,
  generik (`T`), tanpa koleksi bawaan Java, dan memiliki `validateInvariant()`.

## Keputusan
Struktur utama adalah `AntrianGenerik<T>`: Queue FIFO berbasis simpul berantai tunggal dengan pointer
`head` (depan) dan `tail` (belakang). Seluruh putusan disimpan di antrian ini selama aplikasi berjalan.

## Alasan
- Data putusan diproses mengikuti urutan masuk (urutan file CSV), cocok dengan FIFO.
- Skenario "pencarian tidak mengubah urutan akhir" (skenario 6) menjadi bagian inti desain, bukan tambahan:
  `rotateSearch` memutar antrian lalu `restore` mengembalikannya.
- Linked list memberi `enqueue` O(1) tanpa `resize` seperti array.

## Pemetaan operasi ke kontrak umum `StrukturData<T>`

| Kontrak | Pada Queue | Keterangan / penyimpangan |
|---|---|---|
| `add(v)` | `enqueue(v)` | O(1); elemen baru selalu di belakang |
| `get(pos)` | penelusuran dari `head` | O(n); tidak mengubah isi; memulihkan urutan bila masih terputar |
| `remove(pos)` | satu putaran `dequeue` lalu `enqueue`, elemen pos dilewati | O(n). Penyimpangan: Queue murni tidak mengenal hapus di tengah, maka dibuat dari operasi antrian agar tetap FIFO dan urutan sisa terjaga |
| `size()`, `isEmpty()` | field `size` | O(1) |
| `contains(v)` | penelusuran + `equals` | O(n); `Putusan.equals` berdasarkan `caseId` |
| `clear()` | `head = tail = null`, `size = 0` | O(1) |
| `validateInvariant()` | pemeriksaan di bawah | O(n) |
| (khusus Queue) `dequeue`, `peek` | hapus/baca `head` | O(1) |
| (khusus Queue) `rotateSearch`, `restore` | lihat ADR-002 | O(n) |

## Invariant
1. `size >= 0`; antrian kosong berarti `head == null`, `tail == null`, `offsetRotasi == 0`.
2. Antrian tidak kosong berarti `head` dan `tail` tidak null.
3. Jumlah simpul dari `head` sama dengan `size` (juga menangkap siklus), tidak ada elemen `null`.
4. Simpul terakhir adalah `tail` dan `tail.next == null`.
5. `0 <= offsetRotasi < size`.

## Konsekuensi
- Akses acak dan hapus di tengah O(n); untuk 50 record ini tidak berdampak, dan dicatat di analisis kompleksitas.
- `offsetRotasi` menambah satu field status; setiap operasi lain memanggil `restore()` lebih dulu sehingga
  pemanggil tidak pernah melihat antrian dalam keadaan terputar kecuali sengaja memakai `rotateSearch(k, false)`.

## Alternatif yang ditolak
- Array dinamis: akses O(1) tetapi hapus/sisip menggeser elemen dan tidak menonjolkan skenario 6.
- Stack: pencarian non-destruktif memerlukan tumpukan bantu kedua.
