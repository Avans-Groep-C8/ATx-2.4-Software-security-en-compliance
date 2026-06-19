# Bijlage: Bronnen

**Document:** `docs/auditrapport/bijlage-bronnen.md`  
**Project:** ATx-2.4 Software Security & Compliance  
**Datum:** 2026-06-19

---

## 1. Cursusmateriaal (workshopslides)

| Nr. | Titel | Bestand |
|---|---|---|
| WS00 | Kick-off LU 2 projectintroductie en leerdoelen | ICT-I2.4 Security WS00 - Kick-off LU 2.pdf |
| WS02 | Hardening Dev Pipeline CI/CD-beveiliging, branch protection, secret scanning | ICT-I2.4 Security WS02 - Hardening Dev Pipeline.pdf |
| WS03 | Healthcare Risk Assessment risicobeoordeling in de zorgsector, BIV/CIA, threat modeling | ICT-I2.4 Security WS03 - Healthcare Risk Assessment.pdf |
| WS04A | Compliance Scanning SAST, SCA, SBOM, NEN 7510, CRA | ICT-I2.4 Security WS04A - Compliance Scanning.pdf |
| WS04B | Testing en Pentesting DAST, Burp Suite, OWASP Testing Guide | ICT-I2.4 Security WS04B - Testing en Pentesting.pdf |
| WS05 | Secure Coding & Privacy by Design OWASP Top 10, privacy-principes, veilig ontwikkelen | ICT-I2.4 Security WS05 - Secure Coding, Privacy by Design.pdf |
| WS06 | Audit Reporting auditrapportage, structuur, oplevering en digitale handtekening | ICT-I2.4 Security WS06 - Audit Reporting.pdf |

---

## 2. Normen en standaarden

| Norm | Titel | Toepassing in dit project |
|---|---|---|
| NEN 7510-2:2024 | Informatiebeveiliging in de zorg Deel 2: Controls | Normenkader voor de volledige audit; controls gekoppeld via gap-analyse en CRA-mapping |
| CRA (EU) 2024/2847 | Cyber Resilience Act | Gekoppeld via [docs/auditrapport/cra-mapping.md](cra-mapping.md) |
| OWASP Top 10 (2021) | Top tien meest kritieke webapplicatiekwetsbaarheden | Gebruikt als referentie bij pentest en code review |
| OWASP Testing Guide v4.2 | Handleiding voor security testing | Basis voor pentestmethodologie |
| CycloneDX 1.6 | SBOM-standaard voor software-inventarisatie | Formaat voor [docs/auditrapport/bijlage-sbom.cdx.json](bijlage-sbom.cdx.json) |
| CVSS v3.1 | Common Vulnerability Scoring System | Scoringsschaal voor kwetsbaarheden in risicomatrix en SCA |

---

## 3. Gebruikte tools

| Tool | Versie / bron | Doel |
|---|---|---|
| GitHub CodeQL | GitHub Advanced Security | SAST statische code-analyse; output in [docs/SAST-Output/](../SAST-Output/) |
| Snyk Code | Snyk platform | SAST aanvullende statische analyse |
| Snyk test (SCA) | Snyk platform | SCA dependency-kwetsbaarheidsscan; rapport in [docs/snyk-rapport.json](../snyk-rapport.json) |
| Anchore Syft 1.42.3 | `anchore/sbom-action` via GitHub Actions | SBOM-generatie in CycloneDX JSON-formaat |
| Anchore Grype | `anchore/scan-action` via GitHub Actions | SCA SBOM-kwetsbaarheidsscan; SARIF naar GitHub Code Scanning |
| Burp Suite Community | PortSwigger | DAST / pentest handmatige interceptie en replay van HTTP-verkeer |
| OWASP ZAP | OWASP | DAST automatische webscanner (referentie; niet volledig ingezet) |
| Docker / Docker Compose | Docker Inc. | Lokale testomgeving voor OpenMRS |

---

## 4. CVE-referenties

De onderstaande CVE's zijn representatief voor de zwaarste bevindingen uit de Snyk SCA-scan. De volledige lijst staat in [docs/auditrapport/bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md) en [docs/snyk-rapport.json](../snyk-rapport.json).

| CVE | Component | CVSS | Ernst |
|---|---|---|---|
| CVE-2023-44487 | Netty (HTTP/2 Rapid Reset) | 7.5 | Hoog |
| CVE-2022-42003 | Jackson Databind | 7.5 | Hoog |
| CVE-2022-42004 | Jackson Databind | 7.5 | Hoog |
| CVE-2022-45688 | SnakeYAML | 6.5 | Middel |
| CVE-2023-34462 | Netty | 6.5 | Middel |
| CVE-2022-41854 | SnakeYAML | 6.5 | Middel |

Zie voor de volledige CVE-prioritering de kolommen "Golf 1 / Golf 2 / Golf 3" in [bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md).

---

## 5. Projectinterne documenten

| Document | Beschrijving |
|---|---|
| [docs/auditrapport/00-auditrapport.md](00-auditrapport.md) | Hoofdauditrapport |
| [docs/auditrapport/00-risk-assessment.md](00-risk-assessment.md) | Overkoepelende risicoanalyse |
| [docs/auditrapport/01-gap-analyse.md](01-gap-analyse.md) | NEN 7510-2 gap-analyse (tekstueel) |
| [docs/auditrapport/gap-analyse-nen7510.xlsx](gap-analyse-nen7510.xlsx) | NEN 7510-2 gap-analyse (spreadsheet) |
| [docs/auditrapport/bijlage-traceability.md](bijlage-traceability.md) | Traceability matrix control → bewijs |
| [docs/auditrapport/cra-mapping.md](cra-mapping.md) | CRA-artikelmapping |
| [docs/auditrapport/04-risico-matrix.md](04-risico-matrix.md) | Risicomatrix |
| [docs/auditrapport/05-bowtie.md](05-bowtie.md) | Bow-tie diagrammen |
| [docs/auditrapport/06-security-backlog.md](06-security-backlog.md) | Security backlog |
| [docs/auditrapport/08-security-code-review.md](08-security-code-review.md) | SAST-bevindingen en code review |
| [docs/pentest/pentestrapport-definitief.md](../pentest/pentestrapport-definitief.md) | Definitief pentestrapport |
| [docs/auditrapport/bijlage-dependency-updateadvies.md](bijlage-dependency-updateadvies.md) | SCA-bevindingen en patchadvies |
