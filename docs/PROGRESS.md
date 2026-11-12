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
| Release updater endpoint and APK/signature/hash verification | Devam | Removed repository/LFS fallback; updater accepts only GitHub Release assets. Installed-signature, APK hash/size and pre-install gate still pending |
| Release signing configuration and documentation | Devam | `keystore.properties` plus `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` supported; legacy FERMATAX env names remain as fallback; secrets files ignored. Keystore generation/backup and Release upload documentation still pending |
| Dead-code/dependency analysis and safe removals | Devam | Must verify APK contents and references before deletion |
| ABBAS branding and README cleanup | Devam | Not started |
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

## Latest CI diagnosis and fix

- Run `43170437488` failed in `:fermata:compileAutoDebugJavaWithJavac`: `GithubUpdater.java` referenced the removed `UpdateManifest.repositoryAssetUrl(JSONObject,String)` fallback.
- Removed that obsolete raw repository/LFS fallback call. Updater now requires the declared GitHub Release manifest asset, consistent with `UpdateManifest.assetUrl()`.
- Fix commit: `b3390df0d882c5c29fd5ca58f45525676ac546b7`.

## Next step

Inspect the new CI run for the fix commit. Continue package identity audit, signing docs, HTTP consent and secret-log audits; do not treat the updater as verified until the build and tests pass.
