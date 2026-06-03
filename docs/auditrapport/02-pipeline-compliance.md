# 5.2 mini Pipeline compliance

# Mini-complianceverslag NEN 7510-2 Pipeline-maatregel & Bewijsoverzicht

**Document:** `docs/auditrapport/02-pipeline-compliance.md`

---

## 1. Inleiding

Dit document presenteert de resultaten van de GAP-analyse uitgevoerd op de beveiligingsmodule. Per NEN 7510-2 controlgebied is weergegeven welke pipeline-maatregel aanwezig is, waar het bewijs in de git-repository te vinden is (of verwacht wordt), en wat de geconstateerde GAP of aanbeveling inhoudt.

De analyse is gebaseerd op NEN 7510-2 (informatiebeveiliging in de zorg), aangevuld met NEN 7512 (vertrouwensbasis gegevensuitwisseling) en NEN 7513 (logging van toegang tot patiëntgegevens).

---

## 2. Samenvatting bevindingen

| Status | Aantal | Toelichting |
|----------|--------|-------------|
| Afwezig | 14 | Maatregel volledig afwezig; direct actie vereist |
| Gedeeltelijk | 9 | Maatregel gedeeltelijk aanwezig; aanvulling nodig |
| **Totaal** | **23** | Alle getoetste NEN 7510-2 controlgebieden |

---

## 3. Controlgebieden — Status: Afwezig

De onderstaande controlgebieden zijn volledig afwezig in de module. Er is geen bewijs gevonden van een implementatie of documentatie. Directe actie is vereist.

| Control | Controlgebied | Status | Pipeline-maatregel | Bewijs (repo) | GAP / Opmerking |
|----------|---------------|---------|--------------------|---------------|-----------------|
| NEN 7510-2 A.9.4.2 | Sterke authenticatie (MFA) | Afwezig | Geen MFA-maatregel aanwezig in de pipeline | Niet aangetroffen in `/auth/` of authenticatiemodule | Risicogebaseerde MFA benodigd |
| NEN 7510-2 A.10.1.1 / NEN 7512 | Encryptie data-at-rest | Afwezig | Geen encryptie-implementatie beschreven | Niet aangetroffen in infrastructuurconfiguratie of storage | Platformmaatregel nodig |
| NEN 7510-2 A.12.4.1 / A.16.1 | Monitoring beveiligingsincidenten | Afwezig | Geen monitoringoplossing gedocumenteerd | Niet aangetroffen in `monitoring/` of ops-documentatie | Aanvullende tooling vereist |
| NEN 7510-2 A.12.4.1 / A.12.6.1 | Detectie misbruik / anomalieën | Afwezig | Geen anomaliedetectie aanwezig | Niet aangetroffen in `security/` of SIEM-configuratie | SIEM/SOC nodig |
| NEN 7510-2 A.9.4.2 / A.9.4.3 | Brute-force bescherming | Afwezig | Geen lockout- of throttlingmechanisme beschreven | Niet aangetroffen in auth/login of middleware | Lockout/rate limiting nodig |
| NEN 7510-2 A.13.1.1 / A.12.6.1 | Rate limiting | Afwezig | Geen rate-limiting configuratie aanwezig | Niet aangetroffen in API gateway of middleware | API gateway aanbevolen |
| NEN 7510-2 A.12.4.1 / A.12.4.3 | Security logging authenticatie | Afwezig | Geen security-specifieke loginevents gedocumenteerd | Niet aangetroffen in logging/ of auth-events | Uitgebreidere logging nodig |
| NEN 7510-2 A.12.4.1 / A.9.2.3 | Logging privilege-escalaties | Afwezig | Geen logging van rolwijzigingen of escalaties | Niet aangetroffen in rbac/ of audit-log module | Extra logging noodzakelijk |
| NEN 7510-2 A.10.1.2 / A.9.4.3 | Secrets management | Afwezig | Geen vault of secrets-beheer beschreven | Niet aangetroffen in config/secrets of `.env`-beheer | Buiten scope module — organisatorische maatregel |
| NEN 7510-2 A.15.1.1 / A.15.2.1 | Leveranciersbeveiliging | Afwezig | Geen vendor risk management beschreven | Niet behandeld in module | Organisatorische maatregel |
| NEN 7510-2 A.17.1.1 / A.17.2.1 | Continuïteit / beschikbaarheid | Afwezig | Geen BCP/DRP gedocumenteerd | Niet aangetroffen in infra/ of DR-plan | Buiten scope — platformverantwoordelijkheid |
| NEN 7510-2 A.12.3.1 | Back-up ondersteuning | Afwezig | Geen back-upstrategie beschreven in module | Niet aangetroffen in backup/ of ops-runbook | Platformverantwoordelijkheid |
| NEN 7510-2 A.16.1.1 t/m A.16.1.7 | Incidentrespons | Afwezig | Geen IRP gedocumenteerd | Niet aangetroffen in `docs/incident-response.md` | Procesmatig vereist |
| NEN 7510-2 A.18.1.1 / A.18.2.1 | Compliance rapportage | Afwezig | Geen compliance-rapportageproces beschreven | Niet aangetroffen in audit-exports of dashboard | Extra tooling nodig |

---

## 4. Controlgebieden — Status: Gedeeltelijk

De onderstaande controlgebieden zijn gedeeltelijk aanwezig. Een basisimplementatie is gevonden, maar voldoet niet volledig aan de NEN 7510-2 vereisten of is onvoldoende aantoonbaar.

| Control | Controlgebied | Status | Pipeline-maatregel | Bewijs (repo) | GAP / Opmerking |
|----------|---------------|---------|--------------------|---------------|-----------------|
| NEN 7510-2 A.9.4.2 / NEN 7512 | Authenticatie | Gedeeltelijk | Basic Auth en sessietokens aanwezig; MFA ontbreekt | Aangetroffen in auth-module; MFA niet gedocumenteerd | MFA niet standaard aanwezig |
| NEN 7510-2 A.9.1.2 / A.9.2.3 | Least privilege | Gedeeltelijk | Privilege-model aanwezig; afdwinging niet aantoonbaar | Aangetroffen in rbac/ of roles/; inrichting onbekend | Afhankelijk van inrichting |
| NEN 7510-2 A.10.1.1 / NEN 7512 / NEN 7513 | Encryptie tijdens transport | Gedeeltelijk | HTTPS beschreven; HSTS/redirect niet aantoonbaar | Beschreven in config/ of API-documentatie | Niet aantoonbaar afgedwongen |
| NEN 7510-2 A.12.4.1 / NEN 7513 | Logging transacties | Gedeeltelijk | AuditInfo aanwezig; volledige security logging ontbreekt | AuditInfo gevonden in module; dekking onvolledig | Geen volledige security logging |
| NEN 7513 / NEN 7510-2 A.12.4.1 | Audit trail zorggegevens | Gedeeltelijk | Creator/changer/timestamps aanwezig; volledigheid niet aantoonbaar | Aangetroffen in datamodel of audit/ | Dekking niet volledig aantoonbaar |
| NEN 7510-2 A.12.2.1 / A.14.2.5 | Integriteit van gegevens | Gedeeltelijk | RBAC en API-operaties aanwezig; checksums/signing ontbreken | Aangetroffen in rbac/; expliciete controles ontbreken | Geen expliciete integriteitscontroles |
| NEN 7510-2 A.14.1.2 / A.13.1.1 | API security hardening | Gedeeltelijk | IP filtering aanwezig; OAuth2/PKCE/input-validatie ontbreekt | IP filtering in API gateway of config/ | Moderne controls ontbreken |
| NEN 7510-2 A.6.1.2 / A.9.2.3 | Functiescheiding | Gedeeltelijk | Rollen aanwezig; organisatorische scheiding niet aantoonbaar | Aangetroffen in rbac/ of roles/ | Organisatorische implementatie vereist |
| NEN 7510-2 A.9.4.3 / A.9.3.1 | Wachtwoordbeheer | Gedeeltelijk | Wachtwoord wijzigen mogelijk; policy-afdwinging onbekend | Aangetroffen in auth/ of gebruikersbeheer | Policy enforcement onbekend |

---

## 5. Vervolgstappen

1. Prioriteer de 14 afwezige controls op basis van risiconiveau (MFA, brute-force bescherming en encryptie data-at-rest hebben hoogste prioriteit).
2. Stel een roadmap op voor implementatie van ontbrekende technische maatregelen binnen de pipeline.
3. Beleg organisatorische maatregelen (leveranciersbeveiliging, incidentrespons, continuïteit) buiten de module.
4. Hertoets de gedeeltelijke controls na aanvullende implementatie om volledige NEN 7510-2 compliance aan te tonen.