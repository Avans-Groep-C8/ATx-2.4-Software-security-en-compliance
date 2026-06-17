# Auditrapport — aanvullende secties (WS06)

> **Classificatie (NEN-7510:2024-2 — 5.12):** Vertrouwelijk

**Document:** `docs/auditrapport/00-auditrapport.md`  
**Module:** `webservices.rest` v3.2.0 · OpenMRS-platform 2.8.3  
**Norm:** NEN-7510:2024-2 control **8.8** (SBOM/dependencies) + transparantie restrisico's  
**Datum:** 2026-06-17  
**Status:** Definitief — alleen §5 (SBOM/updateadvies) en §6 (niet-uitgevoerde items)

Dit bestand bevat **uitsluitend** de opdrachtsecties voor Sprint 4. Overige auditonderdelen staan in losse documenten (o.a. [00-risk-assessment.md](00-risk-assessment.md), [01-gap-analyse.md](01-gap-analyse.md), [pentestrapport-definitief.md](../pentest/pentestrapport-definitief.md)).

---

## 5. SBOM en dependency-beheer (NEN-7510:2024-2 control 8.8)

Control **8.8** (beheer technische kwetsbaarheden; ISO/IEC 27001:2022 **A.8.8**) vereist inventarisatie, tijdige kwetsbaarheidsinformatie, risicogebaseerde prioritering en traceerbare opvolging.

### 5.1 SBOM — generatie en bijlage

| Eigenschap | Waarde |
|------------|--------|
| **Formaat** | CycloneDX JSON, schema 1.6 |
| **Generator** | Anchore Syft 1.42.3 (`anchore/sbom-action`) |
| **Auditbijlage** | [bijlage-sbom.cdx.json](bijlage-sbom.cdx.json) |
| **CI-bron** | [docs/sbom.cdx.json](../sbom.cdx.json) (workflow [sbom.yml](../../.github/workflows/sbom.yml)) |
| **CI-artifact** | `sbom-cyclonedx-json` per push op `main` |
| **Componenten** | **82** (Maven-dependencies, GitHub Actions, repositorybestanden) |
| **Timestamp bijlage** | 2026-06-03T08:48:41Z |

De SBOM dekt de **repository en build-context**. Voor **runtime-relevante** Java-kwetsbaarheden is de SCA-scan na `mvn clean install` leidend (transitieve boom inclusief OpenMRS API/Web).

**Relatie SBOM ↔ SCA:** SBOM = versie-inventaris (8.9); Snyk SCA koppelt CVE's aan versies. Samen bewijs voor 8.8.

### 5.2 SCA-resultaten (Snyk)

| Bron | Referentie |
|------|------------|
| Workflow | [.github/workflows/snyk.yml](../../.github/workflows/snyk.yml) |
| CI-run | `27360377078` (push `main`, 2026-06-11) |
| Artifact | `snyk-results` → `snyk-sca.json` |
| Triagebeleid | [false-positives-beleid.md](../false-positives-beleid.md) |
| Backlog | SEC-006 in [06-security-backlog.md](06-security-backlog.md) |

**Ontdubbelde telling** over vier Maven-modules:

| Ernst | Uniek | CVSS-bereik |
|-------|-------|-------------|
| Critical | 6 | 9,1 – 9,8 |
| High | 53 | 7,1 – 8,9 |
| Medium | 33 | 5,1 – 6,9 |
| Low | 14 | 2,1 – 3,7 |
| **Totaal** | **106** | |

### 5.3 Geprioriteerde CVE's (gecontroleerd)

Gecontroleerd tegen Snyk SCA (CI-run `27360377078`). Volledige tabellen: [bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md) §4.

#### Critical (CVSS ≥ 9,0)

| Prio | CVE | CVSS | Component | Fix | REST-pad relevant? |
|------|-----|------|-----------|-----|-------------------|
| P1 | CVE-2026-44249 | 9,2 | `netty-handler@4.1.118` | 4.1.135.Final | Ja — HTTP-stack |
| P1 | CVE-2019-10202 | 9,8 | `jackson-mapper-asl@1.9.14` | Geen | Onderzoeken — legacy Jackson 1.x |
| P1 | CVE-2026-43512 | 9,1 | `catalina@6.0.53` (via `jasper`) | Tomcat 9.0.118+ | Provided — container bepaalt runtime |
| P2 | CVE-2025-50106 | 9,2 | `graal-sdk@20.3.17` | GraalVM 21.0.8+ | Waarschijnlijk build-time |
| P2 | CVE-2025-30749 | 9,2 | `graal-sdk@20.3.17` | GraalVM 21.0.8+ | Idem |
| P2 | CVE-2025-21587 | 9,1 | `graal-sdk@20.3.17` | GraalVM 21.0.7+ | Idem |

#### High — direct REST-/platformrelevant (selectie)

| Prio | CVE | CVSS | Component | Fix | Impact patiëntdata |
|------|-----|------|-----------|-----|-------------------|
| P1 | CVE-2026-40076 | 8,6 | `openmrs-web@2.8.3` | Geen in scan | Hoog — directory traversal |
| P1 | CVE-2024-38816 | 8,7 | `spring-webmvc@5.3.30` | 6.1.13+ | Hoog — path traversal |
| P1 | CVE-2024-38819 | 8,7 | `spring-webmvc@5.3.30` | 6.1.14+ | Hoog |
| P1 | CVE-2025-41249 | 8,7 | `spring-core@5.3.30` | 6.2.11+ | Hoog — autorisatie |
| P1 | CVE-2026-42198 | 8,7 | `postgresql@42.7.7` | 42.7.11 | Midden — DB-laag |
| P2 | CVE-2025-58056 | 8,7 | `netty-codec-http` | 4.1.125+ | Hoog — HTTP smuggling |
| P2 | CVE-2026-27830 | 8,9 | `c3p0@0.9.5.5` | 0.12.0 | Laag — connection pool |

### 5.4 Updateadvies

Definitief advies: [bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md). Prioritering: **CVSS × bereikbaarheid × healthcare-impact**.

| Golf | Termijn | Kernacties |
|------|---------|------------|
| **Golf 1** | ≤ 1 sprint | `commons-codec` 1.14→1.17.2; Jackson 2.19.1→2.21.2; PostgreSQL-driver; Netty BOM; Tomcat `jasper`; SnakeYAML testscope |
| **Golf 2** | Kwartaal (platform) | OpenMRS Core; Spring 5→6; `c3p0`, Log4j |
| **Golf 3** | Risicoacceptatie | Legacy zonder fix — compenserende controles + `.snyk` met `expires` |

**Patchdeadlines:** Critical + bereikbaar + patiëntdata → ≤ 24 uur; High + bereikbaar → ≤ 1 week / binnen sprint.

**Status Golf 1:** nog niet uitgevoerd (`commons-codec@1.14`, `jacksonVersion@2.19.1`). Zie §6.4.

### 5.5 Koppeling control 8.8

| Vereiste | Invulling | Bewijs |
|----------|-----------|--------|
| Inventaris | SBOM + `pom.xml` | [bijlage-sbom.cdx.json](bijlage-sbom.cdx.json) |
| Kwetsbaarheidsinformatie | Snyk SCA op PR/`main` | CI-run `27360377078` |
| Prioritering | CVSS + bereikbaarheid + patiëntdata | Bijlage §4–5; §5.3 |
| Maatregelen | Golf 1–3 + SEC-006 | [06-security-backlog.md](06-security-backlog.md) |
| Traceerbaarheid | CI-artifacts, `.snyk` | `reason` + `expires` |

### 5.6 Acceptatiecriteria

| Criterium | Status |
|-----------|--------|
| SBOM als bijlage | ✅ [bijlage-sbom.cdx.json](bijlage-sbom.cdx.json) |
| Updateadvies met CVE/CVSS | ✅ [bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md) |
| Sluit aan op overige auditdocs | ✅ Verwijzing [00-risk-assessment.md](00-risk-assessment.md), SEC-006 |
| CVE's gecontroleerd | ✅ Snyk CI-run `27360377078` |

---

## 6. Niet-uitgevoerde items en beperkingen

Transparante vastlegging van maatregelen die **niet zijn uitgevoerd**: reden, restrisico, vervolgstap en acceptatie binnen projectscope (ATx-2.4, modulescope `webservices.rest`).

### 6.1 Bronnen

[02-pipeline-compliance.md](02-pipeline-compliance.md), [06-security-backlog.md](06-security-backlog.md), [pentest/01-plan.md](../pentest/01-plan.md), dependency-advies Golf 1–3, [01-gap-analyse.md](01-gap-analyse.md).

### 6.2 Pipeline en CI/CD

**Wel uitgevoerd (geen restrisico-item):** trunk-based op `main`; ruleset (PR, 1 reviewer, up-to-date, status checks, geen force-push) — [evidence/github-ruleset-main.png](evidence/github-ruleset-main.png); GitHub Environments `develop` / `test` / `production` — [evidence/github-environments-otap.png](evidence/github-environments-otap.png); workflows CI, CodeQL, Snyk, SBOM, promote-prod. Zie [pipeline-strategie.md](../pipeline-strategie.md).

| Item | Reden | Risico | Vervolgstap | Acceptabel? |
|------|-------|--------|-------------|-------------|
| Signed commits (PGP) | Niet in ruleset | Geen crypto-commit-signing | Org-policy | **Ja** — PR + ruleset compenseren |
| Dependabot + Dependency Review | Niet gekoppeld | Kwetsbare deps per PR mogelijk | Activeren op repo | **Gedeeltelijk** — Snyk detecteert |
| Snyk als hard gate | `continue-on-error: true` | CVE's blokkeren merge niet via Snyk | Hard gate na triage | **Ja** — CI + CodeQL wél verplicht |
| Secret Scanning | Org-niveau | Secrets onopgemerkt | Org instelling | **Ja** — SEC-005 backlog |
| README mini-ISMS | Niet als één doc | Beleid verspreid | README uitbreiden | **Ja** — staat in `docs/` |

### 6.3 Security tests

| Item | Reden | Risico | Vervolgstap | Acceptabel? |
|------|-------|--------|-------------|-------------|
| OWASP ZAP | Burp volstond voor P1 (8.29) | Minder auto-dekking | ZAP bij release | **Ja** — Burp uitgevoerd |
| ffuf | OpenAPI volstond | Endpoints gemist | Bij uitbreiding AS | **Ja** |
| Brute-force (TC-AUTH-03) | Niet op lokale omgeving | Lockout onbekend | SEC-003 + test | **Gedeeltelijk** |
| Volumetrische DoS | Buiten scope | Capaciteit onbekend | SEC-004 gateway | **Ja** |
| Hertest na Golf 1 | Golf 1 open | Patch-effect onbekend | Burp na PR | N.v.t. |

### 6.4 Applicatie, dependencies en platform

| Item | Reden | Risico | Vervolgstap | Acceptabel? |
|------|-------|--------|-------------|-------------|
| Golf 1 updates | Niet in `pom.xml` | 106 CVE's open | PR Golf 1 | **Nee prod** / **Ja audit** |
| SEC-001 autorisatie | Grote wijziging | BOLA op niet-geteste resources | Implementatie | **Gedeeltelijk** — T2 Pass |
| SEC-002 MFA | Platform | Accountovername | IdP | **Ja** — modulescope |
| SEC-003/004 | Gateway | T1/T6 | API-gateway | **Ja** |
| SEC-005–010 (deels) | PT-001 defer e.d. | Info disclosure | Quick wins | **Gedeeltelijk** |
| SEC-011–032 | Tijd/budget | Logging, IRP, encryptie | Backlog | **Ja** |
| Spring 5→6 / OpenMRS 3.x | Platformbesluit | Platform-CVE's | Roadmap | **Ja** |
| Golf 3 legacy | Geen fix | CVSS 9,8 theoretisch | `.snyk` + segmentatie | **Gedeeltelijk** |
| Behandelrelatie (8.3) | Niet in module | Need-to-know | EPD/upstream | **Ja** |
| MFA op REST | Stateless API | Basic Auth zwak | OAuth2 gateway | **Ja** |

### 6.5 Pentest — Defer

| ID | Titel | Restrisico | Vervolgstap |
|----|-------|------------|-------------|
| PT-001 | Stack traces REST | Info disclosure | SEC-010 |
| PT-002 | `patient` zonder `q=` → 400 | Reconnaissance | Evaluatie gewenst gedrag |
| PT-005 | `loggedinusers` 500 | Info disclosure | SEC-019 |
| PT-007 | `swagger.json` stack trace | Schema/stack lek | SEC-029 |

### 6.6 Onderbouwing acceptatie (CGI)

1. **Platform/organisatie** (MFA, Secret Scanning) — buiten modulescope; backlog P3/P4.
2. **Scopebeperking audit** (geen ZAP/DoS/productie) — vervangen door Burp DAST + attack-surface-doc.
3. **Openstaand met restrisico** (Golf 1, SEC-001, Snyk soft-fail) — niet prod-ready, wel traceerbaar voor eindbeoordeling.

Volledige implementatie van 32 backlog-items en 106 CVE-patches viel buiten Sprint 4; **detectie** (SBOM, SCA, pentest) en **behandelplannen** (bijlage, backlog) zijn wel geleverd.

### 6.7 Acceptatiecriteria

| Criterium | Status |
|-----------|--------|
| Niet-uitgevoerd transparant | ✅ §6.2–6.5 |
| Reden, risico, vervolgstap | ✅ Tabellen |
| Acceptatie projectscope | ✅ §6.6 |
| Bruikbaar CGI/eindbeoordeling | ✅ Koppeling backlog + §5 |

---

## Bijlagen bij deze secties

| Bijlage | Bestand |
|---------|---------|
| SBOM | [bijlage-sbom.cdx.json](bijlage-sbom.cdx.json) |
| Updateadvies | [bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md) |
| Ruleset-bewijs | [evidence/github-ruleset-main.png](evidence/github-ruleset-main.png) |
| OTAP-bewijs | [evidence/github-environments-otap.png](evidence/github-environments-otap.png) |

---

*Versie 1.2 — 2026-06-17. Alleen §5 en §6; geen volledig eindauditrapport.*
