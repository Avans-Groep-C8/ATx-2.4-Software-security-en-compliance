# 9. Logging Gap-analyse — OpenMRS REST Web Services Module

**Module:** `webservices.rest` v3.2.0  
**Norm:** NEN-7510 A.8.15 — Logging van gebeurtenissen  
**Auteur:** Auditteam C8  
**Datum:** juni 2026

---

## 9.1 Inleiding

NEN-7510 A.8.15 vereist dat zorginformatiesystemen gebeurtenissen loggen die nodig zijn voor detectie van beveiligingsincidenten, forensisch onderzoek en aantoonbaarheid van compliance. De norm schrijft voor dat logs minimaal bevatten: gebruikersidentiteit, type gebeurtenis, tijdstip, betrokken object/data, IP-adres van herkomst en resultaat (succes/mislukking).

Dit hoofdstuk analyseert welke events de `webservices.rest`-module feitelijk logt, koppelt dit aan het eerder vastgestelde aanvalsoppervlak (zie hoofdstuk 3), en bepaalt de gap ten opzichte van de normvereisten.

---

## 9.2 Overzicht van aanwezige logging

### 9.2.1 Gebruikte logging-frameworks

De module gebruikt twee frameworks naast elkaar:

| Framework | Locatie | Niveau |
|---|---|---|
| SLF4J (`org.slf4j.Logger`) | `AuthorizationFilter`, `SessionController1_9` | `debug`, `warn` |
| Apache Commons Logging (`org.apache.commons.logging.Log`) | `BaseRestController`, `BaseDelegatingResource` | `error`, `info` |

Het gebruik van twee frameworks is inconsistent en vergroot de kans op configuratiefouten waarbij niet alle logs naar dezelfde appender worden gestuurd.

### 9.2.2 Inventarisatie van gelogde events per bestand

#### `AuthorizationFilter.java`

| Logstatement | Level | Inhoud |
|---|---|---|
| `log.debug("Initializing REST WS Authorization filter")` | DEBUG | Initialisatie filter |
| `log.debug("Destroying REST WS Authorization filter")` | DEBUG | Afsluiten filter |
| `log.debug("authenticated [{}]", userAndPass[0])` | DEBUG | Geslaagde login (Basic Auth) — gebruikersnaam gelogd |
| `log.debug("authentication exception ", ex)` | DEBUG | Mislukte login — géén gebruikersnaam, wél stack trace |

Kritieke bevinding: succesvolle én mislukte authenticatiepogingen worden alleen op `DEBUG`-niveau gelogd. In productieomgevingen staat `DEBUG` standaard uit. Dit betekent dat in de praktijk géén authenticatiegebeurtenissen worden gelogd.

#### `BaseRestController.java`

| Logstatement | Level | Inhoud |
|---|---|---|
| `log.error(ex.getMessage(), ex)` | ERROR | HTTP 5xx server errors |
| `log.info(ex.getMessage(), ex)` | INFO | HTTP 4xx client errors (inclusief 401/403) |

Autorisatiefouten (403 Forbidden) en ongeautoriseerde verzoeken (401 Unauthorized) worden als `INFO` gelogd zonder gebruikerscontext of endpoint-informatie.

#### `SessionController1_9.java`

| Logstatement | Level | Inhoud |
|---|---|---|
| `log.warn("Can't handle users with multiple provider accounts")` | WARN | Interne waarschuwing bij meerdere providers |

De `DELETE /session` (uitloggen) en `POST /session` (inloggen via session endpoint) bevatten **geen** logstatements.

Het `/session/diag`-endpoint — dat zonder autorisatiecheck username, rollen én privileges terugstuurt — bevat **geen** logging.

#### `Activator.java`

| Logstatement | Level | Inhoud |
|---|---|---|
| `log.info("Started the REST Web Service module")` | INFO | Module gestart |
| `log.info("Stopped the REST Web Service module")` | INFO | Module gestopt |
| `log.info("Clearing caches...")` | INFO | Cache leeggemaakt |

#### `MainResourceController.java` / `MainSubResourceController.java`

**Geen enkel logstatement aanwezig.** Alle CRUD-operaties op patiëntdata, observaties, medicatie en overige resources verlopen zonder enige logging.

---

## 9.3 Koppeling aan aanvalsoppervlak en kritieke assets

Op basis van de asset-inventarisatie (hoofdstuk 3) en het aanvalsoppervlak zijn de volgende event-categorieën relevant voor NEN-7510 A.8.15:

| Categorie | Endpoint(s) | Betrokken assets |
|---|---|---|
| Authenticatie (login) | `AuthorizationFilter` (Basic Auth header) | A-04, A-05 |
| Sessiebeheer | `POST /session`, `DELETE /session` | A-04 |
| Autorisatiefouten | Alle endpoints (via BaseRestController) | A-06 |
| Toegang tot patiëntdata | `GET /patient`, `GET /obs`, etc. | A-01, A-02, A-03 |
| Wijziging patiëntdata | `POST /patient`, `POST /obs`, etc. | A-01, A-07, A-09, A-10 |
| Verwijdering patiëntdata | `DELETE /{resource}/{uuid}` | A-01 t/m A-10 |
| Privileged operations | `POST /searchindex`, `POST /dbcache` | A-11 |
| Diagnostisch endpoint (geen auth) | `GET /session/diag` | A-04, A-05, A-06 |
| IP-blokkering | `AuthorizationFilter` | A-04 |

---

## 9.4 Gap-analyse tabel

De onderstaande tabel toont per relevant event of het gelogd wordt, welke gevoelige data in de log terechtkomt, en of dit compliant is met NEN-7510 A.8.15.

| # | Event | Gelogd | Gevoelige data in log | Compliant NEN-7510 A.8.15 |
|---|---|---|---|---|
| E-01 | Geslaagde authenticatie (Basic Auth) | ⚠️ Ja, maar alleen op DEBUG | Gebruikersnaam in plaintext | ❌ Nee — DEBUG staat in productie uit; gebruikersnaam in log is risico |
| E-02 | Mislukte authenticatiepoging | ⚠️ Ja, maar alleen op DEBUG | Stack trace, geen gebruikersnaam | ❌ Nee — niveau onjuist; geen gebruikersnaam, IP of tijdstip |
| E-03 | Brute-force detectie (herhaalde mislukte logins) | ❌ Niet gelogd | n.v.t. | ❌ Nee — geen teller of patroondetectie |
| E-04 | Sessieverloop (timeout) | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-05 | Uitloggen (`DELETE /session`) | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-06 | Inloggen via session endpoint (`POST /session`) | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-07 | Autorisatiefout — 403 Forbidden | ⚠️ Ja, op INFO | Exception message (geen user/endpoint) | ❌ Nee — onvolledig; geen gebruikersidentiteit of resource |
| E-08 | Ongeautoriseerd verzoek — 401 Unauthorized | ⚠️ Ja, op INFO | Exception message | ❌ Nee — onvolledig |
| E-09 | IP-blokkering (verboden IP-adres) | ❌ Niet gelogd | n.v.t. | ❌ Nee — 403 wordt gestuurd maar niet gelogd |
| E-10 | Raadplegen patiëntrecord (`GET /patient/{uuid}`) | ❌ Niet gelogd | n.v.t. | ❌ Nee — geen audit trail van data-inzage |
| E-11 | Aanmaken patiëntrecord (`POST /patient`) | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-12 | Wijzigen patiëntrecord (`POST /patient/{uuid}`) | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-13 | Verwijderen/voiden patiëntrecord (`DELETE /patient/{uuid}`) | ❌ Niet gelogd | n.v.t. | ❌ Nee — onomkeerbare handeling zonder spoor |
| E-14 | Permanent verwijderen (`DELETE … ?purge=true`) | ❌ Niet gelogd | n.v.t. | ❌ Nee — hoogste risico: geen log van destructieve actie |
| E-15 | Aanmaken/wijzigen observaties (`POST /obs`) | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-16 | Aanmaken medicatieorder (`POST /order`) | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-17 | Toegang tot allergiegegevens | ❌ Niet gelogd | n.v.t. | ❌ Nee |
| E-18 | Toegang tot `/session/diag` (geen auth vereist) | ❌ Niet gelogd | Username, rollen, privileges in response | ❌ Nee — uitzonderlijk risico: gevoelige data zonder log |
| E-19 | Opbouwen zoekindex (`POST /searchindex`) | ⚠️ Ja, op DEBUG | Resourcenaam, UUID | ⚠️ Gedeeltelijk — DEBUG; geen gebruikerscontext |
| E-20 | Leegmaken DB-cache (`POST /dbcache`) | ⚠️ Ja, op DEBUG | Resourcenaam, UUID | ⚠️ Gedeeltelijk — DEBUG; geen gebruikerscontext |
| E-21 | Module start/stop | ✅ Ja, op INFO | Geen gevoelige data | ✅ Compliant |
| E-22 | HTTP 5xx server-fout | ✅ Ja, op ERROR | Exception + stack trace (mogelijk data-leak) | ⚠️ Gedeeltelijk — stack traces kunnen PHI bevatten |
| E-23 | Bulk-opvraag patiëntlijst (`GET /patient?q=…`) | ❌ Niet gelogd | n.v.t. | ❌ Nee — massale data-extractie ondetecteerbaar |

**Samenvatting:** van de 23 geïdentificeerde kritieke events zijn er slechts **2 volledig compliant** (module lifecycle). De overige 21 events zijn niet of onvolledig gelogd.

---

## 9.5 Gevoelige data in bestaande logs

Hoewel de logging minimaal is, bevat wat er wél gelogd wordt, aandachtspunten:

**Gebruikersnaam in plaintext (E-01):**  
`log.debug("authenticated [{}]", userAndPass[0])` schrijft de gebruikersnaam naar de logfile. Als logging naar een gecentraliseerd systeem gaat (zoals een SIEM), wordt de gebruikersnaam doorgestuurd. Dit is op zichzelf acceptabel voor audit-doeleinden, maar alleen als de logfile zelf voldoende beveiligd is.

**Stack traces met mogelijke PHI (E-22):**  
`log.error(ex.getMessage(), ex)` logt de volledige stack trace bij server errors. Exception messages in OpenMRS bevatten regelmatig object-representaties die UUID's of andere identificatoren van patiënten kunnen bevatten. Dit vereist beheersing via log-filtering of sanitisatie.

**Wachtwoord nooit in log:**  
`userAndPass[1]` (het wachtwoord) wordt niet gelogd. Dit is correct gedrag.

---

## 9.6 Niet-gelogde gebeurtenissen — prioritering

De meest kritieke ontbrekende log-events zijn:

**1. Mislukte authenticatiepogingen op INFO/WARN (E-02, E-03)**  
Brute-force aanvallen op de REST API zijn volledig ondetecteerbaar. NEN-7510 A.8.15 vereist expliciet het loggen van mislukte toegangspogingen, inclusief tijdstip en IP-adres.

**2. Alle CRUD-operaties op medische data (E-10 t/m E-17)**  
De `MainResourceController` verwerkt alle create/read/update/delete-operaties op patiëntrecords, observaties, orders en allergieën zonder één logstatement. Dit is de grootste gap: zonder deze logs is forensisch onderzoek na een datalek onmogelijk.

**3. Permanent verwijderen (purge) (E-14)**  
De `?purge=true` parameter verwijdert data onomkeerbaar uit de database. Geen log van wie, wanneer en welke UUID gepurgd is, maakt dit de meest risicovolle niet-gelogde handeling.

**4. Het `/session/diag`-endpoint (E-18)**  
Dit endpoint retourneert zonder autorisatiecontrole gevoelige gebruikersinformatie (rollen, privileges). Toegang hiertoe wordt niet gelogd, terwijl het potentieel bruikbaar is voor reconnaissance door een aanvaller.

**5. IP-blokkering (E-09)**  
Een geblokkeerd IP-adres krijgt een HTTP 403, maar dit event wordt nergens vastgelegd. Hierdoor blijft gerichte port-scanning of -misbruik van de allowed-IPs-functionaliteit onzichtbaar.

---

## 9.7 Gap-samenvatting

| Aspect | Huidige situatie | Gewenste situatie (NEN-7510 A.8.15) | Gap |
|---|---|---|---|
| Authenticatie logging | DEBUG-level; effectief uitgeschakeld in productie | INFO/WARN voor succes; WARN/ERROR voor mislukking, inclusief IP en tijdstip | Kritiek |
| Autorisatiefouten | INFO zonder gebruikerscontext | WARN met gebruikersidentiteit, endpoint en IP | Hoog |
| Dataraadpleging (reads) | Geen | Audit-log per GET op gevoelige resource (user, tijdstip, UUID) | Kritiek |
| Datawijziging (writes) | Geen | Audit-log per POST/PUT (user, tijdstip, resource, UUID, oude/nieuwe waarden) | Kritiek |
| Datadeletie (delete/purge) | Geen | Verplichte audit-log met reden, user en tijdstip | Kritiek |
| Logniveau-configuratie | Twee frameworks, inconsistent | Één framework (SLF4J), geconfigureerd log-level per omgeving | Hoog |
| Gevoelige data in logs | Stack traces kunnen PHI bevatten | Log-sanitisatie; nooit wachtwoorden of BSN in logs | Midden |
| Centralisatie & retentie | Onbekend | Centrale log-opslag, minimaal 1 jaar bewaard (NEN-7510 vereiste) | Onbekend/risico |
| Integriteit van logs | Geen maatregelen zichtbaar | Tamper-evident log-opslag (bijv. WORM of SIEM) | Hoog |

---

## 9.8 Aanbevelingen

Op basis van de gap-analyse worden de volgende maatregelen aanbevolen, geordend naar prioriteit:

**Prioriteit 1 — Implementeer audit-logging in `MainResourceController`**  
Voeg voor alle CRUD-operaties een audit-log toe op INFO-niveau met: gebruikersidentiteit (`Context.getAuthenticatedUser()`), HTTP-methode, resource, UUID, tijdstip en IP-adres. Overweeg een centrale `AuditLogger`-klasse om dit consistent te houden.

**Prioriteit 2 — Verhoog logniveau authenticatiegebeurtenissen**  
Wijzig in `AuthorizationFilter` het niveau van geslaagde authenticatie naar `INFO` en van mislukte authenticatie naar `WARN`, inclusief het IP-adres van de aanvrager (`request.getRemoteAddr()`).

**Prioriteit 3 — Log toegang tot `/session/diag` en verwijder of beveilig het endpoint**  
Voeg minimaal een `WARN`-log toe bij elke aanroep van het diagnostisch endpoint, inclusief caller-IP en authenticatiestatus. Bij voorkeur wordt het endpoint verwijderd of voorzien van een sterke autorisatiecheck (zie ook bevinding in WS05).

**Prioriteit 4 — Één logging-framework**  
Consolideer naar SLF4J en verwijder de directe afhankelijkheid van Apache Commons Logging om consistente log-uitvoer en eenvoudigere configuratie te garanderen.

**Prioriteit 5 — Log-sanitisatie voor stack traces**  
Implementeer een `Sanitizer`-utility die before logging stack traces scant op patronen die PHI kunnen bevatten (UUID-formaten, namen) en deze maskeert of omschrijft.

---

*Dit document is opgesteld als onderdeel van de NEN-7510 compliance-audit van de OpenMRS REST Web Services Module door Avans Hogeschool, studiegroep C8.*