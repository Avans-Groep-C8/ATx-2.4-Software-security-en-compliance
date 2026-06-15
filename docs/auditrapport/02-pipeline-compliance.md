# 5.2 mini Pipeline compliance

# Mini-complianceverslag NEN-7510:2024-2 Pipeline-maatregel & Bewijsoverzicht

**Document:** `docs/auditrapport/02-pipeline-compliance.md`

---

## 1. Inleiding

Dit document presenteert de resultaten van de GAP-analyse uitgevoerd op de beveiligingsmodule. Per NEN-7510:2024-2 controlgebied is weergegeven welke pipeline-maatregel aanwezig is, waar het bewijs in de git-repository te vinden is (of verwacht wordt), en wat de geconstateerde GAP of aanbeveling inhoudt.

De analyse is gebaseerd op NEN-7510:2024-2 (informatiebeveiliging in de zorg), aangevuld met NEN 7512 (vertrouwensbasis gegevensuitwisseling) en NEN 7513 (logging van toegang tot patiëntgegevens). De controlnummering volgt de 2024-editie (5.x organisatorisch, 8.x technisch), consistent met `00-risk-assessment.md` en `06-security-backlog.md`.

---

## 2. Samenvatting bevindingen

| Status | Aantal | Toelichting |
|----------|--------|-------------|
| Afwezig | 14 | Maatregel volledig afwezig; direct actie vereist |
| Gedeeltelijk | 9 | Maatregel gedeeltelijk aanwezig; aanvulling nodig |
| **Totaal** | **23** | Alle getoetste NEN-7510:2024-2 controlgebieden |

---

## 3. Controlgebieden — Status: Afwezig

De onderstaande controlgebieden zijn volledig afwezig in de module. Er is geen bewijs gevonden van een implementatie of documentatie. Directe actie is vereist.

| Control (NEN-7510:2024-2) | Controlgebied | Status | Pipeline-maatregel | Bewijs (repo) | GAP / Opmerking |
|----------|---------------|---------|--------------------|---------------|-----------------|
| 8.5 | Sterke authenticatie (MFA) | Afwezig | Geen MFA-maatregel aanwezig in de pipeline | Niet aangetroffen in `/auth/` of authenticatiemodule | Risicogebaseerde MFA benodigd (SEC-002) |
| 8.24 / NEN 7512 | Encryptie data-at-rest | Afwezig | Geen encryptie-implementatie beschreven | Niet aangetroffen in infrastructuurconfiguratie of storage | Platformmaatregel nodig (SEC-024) |
| 8.16 | Monitoring beveiligingsincidenten | Afwezig | Geen monitoringoplossing gedocumenteerd | Niet aangetroffen in `monitoring/` of ops-documentatie | Aanvullende tooling vereist (SEC-021) |
| 8.16 / 8.8 | Detectie misbruik / anomalieën | Afwezig | Geen anomaliedetectie aanwezig | Niet aangetroffen in `security/` of SIEM-configuratie | SIEM/SOC nodig (SEC-022) |
| 8.5 / 8.6 | Brute-force bescherming | Afwezig | Geen lockout- of throttlingmechanisme beschreven | Niet aangetroffen in auth/login of middleware | Lockout/rate limiting nodig (SEC-003) |
| 8.6 / 8.20 | Rate limiting | Afwezig | Geen rate-limiting configuratie aanwezig | Niet aangetroffen in API gateway of middleware | API gateway aanbevolen (SEC-004) |
| 8.15 | Security logging authenticatie | Afwezig | Geen security-specifieke loginevents gedocumenteerd | Niet aangetroffen in logging/ of auth-events | Uitgebreidere logging nodig (SEC-013) |
| 8.15 / 5.18 | Logging privilege-escalaties | Afwezig | Geen logging van rolwijzigingen of escalaties | Niet aangetroffen in rbac/ of audit-log module | Extra logging noodzakelijk (SEC-014) |
| 8.9 / 5.17 | Secrets management | Afwezig | Geen vault of secrets-beheer beschreven | Niet aangetroffen in config/secrets of `.env`-beheer | Buiten scope module — organisatorische maatregel (SEC-005) |
| 5.19–5.22 | Leveranciersbeveiliging | Afwezig | Geen vendor risk management beschreven | Niet behandeld in module | Organisatorische maatregel (SEC-027) |
| 5.29 / 5.30 | Continuïteit / beschikbaarheid | Afwezig | Geen BCP/DRP gedocumenteerd | Niet aangetroffen in infra/ of DR-plan | Buiten scope — platformverantwoordelijkheid (SEC-025) |
| 8.13 | Back-up ondersteuning | Afwezig | Geen back-upstrategie beschreven in module | Niet aangetroffen in backup/ of ops-runbook | Platformverantwoordelijkheid (SEC-026) |
| 5.24–5.27 | Incidentrespons | Afwezig | Geen IRP gedocumenteerd | Niet aangetroffen in `docs/incident-response.md` | Procesmatig vereist (SEC-023) |
| 5.36 | Compliance rapportage | Afwezig | Geen compliance-rapportageproces beschreven | Niet aangetroffen in audit-exports of dashboard | Extra tooling nodig (SEC-028) |

---

## 4. Controlgebieden — Status: Gedeeltelijk

De onderstaande controlgebieden zijn gedeeltelijk aanwezig. Een basisimplementatie is gevonden, maar voldoet niet volledig aan de NEN-7510:2024-2 vereisten of is onvoldoende aantoonbaar.

| Control (NEN-7510:2024-2) | Controlgebied | Status | Pipeline-maatregel | Bewijs (repo) | GAP / Opmerking |
|----------|---------------|---------|--------------------|---------------|-----------------|
| 8.5 / NEN 7512 | Authenticatie | Gedeeltelijk | Basic Auth en sessietokens aanwezig; MFA ontbreekt | Aangetroffen in auth-module; MFA niet gedocumenteerd | MFA niet standaard aanwezig (SEC-002) |
| 5.18 / 8.3 | Least privilege | Gedeeltelijk | Privilege-model aanwezig; afdwinging niet aantoonbaar | Aangetroffen in rbac/ of roles/; inrichting onbekend | Afhankelijk van inrichting (SEC-011) |
| 8.24 / NEN 7512 / NEN 7513 | Encryptie tijdens transport | Gedeeltelijk | HTTPS beschreven; HSTS/redirect niet aantoonbaar | Beschreven in config/ of API-documentatie | Niet aantoonbaar afgedwongen (SEC-009) |
| 8.15 / NEN 7513 | Logging transacties | Gedeeltelijk | AuditInfo aanwezig; volledige security logging ontbreekt | AuditInfo gevonden in module; dekking onvolledig | Geen volledige security logging (SEC-013) |
| 8.15 / NEN 7513 | Audit trail zorggegevens | Gedeeltelijk | Creator/changer/timestamps aanwezig; volledigheid niet aantoonbaar | Aangetroffen in datamodel of audit/ | Dekking niet volledig aantoonbaar (SEC-020) |
| 5.33 / 8.3 | Integriteit van gegevens | Gedeeltelijk | RBAC en API-operaties aanwezig; checksums/signing ontbreken | Aangetroffen in rbac/; expliciete controles ontbreken | Geen expliciete integriteitscontroles (SEC-016) |
| 8.26 / 8.20 | API security hardening | Gedeeltelijk | IP filtering aanwezig; OAuth2/PKCE/input-validatie ontbreekt | IP filtering in API gateway of config/ | Moderne controls ontbreken (SEC-012) |
| 5.3 / 5.18 | Functiescheiding | Gedeeltelijk | Rollen aanwezig; organisatorische scheiding niet aantoonbaar | Aangetroffen in rbac/ of roles/ | Organisatorische implementatie vereist (SEC-031) |
| 8.5 / 5.17 | Wachtwoordbeheer | Gedeeltelijk | Wachtwoord wijzigen mogelijk; policy-afdwinging onbekend | Aangetroffen in auth/ of gebruikersbeheer | Policy enforcement onbekend (SEC-018) |

---

## 5. Vervolgstappen

1. Prioriteer de 14 afwezige controls op basis van risiconiveau (MFA, brute-force bescherming en encryptie data-at-rest hebben hoogste prioriteit).
2. Stel een roadmap op voor implementatie van ontbrekende technische maatregelen binnen de pipeline.
3. Beleg organisatorische maatregelen (leveranciersbeveiliging, incidentrespons, continuïteit) buiten de module.
4. Hertoets de gedeeltelijke controls na aanvullende implementatie om volledige NEN-7510:2024-2 compliance aan te tonen.