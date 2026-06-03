# 5.5 Gap-analyse — OpenMRS Webservices REST Module

## Scope

Deze gap-analyse beoordeelt de gekozen module `webservices.rest` tegen de afgesproken NEN 7510:2024-2 controls:

- A.8.3 Toegangsbeperking tot informatie
- A.8.5 Veilige authenticatie
- A.8.15 Logging

De gekozen module is de OpenMRS Webservices REST Module (`webservices.rest`), versie `3.2.0`. De module exposeert de OpenMRS API via REST web services. Hierdoor kunnen externe applicaties gegevens ophalen uit en wegschrijven naar een OpenMRS-database. Omdat het hierbij kan gaan om medische persoonsgegevens, is de module security-kritiek.

## Samenvatting

| Control | Status | Korte conclusie |
|---|---|---|
| A.8.3 Toegangsbeperking tot informatie | Gedeeltelijk | De module is een REST API-laag voor OpenMRS-data. Toegangscontrole is noodzakelijk, maar een expliciete autorisatiematrix per endpoint/resource is niet aangetoond. |
| A.8.5 Veilige authenticatie | Gedeeltelijk | Authenticatie lijkt grotendeels afhankelijk van OpenMRS core/session handling. MFA of sterke authenticatie is niet aantoonbaar in de module zelf aanwezig. |
| A.8.15 Logging | Afwezig / niet aangetoond | Er is geen duidelijk bewijs gevonden dat security-relevante REST-acties, autorisatiefouten of datatoegang structureel worden gelogd. |

---

## Detailanalyse

| Controlgebied | Verwachting | Bevinding | Status | Bewijs / locatie in repo | Gap / risico | Aanbevolen maatregel |
|---|---|---|---|---|---|---|
| A.8.3 Toegangsbeperking tot informatie | Alleen bevoegde gebruikers mogen via de REST API toegang krijgen tot medische gegevens en functies. Per endpoint/resource moet duidelijk zijn welke rol of privilege vereist is. | De module exposeert OpenMRS-data via REST endpoints. Dit maakt autorisatie per resource en operatie essentieel. Er is echter geen aparte autorisatiematrix of documentatie aangetroffen waarin per REST-resource staat welke rechten nodig zijn voor lezen, aanmaken, wijzigen of verwijderen. | Gedeeltelijk | `README.md` beschrijft dat de module de OpenMRS API als REST web services beschikbaar maakt. `docs/module-keuze.md` benoemt authenticatie en autorisatie als kritieke functionaliteit. | Zonder expliciete autorisatiematrix is niet goed aantoonbaar dat toegang tot patiëntdata overal correct beperkt is. Dit vergroot het risico op broken access control. | Maak een autorisatiematrix per REST-resource: resource, HTTP-methode, vereiste rol/privilege, type data en risico. Voeg daarnaast tests toe voor ongeautoriseerde toegang per kritieke resource. |
| A.8.5 Veilige authenticatie | Gebruikers en API-clients moeten veilig worden geauthenticeerd voordat zij toegang krijgen tot de REST API. Sterke authenticatie, sessiebeheer en bescherming tegen misbruik moeten aantoonbaar zijn. | De module gebruikt waarschijnlijk OpenMRS-authenticatie en sessiemechanismen, maar binnen de module zelf is geen MFA-functionaliteit of expliciete sterke-authenticatie-eis aangetoond. Authenticatie lijkt daarmee deels platformafhankelijk. | Gedeeltelijk | `docs/module-keuze.md` noemt request-handling, sessiebeheer en toegangscontrole als security-kritieke onderdelen van de module. | Het is onvoldoende aantoonbaar welke authenticatie-eisen gelden voor REST API-gebruikers. MFA of aanvullende sterke authenticatie is niet zichtbaar in de module. | Documenteer expliciet dat authenticatie wordt afgehandeld door OpenMRS core of de platformlaag. Voeg toe welke sessie-/tokenmechanismen worden gebruikt en welke aanvullende maatregelen nodig zijn, zoals MFA via IdP, reverse proxy of OpenMRS-configuratie. |
| A.8.15 Logging | Security-relevante gebeurtenissen moeten worden gelogd, zodat misbruik, fouten en incidenten achteraf onderzocht kunnen worden. Denk aan loginpogingen, autorisatiefouten, toegang tot gevoelige data en wijzigingen via REST. | Er is geen duidelijk bewijs gevonden dat de module structureel security-events logt rond REST-aanroepen. Vooral logging van autorisatiefouten, toegang tot patiëntdata en mutaties is niet aantoonbaar beschreven. | Afwezig / niet aangetoond | Geen aparte logging- of auditdocumentatie gevonden voor REST-security-events. | Zonder aantoonbare logging is incidentonderzoek moeilijk. Misbruik van REST endpoints kan daardoor te laat of niet worden ontdekt. | Voeg security logging toe of documenteer bestaande OpenMRS audit logging. Log minimaal: gebruiker/client, endpoint, HTTP-methode, objecttype, resultaat, autorisatiefout en timestamp. Zorg dat logs geen medische inhoud of gevoelige tokens bevatten. |

---

## Uitwerking per control

### A.8.3 Toegangsbeperking tot informatie

De module `webservices.rest` vormt een externe API-laag bovenop OpenMRS. Hierdoor kunnen externe applicaties via HTTP gegevens ophalen en wijzigen. Dit betekent dat toegangsbeperking op deze laag zeer belangrijk is.

**Bevinding:**  
Er is wel sprake van een REST-laag waarin autorisatie relevant is, maar er is geen duidelijke audit-evidence aangetroffen in de vorm van een autorisatiematrix per endpoint of resource.

**Status:** Gedeeltelijk.

**Waarom niet volledig aanwezig:**  
Voor een volledige beoordeling moet aantoonbaar zijn:

- welke REST-resources beschikbaar zijn;
- welke HTTP-methoden per resource zijn toegestaan;
- welke rol of privilege nodig is per actie;
- hoe ongeautoriseerde toegang wordt getest;
- hoe toegang tot medische gegevens wordt beperkt.

**Gap:**  
De module heeft waarschijnlijk technische toegangscontrole via OpenMRS-mechanismen, maar de werking is onvoldoende aantoonbaar gemaakt voor auditdoeleinden.

**Aanbeveling:**  
Maak een bestand `docs/auditrapport/autorisatiematrix.md` met per REST-resource:

| Resource | Methode | Data | Vereist privilege | Risico | Test aanwezig |
|---|---|---|---|---|---|
| Patient | GET | Patiëntdata | Nog te bepalen | Hoog | Nog te bepalen |
| Patient | POST | Patiëntdata aanmaken | Nog te bepalen | Hoog | Nog te bepalen |
| Encounter | GET | Consult/contactmomenten | Nog te bepalen | Hoog | Nog te bepalen |

---

### A.8.5 Veilige authenticatie

Omdat de REST API toegang kan geven tot medische data, moet authenticatie goed geregeld en aantoonbaar zijn.

**Bevinding:**  
De module lijkt authenticatie niet zelfstandig af te dwingen met bijvoorbeeld MFA, maar is afhankelijk van OpenMRS core, sessiebeheer of platformconfiguratie. Dat is op zichzelf acceptabel, maar moet dan wel duidelijk worden gedocumenteerd.

**Status:** Gedeeltelijk.

**Waarom niet volledig aanwezig:**  
Er is geen bewijs gevonden dat de module zelf:

- MFA ondersteunt of afdwingt;
- API-tokenbeleid documenteert;
- sessiebeleid beschrijft;
- lockout/rate limiting op login afdwingt;
- authenticatie-eisen voor API-clients documenteert.

**Gap:**  
Sterke authenticatie is niet aantoonbaar binnen de module. Daardoor is onduidelijk welke beveiliging geldt voor externe API-clients.

**Aanbeveling:**  
Leg vast dat authenticatie via OpenMRS core of de infrastructuurlaag loopt. Voeg in de documentatie toe:

- welke authenticatiemethode wordt gebruikt;
- of MFA buiten de module wordt afgedwongen;
- hoe sessies verlopen;
- of API-gebruik via service accounts of gebruikersaccounts plaatsvindt;
- welke aanvullende maatregel nodig is in productie.

Voorbeeldmaatregel:

```text
MFA wordt niet door de module zelf geleverd, maar moet worden afgedwongen via de centrale identity provider of OpenMRS-platformconfiguratie.