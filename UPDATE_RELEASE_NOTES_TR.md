# ABBAS — otomatik test ön sürümü

Bu paket, ABBAS Android Auto uygulamasının feature branch doğrulama yapısı için üretilmiştir.

## Bu sürümde

- Uygulama kimliği ABBAS paket adlarına taşınmıştır.
- Güncelleme denetimi GitHub Release varlıklarıyla sınırlandırılmıştır.
- İndirilen APK için paket kimliği, sürüm, dosya boyutu, SHA-256 ve imza sertifikası doğrulamaları uygulanır.
- Paylaşılan HTTP indiricisinde kullanıcı kaynak URL'leri log ve hata metinlerinde maskelenir.
- IPTV kaynakları için HTTPS varsayılanları ve güvenlik doğrulamaları geliştirilmiştir.

## Güvenlik ve doğrulama notları

Bu bir test ön sürümüdür. HTTP kaynakları için kaynak bazlı açık kullanıcı onayı ve tüm oynatıcı/UI/tanı hata yollarında uçtan uca hassas veri maskelemesi henüz tamamlanmamıştır. Üretim sürümü olarak değerlendirmeyin.

Bu paket yalnızca test ve doğrulama amacıyla yayımlanır.