# Bijlage: Dependency-updateadvies (SBOM + SCA)

**Document:** `docs/auditrapport/bijlage-dependency-updateadvies.md`  
**Module:** `webservices.rest` v3.2.0  
**OpenMRS-platform:** 2.8.3  
**Norm:** NEN-7510:2024-2 control **8.8** (beheer technische kwetsbaarheden; ISO/IEC 27001:2022 **A.8.8**)  
**Datum analyse:** 2026-06-12  
**Status:** Definitief (auditbijlage Sprint 4)

---

## 1. Samenvatting

Op basis van de SBOM ([bijlage-sbom.cdx.json](bijlage-sbom.cdx.json); CI-bron: [docs/sbom.cdx.json](../sbom.cdx.json)) en SCA-output (Snyk `snyk test --all-projects` na `mvn clean install`, CI-run `27360377078`, artifact `snyk-results.json`) zijn over de vier Maven-modules samen **106 unieke kwetsbaarheden** gevonden:

| Ernst (Snyk) | Uniek (alle modules) | CVSS-bereik (indicatief) |
|--------------|----------------------|--------------------------|
| Critical | 6 | 9,1 – 9,8 |
| High | 53 | 7,1 – 8,9 |
| Medium | 33 | 5,1 – 6,9 |
| Low | 14 | 2,1 – 3,7 |

### Leeswijzer: Snyk.io-dashboard vs. dit document

Het Snyk.io-dashboard toont per Maven-module een eigen telling en sommeert die bovenin de organisatie (**21 Critical**: 5 root + 5 `integration-tests` + 5 `omod` + 6 `omod-common`). **Dezelfde CVE telt daar dus tot vier keer mee.** Dit document ontdubbelt per CVE × component × versie, conform het triagebeleid: één kwetsbaarheid = één besluit.

| Module (Snyk-project) | Dependencies | Kwetsbaarheden (per module) | Waarvan Critical |
|------------------------|--------------|------------------------------|------------------|
| `pom.xml` (root) | 203 | 88 | 5 |
| `integration-tests/pom.xml` | 205 | 89 | 5 |
| `omod/pom.xml` | 214 | 95 | 5 |
| `omod-common/pom.xml` | 219 | 98 | **6** |
| **Uniek totaal** | — | **106** | **6** |

**Conclusie:** het merendeel van de Critical/High-CVE's zit in **transitieve platform-dependencies** (Spring 5.3, Netty, GraalVM SDK, legacy Jackson). Twee uitzonderingen zijn direct beïnvloedbaar vanuit de module-POM's: `commons-codec` (compile, in `.omod` — geen open CVE) en **`org.apache.tomcat:jasper@6.0.53`** in `omod-common` (provided), die de zesde Critical binnenhaalt via `catalina@6.0.53` (CVE-2026-43512, CVSS 9,1).

**Prioriteit 1:** platform- en infrastructuurupdates die het REST-pad raken (`openmrs-web`, Spring WebMVC, Netty, PostgreSQL-driver).  
**Prioriteit 2:** direct beheerde module-dependencies (`commons-codec`, Jackson-versie in `pom.xml`).  
**Prioriteit 3:** legacy componenten zonder fix (documenteer risicoacceptatie).

---

## 2. Bronnen en reproduceerbaarheid

| Bron | Pad / referentie | Rol |
|------|------------------|-----|
| SBOM (CycloneDX) — auditbijlage | [bijlage-sbom.cdx.json](bijlage-sbom.cdx.json) | Versie-inventaris (Syft 1.42.3, 2026-06-03) |
| SBOM (CycloneDX) — CI-bron | [docs/sbom.cdx.json](../sbom.cdx.json) | Zelfde inhoud; gegenereerd door [sbom.yml](../../.github/workflows/sbom.yml) |
| SBOM CI-artifact | Workflow [sbom.yml](../../.github/workflows/sbom.yml), artifact `sbom-cyclonedx-json` | Actuele build op `main` |
| SCA (Snyk) | [snyk.yml](../../.github/workflows/snyk.yml) → `snyk-sca.json` in `snyk-results.json` | CVE-detectie op dependency tree |
| Samengevoegd scanartifact | CI artifact `snyk-results` (run 2026-06-11) | Audit trail (NEN-7510 8.29) |
| Triagebeleid | [false-positives-beleid.md](../false-positives-beleid.md) | Besluit patchen / accepteren / supprimeren |
| Security backlog | [06-security-backlog.md](06-security-backlog.md) SEC-006 | Opvolging |

**Scancontext:** Snyk draait na `mvn -B clean install -DskipTests` op Java 8, zodat de volledige transitieve boom (inclusief OpenMRS API/Web) wordt meegenomen. `--all-projects` detecteert de vier module-POM's afzonderlijk (zie §1); het artifact bevat daarnaast twee duplicaat-entries uit `omod/target/` (gekopieerde POM's in de build-output) die buiten de telling blijven. Op Snyk.io zijn dezelfde vier projecten geïmporteerd; de dashboardtotalen tellen CVE's per module en liggen daardoor hoger dan de ontdubbelde aantallen hier.

---

## 3. Dependency-overzicht (SBOM + directe POM)

### 3.1 Directe afhankelijkheden module (`pom.xml`)

| Component | Versie | Scope | In productie-`.omod`? | CVE in SCA |
|-----------|--------|-------|------------------------|------------|
| `commons-codec` | 1.14 | compile | **Ja** (`lib/commons-codec-1.14.jar`) | Geen |
| `org.apache.tomcat:jasper` | 6.0.53 (`omod-common/pom.xml`) | provided | Nee (servletcontainer) | **1 Critical + 6 overige** via `catalina`/`coyote` |
| `openmrs-api` | 2.8.3 | provided | Nee (platform) | Geen direct |
| `openmrs-web` | 2.8.3 | provided | Nee (platform) | **2 High** |
| `jackson-core/databind/annotations` | 2.19.1 | provided | Nee (platform) | **2 High** (Snyk) |
| `javax.servlet-api` | 4.0.1 | provided | Nee | Geen |
| `javax.mail` | 1.6.2 | provided | Nee | Geen |
| `mockito-core` | 3.12.4 | test | Nee | Geen |

### 3.2 Belangrijkste transitieve componenten met CVE's (platform)

| Component | Versie | Unieke CVE's (voorbeeld) | Max CVSS | Fix beschikbaar |
|-----------|--------|--------------------------|----------|-----------------|
| `org.springframework:*` | 5.3.30 | CVE-2024-38816, CVE-2025-41249, CVE-2026-41840, … | 8,7 | Gedeeltelijk (Spring 6.x) |
| `io.netty:*` | 4.1.118.Final | CVE-2026-44249, CVE-2025-58056, … | **9,2** | Ja → 4.1.133+ / 4.1.135+ |
| `org.codehaus.jackson:jackson-mapper-asl` | 1.9.14-MULE-002 | CVE-2019-10202, CVE-2019-10172 | **9,8** | **Nee** |
| `org.graalvm.sdk:graal-sdk` | 20.3.17 | CVE-2025-50106, CVE-2025-30749, … | **9,2** | Ja (GraalVM 21+) |
| `org.postgresql:postgresql` | 42.7.7 | CVE-2026-42198 | 8,7 | Ja → 42.7.11 |
| `com.mchange:c3p0` | 0.9.5.5 | CVE-2026-27830 | 8,9 | Ja → 0.12.0 |
| `org.apache.logging.log4j:log4j-core` | 2.22.1 | CVE-2026-34478, … | 7,7 | Gedeeltelijk |
| `com.google.protobuf:protobuf-java` | 3.19.4 | CVE-2024-7254 | 8,7 | Ja → 3.25.5+ |
| `org.apache.tomcat:catalina` (via `jasper`, `omod-common`) | 6.0.53 | CVE-2026-43512, … | **9,1** | Tomcat 9.0.118+ (artifact-migratie nodig) |
| `org.yaml:snakeyaml` (via `integration-tests`) | — | 7 CVE's (2 High) | 7,x | Ja → 2.x |

### 3.3 SBOM-scope

De audit-SBOM ([bijlage-sbom.cdx.json](bijlage-sbom.cdx.json)) bevat **82 componenten** (Maven, GitHub Actions, bestanden). De SCA-scan na Maven-build is leidend voor **runtime-relevante** Java-dependencies; de SBOM dient als versie-inventaris en supply-chain-bewijs (NEN-7510 8.8 + 8.9).

---

## 4. Relevante CVE's — Critical en High (geprioriteerd)

Onderstaande tabel bevat de **kritieke** bevindingen en een selectie **high**-CVE's met directe relevantie voor een REST-module op patiëntdata. Volledige lijst: `snyk-results.json` → `sca[].vulnerabilities` (106 uniek over vier modules).

### 4.1 Critical (CVSS ≥ 9,0)

| Prio | CVE / ID | CVSS | Component | Beschrijving (kort) | Fix | Bereikbaarheid REST |
|------|----------|------|-----------|---------------------|-----|---------------------|
| **P1** | CVE-2026-44249 | 9,2 | `netty-handler@4.1.118` | Incorrect comparison (Netty) | 4.1.135.Final | Ja — HTTP-stack platform |
| **P1** | CVE-2019-10202 | 9,8 | `jackson-mapper-asl@1.9.14` | Deserialisatie / inputvalidatie | Geen | Onderzoeken — legacy Jackson 1.x in classpath |
| **P2** | CVE-2025-50106 | 9,2 | `graal-sdk@20.3.17` | Deserialisatie onbetrouwbare data | GraalVM 21.0.8+ | Waarschijnlijk tooling — triage |
| **P2** | CVE-2025-30749 | 9,2 | `graal-sdk@20.3.17` | Deserialisatie | GraalVM 21.0.8+ | Idem |
| **P2** | CVE-2025-21587 | 9,1 | `graal-sdk@20.3.17` | Timing attack | GraalVM 21.0.7+ | Idem |
| **P1** | CVE-2026-43512 | 9,1 | `catalina@6.0.53` (via `jasper` in `omod-common`) | Tomcat 6 EOL-kwetsbaarheid | Tomcat 9.0.118+ | Provided scope — runtime-container bepaalt; POM-declaratie wel zelf updatebaar |

### 4.2 High — direct REST-/platformrelevant

| Prio | CVE / ID | CVSS | Component | Beschrijving | Fix | Impact patiëntdata |
|------|----------|------|-----------|--------------|-----|-------------------|
| **P1** | CVE-2026-40076 | 8,6 | `openmrs-web@2.8.3` | Directory traversal | Geen in scan | **Hoog** — weblaag REST |
| **P1** | CVE-2024-38816 | 8,7 | `spring-webmvc@5.3.30` | Path traversal | 6.1.13+ | **Hoog** — MVC/REST routing |
| **P1** | CVE-2024-38819 | 8,7 | `spring-webmvc@5.3.30` | Path traversal | 6.1.14+ | **Hoog** |
| **P1** | CVE-2025-41249 | 8,7 | `spring-core@5.3.30` | Incorrect authorization | 6.2.11+ | **Hoog** — autorisatie |
| **P1** | CVE-2026-42198 | 8,7 | `postgresql@42.7.7` | Resource exhaustion | 42.7.11 | Midden — DB-laag (indien PG) |
| **P2** | SNYK-JAVA-…-15365924 | 8,7 | `jackson-core@2.19.1` | Resource exhaustion | 2.21.1+ | Midden — JSON serialisatie API |
| **P2** | CVE-2025-58056 | 8,7 | `netty-codec-http` | HTTP request smuggling | 4.1.125+ | Hoog — reverse proxy stack |
| **P2** | CVE-2026-27830 | 8,9 | `c3p0@0.9.5.5` | Deserialisatie | 0.12.0 | Laag — connection pool |
| **P3** | CVE-2022-23612 | 7,5 | `openmrs-web@2.8.3` | DoS | Geen | Midden |
| **P3** | CVE-2020-13936 | 8,1 | `velocity@1.7` | Arbitrary code execution | Geen | Laag — template engine |

---

## 5. Prioritering en updatevolgorde

Prioritering volgt [false-positives-beleid.md](../false-positives-beleid.md) §3 en §5: CVSS × bereikbaarheid × healthcare-impact.

### Golf 1 — Direct uitvoerbaar (≤ 1 sprint)

| # | Actie | Component | Van → Naar | Risico update | Eigenaar |
|---|-------|-----------|------------|---------------|----------|
| 1 | Bump directe dependency | `commons-codec` | 1.14 → **1.17.2** (laatste 1.x) | Laag — API stabiel | Module-team |
| 2 | Pin Jackson in `pom.xml` | `jackson-*` | 2.19.1 → **2.21.2** | Laag — provided; test regressie JSON API | Module-team |
| 3 | PostgreSQL-driver (infra) | `postgresql` | 42.7.7 → **42.7.11** | Laag — infra, niet in `.omod` | DevOps / DBA |
| 4 | Netty BOM override (indien platform toestaat) | `io.netty` | 4.1.118 → **4.1.135.Final** | **Middel** — compatibiliteit Spring 5.3 | Platform + module |
| 5 | Tomcat-artefact `jasper` evalueren (`apacheTomcatVersion` in root-POM) | `org.apache.tomcat:jasper` | 6.0.53 → modern equivalent (`tomcat-jasper` 9.x) | **Middel** — provided; alleen compile-time JSP-API, runtime test vereist | Module-team |
| 6 | SnakeYAML in testscope | `org.yaml:snakeyaml` | → 2.x (via `integration-tests`) | Laag — alleen testpad | Module-team |

**Deadline (beleid, conform §5 [false-positives-beleid.md](../false-positives-beleid.md)):** Critical + bereikbaar + patiëntdata → **≤ 24 uur**; High + bereikbaar → **≤ 1 week / binnen sprint** na triage.

### Golf 2 — Platformafhankelijk (kwartaal)

| # | Actie | Component | Advies | Risico update |
|---|-------|-----------|--------|---------------|
| 5 | OpenMRS Core-upgrade evalueren | `openmrs-api/web` 2.8.3 | Volg OpenMRS security advisories; target ≥ 2.7.x LTS patch of 3.x roadmap | **Hoog** — regressietest volledige REST API |
| 6 | Spring Framework | 5.3.30 → 6.2.x | Alleen via OpenMRS 3.x / platform-migratie; niet los upgraden | **Zeer hoog** — breaking changes |
| 7 | Connection pool | `c3p0` 0.9.5.5 → 0.12.0 | Via Hibernate/OpenMRS dependency management | Middel |
| 8 | Log4j | `log4j-core` 2.22.1 → 2.25.3 | Platform + logging config testen | Laag–middel |

### Golf 3 — Geen fix / risicoacceptatie

| Component | CVE | CVSS | Advies |
|-----------|-----|------|--------|
| `jackson-mapper-asl@1.9.14` | CVE-2019-10202 | 9,8 | Trace classpath; verwijderen indien ongebruikt; anders **accepteren** met compenserend controles (WAF, netwerksegmentatie) + `expires` in `.snyk` |
| `commons-lang@2.4` | CVE-2025-48924 | 8,8 | Geen fix — migratie naar `commons-lang3` via upstream |
| `velocity@1.7` | CVE-2020-13936 | 8,1 | Geen fix — bevestig niet in REST-requestpad |
| `struts-core@1.3.8` | CVE-2014-0114 | 7,3 | Legacy — waarschijnlijk niet in REST-pad; **supprimeren** na bereikbaarheidsbewijs |
| `graal-sdk@20.3.17` | Diverse Critical | 9,1+ | Waarschijnlijk build-time; **supprimeren** na verificatie niet in runtime-JAR |

---

## 6. Impact- en risicoanalyse van updates

| Update | Technisch risico | Functioneel risico | Mitigatie |
|--------|------------------|--------------------|-----------|
| `commons-codec` 1.14 → 1.17.2 | Laag | Laag — encoding/hex in REST responses | Unit tests + integratietests |
| Jackson 2.19 → 2.21 | Laag–middel | Middel — JSON serialisatie edge cases | Regression op `/patient`, `/obs`, `/encounter` |
| Netty 4.1.118 → 4.1.135 | Middel | Middel — TLS/HTTP gedrag | Staging + Burp hertest (PT-003/004 context) |
| OpenMRS 2.8.3 → nieuwer | Hoog | **Hoog** — volledige module-compatibiliteit | Volledige test suite + pentest P1 hertest |
| Spring 5 → 6 | Zeer hoog | Zeer hoog — Jakarta EE namespace | Buiten scope module; platformbesluit |

**Restrisico zonder Golf 2:** Spring/Netty-CVE's blijven theoretisch exploiteerbaar op het platform; gecompenseerd door netwerksegmentatie, authenticatie op REST (behalve bekende uitzonderingen PT-003/004) en doorlopende SCA op `main`.

---

## 7. Koppeling NEN-7510:2024-2 control 8.8 (A.8.8)

| Vereiste 8.8 | Invulling in dit project | Bewijs |
|--------------|--------------------------|--------|
| Inventaris van software/componenten | SBOM CycloneDX + Maven `pom.xml` | [bijlage-sbom.cdx.json](bijlage-sbom.cdx.json), CI artifact |
| Tijdige informatie over kwetsbaarheden | Snyk SCA op elke PR/`main` | `snyk-results.json`, [snyk.yml](../../.github/workflows/snyk.yml) |
| Beoordeling en prioritering o.b.v. risico | CVSS + bereikbaarheid + patiëntdata (§4–5) | Dit document + [false-positives-beleid.md](../false-positives-beleid.md) |
| Passende maatregelen (patch/accept/suppress) | Golf 1–3 + SEC-006 backlog | [06-security-backlog.md](06-security-backlog.md) |
| Documentatie en traceerbaarheid | Audit trail artifacts, geen stille ignore | CI artifacts, `.snyk` met `reason` + `expires` |

**Gerelateerde controls:** 8.29 (beveiligingstesten — scanbewijs), 8.9 (configuratiebeheer — SBOM-versies), 8.28 (veilig coderen — SAST naast SCA).

---

## 8. Acceptatiecriteria — check

| Criterium | Status |
|-----------|--------|
| SBOM opgenomen als auditbijlage | ✅ [bijlage-sbom.cdx.json](bijlage-sbom.cdx.json) |
| Advies gebaseerd op SBOM en scanresultaten | ✅ SBOM + Snyk SCA (CI-run 2026-06-11) |
| CVE-referenties gecontroleerd (Snyk/NVD) | ✅ §4 — CI-run `27360377078` |
| Critical en High CVE's geprioriteerd | ✅ §4 en §5 (P1–P3) |
| Impact en risico van updates benoemd | ✅ §6 |
| Sluit aan op auditrapport | ✅ [00-auditrapport.md](00-auditrapport.md) §5 |

---

## 9. Aanbevolen vervolgstappen

1. **Golf 1 uitvoeren** — PR met `commons-codec` + Jackson-bump; CI groen + integratietests.
2. **Platformroadmap** — OpenMRS-upgrade en Netty/Spring met platformteam afstemmen (SEC-006).
3. **Triage resterende 106 → backlog** — per item besluit in `.snyk` of security backlog; geen open Critical zonder gedocumenteerd besluit.
4. **Hertest** — na Golf 1 Burp spot-check op REST JSON-endpoints; na platform-upgrade volledige P1-pentest.

---

*Versie 1.1 — 2026-06-17. Gebaseerd op Snyk SCA-artifact CI-run `27360377078` (push `main`, 2026-06-11) en SBOM-bijlage `bijlage-sbom.cdx.json` (2026-06-03).*
