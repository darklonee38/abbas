# ABBAS — Phase 1 Progress

**Branch:** `work/abbas-phase1-security-brand`
**Base:** `main` (`e4eca9cc1483ca59ae5a56e1fd271b1a926bb229`)
**Last progress update:** `29f2401e3ec35622acd2c66a5a7ef79dadedf565`

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
| Release signing configuration and documentation | Devam | Not started |
| Dead-code/dependency analysis and safe removals | Devam | Must verify APK contents and references before deletion |
| ABBAS branding and README cleanup | Devam | Not started |
| CI, APK inspection, residual scan, PR creation | Devam | No current CI/APK analysis/PR |

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

## Next step

Complete package identity audit (provider authorities, manifest placeholders, update checks and all hard-coded IDs), then implement updater APK SHA-256/size and installed signing-certificate verification before installation. Continue HTTP consent architecture after these deterministic package/updater slices.
