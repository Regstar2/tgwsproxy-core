# Provenance

Initial extraction source:

- repository: `Regstar2/tg-ws-proxy-android`;
- commit: `b2558f16f8a46aa3abec2660acd606bffdfac613`;
- path: `native/tgwsproxy/`;
- application version: `1.10.13`;
- extraction date: 2026-09-07.

Current synchronized runtime:

- repository: `Regstar2/tg-ws-proxy-android`;
- commit: `baf61c29e345a604d3915dbcec516cca5bb9a2fc`;
- path: `native/tgwsproxy/`;
- application version: `1.11.6`;
- synchronization date: 2026-10-31.

The reusable core synchronizes native routing, CF pool, Worker transport, MTProto lifecycle fixes and regression tests from the Android commit, preserving core-specific explicit route order, AWG route policy and standalone Consumer WARP exports. The core-specific Android AAR wrapper, minSdk 21 build path, Kotlin API, and consumer rules remain maintained in this repository.

UI, Compose, Service, preferences and branding from the standalone Android application are intentionally excluded.

Targeted CF Proxy TLS SNI / WebSocket Host backport (2026-11-16):

- source: `Regstar2/tg-ws-proxy-android` commit `96b4c8a28c9be277cba2bafec118b47c570e9e25` (issue #156);
- changed only the CF Proxy native TLS server-name handling for MTProto and SOCKS5, plus native regression coverage;
- TLS SNI: base zone domain; HTTP Host: original `kwsN.<domain>`; WS path: `/apiws`;
- Direct WS / Worker routes retain their existing hostname behavior;
- full synchronization of all later Android runtime changes is not implied by this backport.
