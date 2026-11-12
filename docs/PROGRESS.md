# ABBAS — İlerleme Durumu

**Branch:** `work/abbas-phase1-security-brand`
**Base:** `main` (`e4eca9cc1483ca59ae5a56e1fd271b1a926bb229`)
**Son güncelleme:** 2026-11-12
**Bu oturumun başlangıç HEAD'i:** `4e6f062b9775e13949bbae91f0ce751e856b31f4`

## Görev tablosu

| Görev | Durum (Bitti/Devam/Bekliyor/Bloke) | Commit SHA | Not |
|---|---|---|---|
| Sürüş güvenliği politikası (DrivingRestrictionPolicy) | Bekliyor | doğrulanmadı | Recursive branch ağacında `DrivingRestrictionPolicy` veya eşdeğer adlı bir uygulama bulunamadı. Video/tarayıcı/yansıtma kısıtlamalarının uygulama ve test kapsamı ayrıca araştırılmalı. |
| HTTP açık rıza (liste/Xtream/Stalker, kaynak bazlı) | Devam | `ed415e723e16`, `046ec175d1c0`, `8267037dfc40`, `1b30db784f0b` | Kaynak tipine göre hash anahtarı, uyarı seviyesi, grant/revoke ve legacy-review store altyapısı mevcut. Provider öncesi UI onayı, ilk açılışta eski HTTP kaynaklarının tek tek incelenmesi ve üç sağlayıcıdaki tüm bağlantı yollarına zorunlu gate henüz bağlanmadı. `TvHttpConsentPolicy.java`, `TvHttpConsentStore.java`, `TvRootItem.java`, `TvFragment.java`. |
| HTTPS→HTTP yönlendirme engeli | Devam | `15d6d2b70852`, `c96cfba00d18`, `6f0ae351dca8`, `8e555749bf1c` | Xtream ve Stalker istemcileri HTTPS downgrade yönlendirmelerini reddediyor; Xtream ayrıca cross-origin yönlendirmeyi engelliyor. M3U/ortak downloader ve kaynak bazlı izinle tutarlı uçtan uca davranış doğrulanmadı. İlgili dosyalar: `XtreamHttpClient.java`, `StalkerHttpClient.java`. |
| Kimlik bilgisi maskeleme (log, hata, rapor, UI) | Devam | `9e3fc9ba6060`, `958727542904`, `fa16d67ad394`, `459a5e1b7b73`, `f2465d119c26`, `9c165dfd57bb` | Downloader URL redaksiyonu, Xtream/Stalker hata yüzeyleri, ExoPlayer cause-free hata, MAC/seri maskeleme ve rapor doğrulaması için değişiklikler/testler var. Tüm UI/log/exception/export yollarının kapsamlı denetimi tamamlanmadı. |
| Paket kimliği değişikliği | Devam | `cd266710fe5f`, `2bc226517cfa`, `9ebbd9d2e96d`, `340ecfabee36`, `4ae6b889c198`, `76a5f0869156` | `com.abbas`, `com.abbas.auto`, `com.abbas.auto.debug` yapılandırıldı; eski applicationId mimari testi düzeltildi. Provider/authority ve tüm paket referanslarının build/APK üzerinden doğrulanması bekliyor. Fermata/FermataX iç paket adları korunacak. |
| Updater doğrulaması (paket, imza, SHA-256, boyut) | Devam | `b3390df0d882`, `b8aa8fa4db6c` | Yalnızca GitHub Releases varlıkları kabul ediliyor; paket/sürüm, boyut, APK SHA-256 ve tek imzacı sertifikasının SHA-256 kontrolü kodlandı. Güncel CI ve gerçek APK üzerinde doğrulama bekliyor. |
| Release imzalama altyapısı | Bitti | `b7cef6658628`, `7a7abfe08539`, `0dfd81aee9d1` | `keystore.properties` ve `STORE_*` ortam değişkenleri, eksik/yarım yapılandırmayı reddetme, özel anahtar dosyalarını ignore etme ve README talimatları mevcut. İmzalı gerçek release APK üretimi ayrıca CI/release aşamasında doğrulanmalı. |
| Ölü kod/kalıntı analizi ve temizliği | Devam | doğrulanmadı | APK içeriği, kaynak referansları ve bağımlılık kullanımı doğrulanmadan silme yapılmayacak. Güvenli kaldırma tamamlanmadı. |
| README ve marka temizliği | Devam | `f0bd2a1a5c7b` | Eski KingInstaller/Shizuku kurulum yönergeleri README'den kaldırıldı. Tam marka, strings/resources, TTS, diagnostics ve eski paket referansı taraması bitmedi. LICENSE, THIRD_PARTY_NOTICES.md ve orijinal yazar atıfları korunacak. |
| CI doğrulaması ve PR | Devam | `9eeb198d4c01`, `76a5f0869156`, `6ec46c223f01`, `1fe036aab91b`, `1befd017cc60`, `053062af3f16` | Run #62 (`43179903924`, SHA `4e6f062b9775e13949bbae91f0ce751e856b31f4`): universal APK build, APK içerik/güvenlik kontrolü ve artifact yükleme başarılı; unit testlerde `DiagnosticReportExporterTest.exportValidationRejectsStalkerDeviceIdentifiers` başarısız (1079 testte 1 hata). JSON alan adlarının kapanış tırnağı regex tarafından tanınmıyordu; düzeltme `1befd017cc60` ile yapıldı, henüz CI'da doğrulanmadı. Actions tasarrufu için workflow push tetikleyicisi kaldırıldı; yalnızca `main` hedefli PR ve manuel `workflow_dispatch` çalıştırır. Test/lint APK build'den önce çalışır; prerelease yalnızca manuel çalıştırmada yayımlanır (`053062af3f16`). APK doğrulaması ve PR bekliyor. |

## Son durum

- Depo kökündeki `AGENTS.md` bu oturumda eklendi ve branch'e commit edildi: `4455aa6933abf148ff3ca7e82cd8b7d006f47508`.
- `AGENTS.md` ve bu dosya aynı branch üzerinde tutuluyor; `main` değişmedi. Branch, kontrol sırasında `main` tabanından 74 commit ileride ve geride commit içermiyordu.
- Önceki işlerde tamamlanmış HTTPS varsayılanı, politika/store temelleri, bazı redirect/redaction ve release signing değişiklikleri tekrar yapılmadı.
- HTTP açık rıza şu anda yalnızca politika ve kalıcı izin altyapısı seviyesinde. Kaynak ekleme/düzenleme akışlarında onay penceresi ve bağlantı öncesi zorunlu enforcement yok; bu nedenle güvenlik özelliği tamamlanmış sayılamaz.
- CI Run #62'de APK üretimi ve APK güvenlik doğrulaması geçti; test aşamasında 1079 testten 1'i başarısız oldu. Hata, JSON içindeki `"mac"` alanının regex tarafından eşleşmemesiydi. Üretim regex'i kapanış tırnağını da kabul edecek şekilde düzeltildi (`1befd017cc60`); düzeltme testi henüz çalıştırılmadı.
- Actions tüketimini azaltmak için `build-test-apk.yml` push tetikleyicisi kaldırıldı; workflow artık `main` hedefli PR veya manuel `workflow_dispatch` ile çalışır. Test ve lint APK build'den önce çalışır; test prerelease yalnızca manuel çalıştırmada yayımlanır (`053062af3f16`).

## Sonraki adım

Önce GitHub Actions > Build test APK > Run workflow yoluyla `work/abbas-phase1-security-brand` dalında manuel CI çalıştır. `DiagnosticReportExporterTest.exportValidationRejectsStalkerDeviceIdentifiers` regresyonunun geçtiğini, tüm unit testleri ve lint'i doğrula; testler geçerse universal APK'nin paket kimliği, imzası, SHA-256 ve boyutunu incele. Ardından `main` hedefli PR aç; merge yapma. CI/APK/PR tamamlandıktan sonra HTTP açık rıza entegrasyonuna dön. Sürüş güvenliği politikası en son yapılacak. OBD/Toyota teşhis kapsamına girme.
