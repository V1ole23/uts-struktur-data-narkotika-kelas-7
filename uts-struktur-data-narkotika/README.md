# Identitas

Nama: Muhammad Qhoirul  
NIM: 202510370110245

# Struktur Pilihan

Struktur data utama yang digunakan dalam proyek ini adalah **Queue (Antrian)** yang diimplementasikan secara manual menggunakan **Singly Linked List**.

Implementasi Queue menggunakan prinsip FIFO (_First In, First Out_), yaitu data yang pertama dimasukkan akan menjadi data pertama yang dikeluarkan.

Operasi utama yang digunakan meliputi:

- `enqueue()` untuk menambahkan data ke antrian.
- `dequeue()` untuk menghapus data paling depan.
- `peek()` untuk melihat data paling depan tanpa menghapusnya.
- `rotateSearch()` untuk melakukan pencarian dengan rotasi antrian.
- `restore()` untuk mengembalikan urutan antrian seperti semula.
- `validateInvariant()` untuk memeriksa konsistensi struktur data.

# Cara Build

Pastikan perangkat telah menginstal:

- JDK 21 atau versi yang kompatibel.
- Apache Maven.

Buka terminal pada direktori utama proyek yang berisi file `pom.xml`, kemudian jalankan:

```bash
mvn clean package
```

Perintah tersebut akan membersihkan hasil build sebelumnya, mengompilasi program, menjalankan proses pengujian, dan menghasilkan file build pada direktori `target/`.

# Cara Test

Untuk menjalankan seluruh pengujian menggunakan Maven, jalankan:

```bash
mvn clean test
```

Pengujian dilakukan untuk memastikan operasi Queue berjalan dengan benar, termasuk penambahan data, penghapusan data, pencarian, rotasi antrian, pemulihan urutan, dan validasi invariant.

# Cara Menjalankan

Pastikan:

1. JDK 21 telah terinstal.
2. Maven telah terinstal dan dapat dijalankan melalui terminal.
3. File dataset `putusan-50.csv` tersedia di dalam direktori `data/`.
4. Proyek telah berhasil melalui proses build.

Jalankan program melalui terminal pada direktori utama proyek.

# Contoh Perintah

**1. Build proyek**

```bash
mvn clean package
```

**2. Menjalankan pengujian**

```bash
mvn clean test
```

**3. Menjalankan program**

Pastikan Anda Berata di Direktori Proyek

Perintah berikut merupakan contoh apabila proyek telah dikonfigurasi menggunakan Maven Exec Plugin:

```bash
mvn exec:java -Dexec.mainClass="com.uts.strukdat.App"
```
