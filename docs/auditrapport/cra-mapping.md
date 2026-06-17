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

| CRA-thema | Status module | Bewijs / backlog |
|---|---|---|
| Kwetsbaarhedenbeheer | Deels | Snyk/CodeQL actief; 106 CVE's getriaged — SEC-006 (`bijlage-dependency-updateadvies.md`) |
| SBOM | Aanwezig | `docs/sbom.cdx.json`, CI-workflow |
| Secure by design | Deels | Threat model + code review; gaps op speciale endpoints |
| Secure by default | Gap | PT-004: `settings.form` + stack traces — SEC-007, SEC-010 |
| Toegangsbeheer | Deels | Pentest T2 Pass (`patient` → 401); PT-003: `cleardbcache` anoniem — SEC-019 |
| Security testing | Aanwezig | CodeQL, Snyk, Burp pentest (`docs/pentest/03-bevindingen.md`) |
| Logging | Gap | 21/23 eventcategorieën onvoldoende — SEC-013, SEC-020 (`09-logging-gap-analyse.md`) |

Platformmaatregelen (MFA, HTTPS, rate limiting) staan in `06-security-backlog.md` maar vallen buiten modulescope.

---

## 4. Conclusie

De module dekt de CRA-thema's grotendeels via bestaande NEN 7510-controls en auditartefacten. Open P1-gaps: PT-003/PT-004, vulnerability-triage (SEC-006) en auditlogging. Zie `00-risk-assessment.md` en `06-security-backlog.md` voor details.

---

## Referenties

- Regulation (EU) 2024/2847 — CRA
- NEN 7510-2:2024+A1:2026
- WS06 — Audit Reporting (CRA vs NEN-7510 mapping)
