# Bijlage: CRA-mapping — OpenMRS `webservices.rest`

**Document:** `docs/auditrapport/cra-mapping.md`  
**Module:** OpenMRS `webservices.rest` v3.2.0  
**Project:** ATx-2.4 Software Security & Compliance  
**Normenkader:** NEN 7510-2:2024+A1:2026 (primair) + CRA (aanvullend)  
**Status:** Concept  
**Datum:** juni 2026

---

## 1. Doel

Korte bijlage bij het auditrapport (WS06). De CRA is **aanvullend** op NEN 7510-2 — geen juridische conformiteitsverklaring. Scope: technische security van deze OpenMRS-module, niet fabrikant- of organisatieplichten.

**Statuslegenda**

| Status | Betekenis |
|---|---|
| Aanwezig | Vereiste is ingevuld en aantoonbaar met artefact |
| Deels | Basis aanwezig, maar niet volledig of nog open punten |
| Gap | Belangrijk onderdeel ontbreekt of faalt nog duidelijk |

---

## 2. CRA → NEN 7510-2

| CRA-verplichting | NEN 7510-2 control |
|---|---|
| Software zonder bekende actieve kwetsbaarheden | 8.8 Kwetsbaarhedenbeheer |
| SBOM beschikbaar stellen | 8.8 + 5.22 Monitoring leveranciers |
| Beveiligingsupdates gedurende levensduur | 8.8 Patch management |
| Secure by design | 8.25 Beveiligen tijdens ontwikkelcyclus |
| Logging en monitoring | 8.15 Logregistratie + 8.16 Bewaking |
| Toegangscontrole beheerinterfaces | 8.2 + 8.3 Toegangsbeheer |

> Meldplicht actief misbruikte kwetsbaarheden (ENISA) en responsible disclosure zijn organisatie-/fabrikantplichten — buiten scope van deze module-audit.

---

## 3. Koppeling aan dit project

| CRA-thema | Status | Redenering | Bewijs |
|---|---|---|---|
| Kwetsbaarhedenbeheer | **Deels** | Snyk/CodeQL draaien en CVE's zijn getriaged, maar 106 unieke bevindingen staan nog open en de build blokkeert niet op Critical. Detectie ≠ volledig opgelost. | SEC-006, `bijlage-dependency-updateadvies.md`, `snyk-results.json` |
| SBOM | **Aanwezig** | CycloneDX-SBOM wordt in CI gegenereerd en is gekoppeld aan de build. | `docs/sbom.cdx.json`, `.github/workflows/sbom.yml` |
| Secure by design | **Deels** | Threat model, attack surface en code review zijn uitgevoerd. Speciale endpoints (cleardbcache, settings, systemsetting) zijn na fix gehard, maar niet elk REST-pad is afzonderlijk geaudit. | `threat-model.md`, `07-security-code-review.md`, hertest PT-003/004/006 |
| Secure by default | **Deels** | **PT-004 opgelost** (2026-06-15): `settings.form` geeft anoniem 401, geen stack trace meer (`bevinding-PT-004-na.md`). **Nog open:** `enableStackTraceDetails` staat default op `true`; PT-001/PT-005/PT-007 (stack traces op andere paden) zijn uitgesteld naar SEC-010. | SEC-007 ✓, SEC-010 open — `bevinding-PT-004-na.md`, `07-security-code-review.md` SCR-003 |
| Toegangsbeheer | **Deels** | **PT-003 opgelost:** anoniem `POST /cleardbcache` → 401 (`bevinding-PT-003-na.md`). **PT-006 opgelost:** nurse `GET /systemsetting` → 403. **T2 Pass:** anoniem `GET /patient` → 401. **Nog open:** geen rate limiting/brute-force (SEC-003/004, platform); fine-grained auth niet op alle resources gevalideerd (SEC-001). | `pentestrapport-definitief.md`, `03-bevindingen.md` |
| Security testing | **Aanwezig** | SAST (CodeQL), SCA (Snyk), DAST (Burp pentest) en unit tests zijn uitgevoerd; kritieke bevindingen zijn hertest na fix. | `07-security-code-review.md`, `docs/pentest/03-bevindingen.md` |
| Logging | **Gap** | 21 van 23 kritieke eventcategorieën worden niet of onvoldoende auditwaardig gelogd. Auth-events op DEBUG; CRUD op patiëntdata ontbreekt. Zonder dit is detectie en reconstructie bij incidenten niet mogelijk. | SEC-013, SEC-020 — `09-logging-gap-analyse.md` |

Platformmaatregelen (MFA, HTTPS, rate limiting) staan in `06-security-backlog.md` maar vallen buiten modulescope.

---

## 4. Conclusie

De module dekt de CRA-thema's grotendeels via NEN 7510-controls en auditartefacten. **PT-003, PT-004 en PT-006 zijn gedicht** (hertest 2026-06-15). Resterende gaps: vulnerability-triage zonder harde gate (SEC-006), stack traces op overige paden (SEC-010) en auditlogging (SEC-013/SEC-020). Zie `00-risk-assessment.md` en `06-security-backlog.md`.

---

## Referenties

- Regulation (EU) 2024/2847 — CRA
- NEN 7510-2:2024+A1:2026
- `docs/pentest/pentestrapport-definitief.md` — actuele penteststatus
- WS06 — Audit Reporting (CRA vs NEN-7510 mapping)
