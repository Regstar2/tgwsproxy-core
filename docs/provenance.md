# Provenance

Initial extraction source:

- repository: `Regstar2/tg-ws-proxy-android`;
- commit: `b2558f16f8a46aa3abec2660acd606bffdfac613`;
- path: `native/tgwsproxy/`;
- application version: `1.10.13`;
- extraction date: 2026-09-07.

Current synchronized runtime:

- repository: `Regstar2/tg-ws-proxy-android`;
- commit: `c5c4d03ce6f731642cfd5d66d0bdc56377606831`;
- path: `native/tgwsproxy/`;
- application version: `1.11.0`;
- synchronization date: 2026-09-20.

The reusable core mirrors the complete native runtime source and tests from the synchronized Android commit. The core-specific Android AAR wrapper, minSdk 21 build path, Kotlin API, and consumer rules remain maintained in this repository.

UI, Compose, Service, preferences and branding from the standalone Android application are intentionally excluded.
