# ABBAS


TV/IPTV izleme, YouTube, tarayıcı ve izinle ekran yansıtma için Türkçe Android uygulaması.

[APK indirme ve kurulum sayfası](https://github.com/darklonee38/abbas/releases) · [**En son ABBAS APK’sını indir**](https://github.com/darklonee38/abbas/releases/latest) · [Lisans](LICENSE) · [Üçüncü taraf bildirimleri](THIRD_PARTY_NOTICES.md)

## Sürüm durumu

> **Not:** Android Auto görünürlüğü telefon, Android Auto sürümü ve Google’ın uygulama kategorisi/dağıtım kurallarına bağlıdır. Standart Android paket yükleyicisi kullanılır; görünürlük veya uyumluluk garantisi verilmez.

**ABBAS — ilk yeniden markalanmış sürüm bekleniyor** — [APK’yı indir](https://github.com/darklonee38/abbas/releases/latest)

- Ana sayfada **Favori Kanallar** rafı ve kendi web sitelerinizi ekleyebileceğiniz **Kısayol ekle** kartı.
- **Bildirimsiz ve arka plan güncellemesiz:** veriler yalnızca uygulama kullanılırken güncellenir.
- **Sesli karşılama** (isteğe bağlı): araca bağlanınca “Günaydın, ABBAS’ya hoş geldin” karşılaması.
- Ana sayfada **Podcast**: arama, abone olma, bölüm dinleme ve kaldığı yerden devam.
- **Direksiyon tuşları**: ileri/geri tuşlarıyla kanal, istasyon veya bölüm değiştirme.
- Ana sayfada **Radyo**: Türk radyoları, Popüler, En çok oy alan, Ülkeler, Etiketler ve kendi radyo akışını ekleme; araçta radyo oynatıcı çubuğuyla çalışır.
- TV listesinde varsayılan **Türk radyoları** listesi (Radio Browser dizini).
- TV ana listesinde **+** düğmesiyle kendi M3U/IPTV listenizi ekleme; düğme yalnızca bu ekranda bulunur.

2.1.7 ve sonrası yeni bir imza anahtarıyla derlenir; telefonda daha eski bir ABBAS kuruluysa önce onu kaldırın. Fiziksel telefon ve araç testi yapılmadı.

2.1.4, kullanıcıların TV/YouTube açarken kapanma bildirmesi üzerine kararlı sürüm listesinden geri çekildi. **Kullanıcı 2.1.5’in çalıştığını bildirdi; normal kurulumla Android Auto’da görünmediği de bildirildi.** Android Auto görünürlüğü doğrulaması beklenmektedir; sürüm ön sürüm olarak kalır. Kapanmanın kesin nedeni cihaz hata kaydı olmadan doğrulanmış sayılmaz. Ön sürüm otomatik güncelleme olarak önerilmez.

## Kapsam

Ana sayfada TV, YouTube ve ekran yansıtma ana kartları; tarayıcı, WindAsist ve ABBAS web kısayolları bulunur. Uygulama sayfalarında ana sayfaya dönüş korunur. Yan kısayollar varsayılan olarak gizlidir; ayarlardan açılır.

TV’nin varsayılan M3U kaynağı https://onureroz.com/indirmeler/turk/index.m3u adresidir; yanında Radio Browser dizininden en çok oy alan Türk radyolarının listesi bulunur. Kullanıcı TV ana listesindeki + düğmesiyle kaynak ekleyebilir, uzun basarak silebilir; silinen varsayılan listeler aynı menüden geri yüklenir. HLS, DASH ve diğer IPTV biçimlerini oynatmak için ExoPlayer/VLC motorları korunmuştur. Radyo (Radio Browser dizini) ve Podcast eklentileri APK’dadır. Sesli kitap, Stremio, sohbet, Cast, Google Drive, SMB ve SFTP eklentileri APK’ya derlenmez. Kamera, mikrofon, konum ve genel medya/dosya okuma izinleri kaldırılmıştır.

Ekran yansıtma Android’in MediaProjection iznini ister. Android Auto telefon ekranını kendiliğinden açmaya izin vermezse telefon uygulama simgesi veya bildirim aynı bekleyen isteği açar. İzin iptali kaydı başlatmaz; ana sayfaya dönüş/bağlantı kesilmesi paylaşımı durdurur. Bu görüntü paylaşımıdır; uzaktan dokunmatik kontrol veya telefon sesi aktarımı değildir.

## Android Auto

Uygulama standart Android paket yükleyicisiyle kurulur. ABBAS’ın Android Auto’da görünmesi; Android Auto/telefon sürümüne, uygulamanın araç ekranı kategorisine ve Google’ın dağıtım kurallarına bağlıdır. Özel video/tarayıcı/yansıtma arayüzü eski projection SDK’sını kullanır; Google onayı veya tüm cihazlarda görünürlük garantisi verilmez.

## Doğrulama ve derleme

`verifyWebOnlyProductionGraph` görevi artık yalnızca `tv`, `web`, `exoplayer`, `vlc` üretim modüllerine izin verir. Statik APK kapısı yasak izinleri, kaldırılan eklenti sınıflarını, root/Xposed/erişilebilirlik kodlarını ve yansıtma bileşenlerini kontrol eder. Bu kontroller banka/Play Protect onayı veya zararlı yazılım sertifikası değildir.

Java 21, Android SDK 36 ve Gradle wrapper ile:

```bash
./gradlew :fermata:testMobileDebugUnitTest :fermata:testAutoDebugUnitTest :web:testMobileDebugUnitTest :tv:testMobileDebugUnitTest
./gradlew :fermata:lintMobileDebug :fermata:lintAutoDebug verifyWebOnlyProductionGraph
./gradlew :fermata:packageAutoDebugUniversalApk
python3 scripts/verify_security_apk.py /path/to/app.apk --aapt /path/to/aapt
```

Eski kurulumların güncellenmesi için paket kimliği ve imza korunur. Dağıtım varyantı dosya adında `debug` taşısa da APK `debuggable=false` olarak paketlenir. Yayın sertifikasının özel anahtarı depoya konmaz.

## Lisans ve kaynak

ABBAS, [Fermata Media Player](https://github.com/AndreyPavlenko/Fermata) ve FermataX kaynaklarından türetilmiş GPL-3.0 projesidir. Andrey Pavlenko ve diğer katkıcıların telif/lisans bildirimleri korunur. Kaldırılan özelliklerin kaynak geçmişi Git’te bulunabilir; bu onların dağıtılan APK’da bulunduğu anlamına gelmez. [Lisans](LICENSE).

## Release signing and update publishing

Release APKs must be signed with the approved ABBAS upload certificate. Configure signing locally with a root-level `keystore.properties` file (never commit it):

```properties
STORE_FILE=/absolute/path/to/abbas-upload.jks
STORE_PASSWORD=your-keystore-password
KEY_ALIAS=abbas-upload
KEY_PASSWORD=your-key-password
```

The same values can be supplied through the `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` environment variables. The legacy `FERMATAX_STORE_FILE`, `FERMATAX_STORE_PASSWORD`, `FERMATAX_KEY_ALIAS`, and `FERMATAX_KEY_PASSWORD` names remain accepted for existing build environments. Partial configuration is rejected. Release artifact tasks require an explicitly configured signing key; debug/test builds are not silently re-signed as release builds.

Create the upload key once and keep at least two encrypted offline backups. Example:

```bash
keytool -genkeypair -v -keystore abbas-upload.jks -alias abbas-upload -keyalg RSA -keysize 4096 -validity 10000
```

Record the certificate SHA-256 fingerprint and protect the keystore and passwords separately. Losing the key prevents in-place updates for installations signed with it. Never upload the keystore, passwords, or `keystore.properties` to GitHub.

For a release, build the intended Auto variant with the approved signing key, generate the update metadata with `scripts/create_update_manifest.py`, and upload both `ABBAS-universal.apk` and `update-auto-debug.json` as assets of the same published GitHub Release. The updater accepts only those declared Release assets and verifies package ID, version, file size, APK SHA-256, and signing-certificate SHA-256 before opening Android's package installer. Installation always requires the user's confirmation.

The app declares `REQUEST_INSTALL_PACKAGES` because its in-app updater hands a verified APK to Android's system package installer. Android may require the user to grant the app permission to request package installation; ABBAS does not install silently.
