# AdvancedCoreSurvival

**AdvancedCoreSurvival** adalah plugin inti survival yang komprehensif dan modular untuk server berbasis Paper, yang dirancang untuk memberikan pengalaman premium dan lengkap dalam satu paket. Dikembangkan oleh **Minekarta Studio**, plugin ini menggabungkan fitur-fitur esensial, ekonomi, klaim tanah, perbankan, dan RPG ke dalam satu paket yang sangat dapat dikonfigurasi.

---

## Daftar Isi

*   [Bab 1: Fitur Utama](#bab-1-fitur-utama)
*   [Bab 2: Instalasi](#bab-2-instalasi)
*   [Bab 3: Konfigurasi](#bab-3-konfigurasi)
    *   [Konfigurasi Utama (`config.yml`)](#konfigurasi-utama-configyml)
    *   [Konfigurasi Pesan (`messages.yml`)](#konfigurasi-pesan-messagesyml)
*   [Bab 4: Modul-Modul](#bab-4-modul-modul)
    *   [Modul Ekonomi](#modul-ekonomi)
    *   [Modul Bank](#modul-bank)
    *   [Modul RPG & Keterampilan](#modul-rpg--keterampilan)
    *   [Modul Klaim Tanah](#modul-klaim-tanah)
    *   [Modul Esensial](#modul-esensial)
*   [Bab 5: Perintah & Izin](#bab-5-perintah--izin)
*   [Bab 6: Panduan untuk Pengembang (API)](#bab-6-panduan-untuk-pengembang-api)
*   [Bab 7: Dukungan](#bab-7-dukungan)

---

## Bab 1: Fitur Utama

*   **🔌 Sistem Modular**: Aktifkan atau nonaktifkan fitur-fitur utama seperti Ekonomi, RPG, dan Klaim sesuai kebutuhan server Anda melalui `config.yml`.
*   **💾 Penyimpanan Data Fleksibel**: Dukungan penuh untuk **SQLite** (default, tanpa setup) dan **MySQL** (untuk skalabilitas server yang lebih besar). Semua operasi database dilakukan secara **asinkron** untuk mencegah lag server.
*   **🌐 Sistem Lokalisasi**: Semua pesan yang ditampilkan kepada pemain dapat diubah dan diterjemahkan dengan mudah melalui file `messages.yml`.
*   **🏦 Sistem Perbankan**: Pemain dapat membuat bank milik mereka sendiri, menyimpan uang, dan mengundang anggota lain untuk bergabung.
*   **⚔️ Sistem RPG**: Sistem leveling dengan statistik (Kekuatan, Kelincahan, Daya Tahan) dan pohon keterampilan yang dapat ditingkatkan menggunakan Poin Keterampilan.
*   **🔒 Klaim Tanah**: Lindungi tanah Anda dari pemain lain dengan sistem klaim berbasis chunk.
*   **💰 Ekonomi Terintegrasi**: Sistem ekonomi yang kuat dengan dukungan Vault.
*   **🏠 Fitur Esensial**: Perintah-perintah penting seperti `/home`, `/spawn`, dan lainnya.
*   **📊 GUI Interaktif**: Antarmuka pengguna grafis untuk fitur-fitur seperti Pohon Keterampilan dan Bank, memberikan pengalaman pengguna yang lebih baik.

## Bab 2: Instalasi

1.  Unduh rilis terbaru `AdvancedCoreSurvival.jar` dari [halaman rilis](https://github.com/Minekarta-Studio/AdvancedCoreSurvival/releases).
2.  Tempatkan file `.jar` ke dalam folder `plugins/` server Anda.
3.  (Wajib) Instal [Vault](https://www.spigotmc.org/resources/vault.34315/).
4.  (Opsional) Instal [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) untuk menggunakan placeholder dari plugin ini.
5.  Mulai server Anda sekali untuk menghasilkan file konfigurasi default.
6.  Edit file konfigurasi di `/plugins/AdvancedCoreSurvival/` sesuai kebutuhan.
7.  Mulai ulang server atau gunakan `/acs reload` (jika diimplementasikan).

## Bab 3: Konfigurasi

### Konfigurasi Utama (`config.yml`)

File ini mengontrol semua aspek fungsional dari plugin.

*   **`modules`**: Aktifkan (`true`) atau nonaktifkan (`false`) modul-modul utama.
    ```yaml
    modules:
      claims: true
      economy: true
      rpg: true
      bank: true
      # ...dan lainnya
    ```
*   **`storage`**: Pilih jenis penyimpanan data Anda.
    *   **`type`**: `sqlite` (direkomendasikan untuk server kecil) atau `mysql` (direkomendasikan untuk server besar atau jaringan).
    *   **`mysql`**: Jika Anda menggunakan `mysql`, isi detail koneksi database Anda di sini.
    ```yaml
    storage:
      type: sqlite # atau mysql
      mysql:
        host: "localhost"
        port: 3306
        database: "advancedcoresurvival"
        username: "root"
        password: ""
    ```
*   **Konfigurasi per Modul**: Setiap modul memiliki bagian konfigurasinya sendiri (misalnya, `bank`, `rpg`, `claims`) di mana Anda dapat menyesuaikan pengaturan spesifik seperti biaya pembuatan bank, perolehan EXP, dll.

### Konfigurasi Pesan (`messages.yml`)

File ini berisi semua teks yang dilihat oleh pemain. Anda dapat menerjemahkan atau mengubah pesan apa pun di sini.

*   **`prefix`**: Awalan global yang ditambahkan ke sebagian besar pesan plugin.
*   **Struktur Pesan**: Pesan diatur berdasarkan kategori (misalnya, `bank`, `rpg`, `general`). Anda dapat menggunakan kode warna Bukkit standar dengan simbol `&`.
    ```yaml
    prefix: "&8[&aACS&8] &r"
    bank:
      deposit-success: "&aAnda menyimpan %amount% ke bank."
      insufficient-funds: "&cSaldo bank tidak mencukupi untuk penarikan ini."
    ```

## Bab 4: Modul-Modul

### Modul Ekonomi
*   **Deskripsi**: Menyediakan fondasi untuk semua fitur ekonomi. Terintegrasi dengan Vault.
*   **Perintah Utama**: `/balance`, `/pay`, `/baltop`.

### Modul Bank
*   **Deskripsi**: Memungkinkan pemain untuk membuat dan mengelola bank bersama. Bank memiliki saldo sendiri dan daftar anggota.
*   **Fitur**:
    *   Buat dan hapus bank dengan biaya yang dapat dikonfigurasi.
    *   Setor dan tarik uang dari saldo bank.
    *   Undang pemain lain untuk menjadi anggota atau keluarkan mereka.
*   **Perintah Utama**: `/bank <gui|create|deposit|withdraw|...>`

### Modul RPG & Keterampilan
*   **Deskripsi**: Tambahkan elemen RPG ke server Anda. Pemain mendapatkan EXP dari berbagai aktivitas (misalnya, membunuh monster) untuk naik level.
*   **Fitur**:
    *   **Level & EXP**: Sistem leveling yang dapat dikonfigurasi.
    *   **Poin Keterampilan**: Dapatkan poin setiap kali naik level untuk dibelanjakan di Pohon Keterampilan.
    *   **GUI Pohon Keterampilan**: Buka dengan `/skills` atau `/rpg`. GUI interaktif untuk melihat statistik dan meningkatkan keterampilan pasif seperti kerusakan pedang atau pertahanan.
*   **Perintah Utama**: `/skills`, `/rpg <stats|skills>`

### Modul Klaim Tanah
*   **Deskripsi**: Sistem perlindungan tanah berbasis chunk.
*   **Fitur**:
    *   Klaim tanah untuk melindunginya dari orang lain.
    *   Pajak klaim harian (opsional) untuk menyeimbangkan ekonomi.
*   **Perintah Utama**: `/claim <create|delete|trust|...>`

### Modul Esensial
*   **Deskripsi**: Menyediakan perintah-perintah dasar yang penting untuk server survival.
*   **Perintah Utama**: `/spawn`, `/sethome`, `/home`, `/delhome`, `/tpa`.

## Bab 5: Perintah & Izin

Setiap perintah memiliki izin terkait. Berikut adalah ringkasan singkat. Untuk daftar lengkap, periksa file `plugin.yml`.

| Perintah                 | Izin                                        | Deskripsi                                        |
| ------------------------ | ------------------------------------------- | ------------------------------------------------ |
| `/acs`                   | `advancedcoresurvival.admin`                | Perintah admin utama.                            |
| `/bank`                  | `advancedcoresurvival.bank.base`            | Perintah dasar untuk manajemen bank.             |
| `/bank create`           | `advancedcoresurvival.bank.create`          | Untuk membuat bank baru.                         |
| `/skills` atau `/rpg`    | `advancedcoresurvival.rpg.base`             | Membuka GUI Keterampilan atau melihat statistik. |
| `/claim create`          | `advancedcoresurvival.claim.create`         | Untuk mengklaim sebidang tanah.                  |
| `/home`, `/sethome`      | `advancedcoresurvival.essentials.home`      | Perintah dasar untuk rumah pemain.               |

## Bab 6: Panduan untuk Pengembang (API)

AdvancedCoreSurvival dirancang agar dapat diperluas.

*   **Antarmuka `Storage`**: Semua interaksi database dilakukan melalui antarmuka `Storage`. Anda dapat mengaksesnya melalui `plugin.getStorageManager().getStorage()` untuk berinteraksi dengan data pemain secara asinkron dan aman.
*   **Event Kustom**: Plugin ini menyediakan event-event kustom (misalnya, `ACSBalanceChangeEvent`) yang dapat Anda dengarkan untuk berintegrasi dengan sistem Anda.
*   **Sistem Modul**: Anda dapat membangun modul Anda sendiri dengan mengimplementasikan antarmuka `Module` dan mendaftarkannya di `ModuleManager`.

## Bab 7: Dukungan

Jika Anda menemukan bug, memiliki permintaan fitur, atau butuh bantuan, silakan [buka isu](https://github.com/Minekarta-Studio/AdvancedCoreSurvival/issues) di repositori GitHub kami. Harap sertakan versi server, versi plugin, dan log atau file konfigurasi yang relevan.
