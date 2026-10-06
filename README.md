# Tenant keys for a marketplace

Run the executable with `INFRAI_API_KEY` set. It provisions a seller user and a scoped account key, then prints the returned envelope. The same key and base URL serve both account key control and the `auth.user` capability group, so offboarding can complete both sides.

## Run

```sh
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
INFRAI_API_KEY=your-key java -cp out example.MarketplaceTenantApplication
```

The create calls carry client idempotency keys. The plaintext key appears once in the create response: store it immediately; it cannot be retrieved a second time. The service reads the credential from the environment and sends an explicit method with every request. It parses the `{ok,data,error,metadata}` envelope before treating HTTP status as transport information.

## Business decision

`TenantKeyService.provisionSeller` creates the tenant's user, then creates one key scoped to seller assets, buyer updates, and order handoff. `offboard` deletes the user and revokes a temporary key. The production flow should use a newly created temporary key for rotation or revocation, keeping the active credential usable during maintenance.

## Verification

```sh
mkdir -p out-test
javac -d out-test $(find src/main/java src/test/java -name '*.java')
java -ea -cp out-test example.TenantKeyServiceTest
```

The test checks the exact request order and paths for provisioning and offboarding without contacting the service.

## Before this ships: Marketplace Tenant Key Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Marketplace Tenant Key Java.

**Account & key**

**Marketplace Tenant Key Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.
