# ABBAS — Phase 1 Progress

**Branch:** `work/abbas-phase1-security-brand`
**Base:** `main` (`e4eca9cc1483ca59ae5a56e1fd271b1a926bb229`)
**Last progress update:** 2026-11-12; current branch HEAD verified after CI diagnosis

## Task status

| Task | Status | Notes |
|---|---|---|
| HTTPS default for schemeless M3U URLs | Bitti | `TvPlaylistUrls.normalize()`; commit `9aeeba73a028a785131427567fca6f9b9256d165` |
| M3U URL normalization test | Bitti | commit `0dcf0008902c9a27e8ff12775b4baa4f795728c0` |
| HTTPS default for schemeless Stalker portals | Bitti | commit `615eedba279847e0a600e4de228429f901920f85` |
| Stalker HTTPS normalization tests | Bitti | commit `2496d9a5f7c6777655e3bdd29051c355adb325f2` |
| HTTP explicit-consent enforcement for M3U/Xtream/Stalker | Devam | HTTPS is default for schemeless/new sources; explicit HTTP and legacy HTTP still lack source-bound persisted consent. Required design: source-keyed grant for M3U URL, Xtream source ID and Stalker portal; credential-bearing Xtream/Stalker warning is elevated; legacy HTTP sources are listed once at first mobile startup and individually approved; no consent dialog on TV surfaces; unapproved HTTP and HTTPS-to-HTTP redirects fail closed. Pure policy primitives now exist for per-source hashed keys, warning levels, HTTP grant decisions and downgrade detection, with tests (`ed415e723e16`, `046ec175d1c0`). Source-keyed grant persistence and a one-time legacy-review marker now have a store abstraction with unit tests (`8267037dfc40`, `1b30db784f0b`). These are not yet wired to provider dialogs or transport enforcement; no end-to-end enforcement is claimed. |
| IPTV/Xtream/Stalker secret-safe logging, exceptions, player UI and report audit | Devam | Shared downloader URL masking/tests (`9e3fc9ba6060`, `d4520f511317`, `4e50a538bdc9`); Stalker/M3U partial redaction (`2c444b3dd3dc`, `30b45304b43b`). This turn: Stalker connection/timeout messages now pass portal identity through redaction (`958727542904`); Xtream no longer displays untrusted HTTP reason phrases (`34360a4d901f`, formatting follow-up `704065660729`); tests added for Stalker fallback/network errors (`d7bc081d84e8`) and Xtream HTTP reason leakage (`d0cf0be7ac75`). TV source UI error surfaces use `TvErrorMessages` (`9dc285345b1a`, `4d330fb4c1dc`, `49eae82cac38`, latest edit dialogs `fa16d67ad394`). ExoPlayer now wraps listener failures in a sanitized, cause-free `MediaEngineException` (`459a5e1b7b73`, `d2199c546b36`) with URL/credential tests (`fc71aacce2f9`). `DiagnosticSanitizer` masks raw MAC addresses and labeled serial/device IDs (`f2465d119c26`) with tests (`dba8523f2df1`); TV UI test updated (`d3096d995e7f`). Export validation now rejects raw URLs and labeled MAC/serial/device identifiers (`9c165dfd57bb`, tests `99754f9f94c4`). Full UI path coverage and all report payload permutations still need validation. |
| Package identity migration to `com.abbas.auto` | Devam | Base applicationId now `com.abbas`; Auto release resolves to `com.abbas.auto`, debug to `com.abbas.auto.debug`; updater manifest generator/tests aligned. Stale architecture assertion was fixed in `76a5f0869156`; full provider/authority/build verification still pending |
| Release updater endpoint and APK/signature/hash verification | Devam | GitHub Release-only manifest/APK assets; APK package/version/size/SHA-256 and signer-certificate SHA-256 are checked before Android installer. CI verification pending |
| Release signing configuration and documentation | Bitti | `keystore.properties` and `STORE_*` env supported; partial config rejected; private key files ignored; README documents key generation, offline backup, Release assets, and `REQUEST_INSTALL_PACKAGES` rationale. Commits: `b7cef6658628`, `7a7abfe08539`, `0dfd81aee9d1` |
| Dead-code/dependency analysis and safe removals | Devam | Must verify APK contents and references before deletion |
| README cleanup: obsolete installer guidance | Bitti | README removed obsolete KingInstaller/Shizuku installation guidance and Dem TV alias direction cleanup. Commit: `f0bd2a1a5c7b` |
| ABBAS package identity migration | Devam | `com.abbas` / `com.abbas.auto` / `com.abbas.auto.debug` configured; provider/authority and all-resource verification still pending. Commits: `cd266710fe5f`, `2bc226517cfa`, `9ebbd9d2e96d`, `340ecfabee36`, `4ae6b889c198` |
| ABBAS branding and resource cleanup | Devam | README identity and obsolete installer guidance are updated; full strings/resources/TTS/diagnostics scan still pending |
| CI, APK inspection, residual scan, PR creation | Devam | Run `43171296149` built the Auto debug universal APK successfully but failed in `verify_security_apk.py` because the audit incorrectly classified the required voice-recognition `RECORD_AUDIO` permission as forbidden. Fixed the audit to allow this documented, runtime-requested permission while retaining the other sensitive-permission denylist; fresh CI required. APK inspection/tests/lint remain pending |

## Guardrails

- Work only on this feature branch; never commit to `main`.
- No force push, no history rewriting, and do not merge the PR.
- Do not modify OBD/Toyota diagnostic code or Shizuku/KingInstaller functionality.
- Preserve LICENSE, THIRD_PARTY_NOTICES.md, and original author attributions.
- Do not claim tests, CI, APK inspection, or device verification unless actually completed.

## Completed in latest work

- Added preference-store-backed HTTP grants keyed by hashed source identity and a separate one-time legacy review marker; tests verify grant isolation, revocation and marker persistence. Commits: `8267037dfc40`, `1b30db784f0b`. This storage layer is groundwork; no UI/transport gate yet.\n
- Added source-scoped HTTP consent policy primitives: per-source hashed identity keys (M3U URL vs Xtream source ID vs Stalker portal ID), stronger warning classification for credential-bearing providers, HTTP grant decision and downgrade policy. Unit tests added. Commits: `ed415e723e16`, `046ec175d1c0`. These are groundwork only, not an end-to-end consent gate.

- Routed remaining Xtream/Stalker source edit error dialogs through `TvErrorMessages` rather than displaying raw localized exception text. Commit: `fa16d67ad394`.

- ExoPlayer errors are converted to cause-free, sanitized UI exceptions; tests cover URL credentials and safe fallback messages. Commits: `459a5e1b7b73`, `d2199c546b36`, `fc71aacce2f9`.
- Diagnostic sanitizer now masks MAC addresses and labeled serial/device identifiers; exported report validation rejects these identifiers and raw URLs, with tests. Commits: `f2465d119c26`, `dba8523f2df1`, `d3096d995e7f`, `9c165dfd57bb`, `99754f9f94c4`.

- Xtream and Stalker redirect handlers now disable automatic redirect following and block HTTPS-to-HTTP downgrade. Xtream also rejects cross-origin redirects to avoid forwarding credential-bearing API URLs; focused downgrade policy tests added. Commits: `15d6d2b70852`, `c96cfba00d18`, `6f0ae351dca8`, `8e555749bf1c`.

- Added `TvErrorMessages` and routed TV source add/edit/check/reload error dialogs through `DiagnosticSanitizer`; added tests for Xtream URL credentials, Stalker MAC/serial and stream tokens. Commits: `9dc285345b1a`, `4d330fb4c1dc`, `49eae82cac38`.
- Added Turkish CI prerelease notes after the release job exposed the missing workflow input. Commit: `c6c9436805d7`.

- Redacted full user-source URLs (including userinfo, path and query) from shared downloader start/success/failure logs and explicit HTTP status errors; added unit tests covering embedded credentials/tokens and non-HTTP URL input. Commits: `9e3fc9ba60602c613c41c906e10b8372cb989bb3`, `d4520f5113172b8d1981e1a99e961cfb96015274`, `4e50a538bdc9d32315192b3fb16ab6ace207295`.
- Fixed the APK permission audit false positive: `RECORD_AUDIO` is required for the existing user-triggered voice-recognition feature and is checked at runtime by `VoiceInteractionCoordinator`; the verifier now reports it separately as an explicitly approved permission. Commit: `9eeb198d4c010f15db90a1ab4522c11f5de283a2`.

- Updater metadata no longer supports the raw.githubusercontent.com / Git LFS fallback. Release APK must be a declared GitHub Release asset; non-release distribution channels are rejected.
- Default application ID changed from `me.app.fermataX` to `com.abbas`. The Auto release variant therefore resolves to `com.abbas.auto`; the Auto debug test/build variant resolves to `com.abbas.auto.debug`.
- Updater manifest generator and package identity tests aligned with `com.abbas.auto.debug`.

## Latest CI diagnosis and fixes

- Run `43177187948` on `c23e20d950ec`: universal APK build, APK verification and artifact upload succeeded. Unit tests failed in `TvErrorMessagesTest.uiMessageDoesNotExposeStalkerIdentityOrToken` because generic sanitization did not mask bare MAC addresses. Added MAC and labeled serial/device-ID masking, updated tests and all remaining Xtream/Stalker edit dialogs (`f2465d119c26`, `dba8523f2df1`, `d3096d995e7f`, `fa16d67ad394`). Later CI attempts were cancelled by subsequent pushes before tests completed; latest run `43178047719` is validating the consent policy primitives. Lint/prerelease have not been revalidated after these changes.
- Run `43175102940` on `0b23c5759b05`: universal Auto debug APK build, APK verification and artifact upload succeeded. Unit tests failed: `ArchitectureBoundaryTest.runtimeIdentityRemainsExplicitAndStable` still asserted the old `me.app.fermataX` application ID after migration. Updated the assertion to `com.abbas`; fix commit `76a5f0869156`. Lint and prerelease publication were skipped. A new CI run is required.


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

Run CI after the architecture assertion fix; inspect APK only after the complete workflow succeeds. Continue source-bound HTTP consent enforcement and end-to-end redaction across ExoPlayer errors, UI, exceptions and exported reports. Continue implementing HTTP consent with all-source and all-network-path enforcement; finish M3U/Xtream/Stalker URL redaction and diagnostics. Complete package/provider and branding scans. Dead-code removal remains gated on APK inspection.
