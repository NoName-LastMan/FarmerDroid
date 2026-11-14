# Farming Droid (Minecraft Mod)

Mod Minecraft berbasis **NeoForge** untuk versi Minecraft **1.21.1** (LTS). Mod ini berfokus pada otomasi pertanian dengan drone/droid pembantu.

> **Catatan Git & Kontributor:**  
> Proyek ini di-setup secara bersih dari nol (*clean scaffold*) tanpa melakukan `git clone` dari repositori template atau upstream MDK. Dengan cara ini, riwayat commit bersih 100% milik Anda dan tidak akan ada akun kontributor asing yang tiba-tiba muncul di tab *Contributors* repositori GitHub Anda.

---

## 🛠️ Persyaratan Lingkungan (Prerequisites)

- **Java JDK 21** (Minecraft 1.20.6 / 1.21+ memerlukan Java 21).
  - Cek dengan: `java -version`
- **IDE yang disarankan:** IntelliJ IDEA (Community atau Ultimate) atau VS Code dengan Extension Pack for Java.

---

## 📂 Struktur Proyek

```text
FarmingDroid/
├── .gitignore
├── build.gradle                               # Konfigurasi ModDevGradle NeoForge
├── gradle.properties                          # Versi mod, Minecraft, dan NeoForge
├── settings.gradle                            # Pengaturan repository plugin
├── gradlew & gradlew.bat                      # Gradle wrapper executable
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── src/
    ├── main/
    │   ├── java/com/farmingdroid/
    │   │   ├── FarmingDroid.java              # Kelas utama Mod (@Mod)
    │   │   └── init/
    │   │       ├── ModItems.java              # Registrasi Item
    │   │       └── ModCreativeTabs.java       # Registrasi Creative Tab
    │   ├── resources/
    │   │   ├── pack.mcmeta                    # Resource pack metadata (pack_format: 34)
    │   │   ├── META-INF/neoforge.mods.toml    # Metadata mod
    │   │   └── assets/farmingdroid/
    │   │       └── lang/en_us.json            # Lokalisasi bahasa
    │   └── templates/
    │       └── META-INF/neoforge.mods.toml    # Template token untuk build otomatis
```

---

## 🚀 Membuka di IDE

### 1. IntelliJ IDEA (Rekomendasi)
1. Buka IntelliJ IDEA -> **Open**.
2. Pilih direktori `FarmingDroid` (pastikan memilih file `build.gradle` atau root folder).
3. Pilih **Open as Project**.
4. Tunggu IntelliJ menyelesaikan sinkronisasi Gradle (akan mengunduh dependensi Minecraft dan NeoForge).

### 2. VS Code
1. Buka folder `FarmingDroid` di VS Code.
2. Pastikan ekstensi Java & Gradle aktif.

---

## 💻 Perintah Gradle yang Sering Digunakan

Jalankan perintah ini di terminal root direktori proyek:

- **Menjalankan Minecraft Client untuk Testing:**
  ```bash
  ./gradlew runClient
  ```

- **Menjalankan Dedicated Server:**
  ```bash
  ./gradlew runServer
  ```

- **Menjalankan Data Generators (untuk loot tables, recipes, models otomatis):**
  ```bash
  ./gradlew runData
  ```

- **Membuat File Mod (`.jar`):**
  ```bash
  ./gradlew build
  ```
  File `.jar` hasil kompilasi akan berada di folder `build/libs/`.

---

## ⚙️ Konfigurasi Mod

Jika Anda ingin mengubah nama mod, deskripsi, atau versi NeoForge, Anda cukup mengubah nilai di file:
- `gradle.properties`

Nilai tersebut akan otomatis diterapkan ke seluruh proyek dan file metadata.
