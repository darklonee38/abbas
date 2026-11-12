# ABBAS — Phase 1 Progress

**Branch:** `work/abbas-phase1-security-brand`
**Base:** `main` (`e4eca9cc1483ca59ae5a56e1fd271b1a926bb229`)
**Last progress update:** `952b47ac110bcc73feabe47217442edad914d795`

## Task status

| Task | Status | Notes |
|---|---|---|
| HTTPS default for schemeless M3U URLs | Bitti | `TvPlaylistUrls.normalize()`; commit `9aeeba73a028a785131427567fca6f9b9256d165` |
| M3U URL normalization test | Bitti | commit `0dcf0008902c9a27e8ff12775b4baa4f795728c0` |
| HTTPS default for schemeless Stalker portals | Bitti | commit `615eedba279847e0a600e4de228429f901920f85` |
| Stalker HTTPS normalization tests | Bitti | commit `2496d9a5f7c6777655e3bdd29051c355adb325f2` |
| HTTP explicit-consent enforcement for M3U/Xtream/Stalker | Devam | Must cover source creation, persisted sources, and all connection entry points |
| IPTV/Xtream/Stalker secret-safe logging audit and tests | Devam | Existing redact helpers found; all call sites still require audit |
| Package identity migration to `com.abbas.auto` | Devam | Base applicationId now `com.abbas`; Auto release resolves to `com.abbas.auto`, debug to `com.abbas.auto.debug`; updater manifest generator/tests aligned. Full provider/authority/build verification still pending |
| Release updater endpoint and APK/signature/hash verification | Devam | GitHub Release-only manifest/APK assets; APK package/version/size/SHA-256 and signer-certificate SHA-256 are checked before Android installer. CI verification pending |
| Release signing configuration and documentation | Bitti | `keystore.properties` and `STORE_*` env supported; partial config rejected; private key files ignored; README documents key generation, offline backup, Release assets, and `REQUEST_INSTALL_PACKAGES` rationale |
| Dead-code/dependency analysis and safe removals | Devam | Must verify APK contents and references before deletion |
| ABBAS branding and README cleanup | Devam | README identifies ABBAS and removes KingInstaller/Shizuku and Dem TV directions; full resource/string/TTS scan still pending |
| CI, APK inspection, residual scan, PR creation | Devam | Run `43170437488` failed at Java compilation because `GithubUpdater` still called removed `repositoryAssetUrl`; fixed in `b3390df`. New CI run must complete before APK inspection |

## Guardrails

- Work only on this feature branch; never commit to `main`.
- No force push, no history rewriting, and do not merge the PR.
- Do not modify OBD/Toyota diagnostic code or Shizuku/KingInstaller functionality.
- Preserve LICENSE, THIRD_PARTY_NOTICES.md, and original author attributions.
- Do not claim tests, CI, APK inspection, or device verification unless actually completed.

## Completed in latest work

- Updater metadata no longer supports the raw.githubusercontent.com / Git LFS fallback. Release APK must be a declared GitHub Release asset; non-release distribution channels are rejected.
- Default application ID changed from `me.app.fermataX` to `com.abbas`. The Auto release variant therefore resolves to `com.abbas.auto`; the Auto debug test/build variant resolves to `com.abbas.auto.debug`.
- Updater manifest generator and package identity tests aligned with `com.abbas.auto.debug`.

## Latest CI diagnosis and fixes

- Run `43170437488` failed in `:fermata:compileAutoDebugJavaWithJavac`: `GithubUpdater.java` referenced the removed `UpdateManifest.repositoryAssetUrl(JSONObject,String)` fallback.
- Removed the obsolete repository/LFS fallback call; updater now requires the declared GitHub Release manifest asset. Fix commit: `b3390df0d882c5c29fd5ca58f45525676ac546b7`.
- Updated `UpdateActivity` to compare SHA-256 digests of the installed and downloaded APK signing certificates (single-signer only), in addition to package/version and APK size/content SHA-256. Commit: `b8aa8fa4db6c2f4c5a3019ba343ba3dcb47c4795`.
- Added release-key generation/backup, environment configuration, Release asset publishing, and `REQUEST_INSTALL_PACKAGES` documentation to README. Commit: `0dfd81aee9d141bd8e555b635487bdc606a31de6`.
- The failed run's exact compiler issue is fixed in source, but CI for the latest commit is still pending/in progress; no build/test success is claimed.

## Next step

Inspect CI for the latest SHA. Continue package identity and branding audits, then implement the HTTP-consent and secret-log policies with complete connection-path coverage. Dead-code removal remains gated on APK inspection.
