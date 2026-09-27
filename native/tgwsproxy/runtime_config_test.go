package main

import (
	"testing"

	"tg-ws-proxy/tgwsroute"
)

func TestParseRuntimeConfigRejectsShortCachedCFDomainList(t *testing.T) {
	_, settings, err := parseRuntimeConfig(
		"@cf_cached_domains=one.example|two.example,@cf_manual_domains=manual.example",
	)
	if err != nil {
		t.Fatalf("parseRuntimeConfig: %v", err)
	}
	if len(settings.CFCachedUpstream) != 0 {
		t.Fatalf("cached upstream=%v, want rejected", settings.CFCachedUpstream)
	}
	if len(settings.CFManualDomains) != 1 || settings.CFManualDomains[0] != "manual.example" {
		t.Fatalf("manual domains=%v, want manual domain preserved", settings.CFManualDomains)
	}
}

func TestParseRuntimeConfigAcceptsCachedCFDomainQualityGate(t *testing.T) {
	_, settings, err := parseRuntimeConfig(
		"@cf_cached_domains=one.example|two.example|three.example",
	)
	if err != nil {
		t.Fatalf("parseRuntimeConfig: %v", err)
	}
	if len(settings.CFCachedUpstream) != 3 {
		t.Fatalf("cached upstream=%v, want 3 domains", settings.CFCachedUpstream)
	}
}

func TestParseRuntimeConfigDecodesFlowsealCachedCFDomains(t *testing.T) {
	_, settings, err := parseRuntimeConfig(
		"@cf_cached_domains=virkgj.com|vmmzovy.com|mkuosckvso.com",
	)
	if err != nil {
		t.Fatalf("parseRuntimeConfig: %v", err)
	}
	wantFirst := tgwsroute.DecodeFlowsealCFDomain("virkgj.com")
	if len(settings.CFCachedUpstream) == 0 || settings.CFCachedUpstream[0] != wantFirst {
		t.Fatalf("cached upstream=%v, want first %s", settings.CFCachedUpstream, wantFirst)
	}
}


func TestParseRuntimeConfigPreservesExplicitRouteOrderIncludingAWG(t *testing.T) {
	_, settings, err := parseRuntimeConfig(
		"@connection_mode=auto,@route_direct_ws=1,@route_cf_proxy_ws=1,@route_awg_warp=1,@route_worker_ws=1,@route_tcp_fallback=0,@route_order=direct_ws|cf_proxy_ws|awg_warp|cf_worker_ws",
	)
	if err != nil {
		t.Fatalf("parseRuntimeConfig: %v", err)
	}

	want := []routeKind{routeDirectWS, routeCFProxyWS, routeAWGWarp, routeCFWorkerWS}
	if len(settings.ExplicitRouteOrder) != len(want) {
		t.Fatalf("route order=%v, want=%v", settings.ExplicitRouteOrder, want)
	}
	for i := range want {
		if settings.ExplicitRouteOrder[i] != want[i] {
			t.Fatalf("route order=%v, want=%v", settings.ExplicitRouteOrder, want)
		}
	}
	if !settings.AllowAWG {
		t.Fatal("route_awg_warp was not enabled")
	}
	if settings.AllowTCP {
		t.Fatal("route_tcp_fallback should be disabled")
	}
}

func TestParseRouteOrderDropsUnknownAndDuplicateRoutes(t *testing.T) {
	got := parseRouteOrder("direct_ws|unknown|cf_proxy_ws|direct_ws|awg")
	want := []routeKind{routeDirectWS, routeCFProxyWS, routeAWGWarp}
	if len(got) != len(want) {
		t.Fatalf("route order=%v, want=%v", got, want)
	}
	for i := range want {
		if got[i] != want[i] {
			t.Fatalf("route order=%v, want=%v", got, want)
		}
	}
}
