# Security Code Review — webservices.rest

**Document:** `docs/auditrapport/07-security-code-review.md`  
**Module:** OpenMRS `webservices.rest` v3.2.0  
**Norm:** NEN-7510:2024-2 (8.3, 8.26, 8.28, 8.29)  
**Datum review:** 2026-06-12  
**Reviewer:** Security audit (tooling + handmatige analyse + AI-assisted review)  
**Status:** Definitief

---

## 1. Samenvatting

De security code review combineert **geautomatiseerde SAST** (Snyk Code, GitHub CodeQL), **DAST-bevindingen** (Burp pentest P1) en **handmatige broncode-analyse** op security-kritieke paden (authenticatie, autorisatie, foutafhandeling, admin-endpoints).

| Bron | Bevindingen (uniek) | Ernst |
|------|---------------------|-------|
| Handmatig + DAST (gevalideerd in code) | 6 | 2 Critical, 2 High, 1 Medium, 1 Info |
| Snyk Code (SAST) | 42 | 7 error, 15 warning, 20 note |
| GitHub CodeQL | 3 open alerts | 1 error, 2 warning |
| SCA (dependencies) | 106 unieke CVE's (4 modules) | Zie bijlage dependency-advies |
| AI-assisted review | 4 aanvullende observaties | Design/ configuratie |

**Conclusie:** de module heeft **werkende privilege-checks op standaard REST-resources** (pentest: anoniem `GET /patient` → 401), maar **kritieke gaten op speciale endpoints** (`cleardbcache`, module-settings) en **onveilige defaults** (`enableStackTraceDetails=true`). SAST signaleert vooral XSS en HTTP-header-manipulatie in module-UI-paden; dependency-risico's zijn grotendeels platform-transitief.

**Aanbevolen volgorde:** SCR-001 → SCR-002/004 → SCR-003 → SCR-005/006 → overige SAST-warnings.

---

## 2. Scope en methodologie

### 2.1 Scope

| In scope | Buiten scope |
|----------|--------------|
| `omod-common/` — filters, REST-kern, foutafhandeling | OpenMRS Core (platform) broncode |
| `omod/` — controllers, resources, JSP/module-UI | Productie-deployment ziekenhuis |
| `config.xml` — global properties, filter-registratie | Organisatorisch ISMS |
| CI-workflows SAST/SCA | Volledige IDOR/RBAC nurse-test (geblokkeerd pentest) |

**Omvang:** 345 productie-`.java`-bestanden (excl. tests); ~511 testbestanden niet diep gereviewed.

### 2.2 Methodologie

1. **SAST** — Snyk Code + CodeQL op `main` (CI-run 2026-06-11, artifact `snyk-results.json`)
2. **DAST-validatie** — pentest-bevindingen PT-001 t/m PT-005 teruggekoppeld naar broncode
3. **Handmatige review** — focus op OWASP API Top 10:2023 (Broken Auth, BOLA/BOPLA, misconfig, injection)
4. **AI-assisted review** — systematische trace van auth-flow, error handling, proxy privileges
5. **Triagering** — conform [false-positives-beleid.md](../false-positives-beleid.md): bereikbaarheid × patiëntdata × fixbaarheid

---

## 3. Gebruikte tooling

| Tool | Type | Workflow / bron | Output |
|------|------|---------------|--------|
| **Snyk Code** | SAST | [.github/workflows/snyk.yml](../../.github/workflows/snyk.yml) | `snyk-sast.json` in artifact `snyk-results` |
| **Snyk test** | SCA | Idem | `snyk-sca.json` (106 unieke CVE's over 4 modules) |
| **GitHub CodeQL** | SAST | [.github/workflows/codeql.yml](../../.github/workflows/codeql.yml) | GitHub Security tab |
| **Burp Suite** | DAST (handmatig) | [docs/pentest/03-bevindingen.md](../pentest/03-bevindingen.md) | Runtime-bevestiging |
| **AI-assisted review** | Statische analyse | Cursor-agent op auth/error/admin-paden | Bevindingen SCR-003, SCR-010, SCR-014 |
| **Handmatige review** | Code walkthrough | Security-kritieke bestanden (§4) | SCR-001, SCR-002, SCR-004 |

**Reproduceerbaarheid:** CI-run `27360377078` (push `main`, 2026-06-11). Lokaal: `docs/scan-output/snyk-results.json`.

---

## 4. Onderzochte componenten

### 4.1 Security-kritieke paden (diep gereviewed)

| Component | Pad | Reden selectie |
|-----------|-----|----------------|
| AuthorizationFilter | `omod-common/.../filter/AuthorizationFilter.java` | Basic Auth, IP-allowlist |
| ContentTypeFilter | `omod-common/.../filter/ContentTypeFilter.java` | XXE/XML-blokkade |
| BaseRestController | `omod-common/.../controller/BaseRestController.java` | Exception → HTTP-status |
| RestUtil.wrapErrorResponse | `omod-common/.../RestUtil.java` (L820–868) | Stack trace in JSON |
| MainResourceController | `omod-common/.../controller/MainResourceController.java` | CRUD REST-kern |
| ClearDbCacheController | `omod/.../ClearDbCacheController2_0.java` | Destructief endpoint (PT-003) |
| SettingsFormController | `omod/.../SettingsFormController.java` | Admin-config (PT-004) |
| SwaggerDocController | `omod/.../SwaggerDocController.java` | API-docs + debug XSS |
| Module config | `omod/src/main/resources/config.xml` | Defaults global properties |
| Session/password controllers | `omod/.../SessionController*.java`, `PasswordResetController*.java` | Authenticatie |

### 4.2 SAST-brede dekking

Snyk Code scande de volledige `omod` + `omod-common` boom. CodeQL analyseerde Java + GitHub Actions (autobuild).

### 4.3 Positieve bevindingen (geen SCR-ID)

| Control | Bewijs |
|---------|--------|
| Anonieme patient-read geblokkeerd | Pentest TC-AUTHZ-01 → 401 + privilege-melding |
| XML Content-Type geblokkeerd | `ContentTypeFilter` + pentest TC-INJ-01 → 415 |
| Password/searchindex endpoints beschermd | Pentest TC-SPEC-04/05 → 401 |

---

## 5. Bevindingenregister

### 5.1 Prioriteit P1 — Kritiek (directe actie)

| ID | Titel | Bron | Ernst | Backlog | Status |
|----|-------|------|-------|---------|--------|
| **SCR-001** | `cleardbcache` zonder authenticatie/autorisatie | DAST + code | **Critical** | SEC-019 | Open (PT-003) |
| **SCR-004** | `settings.form/search` lekt global properties zonder auth | SAST + code | **Critical** | SEC-007 | Open |

### 5.2 Prioriteit P2 — Hoog

| ID | Titel | Bron | Ernst | Backlog | Status |
|----|-------|------|-------|---------|--------|
| **SCR-002** | `settings.form` anoniem bereikbaar + stack trace | DAST + code | **High** | SEC-007 | Open (PT-004) |
| **SCR-003** | Stack traces in API-responses (default `true`) | Code + config | **High** | SEC-010 | Open (PT-001, PT-005) |
| **SCR-005** | Reflected XSS op `apiDocs/debug` | CodeQL + Snyk | **High** | SEC-032 | Open |
| **SCR-006** | XSS-cluster module-UI (7× Snyk error) | Snyk Code | **High** | SEC-029* | Open |

\*Nog geen dedicated backlog-item; koppelen aan SEC-010/SEC-029 of nieuw SEC-033.

### 5.3 Prioriteit P3 — Midden

| ID | Titel | Bron | Ernst | Backlog | Status |
|----|-------|------|-------|---------|--------|
| **SCR-007** | HTTP Response Splitting in REST controllers | Snyk Code | Medium | — | Triage |
| **SCR-008** | Open Redirect via response headers | Snyk Code | Medium | — | Triage |
| **SCR-009** | Unsafe reflection (`Class.forName`) in RestUtil | Snyk Code | Medium | — | Triage |
| **SCR-010** | Auth-filter blokkeert niet bij mislukte Basic Auth | Handmatig | Medium (design) | SEC-001 | Geaccepteerd* |
| **SCR-011** | Spring CSRF op POST-endpoints (geen token) | Snyk Code | Medium/Laag** | — | Defer |
| **SCR-013** | 106 dependency-CVE's (grotendeels transitief) | SCA | Variabel | SEC-006 | Zie bijlage |

\*By design: filter delegeert aan OpenMRS API; pentest bevestigt dat patient-endpoints wél 401 geven.  
\*\*Voor stateless REST + Basic Auth is CSRF-risico beperkt; relevant voor browser-sessie op module-UI.

### 5.4 Prioriteit P4 — Laag / Info

| ID | Titel | Bron | Ernst | Backlog | Status |
|----|-------|------|-------|---------|--------|
| **SCR-012** | Hardcoded wachtwoorden in unit tests | Snyk Code | Info | — | Accept |
| **SCR-014** | Proxy privilege `GET_GLOBAL_PROPERTIES` in RestUtil | AI-review | Info | — | Monitor |

---

## 6. Detailbevindingen

### SCR-001 — Anonieme `cleardbcache` (Critical)

| Veld | Waarde |
|------|--------|
| **Bestand** | `omod/.../ClearDbCacheController2_0.java` |
| **Regel** | 37–105 (`@RequestMapping` POST, geen `Context.requirePrivilege`) |
| **CWE** | CWE-306 (Missing Authentication for Critical Function) |
| **OWASP API** | API5:2023 Broken Function Level Authorization |
| **DAST-ref** | PT-003, CVSS 9.1 — [03-bevindingen.md](../pentest/03-bevindingen.md) |
| **Backlog** | SEC-019 |

**Beschrijving:** `POST /ws/rest/v1/cleardbcache` roept `SessionFactory.getCache().evictAllRegions()` aan zonder enige authenticatie- of autorisatiecheck. Pentest bevestigt HTTP 204 voor anonieme callers.

**Risico bij niet oplossen:** beschikbaarheids- en integriteitsimpact in productie-OpenMRS — cache flush onder load kan performance-collaps en inconsistent gedrag veroorzaken; aanvaller hoeft geen account. In zorgcontext: indirect patiëntveiligheidsrisico bij vertraagde of inconsistente medische data-weergave.

**Mitigatie:** `Context.requirePrivilege(...)` (superuser) vóór cache-operatie; of endpoint uitschakelen in productie. Unit test: anoniem → 401/403.

---

### SCR-002 — `settings.form` niet afgeschermd (High)

| Veld | Waarde |
|------|--------|
| **Bestand** | `omod/.../SettingsFormController.java` |
| **Regel** | 38–40 (`showForm()` leeg, geen auth-check) |
| **CWE** | CWE-862 (Missing Authorization) |
| **DAST-ref** | PT-004, CVSS 7.5 |
| **Backlog** | SEC-007 |

**Beschrijving:** GET-handler heeft geen expliciete autorisatie; OpenMRS `AuthorizationAdvice` gooit exception die als stack trace naar client lekt i.p.v. 401/403.

**Risico bij niet oplossen:** configuratie-endpoint en interne code-paden zichtbaar voor reconnaissance; bij fout in autorisatielaag potentieel wijziging van module-instellingen (IP-allowlist, maxResults).

**Mitigatie:** `@Authorized` of `Context.checkPrivilege(RestConstants.MODULE_ID + ".manage.settings")` op alle handlers; globale exception handler voor module-paden zonder stack trace.

---

### SCR-003 — Stack traces in JSON-foutresponses (High)

| Veld | Waarde |
|------|--------|
| **Bestanden** | `config.xml` L65–67; `RestUtil.java` L844–859 |
| **CWE** | CWE-209 (Generation of Error Message Containing Sensitive Information) |
| **DAST-ref** | PT-001, PT-005 (INFO-001 stack trace patroon) |
| **Backlog** | SEC-010 |

**Beschrijving:** Global property `webservices.rest.enableStackTraceDetails` heeft **default `true`** in `config.xml`, terwijl de beschrijving adviseert `false`. `wrapErrorResponse` voegt bij `true` volledige `ExceptionUtils.getStackTrace(ex)` toe aan JSON veld `detail`.

```65:67:omod/src/main/resources/config.xml
		<property>@MODULE_ID@.enableStackTraceDetails</property>
		<defaultValue>true</defaultValue>
		<description>If the value of this setting is "true", then the details of the stackTrace would be shown in the error response. However, the recommendation is to keep it as "false", from the Security perspective, to avoid leaking implementation details.</description>
```

**Risico bij niet oplossen:** information disclosure — aanvaller leert framework-versies, class-paden en autorisatielogica; versnelt gerichte aanvallen op REST API met patiëntdata.

**Mitigatie:** Wijzig default naar `false`; in productie global property afdwingen; optioneel `code`-veld ook verwijderen voor externe clients.

---

### SCR-004 — Ongeautoriseerde global-property-enumeratie (Critical)

| Veld | Waarde |
|------|--------|
| **Bestand** | `omod/.../SettingsFormController.java` |
| **Regel** | 50–62 (`searchProperties`) |
| **CWE** | CWE-200 (Exposure of Sensitive Information), CWE-862 |
| **SAST-ref** | Snyk `java/XSS` (regel 62) — secundair |
| **Backlog** | SEC-007 (uitbreiding) |

**Beschrijving:** Endpoint `GET .../settings.form/search?prefix=` heeft geen `Context.isAuthenticated()` check. Retourneert **property-namen én -waarden** via string-concatenatie, inclusief potentiele secrets in global properties.

**Risico bij niet oplossen:** directe leakage van API-keys, wachtwoorden of connection strings opgeslagen als global properties; ernstig in multi-tenant of gedeelde test-OTAP.

**Mitigatie:** Verwijder endpoint of vereis admin-privilege; retourneer alleen property-namen (geen waarden); JSON via serializer i.p.v. string concat (ook XSS mitigatie).

---

### SCR-005 — Reflected XSS `apiDocs/debug` (High)

| Veld | Waarde |
|------|--------|
| **Bestand** | `omod/.../SwaggerDocController.java` |
| **Regel** | 24–28 |
| **CWE** | CWE-79 (Cross-site Scripting) |
| **CodeQL** | `java/xss` — error, open |
| **Snyk** | `java/XSS` — error |
| **Backlog** | SEC-032 |

```24:28:omod/src/main/java/org/openmrs/module/webservices/rest/web/controller/SwaggerDocController.java
	@RequestMapping(value = "/debug", method = RequestMethod.GET)
	@org.springframework.web.bind.annotation.ResponseBody
	public String debug(@org.springframework.web.bind.annotation.RequestParam("tag") String tag) {
		return "<h1>Debugging Tag: " + tag + "</h1>";
	}
```

**Risico bij niet oplossen:** sessie-diefstal of admin-acties via geïnjecteerde script bij bezoek door beheerder; in browser-context naast REST API.

**Mitigatie:** Verwijder debug-endpoint in productie; HTML-escape input; of `@ResponseBody` JSON + Content-Type `application/json`.

---

### SCR-006 — XSS-cluster module-UI (High)

| Bestand | Regel | Snyk rule |
|---------|-------|-----------|
| `SettingsFormController.java` | 62 | java/XSS |
| `SwaggerDocController.java` | 27 | java/XSS |
| `ClobDatatypeStorageController.java` | 60 | java/XSS |
| `FormResourceController1_9.java` | 67 | java/XSS |
| `localHeader.jsp` | 7, 14, 20 | java/XSS |

**Risico bij niet oplossen:** XSS in module-beheer-UI; beperkter dan REST JSON API maar relevant voor admin-gebruikers met toegang tot patiëntdata.

**Mitigatie:** Output encoding (JSTL `c:out`, OWASP Java Encoder); CSP headers op module-pagina's.

---

### SCR-007 / SCR-008 — HTTP header manipulatie (Medium)

**Bestanden:** `MainResourceController.java` (L93, 105, 130, 135), `MainSubResourceController.java`, `ObsComplexValueController1_8.java`, `FormResourceController1_9.java`.

**Snyk:** `java/HttpResponseSplitting`, `java/OR` (Open Redirect).

**Risico bij niet oplossen:** response splitting of cache poisoning in specifieke browser/proxy-combinaties; beperkte impact op JSON REST-clients.

**Mitigatie:** Valideer/sanitize `Location`- en custom headers; gebruik `ServletResponse.encodeURL` waar nodig.

---

### SCR-009 — Reflection op zip-input (Medium)

| Veld | Waarde |
|------|--------|
| **Bestand** | `RestUtil.java` ~L789 |
| **Snyk** | `java/Reflection` |

**Risico:** potentieel unsafe deserialization/class loading als zip-inhoud aanvaller-gecontroleerd is. **Triage:** trace aanroeppad — waarschijnlijk module-load/test; suppressie alleen na bereikbaarheidsbewijs.

---

### SCR-010 — AuthorizationFilter pass-through (Medium, design)

```33:38:omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java
 * Filter intended for all /ws/rest calls that allows the user to authenticate via Basic
 * authentication. (It will not fail on invalid or missing credentials. We count on the API to throw
 * exceptions if an unauthenticated user tries to do something they are not allowed to do.)
```

**Risico:** endpoints die zelf geen privilege-check doen (SCR-001) blijven open. **Mitigatie:** defense-in-depth — default deny op destructieve/special endpoints.

---

### SCR-013 — Dependency-kwetsbaarheden (SCA)

106 unieke CVE's over de vier Maven-modules (6 Critical, 53 High); het Snyk.io-dashboard toont hogere totalen omdat het per module telt. Niet herhaald in dit document — zie [bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md). **Backlog:** SEC-006.

---

### SCR-014 — Proxy privilege `GET_GLOBAL_PROPERTIES` (Info)

| Veld | Waarde |
|------|--------|
| **Bestand** | `omod-common/.../RestUtil.java` (o.a. L73–77, L225–229, L849–853) |
| **Patroon** | `Context.addProxyPrivilege(...)` → lezen global property → `removeProxyPrivilege` in `finally` |
| **Bron** | AI-assisted review |

**Beschrijving:** Op meerdere plekken verhoogt `RestUtil` tijdelijk de privileges van de huidige (mogelijk anonieme) context om global properties te lezen, onder andere in `wrapErrorResponse` (het foutafhandelingspad). Het patroon is correct geïmplementeerd (`finally`-blok voorkomt privilege-lek), maar betekent wel dat configuratiewaarden gelezen worden namens niet-geauthenticeerde callers.

**Risico bij niet oplossen:** laag — de gelezen properties worden niet direct aan de client geretourneerd (behalve via SCR-003/SCR-004, die separaat zijn geregistreerd). Het patroon vergroot wel het belang van die fixes: elke nieuwe code die dit patroon kopieert en de waarde wél doorgeeft, creëert een lek.

**Mitigatie:** geen directe actie; monitoren bij code review van nieuwe endpoints. Na fix van SCR-003/004 is het restrisico verwaarloosbaar.

---

## 7. Snyk Code — samenvatting triage

| Snyk severity | Aantal | Besluit review |
|---------------|--------|----------------|
| error | 7 | SCR-004, SCR-005, SCR-006 — **patchen** |
| warning | 15 | SCR-007, SCR-008, SCR-009 — triage / plan |
| note | 20 | SCR-011, SCR-012 — grotendeels **accept/defer** (tests, CSRF op API) |

Volledige lijst: `snyk-results.json` → `sast.runs[0].results` (42 items).

---

## 8. CodeQL — open alerts

| Rule | Ernst | Bestand | Koppeling SCR |
|------|-------|---------|---------------|
| `java/xss` | error | `SwaggerDocController.java:27` | SCR-005 |
| `java/overly-large-range` | warning | `ServerLogActionWrapper.java:70` | — (ReDoS-risico log-filter, P4) |

Bekijk alle alerts: GitHub → Security → Code scanning.

---

## 9. Koppeling security backlog

| SCR-ID | Backlog | NEN-7510 | Deadline (beleid) |
|--------|---------|----------|-------------------|
| SCR-001 | SEC-019 | 8.3, 8.26 | ≤ 24 uur (Critical) |
| SCR-002, SCR-004 | SEC-007 | 8.9, 8.26 | ≤ 1 sprint |
| SCR-003 | SEC-010 | 8.26 | Quick win sprint 1 |
| SCR-005 | SEC-032 | 8.26 | ≤ 1 sprint |
| SCR-006 | SEC-029 / nieuw | 8.26, 8.28 | ≤ 2 sprints |
| SCR-013 | SEC-006 | 8.8 | Per CVSS-matrix [false-positives-beleid.md](../false-positives-beleid.md) |
| SCR-010 | SEC-001 | 8.3 | Parallel pentest-hertest |

---

## 10. Prioritering — implementatievolgorde

```text
1. SCR-001  cleardbcache auth          → direct, laag regressierisico
2. SCR-003  enableStackTraceDetails    → config-default wijzigen
3. SCR-004  verwijderen/beveiligen search endpoint
4. SCR-002  settings.form auth + error handler
5. SCR-005  verwijderen apiDocs/debug
6. SCR-006  XSS fixes module-UI
7. SCR-007/008  header sanitization (indien REST-clients betrokken)
8. SCR-013  dependency patches (Golf 1 bijlage dependency-advies)
```

---

## 11. Acceptatiecriteria — check

| Criterium | Status |
|-----------|--------|
| Review navolgbaar uitgevoerd (tooling + handmatig + bronverwijzingen) | ✅ §3, §5, §6 |
| Kwetsbaarheden duidelijk vastgelegd en onderbouwd | ✅ SCR-register + code/DAST-referenties |
| Geprioriteerd (P1–P4) | ✅ §5, §10 |
| Risico bij niet oplossen beschreven + systeemgebruik | ✅ per SCR in §6 |
| Koppeling security backlog | ✅ §9 |
| Verwijzing valide scanresultaten | ✅ CI-run 27360377078, pentest 2026-06-09 |

---

## 12. Referenties

| Document | Pad |
|----------|-----|
| Pentest-bevindingen | [docs/pentest/03-bevindingen.md](../pentest/03-bevindingen.md) |
| Security backlog | [06-security-backlog.md](06-security-backlog.md) |
| Scan-triagebeleid | [false-positives-beleid.md](../false-positives-beleid.md) |
| Dependency-updateadvies | [bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md) |
| Risk Assessment | [00-risk-assessment.md](00-risk-assessment.md) |
| Snyk workflow | [.github/workflows/snyk.yml](../../.github/workflows/snyk.yml) |
| CodeQL workflow | [.github/workflows/codeql.yml](../../.github/workflows/codeql.yml) |
| SCA/SAST artifact | CI `snyk-results` (run `27360377078`) |

---

*Versie 1.0 — 2026-06-12. Onderdeel auditrapport Sprint 4; combineer met §4 risico-analyse en §5 supply chain.*
