# ABBAS — Phase 1 Progress

**Branch:** `work/abbas-phase1-security-brand`
**Base:** `main` (`e4eca9cc1483ca59ae5a56e1fd271b1a926bb229`)
**Last progress update:** pending this commit (previous reference `952b47ac...` is not an ancestor of current branch HEAD)

## Task status

| Task | Status | Notes |
|---|---|---|
| HTTPS default for schemeless M3U URLs | Bitti | `TvPlaylistUrls.normalize()`; commit `9aeeba73a028a785131427567fca6f9b9256d165` |
| M3U URL normalization test | Bitti | commit `0dcf0008902c9a27e8ff12775b4baa4f795728c0` |
| HTTPS default for schemeless Stalker portals | Bitti | commit `615eedba279847e0a600e4de228429f901920f85` |
| Stalker HTTPS normalization tests | Bitti | commit `2496d9a5f7c6777655e3bdd29051c355adb325f2` |
| HTTP explicit-consent enforcement for M3U/Xtream/Stalker | Devam | HTTPS is now the default for new schemeless Xtream sources (scheme preference default changed to index 1, test added); explicit HTTP remains selectable and legacy HTTP sources still need a consent gate across all network entry points |
| IPTV/Xtream/Stalker secret-safe logging audit and tests | Devam | Added URL redaction in the shared `HttpFileDownloader` logs and safe URL formatting for its explicit HTTP errors; added unit tests for credentials/path/query and non-HTTP URLs. Commits `9e3fc9b`, `d4520f5`, `4e50a53`. Other clients, response diagnostics and exception payloads still need audit |
| Package identity migration to `com.abbas.auto` | Devam | Base applicationId now `com.abbas`; Auto release resolves to `com.abbas.auto`, debug to `com.abbas.auto.debug`; updater manifest generator/tests aligned. Full provider/authority/build verification still pending |
| Release updater endpoint and APK/signature/hash verification | Devam | GitHub Release-only manifest/APK assets; APK package/version/size/SHA-256 and signer-certificate SHA-256 are checked before Android installer. CI verification pending |
| Release signing configuration and documentation | Bitti | `keystore.properties` and `STORE_*` env supported; partial config rejected; private key files ignored; README documents key generation, offline backup, Release assets, and `REQUEST_INSTALL_PACKAGES` rationale |
| Dead-code/dependency analysis and safe removals | Devam | Must verify APK contents and references before deletion |
| ABBAS branding and README cleanup | Devam | README identifies ABBAS and removes KingInstaller/Shizuku and Dem TV directions; full resource/string/TTS scan still pending |
| CI, APK inspection, residual scan, PR creation | Devam | Run `43171296149` built the Auto debug universal APK successfully but failed in `verify_security_apk.py` because the audit incorrectly classified the required voice-recognition `RECORD_AUDIO` permission as forbidden. Fixed the audit to allow this documented, runtime-requested permission while retaining the other sensitive-permission denylist; fresh CI required. APK inspection/tests/lint remain pending |

## Guardrails

- Work only on this feature branch; never commit to `main`.
- No force push, no history rewriting, and do not merge the PR.
- Do not modify OBD/Toyota diagnostic code or Shizuku/KingInstaller functionality.
- Preserve LICENSE, THIRD_PARTY_NOTICES.md, and original author attributions.
- Do not claim tests, CI, APK inspection, or device verification unless actually completed.

## Completed in latest work

- Redacted full user-source URLs (including userinfo, path and query) from shared downloader start/success/failure logs and explicit HTTP status errors; added unit tests covering embedded credentials/tokens and non-HTTP URL input. Commits: `9e3fc9ba60602c613c41c906e10b8372cb989bb3`, `d4520f5113172b8d1981e1a99e961cfb96015274`, `4e50a538bdc9d32315192b3fb16ab6ace207295`.
- Fixed the APK permission audit false positive: `RECORD_AUDIO` is required for the existing user-triggered voice-recognition feature and is checked at runtime by `VoiceInteractionCoordinator`; the verifier now reports it separately as an explicitly approved permission. Commit: `9eeb198d4c010f15db90a1ab4522c11f5de283a2`.

- Updater metadata no longer supports the raw.githubusercontent.com / Git LFS fallback. Release APK must be a declared GitHub Release asset; non-release distribution channels are rejected.
- Default application ID changed from `me.app.fermataX` to `com.abbas`. The Auto release variant therefore resolves to `com.abbas.auto`; the Auto debug test/build variant resolves to `com.abbas.auto.debug`.
- Updater manifest generator and package identity tests aligned with `com.abbas.auto.debug`.

## Latest CI diagnosis and fixes

- Run `43171296149` on `5c345ab16560f4a0e79cf5a2377063a5485ee927`: universal APK build succeeded; APK verification failed at the `RECORD_AUDIO` denylist assertion. Unit tests, lint, and release publication were skipped. The verifier fix is committed as `9eeb198d4c010f15db90a1ab4522c11f5de283a2`; rerun required.

- Run `43170437488` failed in `:fermata:compileAutoDebugJavaWithJavac`: `GithubUpdater.java` referenced the removed `UpdateManifest.repositoryAssetUrl(JSONObject,String)` fallback.
- Removed the obsolete repository/LFS fallback call; updater now requires the declared GitHub Release manifest asset. Fix commit: `b3390df0d882c5c29fd5ca58f45525676ac546b7`.
- Updated `UpdateActivity` to compare SHA-256 digests of the installed and downloaded APK signing certificates (single-signer only), in addition to package/version and APK size/content SHA-256. Commit: `b8aa8fa4db6c2f4c5a3019ba343ba3dcb47c4795`.
- Added release-key generation/backup, environment configuration, Release asset publishing, and `REQUEST_INSTALL_PACKAGES` documentation to README. Commit: `0dfd81aee9d141bd8e555b635487bdc606a31de6`.
- New Xtream sources now default to HTTPS when the host has no scheme; invalid scheme indices also fall back to HTTPS. Added a unit test. Commits: `b130c9f917540bcd1cc3fe49f45228e0255752ab`, `ab8e538b31a58edad14ee27c167149825f011474`.
- M3U source `toString()` no longer prints the raw playlist URL, which can carry credentials in its path or query. Commit: `30b45304b43be7498750ac5762170e5333a309c6`.
- Stalker connection error messages now redact the portal value before display. Commit: `2c444b3dd3dcebe4bfff3a11826fa6cfc9755e89`.
- The failed run's exact compiler issue is fixed in source, but CI for the latest commit is still pending/in progress; no build/test success is claimed.

## Next step

Run CI on the latest source/test commits; inspect APK only after the complete workflow succeeds. Continue HTTP consent enforcement and remaining redaction audit. Continue implementing HTTP consent with all-source and all-network-path enforcement; finish M3U/Xtream/Stalker URL redaction and diagnostics. Complete package/provider and branding scans. Dead-code removal remains gated on APK inspection.
