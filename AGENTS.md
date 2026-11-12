# ABBAS çalışma kuralları
- Depo: github.com/darklonee38/abbas. Çalışma dalı: work/abbas-phase1-security-brand. main'e doğrudan yazma, force push yok, merge yok; iş bitince PR aç.
- Kapsam dışı: OBD/Toyota teşhis modülü; Shizuku/KingInstaller ile ilgili her şey (yalnızca dokümandan kaldırılacak).
- LICENSE, THIRD_PARTY_NOTICES.md ve dosyalardaki orijinal yazar atıfları (Andrey Pavlenko vb.) SİLİNMEZ. Fermata/FermataX iç paket adları değişmez.
- Çalıştırılmayan komut, test edilmeyen özellik, doğrulanmayan sonuç "yapıldı" diye raporlanmaz. Her iddiaya dosya yolu veya commit SHA eklenir.
- Her başlangıçta ÖNCE docs/PROGRESS.md okunur; biten iş tekrarlanmaz.
- Yalnızca gerçekten bloke olunca durulur ve tek net soru sorulur; rutin kararlar kendi verilir.
- Güvenlik ilkeleri: varsayılan HTTPS, HTTP yalnızca kaynak bazlı açık kullanıcı onayıyla; kimlik bilgisi (Xtream/Stalker) hiçbir log/hata/rapor yoluna açık yazılmaz; hareket hâlinde araç ekranında video/tarayıcı/yansıtma kapalı; güncelleme yalnızca darklonee38/abbas Releases'ten, paket kimliği + imza + SHA-256 + boyut doğrulamasıyla.
