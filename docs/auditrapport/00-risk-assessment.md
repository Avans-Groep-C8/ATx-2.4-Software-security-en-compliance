# Risk Assessment Report — OpenMRS webservices.rest

**Document:** `docs/auditrapport/00-risk-assessment.md`  
**Module:** `webservices.rest` v3.2.0  
**Norm:** NEN-7510:2024-2 (+ NEN 7512, NEN 7513 waar relevant)  
**Classificatie:** Vertrouwelijk (5.12)  
**Datum:** 2026-06-09  
**Status:** Definitief (risicoanalyse) — pentest P1 uitgevoerd 2026-06-09; besluiten fix/accept/defer vastgesteld 2026-06-10

---

## 1. Management summary

De OpenMRS REST Web Services Module ontsluit **herleidbare patiëntgegevens** via HTTP API. De risicoanalyse combineert:

- **Asset-identificatie** (`03-assets.md`) — 10 kroonjuwelen met hoog risico
- **Risicomatrix** (`04-risico-matrix`) — 3 kritieke dreigingen (score ≥ 15)
- **Bow-tie** (`05-bowtie.md`) — top event: ongeautoriseerde bulk-extractie patiëntdata
- **Pipeline GAP** (`02-pipeline-compliance.md`) — 14 afwezige, 9 gedeeltelijke controls
- **Geautomatiseerde scans** — Snyk SAST/SCA/SBOM (`snyk.yml`), CodeQL, SBOM (`sbom.cdx.json`)
- **Security backlog** (`06-security-backlog.md`) — 32 geprioriteerde requirements
- **Pentest** (`docs/pentest/03-bevindingen.md`) — **uitgevoerd** (Burp, P1-scope T1/T2)

### Conclusie

| Aspect | Beoordeling |
|--------|------------|
| **Huidig risiconiveau** | **Onacceptabel (rood)** voor productie — Critical pentest-bevinding PT-003 |
| **Primaire oorzaak** | Destructieve endpoints zonder auth; misconfiguratie module; geen MFA/rate limiting |
| **Pentest T2 (patiëntdata)** | **Beheerst** — anonieme patient-read → 401, geen data-lek |
| **Pentest T1 (API-toegang)** | **Onvoldoende** — `cleardbcache` anoniem 204; `settings.form` anoniem bereikbaar |
| **Aanbevolen actie** | PT-003/PT-004 direct fixen; daarna hertest; P1-backlog resterend |
| **Geschatte investering P1+P2** | € 96.000 – € 165.600 (zie §8) |
| **Restrisico na P1-fixes + hertest** | Middel (geel) — MFA, SIEM, RBAC-review blijven nodig |

---

## 2. Scope en methodologie

### 2.1 Scope

| In scope | Buiten scope |
|----------|--------------|
| Module `webservices.rest` broncode en `.omod` | OpenMRS core UI (buiten REST) |
| CI/CD-pipeline (GitHub Actions) | Productie-omgeving ziekenhuis |
| Test/OTAP OpenMRS RefApp | Organisatorisch ISMS (gedeeltelijk) |
| Dependencies (Maven, SBOM) | Fysieke beveiliging |

### 2.2 Methodologie

1. **Identificatie** — assets en dreigingen (CIA-triad)
2. **Analyse** — kans × impact (1–5), risicomatrix
3. **Evaluatie** — bow-tie top event, NEN 7510-2 GAP
4. **Behandeling** — security backlog met NEN-7510:2024-2 controls
5. **Verificatie** — SAST/SCA (doorlopend) + DAST pentest (Burp, P1 uitgevoerd)
6. **Rapportage** — dit document + pipeline-artifacts

---

## 3. Gevoelige gegevens (kroonjuwelen)

Bron: `03-assets.md`. Onder NEN-7510:2024-2 vallen deze onder **5.12** (classificatie) en **5.34** (privacy/PII).

| ID | Gegeven | Categorie | BIV-impact | REST-resource (indicatief) |
|----|---------|-----------|------------|----------------------------|
| A-01 | Patiëntrecords | Patiëntdata | **C: hoog, I: hoog, B: midden** | `/patient` |
| A-02 | Patiëntidentifiers (UUID) | Patiëntdata | C: hoog | `/patient/{uuid}` |
| A-03 | Persoonsgegevens (naam, adres) | Patiëntdata | C: hoog | `/person`, `/patient` |
| A-04 | Sessietokens | Authenticatie | C: hoog, I: hoog | `/session` |
| A-05 | Credentials | Authenticatie | C: hoog, I: hoog | `/session`, `/password` |
| A-06 | Rollen & privileges | Autorisatie | I: hoog | `/role`, `/privilege` |
| A-07 | Observaties (vitals, labs) | Medisch | C: hoog, I: hoog | `/obs` |
| A-09 | Orders & medicatie | Medisch | I: **kritiek** (patiëntveiligheid) | `/order` |
| A-10 | Allergieregistraties | Medisch | I: **kritiek** | `/allergy` |
| A-08 | Encounters | Medisch | C: midden | `/encounter` |
| A-11 | Global properties | Systeem | I: midden | `/systemsetting` |
| A-12 | Concept dictionary | Systeem | I: midden | `/concept` |

**Juridische context:** Verwerking van gezondheidsgegevens onder **AVG art. 9** (bijzondere categorie) en **WGBO**. Datalekken kunnen meldplicht bij AP triggeren.

---

## 4. Risicoregister

### 4.1 Dreigingen (risicomatrix)

| ID | Dreiging | Kans | Impact | Score | Niveau | Status mitigatie |
|----|----------|:----:|:------:|:-----:|--------|------------------|
| T1 | Ongeautoriseerde API-toegang | 4 | 5 | **20** | Kritiek | **Fail pentest** — cleardbcache/settings anoniem; Basic Auth werkt |
| T4 | Credential-lek in repository | 4 | 4 | **16** | Kritiek | Onbekend — SAST/Snyk; niet pentest-scope |
| T2 | Blootstelling patiëntdata | 3 | 5 | **15** | Kritiek | **Pass pentest** — anoniem patient → 401; geen bulk-extractie |
| T3 | Manipulatie orders/allergieën | 2 | 5 | **10** | Hoog | Gedeeltelijk — geen integriteitscontroles |
| T5 | Supply chain via CI/CD | 2 | 5 | **10** | Hoog | Gedeeltelijk — Snyk/CodeQL actief, geen hard gate |
| T6 | Denial of Service REST API | 3 | 3 | **9** | Midden | Afwezig — geen rate limiting |
| T7 | Privilege escalatie RBAC | 2 | 4 | **8** | Midden | Gedeeltelijk |
| T8 | Concept dictionary poisoning | 1 | 4 | **4** | Laag | Acceptabel met changemanagement |

### 4.2 Compliance-gaps (pipeline)

| Status | Aantal | Risico-effect |
|--------|--------|---------------|
| Afwezig | 14 | Geen aantoonbare controle — audit-falen |
| Gedeeltelijk | 9 | Restrisico op patiëntdata en integriteit |
| **Totaal** | **23** | Zie `02-pipeline-compliance.md` |

Kritieke afwezige controls: MFA (8.5), brute-force bescherming (8.5), rate limiting (8.6/8.20), security logging (8.15), incidentrespons (5.24–5.27).

### 4.3 Scanresultaten (geautomatiseerd)

| Scan | Tool | Frequentie | Resultaat (samenvatting) | Opvolging |
|------|------|------------|--------------------------|-----------|
| SAST | CodeQL | Elke PR + `main` | Bevindingen in GitHub Security tab | Triage verplicht |
| SAST | Snyk Code | PR + `main` | `snyk-sast.json` in artifact | SEC-006 triage |
| SCA | Snyk test | PR + `main` | CVE's in dependencies → `snyk-sca.json` | Patchen per `false-positives-beleid.md` |
| SBOM | Syft/CycloneDX | `main` | `snyk-sbom.json` + `docs/sbom.cdx.json` | Versie-inventaris 8.9 |
| DAST | Burp Suite (handmatig) | 2026-06-09 | 1 Critical, 1 High, 3 Medium | `docs/pentest/03-bevindingen.md` |

**Opmerking pipeline:** Snyk draait met `continue-on-error: true` — bevindingen worden niet automatisch geblokkeerd. Restrisico afgedekt door verplichte triage (SEC-006) en security backlog als quality gate.

### 4.4 Pentest-resultaten (2026-06-09)

**Scope:** P1 per risicomatrix — **T1** (score 20) en **T2** (score 15). Tool: Burp Repeater. Omgeving: OpenMRS RefApp lokaal Docker.

| PT-ID | Ernst | Dreiging | Aanbeveling | Besluit | SEC |
|-------|-------|----------|-------------|---------|-----|
| PT-003 | **Critical** | T1 | Fix | _fix_ | SEC-019 — anoniem `POST /cleardbcache` → 204 |
| PT-004 | **High** | T1/T5 | Fix | _fix_ | SEC-007 — anoniem `settings.form` → 200 + stack trace |
| PT-001 | Medium | T5 | Fix | _defer_ | SEC-010 — stack traces in foutresponses |
| PT-005 | Medium | T1 | Fix | _defer_ | SEC-019 — `loggedinusers` → 500 i.p.v. 401 |
| PT-002 | Medium | T1 | Defer | _defer_ | SEC-001 — verkeerd patient-endpoint geeft 400; authz OK met `?q=` |
| INFO-001 | Info | — | Accept | _accept_ | Normale redirect + security headers |

*Besluiten vastgesteld 2026-06-10 — zie `03-bevindingen.md` §8. Tester: Boyan.*

**Geslaagde P1-controles (T2):**

- Anoniem `GET /patient?q=a&limit=1` → **401**, geen `results[]`
- Anoniem `POST /password`, `POST /searchindexupdate` → **401**
- Admin Basic Auth → `authenticated: true`; patient-read met auth → 200

**Niet uitgevoerd:** OWASP ZAP (Burp volstaat), ffuf, Intruder, IDOR, RBAC nurse (geen testaccount) — buiten P1-minimum.

Zie bewijs: `docs/pentest/evidence/` + `03-bevindingen.md`.

---

## 5. Mitigatiestrategie (NEN-7510:2024-2)

### 5.1 Overzicht per control

| NEN-7510:2024-2 | Control | Huidige status | Mitigatie (backlog) | Prioriteit |
|-----------------|---------|----------------|---------------------|------------|
| **5.12** | Classificatie informatie | Gedeeltelijk | Label patiëntdata; vertrouwelijk rapportage | P1 |
| **5.18** | Toegangsrechten | Gedeeltelijk | SEC-011 RBAC-review, SEC-014 logging | P2 |
| **5.24–5.27** | Incidentbeheer | Afwezig | SEC-023 IRP documenteren | P3 |
| **5.33** | Bescherming dossiers | Gedeeltelijk | SEC-016 integriteit orders/allergieën | P2 |
| **5.34** | Privacy / PII | Gedeeltelijk | SEC-001 autorisatie op alle PHI-resources | P1 |
| **8.3** | Informatietoegangsbeperking | Gedeeltelijk | SEC-001, SEC-019 endpoint-hardening | P1 |
| **8.5** | Veilige authenticatie | Gedeeltelijk | SEC-002 MFA, SEC-003 brute-force | P1 |
| **8.6** | Capaciteitsbeheer | Afwezig | SEC-004 rate limiting, SEC-015 maxResults | P1 |
| **8.8** | Kwetsbaarhedenbeheer | Gedeeltelijk | SEC-006 Snyk-triage + patch | P1 |
| **8.9** | Configuratiebeheer | Gedeeltelijk | SEC-005 secrets, SEC-007 settings.form | P1 |
| **8.15** | Logging | Gedeeltelijk | SEC-013, SEC-020 audit trail (NEN 7513) | P2 |
| **8.16** | Bewaking | Afwezig | SEC-021 SIEM, SEC-022 anomalieën | P3 |
| **8.20** | Netwerkbeveiliging | Gedeeltelijk | SEC-009 HTTPS/HSTS, SEC-012 IP-filter | P1/P2 |
| **8.24** | Cryptografie | Gedeeltelijk | SEC-009 transport, SEC-024 at-rest | P1/P3 |
| **8.26** | Applicatie-eisen | Gedeeltelijk | SEC-007–010, SEC-019 | P1 |
| **8.28** | Veilig coderen | Gedeeltelijk | SEC-008 XML-filter; SAST opvolging | P1 |
| **8.29** | Beveiligingstesten | Gedeeltelijk | Pipeline + pentest (`docs/pentest/`) | P1 |
| **8.32** | Wijzigingsbeheer | Gedeeltelijk | SEC-017 pipeline pinning | P2 |

### 5.2 Bow-tie preventieve barrières → implementatie

| Barrière (bow-tie) | NEN-control | Backlog | Verwacht effect op T1 |
|--------------------|-------------|---------|----------------------|
| Fine-grained authorization | 8.3 | SEC-001 | Kans 4 → 2 |
| MFA/SSO gateway | 8.5 | SEC-002 | Kans 4 → 2 |
| Least privilege matrix | 5.18, 8.3 | SEC-011 | Impact beperkt |
| Rate limiting / IP-allowlist | 8.6, 8.20 | SEC-004, SEC-012 | Kans 4 → 2 |
| SAST + PR-review | 8.28, 8.29 | SEC-006, SEC-017 | Voorkomt regressie |

**Geschat restrisico na P1:** T1 score van 20 → **8** (kans 2 × impact 5) = middel (geel).

---

## 6. Behandelplan en planning

### Fase 1 — Direct (week 1–4): P1 backlog

| SEC | Actie | Eigenaar |
|-----|-------|----------|
| SEC-005, SEC-010, SEC-007, SEC-008 | Quick wins configuratie | Module-team |
| SEC-001, SEC-019 | Autorisatie-review alle resources | Module-team |
| SEC-006 | Snyk/CodeQL triage eerste artifact | Security champion |
| SEC-009 | HTTPS op test-omgeving afdwingen | Platform |
| Pentest uitgevoerd | Besluiten §8 + hertest na fix | Boyan ✓ uitgevoerd 2026-06-09; besluiten ✓ vastgesteld 2026-06-10 |

### Fase 2 — Kort termijn (maand 2–3): P2 backlog

SEC-011 t/m SEC-020 — logging, RBAC, integriteit medische data.

### Fase 3 — Middellang (maand 4–6): P3 backlog

SEC-021 t/m SEC-028 — SIEM, IRP, encryptie-at-rest, compliance-rapportage.

### Fase 4 — Pentest hertest

Na P1-fixes: herhaal Burp-hertest + handmatige IDOR-tests; werk `03-bevindingen.md` en dit rapport bij.

---

## 7. Restrisico en acceptatie

| Restrisico | Omschrijving | Acceptatie |
|------------|--------------|------------|
| Pipeline soft-fail | Snyk blokkeert niet op CVE | Geaccepteerd mits SEC-006 triage actief (`false-positives-beleid.md`) |
| Platform-afhankelijkheden | MFA, SIEM, encryptie-at-rest | Uitgesteld naar P2/P3 — eigenaar platform |
| Organisatorische controls | Vendor risk, BCP, functiescheiding | P3/P4 — management |
| Pentest Critical open | PT-003 cleardbcache anoniem | **Niet geaccepteerd voor productie** — fix verplicht (besluit: Fix) |
| Pentest High open | PT-004 settings.form anoniem | **Niet geaccepteerd voor productie** — fix verplicht (besluit: Fix) |
| Pentest Medium uitgesteld | PT-001, PT-005 stack traces / error handling | Uitgesteld (Defer) — SEC-010 (backlog P1, quick win) / SEC-019 (P2) |
| Pentest T2 positief | Geen anonieme patiëntdata | Bevestigt SEC-001 deels effectief; PT-003/004-fixes nodig voor productie |

**Productie-gate:** Geen deployment met echte patiëntdata totdat:

1. Alle P1-items op **Fix** of onderbouwde **Accept**
2. Pentest uitgevoerd; **PT-003 en PT-004 opgelost** (Critical/High)
3. `snyk-results.json` van productie-build getriaged (geen open Critical CVE's op bereikbare paden)

---

## 8. Kostenraming

Raming op basis van **€ 600/uur** (senior Java/security consultant, NL markt 2026). Bandbreedtes per fase.

### 8.1 P1 — Kritiek (verplicht vóór productie)

| Mitigatie | SEC | Uren (min–max) | Kosten (€) |
|-----------|-----|----------------|------------|
| Autorisatie-review 23+ resources | SEC-001 | 40–80 | 24.000 – 48.000 |
| Module hardening (settings, stack trace, XML) | SEC-007–010 | 8–16 | 4.800 – 9.600 |
| Secrets + pipeline review | SEC-005, SEC-017 | 8–12 | 4.800 – 7.200 |
| Snyk/CodeQL triage (eerste cyclus) | SEC-006 | 8–16 | 4.800 – 9.600 |
| Rate limiting (gateway config) | SEC-004 | 16–24 | 9.600 – 14.400 |
| Pentest uitvoering + rapportage | 8.29 | 16–24 | 9.600 – 14.400 |
| **Subtotaal P1** | | **96–172 u** | **€ 57.600 – € 103.200** |

### 8.2 P2 — Hoog (binnen 3 maanden)

| Mitigatie | SEC | Uren (min–max) | Kosten (€) |
|-----------|-----|----------------|------------|
| RBAC-review + testaccounts | SEC-011 | 16–24 | 9.600 – 14.400 |
| Security + audit logging uitbreiden | SEC-013, SEC-014, SEC-020 | 24–40 | 14.400 – 24.000 |
| Integriteit orders/allergieën | SEC-016 | 16–24 | 9.600 – 14.400 |
| IP-allowlist + HTTPS/HSTS prod | SEC-009, SEC-012 | 8–16 | 4.800 – 9.600 |
| **Subtotaal P2** | | **64–104 u** | **€ 38.400 – € 62.400** |

### 8.3 P3 — Platform / organisatie (6–12 maanden)

| Mitigatie | SEC | Uren (min–max) | Kosten (€) |
|-----------|-----|----------------|------------|
| MFA via IdP | SEC-002 | 24–40 | 14.400 – 24.000 |
| SIEM + anomaliedetectie | SEC-021, SEC-022 | 40–80 | 24.000 – 48.000 |
| IRP + compliance rapportage | SEC-023, SEC-028 | 16–24 | 9.600 – 14.400 |
| Encryptie-at-rest + backup | SEC-024, SEC-026 | 24–40 | 14.400 – 24.000 |
| **Subtotaal P3** | | **104–184 u** | **€ 62.400 – € 110.400** |

### 8.4 Totaaloverzicht

| Fase | Kosten (€) | Toelichting |
|------|------------|-------------|
| **P1 — configuratie-quick-wins** | 19.200 – 31.200 | SEC-005–010 + pentest, zonder volledige autorisatie-review |
| **P1 — volledig** | 57.600 – 103.200 | Inclusief SEC-001 autorisatie alle resources |
| **P1 + P2** | 96.000 – 165.600 | Aanbevolen voor zorg-OTAP met testdata |
| **Volledig (P1+P2+P3)** | 158.400 – 276.000 | Inclusief platform SIEM/MFA |

**Niet inbegrepen:** licenties (Snyk Team, Burp Pro, SIEM), hardware, OpenMRS hosting, juridische AVG-advieskosten.

**Kostenbesparing:** veel P1-items (SEC-007, SEC-010, SEC-008) zijn configuratie — geen grote code-wijziging; start daar om budget te spreiden.

---

## 9. Monitoring en herbeoordeling

| Activiteit | Frequentie | Bewijs |
|------------|------------|--------|
| Snyk/CodeQL scan | Elke PR + `main` | `snyk-results.json` artifact |
| Backlog-review P1-items | Wekelijks | `06-security-backlog.md` |
| Pentest hertest | Na elke major release | `03-bevindingen.md` |
| Risicoherbeoordeling | Halfjaarlijks of na incident | Dit document (nieuwe versie) |
| Risicoacceptaties review | Halfjaarlijks | `.snyk` + acceptatie-register |

---

## 10. Referenties

| Document | Pad |
|----------|-----|
| Asset-identificatie | `03-assets.md` |
| Risicomatrix | `04-risico-matrix` |
| Bow-tie analyse | `05-bowtie.md` |
| Pipeline compliance | `02-pipeline-compliance.md` |
| Security backlog | `06-security-backlog.md` |
| False positives beleid | `false-positives-beleid.md` |
| Pentest plan | `docs/pentest/01-plan.md` |
| Pentest testcases | `docs/pentest/02-testcases.md` |
| Pentest bevindingen | `docs/pentest/03-bevindingen.md` |
| SBOM | `docs/sbom.cdx.json` |
| CI Snyk workflow | `.github/workflows/snyk.yml` |

---

## 11. Goedkeuring

| Rol | Naam | Datum | Handtekening |
|-----|------|-------|--------------|
| Risk owner (module) | | | |
| Security champion | | | |
| Management | | | |

---

*Versie 1.2 — 2026-06-10. Pentest-besluiten vastgesteld (§4.4). Hertest na PT-003/004-fix.*
