# Architecture

```text
Android host
    -> TgWsProxyCore Kotlin API
    -> JNA bridge
    -> libtgwsproxy.so
    -> Go TgWsProxy runtime
```

Core intentionally owns no UI, Activity, Service, Telegram classes or application preferences.

The public boundary is limited to start, stop and status operations. The host owns lifecycle policy.
