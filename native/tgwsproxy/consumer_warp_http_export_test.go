package main

import "testing"

func TestNormalizeConsumerWarpWorkerBaseAcceptsHostname(t *testing.T) {
	got, err := normalizeConsumerWarpWorkerBase(" Example.UserName.Workers.Dev ")
	if err != nil {
		t.Fatalf("normalize: %v", err)
	}
	if got != "https://example.username.workers.dev" {
		t.Fatalf("got=%q", got)
	}
}

func TestNormalizeConsumerWarpWorkerBaseRejectsPathPortAndHTTP(t *testing.T) {
	for _, value := range []string{
		"http://example.workers.dev",
		"https://example.workers.dev/warp-bootstrap",
		"https://example.workers.dev:8443",
		"https://example.workers.dev?x=1",
	} {
		if _, err := normalizeConsumerWarpWorkerBase(value); err == nil {
			t.Fatalf("expected %q to be rejected", value)
		}
	}
}

func TestConsumerWarpRegistrationBodyRejectsInvalidKey(t *testing.T) {
	if _, err := consumerWarpRegistrationBody("not-a-wireguard-key"); err == nil {
		t.Fatal("expected invalid key rejection")
	}
}
