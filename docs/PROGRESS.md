# ABBAS — Phase 1 Progress

**Branch:** `work/abbas-phase1-security-brand`
**Base:** `main` (`e4eca9cc1483ca59ae5a56e1fd271b1a926bb229`)
**Last progress update:** `7736f6f54f0bc6fd0155bbd48fccf3b0bdbc19b8`

## Task status

| Task | Status | Notes |
|---|---|---|
| HTTPS default for schemeless M3U URLs | Bitti | `TvPlaylistUrls.normalize()`; commit `9aeeba73a028a785131427567fca6f9b9256d165` |
| M3U URL normalization test | Bitti | commit `0dcf0008902c9a27e8ff12775b4baa4f795728c0` |
| HTTPS default for schemeless Stalker portals | Bitti | commit `615eedba279847e0a600e4de228429f901920f85` |
| Stalker HTTPS normalization tests | Bitti | commit `2496d9a5f7c6777655e3bdd29051c355adb325f2` |
| HTTP explicit-consent enforcement for M3U/Xtream/Stalker | Devam | Must cover source creation, persisted sources, and all connection entry points |
| IPTV/Xtream/Stalker secret-safe logging audit and tests | Devam | Existing redact helpers found; all call sites still require audit |
| Package identity migration to `com.abbas.auto` | Devam | Not started |
| Release updater endpoint and APK/signature/hash verification | Devam | Not started |
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

## Next step

Inspect the existing IPTV source lifecycle and preference model to design a single per-source HTTP consent mechanism that is enforced before every network connection. Add focused unit tests, then update this file and commit the completed slice.
