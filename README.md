# TgWsProxy Core

[![CI](https://github.com/Regstar2/tgwsproxy-core/actions/workflows/ci.yml/badge.svg)](https://github.com/Regstar2/tgwsproxy-core/actions/workflows/ci.yml)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84)

Переиспользуемая Android-библиотека с native TgWsProxy runtime. Core отделяет локальный MTProto frontend и WebSocket/Cloudflare transport от UI и lifecycle конкретного приложения.

[Быстрый старт](#быстрый-старт) · [Документация](#документация) · [Обратная связь](../../issues)

## О проекте

Исходный runtime извлекается из `Regstar2/tg-ws-proxy-android` commit `b2558f16f8a46aa3abec2660acd606bffdfac613`. Исходный репозиторий при extraction не изменяется.

## Статус проекта

Стадия: **Prototype**.

Цель текущей ветки — получить независимый Android Library/AAR с API `start / stop / status`, ARM64 native runtime и GitHub-hosted CI.

## Возможности

- локальный MTProto frontend;
- WebSocket/Cloudflare route runtime;
- минимальный Kotlin API;
- сборка `libtgwsproxy.so` из включённого Go source;
- без Compose, Activity, Service и Telegram classes.

## Быстрый старт

Требуются JDK 17, Go 1.25, Android SDK/NDK и Gradle 8.2.1.

```powershell
gradle :core:assembleDebug
```

Результат: `core/build/outputs/aar/core-debug.aar`.

## Требования

- Android minSdk 26;
- compileSdk 35;
- ABI: `arm64-v8a`;
- Go 1.25;
- AGP 8.2.2;
- Kotlin 1.9.22;
- JNA 5.14.0.

## Использование

```kotlin
val result = TgWsProxyCore.start(
    TgWsProxyConfig(
        host = "127.0.0.1",
        port = 1443,
        secret = "00112233445566778899aabbccddeeff",
    )
)

if (result.success) {
    val status = TgWsProxyCore.status()
}
```

## API

- `TgWsProxyCore.start(config)`;
- `TgWsProxyCore.stop()`;
- `TgWsProxyCore.status()`;
- `TgWsProxyCore.transportStatus()`.

## Архитектура

Подробнее: [docs/architecture.md](docs/architecture.md).

## Сборка

```powershell
./scripts/build-native-android.ps1
gradle :core:assembleDebug
```

## Тестирование

```powershell
./scripts/ci.ps1
```

CI выполняется на GitHub-hosted `ubuntu-latest`.

## Документация

- [Архитектура](docs/architecture.md)
- [Происхождение](docs/provenance.md)

## Ограничения

- только `arm64-v8a`;
- minSdk 26;
- JNA остаётся отдельной Gradle dependency;
- Maven/GitHub Packages publication пока нет;
- `tg-ws-proxy-android` пока продолжает использовать собственную копию runtime.

## Лицензия

Проект распространяется под GNU GPL v3. Third-party лицензии и provenance сохранены в [NOTICE.md](NOTICE.md) и `third_party/`.
