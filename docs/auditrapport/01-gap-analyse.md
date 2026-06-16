# 5.5 Gap-analyse - OpenMRS webservices.rest (NEN-7510:2024-2)

**OpenMRS REST Web Services Module - toetsing van de applicatie zelf**

| Onderdeel | Waarde |
|---|---|
| **Onderwerp / scope** | OpenMRS module `webservices.rest` (REST API-laag, applicatiecode). Alleen maatregelen die in de broncode van de applicatie aantoonbaar zijn vallen binnen scope. Organisatie- en platformmaatregelen zijn gemarkeerd als *Niet van toepassing (modulescope)*. |
| **Module-versie** | 3.2.0 (root-`pom.xml` van de aangeleverde module - zie [module-keuze.md](../module-keuze.md)) |
| **Norm** | NEN-7510:2024-2 (Beheersmaatregelen, hoofdstukken 5 t/m 8). **Uitsluitend deze norm** is als toetsingskader gebruikt. |
| **Afbakening** | Toetsing van **de applicatie zelf** (broncode van de module). CI/CD-pipeline en versiebeheer vallen **buiten deze gap-analyse** en worden elders behandeld (WS02 - beveiligde pipeline). |
| **Datum** | 5 juni 2026 |
| **Methode** | Bewijsgericht (audit-mindset WS01): per control is in de broncode van de applicatie gezocht. Bewijs = volledig bestandspad:regel. *Niet gevonden* is gedocumenteerd als negatief bewijs, niet als aanname. |
| **Doel** | Verschil bepalen tussen huidige situatie (OpenMRS-applicatie) en gewenste situatie (NEN-7510-2-eis), met risico bij falen en concreet actieplan. Input voor het auditrapport (WS06). |

## Scope-afbakening opdracht 5.5

- **Primaire scope (opdracht 5.5):** `8.3` Toegangsbeperking tot informatie · `8.5` Beveiligde authenticatie · `8.15` Logging
- **Aanvullende observaties:** `8.24` Gebruik van cryptografie (transport) · `8.26`/`8.20` Beveiligingseisen applicaties (API-hardening)
- **Buiten modulescope:** `8.13`, `8.14`/`5.29`, `8.16`, `5.19`/`5.22`, `5.24`-`5.27`, `5.31`/`5.35` (organisatie-/platformcontrols, belegd in ISMS/infra)

## Legenda status

| Status | Betekenis |
|---|---|
| ✅ **Aanwezig** | Maatregel aantoonbaar volledig geïmplementeerd in de applicatie. |
| 🟡 **Gedeeltelijk** | Fundament aanwezig, maar onvolledig t.o.v. de NEN-7510-2-eis. |
| 🔴 **Afwezig** | Geen implementatie aangetroffen terwijl de control binnen modulescope hoort. |
| ⬜ **Niet van toepassing (modulescope)** | Organisatie-/platformmaatregel; niet afdwingbaar in deze applicatiecode. Belegd bij ISMS/infrastructuur. |

> **Noot:** controlnummering volgt NEN-7510:2024-2 (hoofdstukken 5 t/m 8). Er is geen ander normkader gebruikt.

---

## Primaire scope (opdracht 5.5)

### 8.3 - Toegangsbeperking tot informatie
*Gerelateerde NEN-7510-2 controls: 5.15, 5.18 · Categorie: Technisch*

| Veld | Inhoud |
|---|---|
| **Eis (gewenste situatie)** | Toegang tot informatie beperkt op rol en behandelrelatie; granulaire autorisatie; "break-the-glass". |
| **Huidige situatie** | OpenMRS kent een privilege-/rolmodel dat in de module wordt afgedwongen via privilege-checks; ongeautoriseerde calls geven `APIAuthenticationException`. Autorisatie is rol-gebaseerd, **niet gebonden aan behandelrelatie**; break-the-glass ontbreekt in de module. |
| **Bewijs** | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/RestHelperService.java:35` (`@Authorized("View Patients")`); `omod-common/src/main/java/org/openmrs/module/webservices/helper/ModuleFactoryWrapper.java:160-161` (`Context.hasPrivilege` + `APIAuthenticationException`); `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/RestUtil.java:73/225/849` (proxy-privileges). |
| **Status** | 🟡 Gedeeltelijk |
| **Risico als de control faalt** | Te ruime toegang: zorgverlener kan dossiers inzien buiten zijn behandelrelatie. Schending van het need-to-know-beginsel (NEN-7510-2 8.3). |
| **Risicoclassificatie** | Midden |
| **Aanbevolen actie** | Autorisatie koppelen aan behandelrelatie (EPD/agenda). Privileges per veld/resource fijnmaziger inrichten; "break-the-glass" met extra logging implementeren. |

### 8.5 - Beveiligde authenticatie
*Gerelateerde NEN-7510-2 controls: 5.17 · Categorie: Technisch*

| Veld | Inhoud |
|---|---|
| **Eis (gewenste situatie)** | Sterke authenticatie: meerdere factoren (weet/heeft/bent), brute-force-bescherming, sessietimeout, geen gedeelde accounts. |
| **Huidige situatie** | Authenticatie via HTTP Basic auth (gebruiker:wachtwoord, base64 bij elke request). Sessietimeout wordt afgehandeld (401 bij verlopen sessie). MFA is voor een stateless REST-API geen passende maatregel; Basic auth is volgens OWASP API Security echter een zwakke keuze t.o.v. token-gebaseerde authenticatie. Brute-force-bescherming (lockout/rate limiting) ontbreekt. |
| **Bewijs** | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java:85-117` (Basic auth); idem `:80-83` (sessietimeout 401). Geen throttling-code in `omod*/src/main` aangetroffen. |
| **Status** | 🟡 Gedeeltelijk |
| **Risico als de control faalt** | Credentials worden bij elke call meegestuurd; accountovername via onderschepte/gestolen wachtwoorden geeft toegang tot patiëntdossiers (PHI). |
| **Risicoclassificatie** | **Hoog** |
| **Aanbevolen actie** | Vervang Basic auth door **token-gebaseerde authenticatie (OAuth2 / bearer tokens)**, zodat credentials niet bij elke request worden meegestuurd - dit is de passende richting voor een REST-API (MFA is hier niet van toepassing). Brute-force-bescherming (rate limiting) op de gateway. Zie OWASP API Security Top 10 (2023) - zie Referenties. |

### 8.15 - Logging
*Gerelateerde NEN-7510-2 controls: 8.17 · Categorie: Technisch*

| Veld | Inhoud |
|---|---|
| **Eis (gewenste situatie)** | Vastleggen wie/wat/waar/wanneer per handeling; centraal, immutable/append-only, met bewaartermijn. "Niet gelogd = niet gebeurd". |
| **Huidige situatie** | De module logt authenticatie alleen op **DEBUG-niveau** (niet auditwaardig). Wel wordt audit-metadata (`creator`, `dateCreated`, `changedBy`, `dateChanged`) per resource ontsloten. Geen persistente, onveranderbare audit trail in de module; afhankelijk van core/auditlog-module. |
| **Bewijs** | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java:108/113` (`log.debug` bij auth); `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/resource/impl/BaseDelegatingResource.java` (`auditInfo`: creator/dateChanged). Geen append-only audit trail in de applicatie. |
| **Status** | 🟡 Gedeeltelijk |
| **Risico als de control faalt** | Acties (incl. inzage van dossiers) zijn achteraf niet herleidbaar; datalekken niet aantoonbaar; bij audit niet te bewijzen wie wat deed. Strijdig met de logging-eis van NEN-7510-2 8.15. |
| **Risicoclassificatie** | **Hoog** |
| **Aanbevolen actie** | Audit logging op API-laag activeren (actor/patiënt/tijdstip/actie) op INFO-niveau, append-only naar centrale logvoorziening met bewaartermijn. Auth-events van DEBUG naar een apart audit-kanaal tillen. |

---

## Aanvullende observaties

### 8.24 - Gebruik van cryptografie (transport)
*Categorie: Technisch*

| Veld | Inhoud |
|---|---|
| **Eis (gewenste situatie)** | Versleuteling van gevoelige gegevens, inclusief transport; sleutelbeheer. |
| **Huidige situatie** | Betreft hier **transportbeveiliging**: Basic-auth-credentials worden enkel base64-gecodeerd (geen versleuteling). Vertrouwelijkheid hangt af van TLS, dat door infra/reverse proxy moet worden afgedwongen. De applicatie controleert momenteel **niet** of het publieke endpoint daadwerkelijk via HTTPS wordt benaderd. Data-at-rest-encryptie valt buiten modulescope (platform). |
| **Bewijs** | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java:99` (Base64-decode credentials). Geen TLS-/HTTPS-controle in de applicatie. |
| **Status** | 🟡 Gedeeltelijk |
| **Risico als de control faalt** | Bij ontbreken van TLS zijn credentials en PHI af te luisteren (man-in-the-middle). |
| **Risicoclassificatie** | **Hoog** |
| **Aanbevolen actie** | TLS-terminatie op reverse proxy (HSTS/redirect) blijft infra. De applicatie kan zelf **wél verifiëren** dat het publieke endpoint TLS gebruikt: inspecteer de header **`X-Forwarded-Proto`** (gezet door de reverse proxy) en weiger het verzoek wanneer die niet `https` is. Data-at-rest-encryptie beleggen op platformniveau. |

### 8.26 - Beveiligingseisen voor applicaties
*Gerelateerde NEN-7510-2 controls: 8.20, 8.23 · Categorie: Technisch*

| Veld | Inhoud |
|---|---|
| **Eis (gewenste situatie)** | Beveiligingseisen voor applicatiediensten; bescherming van netwerk(diensten); o.a. rate limiting en toegangsrestricties. |
| **Huidige situatie** | De applicatie ondersteunt een IP-allowlist (global property `webservices.rest.allowedips`); calls van niet-toegestane IP's krijgen 403. Geen rate limiting/throttling en geen moderne API-controls (CORS-policy, scopes). |
| **Bewijs** | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java:69-74` (403 bij IP); `.../RestUtil.java:133` (`isIpAllowed`); `.../RestConstants.java:66` (`allowedips`). |
| **Status** | 🟡 Gedeeltelijk |
| **Risico als de control faalt** | API kwetsbaar voor brute-force, scraping en (D)DoS; bij gestolen credentials ongelimiteerde toegang tot endpoints. |
| **Risicoclassificatie** | Midden |
| **Aanbevolen actie** | Rate limiting/throttling via API-gateway of middleware. IP-allowlist combineren met TLS-terminatie en een strakke CORS-policy. |

---

## Buiten modulescope (organisatie-/platformcontrols)

| Control | Eis | Waarom buiten modulescope | Risico bij falen | Aanbevolen actie |
|---|---|---|---|---|
| **8.13** Back-up | Reservekopieën + getest herstel | Applicatie bevat geen back-uplogica | Dataverlies, geen herstel na incident | ISMS/infra: DBA-back-upbeleid + hersteltest |
| **8.14** Redundantie / continuïteit *(5.29)* | IB tijdens verstoring; redundantie | Geen DR/HA-logica in scope | Uitval zorgsysteem zonder uitwijk | Infra/SLA + BCP/DR-plan |
| **8.16** Monitoring & anomaliedetectie | Monitoren op afwijkend gedrag; SIEM/SOC | Detectie/SIEM is operationele voorziening (applicatie levert wel logbron - zie 8.15) | Misbruik/aanvallen niet tijdig opgemerkt | SIEM/SOC + applicatie-audit logs aankoppelen |
| **5.19** Leveranciersbeveiliging *(5.22)* | IB in leveranciersrelaties | Organisatorische/inkoopmaatregel | Risico's via derden onbeheerst | Inkoop-/third-party-risk-proces (ISMS) |
| **5.24** Incidentbeheer *(5.25-5.27)* | Respons en lering uit IB-incidenten | Procesmatige maatregel (ISMS/runbooks) | Incidenten ad hoc, niet beheerst | Incidentresponsproces + meldprocedure in ISMS |
| **5.31** Wettelijke eisen & onafhankelijke beoordeling *(5.35)* | Voldoen aan wettelijke eisen; periodieke review | Compliance-rapportage/audit is organisatieproces | Non-compliance pas zichtbaar bij externe audit | Periodieke interne audit + dit gap-/auditrapport als bewijslast (IGJ/certificering) |

---

## Samenvatting

| Status | Aantal | Toelichting |
|---|---|---|
| ✅ Aanwezig | 0 | Binnen strikte applicatiescope is geen control volledig compleet. |
| 🟡 Gedeeltelijk | 5 | Fundament aanwezig (auth, RBAC, logging-metadata, IP-filter) maar onvolledig t.o.v. NEN-7510-2-eis. |
| 🔴 Afwezig | 0 | Geen technische controls binnen applicatiescope volledig afwezig bevonden. |
| ⬜ Niet van toepassing (modulescope) | 6 | Organisatie-/platformcontrols; te beleggen in ISMS/infrastructuur. |
| **Totaal beoordeeld** | **11** | |

### Primaire scope opdracht 5.5 (8.3 / 8.5 / 8.15)

- **8.3** Toegangsbeperking → 🟡 Gedeeltelijk (RBAC aanwezig, niet gebonden aan behandelrelatie).
- **8.5** Beveiligde authenticatie → 🟡 Gedeeltelijk (eenfactor Basic auth; MFA hoort op platform/SSO).
- **8.15** Logging → 🟡 Gedeeltelijk (audit-metadata aanwezig; geen append-only audit trail in de applicatie).

### Belangrijkste bevindingen (prioriteit)

- 🔴 **HOOG - 8.5:** zwakke Basic auth; overstappen op token-gebaseerde auth (OAuth2) i.p.v. Basic auth (MFA is n.v.t. voor een REST-API). Zie OWASP API Security.
- 🔴 **HOOG - 8.15:** geen audit trail op API-laag (auth alleen op DEBUG); acties niet herleidbaar.
- 🔴 **HOOG - 8.24:** vertrouwelijkheid leunt op niet-afgedwongen TLS; de module kan TLS verifiëren via de header `X-Forwarded-Proto`.
- 🟡 **MIDDEN - 8.3:** RBAC aanwezig maar niet gebonden aan behandelrelatie (need-to-know te ruim).
- 🟡 **MIDDEN - 8.26/8.20:** IP-allowlist aanwezig, maar geen rate limiting.

## Referenties

- **NEN-7510:2024-2** - Beheersmaatregelen (toetsingskader van deze gap-analyse).
- **OWASP API Security Top 10 (2023)** - geraadpleegd als best-practice bij de aanbevelingen voor 8.5 (authenticatie) en 8.26 (API-hardening): <https://owasp.org/API-Security/editions/2023/en/0x00-header/>
