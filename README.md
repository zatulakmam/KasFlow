# KasFlow

Aplikasi Android untuk mencatat pemasukan dan pengeluaran pribadi, dibangun dengan Kotlin dan Jetpack Compose. KasFlow mendukung sinkronisasi real-time melalui Cloud Firestore, grafik keuangan interaktif, serta tema terang dan gelap.

## Fitur Utama

### Ringkasan Kas

- Saldo bersih keseluruhan dan persentase tingkat hemat bulanan.
- Total pemasukan dan pengeluaran pada bulan terpilih.
- Pratinjau arus kas enam bulan terakhir.
- Daftar transaksi terbaru dan kategori pengeluaran terbesar.

### Grafik dan Analisis

- **Arus kas enam bulan** — grafik batang interaktif untuk membandingkan pemasukan dan pengeluaran. Ketuk batang untuk memilih bulan.
- **Tren saldo bersih** — grafik garis untuk melihat selisih pemasukan dan pengeluaran setiap bulan.
- **Aktivitas harian** — distribusi pemasukan dan pengeluaran dalam satu bulan.
- **Rincian kategori** — grafik donat interaktif untuk kategori pemasukan atau pengeluaran.
- **Insight pengeluaran** — rata-rata pengeluaran harian dan hari dengan pengeluaran tertinggi.

### Manajemen Transaksi

- Tambah, ubah, dan hapus transaksi melalui bottom sheet.
- Tombol nominal cepat `+25rb`, `+50rb`, `+100rb`, dan `+500rb`.
- Pemilih tanggal transaksi.
- Pencarian langsung berdasarkan judul, kategori, atau catatan.
- Filter berdasarkan tipe dan kategori.
- Daftar transaksi yang dikelompokkan berdasarkan tanggal.

| Kategori pengeluaran | Kategori pemasukan |
| --- | --- |
| Makanan & Minuman | Gaji |
| Transportasi | Bonus & THR |
| Belanja | Usaha & Freelance |
| Tagihan & Utilitas | Investasi |
| Hiburan | Hadiah |
| Kesehatan | Lainnya |
| Pendidikan | |
| Lainnya | |

### Akun dan Sinkronisasi

- Login Google melalui Jetpack Credential Manager dan Firebase Authentication.
- Sinkronisasi transaksi real-time menggunakan Cloud Firestore.
- Data transaksi disimpan pada jalur `users/{userId}/transactions/{transactionId}`.
- Akses data per pengguna diatur melalui `firestore.rules`.

### Tampilan Adaptif

- Pilihan tema Sistem, Terang, dan Gelap.
- Tombol pergantian tema cepat.
- Preferensi tema tersimpan secara lokal melalui Jetpack DataStore.
- Navigasi bawah untuk ponsel dan sidebar untuk layar lebar atau tablet.

## Teknologi

| Komponen | Teknologi |
| --- | --- |
| Bahasa | Kotlin |
| Antarmuka | Jetpack Compose dan Material Design 3 |
| Arsitektur | MVVM, StateFlow, ViewModel, Repository Pattern |
| Autentikasi | Firebase Authentication dan Jetpack Credential Manager |
| Database | Cloud Firestore |
| Preferensi lokal | Jetpack DataStore Preferences |
| Pemuatan gambar | Coil Compose |
| Tipografi | Plus Jakarta Sans dan Inter |

## Struktur Proyek

Kode utama berada di `app/src/main/java/com/example/`.

| Lokasi | Fungsi |
| --- | --- |
| `MainActivity.kt` | Entry point, konfigurasi edge-to-edge, dan manajemen tema |
| `auth/GoogleAuthHelper.kt` | Integrasi Google Sign-In dan layar login |
| `data/FirestoreErrorHandler.kt` | Penanganan error Firestore |
| `data/TransactionRepository.kt` | Operasi CRUD dan stream real-time Firestore |
| `model/TransactionModels.kt` | Model transaksi, kategori, dan data grafik |
| `ui/CashflowApp.kt` | Navigasi utama, top bar, bottom bar, dan sidebar |
| `ui/components/` | Komponen UI, grafik Canvas, dialog, dan editor transaksi |
| `ui/screens/` | Halaman beranda, laporan, dan transaksi |
| `ui/theme/` | Warna, tipografi, tema, dan preferensi tema |
| `ui/viewmodel/CashflowViewModel.kt` | Pengelolaan state dan agregasi statistik bulanan |
| `util/Formatters.kt` | Format Rupiah serta perhitungan tanggal dan bulan |

## Menjalankan Proyek

### Prasyarat

- Android Studio yang kompatibel dengan versi Android Gradle Plugin proyek.
- Gradle dan JDK yang sesuai dengan konfigurasi build proyek.
- Android SDK 36.
- Perangkat atau emulator dengan Android API 24 atau lebih baru.
- Proyek Firebase dengan Google Authentication dan Cloud Firestore aktif.
- Node.js dan Firebase CLI untuk pengujian Firestore Security Rules.

Proyek menggunakan `minSdk = 24` dan `targetSdk = 36`.

### 1. Klon Repositori

Ganti `<username>` dengan pemilik repositori.

```bash
git clone https://github.com/<username>/kasflow.git
cd kasflow
```

### 2. Konfigurasi Firebase

1. Daftarkan aplikasi Android pada proyek Firebase menggunakan application ID yang sesuai dengan konfigurasi aplikasi.
2. Aktifkan provider Google pada Firebase Authentication.
3. Sesuaikan konfigurasi Google Sign-In, termasuk fingerprint sertifikat aplikasi.
4. Unduh konfigurasi Firebase dan simpan sebagai `app/google-services.json`.
5. Sesuaikan ID database Firestore pada `app/src/main/res/values/firebase_applet_config.xml`.
6. Deploy `firestore.rules` ke database Firestore yang digunakan aplikasi.

### 3. Buka di Android Studio

Buka folder proyek, jalankan Gradle Sync, lalu pilih perangkat atau emulator untuk menjalankan aplikasi.

## Build dan Pengujian

Perintah berikut menggunakan Gradle yang tersedia di terminal dan dijalankan dari direktori utama proyek.

### Build APK Debug

```bash
gradle assembleDebug
```

### Unit Test

```bash
gradle :app:testDebugUnitTest --tests "com.example.FormattersAndModelsTest" --tests "com.example.ExampleUnitTest"
```

### Pengujian Firestore Security Rules

Pastikan Firestore Emulator sudah berjalan pada port `8085` dan dependensi pengujian telah terpasang.

**Bash**

```bash
FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
```

**PowerShell**

```powershell
$env:FIRESTORE_EMULATOR_HOST = "127.0.0.1:8085"
node --test firestore.test.js
```

## Lisensi

Proyek ini dapat disesuaikan dan dikembangkan untuk kebutuhan pengelolaan keuangan pribadi maupun pembelajaran.

Ketentuan lisensi formal belum dicantumkan. Tambahkan berkas `LICENSE` untuk menetapkan ketentuan penggunaan, modifikasi, dan distribusi proyek.
