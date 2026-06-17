# Bijlage: CRA-mapping — OpenMRS `webservices.rest`

**Document:** `docs/auditrapport/cra-mapping.md`  
**Module:** OpenMRS `webservices.rest` v3.2.0  
**Project:** ATx-2.4 Software Security & Compliance  
**Normenkader:** Cyber Resilience Act (Regulation (EU) 2024/2847) gekoppeld aan NEN 7510-2:2024+A1:2026  
**Status:** Concept auditbijlage  
**Datum:** juni 2026

---

## 1. Doel van deze bijlage

Deze bijlage maakt inzichtelijk hoe de onderzochte OpenMRS REST Web Services Module zich verhoudt tot de belangrijkste cybersecurityverplichtingen uit de **Cyber Resilience Act (CRA)**. De mapping is opgezet als audit-traceabilitymatrix: elke CRA-verplichting wordt gekoppeld aan relevante NEN 7510-2-controls, gevonden risico's of kwetsbaarheden, maatregelen/backlog-items, bewijsartefacten en de status van het restrisico.

De CRA is in dit project gebruikt als aanvullend wetgevingskader naast NEN 7510. NEN 7510-2 is het primaire controlkader voor informatiebeveiliging in de zorg. De CRA legt de nadruk op producten met digitale elementen, secure-by-design, secure-by-default, vulnerability handling, SBOM, security updates en lifecycle-verantwoordelijkheid.

> **Afbakening:** deze mapping is geen juridische conformiteitsverklaring of CE-conformiteitsbeoordeling. De exacte CRA-plicht hangt af van de rol van de organisatie, bijvoorbeeld fabrikant, distributeur, open-source steward of gebruiker. Binnen deze onderwijsopdracht wordt de CRA gebruikt als toetsings- en traceabilitykader voor security-eisen rond softwareproducten.

---

## 2. Bronnen en bewijsbasis

| Bron | Pad / referentie | Gebruik in deze mapping |
|---|---|---|
| Risicoanalyse | `docs/auditrapport/00-risk-assessment.md` | Overzicht van kroonjuwelen, risico's, penteststatus, restrisico en productiegate |
| Assetinventarisatie | `docs/auditrapport/03-assets.md` | Bepalen welke patiëntdata, credentials en systeemassets beschermd moeten worden |
| Risicomatrix | `docs/auditrapport/04-risico-matrix.md` | Prioritering op basis van kans × impact |
| Bow-tie analyse | `docs/auditrapport/05-bowtie.md` | Top event en preventieve/herstelbarrières voor ongeautoriseerde API-toegang |
| Pipeline compliance | `docs/auditrapport/02-pipeline-compliance.md` | CI/CD-controls, scanmaatregelen en gaps |
| Security backlog | `docs/auditrapport/06-security-backlog.md` | Geprioriteerde maatregelen SEC-001 t/m SEC-032 |
| Security code review | `docs/auditrapport/07-security-code-review.md` | SAST, CodeQL, DAST en handmatige bevindingen |
| Dependency-updateadvies | `docs/auditrapport/bijlage-dependency-updateadvies.md` | SBOM, SCA, CVE/CVSS en updateadvies |
| Logging gap-analyse | `docs/auditrapport/09-logging-gap-analyse.md` | Gap ten opzichte van logging/audit requirements |
| Attack surface analyse | `docs/auditrapport/10-attack-surface.md` | REST API, CI/CD, Docker, Maven, testcredentials en entry points |
| SBOM | `docs/sbom.cdx.json` | Machineleesbare componentenlijst in CycloneDX-formaat |
| Snyk/CodeQL artifacts | `snyk-results.json`, GitHub Security tab | Bewijs voor SAST/SCA en kwetsbaarhedentriage |
| Pentestbevindingen | `docs/pentest/03-bevindingen.md` | Runtimebewijs voor kritieke kwetsbaarheden |
| NEN 7510-2 | `NEN 7510-2:2024+A1:2026` | Primair controlkader voor zorginformatiebeveiliging |
| CRA | Regulation (EU) 2024/2847 | Wetgevingskader voor cyberweerbaarheid van producten met digitale elementen |

---

## 3. Selectie van relevante CRA-verplichtingen

Niet elke CRA-verplichting is op dezelfde manier toetsbaar binnen de scope van een OpenMRS-module. In deze mapping zijn vooral de verplichtingen geselecteerd die direct aansluiten op softwaresecurity, dependencybeheer, technische kwetsbaarheden en auditbaarheid.

| CRA-thema | Betekenis voor deze module | Relevante NEN 7510-2-controls |
|---|---|---|
| Security by design / risk-based security | Security-eisen moeten voortkomen uit risicoanalyse, threat model en ontwerpkeuzes. | 5.8, 5.31, 5.34, 8.25, 8.26, 8.27 |
| Secure by default | Onveilige standaardinstellingen, debug-info en open adminfuncties moeten worden voorkomen. | 8.9, 8.24, 8.26, 8.28 |
| Bescherming tegen ongeautoriseerde toegang | REST-resources en speciale endpoints moeten authenticatie en autorisatie afdwingen. | 5.15, 5.16, 5.17, 5.18, 8.2, 8.3, 8.5 |
| Bescherming van vertrouwelijkheid en integriteit | Patiëntdata, sessies, credentials, orders en allergieën moeten beschermd blijven. | 5.12, 5.34, 8.3, 8.11, 8.12, 8.15, 8.24 |
| Beschikbaarheid en misbruikpreventie | Brute-force, scraping, DoS en destructieve endpoints moeten worden beperkt. | 8.6, 8.13, 8.14, 8.16, 8.20, 8.21 |
| Attack surface minimalisatie | Onnodige endpoints, debugpaden, adminfuncties en blootgestelde services moeten worden beperkt. | 8.9, 8.20, 8.22, 8.26, 8.27, 8.31 |
| Vulnerability handling | Kwetsbaarheden moeten worden geïdentificeerd, beoordeeld, verholpen of expliciet geaccepteerd. | 5.7, 8.8, 8.16, 8.29, 8.32 |
| SBOM en supply-chain security | Softwarecomponenten en kwetsbare dependencies moeten traceerbaar zijn. | 5.9, 5.19, 5.20, 5.21, 8.8, 8.32 |
| Logging, monitoring en incidentrespons | Misbruik moet detecteerbaar zijn en incidenten moeten aantoonbaar kunnen worden onderzocht. | 5.24 t/m 5.28, 8.15, 8.16, 8.17 |
| Security testing en validatie | Securitymaatregelen moeten aantoonbaar getest en her-testbaar zijn. | 8.25, 8.28, 8.29, 8.31, 8.33 |
| Veilige updates en wijzigingsbeheer | Updates, dependencywijzigingen en pipelinewijzigingen moeten gecontroleerd plaatsvinden. | 8.8, 8.19, 8.25, 8.29, 8.32 |
| Responsible disclosure / vulnerability contact | Meldingen van kwetsbaarheden moeten via een vast proces binnenkomen en worden afgehandeld. | 5.5, 5.6, 5.24 t/m 5.28, 6.8 |

---

## 4. CRA-traceabilitymatrix

| # | CRA-verplichting / thema | Relevante NEN 7510-2-controls | Bevinding / risico in project | Maatregel / backlog | Bewijsartefact | Status / restrisico |
|---:|---|---|---|---|---|---|
| 1 | **Risk-based cybersecurity**: cybersecurityrisico's moeten tijdens ontwerp, ontwikkeling en onderhoud worden beoordeeld. | 5.8, 5.31, 5.34, 8.25, 8.26, 8.27 | De module ontsluit herleidbare patiëntgegevens via REST. T1 ongeautoriseerde API-toegang heeft score 20; T2 patiëntdata-exposure score 15; T4 credential-lek score 16; T5 supply-chain score 10. | Risicoanalyse, risicomatrix, bow-tie, threat model en security backlog gebruiken als formele risicobehandeling. | `00-risk-assessment.md`, `04-risico-matrix.md`, `05-bowtie.md`, `threat-model.md`, `06-security-backlog.md` | **Gedeeltelijk compliant.** Risicoproces is aantoonbaar, maar meerdere P1-risico's staan nog open. |
| 2 | **Secure by design**: beveiliging moet onderdeel zijn van requirements, ontwerp, code en tests. | 5.8, 8.25, 8.26, 8.27, 8.28, 8.29 | Threat model en attack surface zijn opgesteld. Bevindingen tonen echter aan dat speciale endpoints en foutafhandeling niet volledig secure-by-design zijn. | Gebruik threat model als input voor security requirements; voer security code review uit op auth-, admin- en foutpaden; voeg securitytests toe. | `threat-model.md`, `10-attack-surface.md`, `07-security-code-review.md`, `07-code-coverage.md` | **Gedeeltelijk compliant.** Analyse en review aanwezig; implementatie/fix van kritieke paden nog nodig. |
| 3 | **Secure by default**: standaardconfiguratie mag geen onnodige risico's introduceren. | 8.9, 8.24, 8.26, 8.28 | `webservices.rest.enableStackTraceDetails=true` veroorzaakt stack traces in foutresponses. `settings.form` is anoniem bereikbaar. | SEC-007 module-instellingen afschermen; SEC-010 stack traces uitzetten in productie; productieprofiel hardenen. | `07-security-code-review.md`, `00-risk-assessment.md`, `06-security-backlog.md`, pentest PT-001/PT-004/PT-005 | **Niet compliant voor productie.** Open P1/P2-configuratiegaps moeten vóór productie worden opgelost. |
| 4 | **Bescherming tegen ongeautoriseerde toegang**: toegang tot functies/data moet alleen voor bevoegden mogelijk zijn. | 5.15, 5.16, 5.17, 5.18, 8.2, 8.3, 8.5 | Standaard patient-read is anoniem geblokkeerd, maar `POST /cleardbcache` geeft anoniem 204 en `settings.form` is anoniem bereikbaar. Basic Auth is zwak en brute-forcebescherming ontbreekt. | SEC-001 fine-grained authorization; SEC-002 MFA via gateway/IdP; SEC-003 brute-forcebescherming; SEC-007 settings afschermen; SEC-019 speciale endpoints beschermen. | `00-risk-assessment.md`, `01-gap-analyse.md`, `06-security-backlog.md`, `07-security-code-review.md`, pentestbevindingen PT-003/PT-004 | **Niet compliant voor productie.** PT-003 en PT-004 zijn fix-verplicht. |
| 5 | **Bescherming van vertrouwelijkheid van gegevens**: gevoelige gegevens mogen niet uitlekken. | 5.12, 5.34, 8.3, 8.11, 8.12, 8.15, 8.24 | Kroonjuwelen bevatten patiëntrecords, identifiers, PII, sessietokens en credentials. TLS is afhankelijk van infrastructuur; applicatie controleert HTTPS niet. Stack traces en settings kunnen gevoelige informatie lekken. | SEC-001 autorisatie; SEC-009 HTTPS/HSTS; SEC-010 stack traces uitzetten; SEC-020 audit trail zorgdata; dataminimalisatie in responses. | `03-assets.md`, `01-gap-analyse.md`, `09-logging-gap-analyse.md`, `10-attack-surface.md` | **Gedeeltelijk compliant.** Patient-read anoniem is geblokkeerd, maar settings/stack traces/TLS-afdwinging blijven risico's. |
| 6 | **Bescherming van integriteit**: manipulatie van medische gegevens moet worden voorkomen of detecteerbaar zijn. | 5.33, 5.34, 8.3, 8.15, 8.24, 8.26 | T3 manipulatie van orders/allergieën heeft score 10. De module heeft RBAC, maar geen aanvullende integriteitscontrole of onweerlegbare audit trail op kritieke medische wijzigingen. | SEC-011 RBAC-review; SEC-016 integriteit kritieke medische records; SEC-020 audit trail zorgdata; logging van wijzigingsacties. | `04-risico-matrix.md`, `06-security-backlog.md`, `09-logging-gap-analyse.md` | **Gedeeltelijk compliant.** Basis-RBAC aanwezig; extra integriteits- en auditmaatregelen zijn nodig. |
| 7 | **Beschikbaarheid en weerbaarheid**: het product moet bestand zijn tegen misbruik dat beschikbaarheid raakt. | 8.6, 8.13, 8.14, 8.16, 8.20, 8.21, 5.29, 5.30 | Geen rate limiting; destructief cache-endpoint is anoniem uitvoerbaar; DoS-risico T6 score 9. BCP/DRP en backup zijn platform/organisatiegaps. | SEC-004 rate limiting; SEC-015 maxResults-cap; SEC-019 speciale endpoints beschermen; SEC-021 SIEM; SEC-025 BCP/DRP; SEC-026 backup. | `02-pipeline-compliance.md`, `04-risico-matrix.md`, `06-security-backlog.md`, `10-attack-surface.md` | **Niet volledig compliant.** Beschikbaarheid hangt sterk af van platform- en gatewaymaatregelen. |
| 8 | **Attack surface minimalisatie**: onnodige blootstelling en debugfuncties moeten worden beperkt. | 8.9, 8.20, 8.22, 8.26, 8.27, 8.31 | Attack surface omvat REST API, admin/config-paden, CI/CD, Docker, Maven dependencies en testcredentials. Debug/API-docs/XSS-paden vergroten blootstelling. | SEC-007 settings afschermen; SEC-010 stack traces uitzetten; SEC-012 IP-allowlist; SEC-017 pipeline pinning; SEC-029/SEC-032 XSS-fixes; ongebruikte endpoints beperken. | `10-attack-surface.md`, `07-security-code-review.md`, `04b-cicd-risico.md` | **Gedeeltelijk compliant.** Attack surface is in kaart gebracht; reductie nog niet volledig gerealiseerd. |
| 9 | **SBOM en componententraceerbaarheid**: componenten moeten machineleesbaar en actueel te inventariseren zijn. | 5.9, 5.19, 5.21, 8.8, 8.32 | SBOM is aanwezig in CycloneDX-formaat. Snyk-dashboard telt dubbel per module; het updateadvies ontdubbelt CVE's per component/versie. | SBOM in CI genereren; Snyk SCA uitvoeren; dependency-overzicht en updateadvies onderhouden. | `docs/sbom.cdx.json`, `.github/workflows/sbom.yml`, `bijlage-dependency-updateadvies.md`, `snyk-results.json` | **Gedeeltelijk compliant.** SBOM is aanwezig; actualiteit en opvolging moeten per release geborgd blijven. |
| 10 | **Vulnerability handling**: kwetsbaarheden moeten worden geïdentificeerd, beoordeeld, opgelost of gemotiveerd geaccepteerd. | 5.7, 8.8, 8.16, 8.29, 8.32 | SCA toont 106 unieke kwetsbaarheden: 6 Critical, 53 High, 33 Medium en 14 Low. Snyk draait met `continue-on-error`, waardoor build niet automatisch blokkeert. | SEC-006 supply-chain hardening; triageproces patchen/accepteren/supprimeren; Critical ≤ 24 uur triageren; updateadvies uitvoeren. | `bijlage-dependency-updateadvies.md`, `06-security-backlog.md`, `snyk-results.json`, GitHub Security tab | **Gedeeltelijk compliant.** Detectie en advies aanwezig; hard gate en structurele patchopvolging moeten sterker. |
| 11 | **Security updates en onderhoud gedurende lifecycle**: kwetsbaarheden moeten via updates beheerst worden. | 8.8, 8.19, 8.25, 8.32 | Veel risico's zitten in transitieve platformdependencies zoals Spring, Netty, legacy Jackson en Tomcat/Jasper. Niet alle fixes zijn direct vanuit de module beïnvloedbaar. | Prioriteit 1 platformupdates; prioriteit 2 direct beheerde dependencies; prioriteit 3 legacy/no-fix risicoacceptatie. | `bijlage-dependency-updateadvies.md`, `02-pipeline-compliance.md`, `04b-cicd-risico.md` | **Gedeeltelijk compliant.** Updateadvies is concreet; implementatie vereist platformkeuzes en changemanagement. |
| 12 | **Security testing**: beveiliging moet aantoonbaar getest worden tijdens ontwikkeling en acceptatie. | 8.25, 8.28, 8.29, 8.31, 8.33 | CodeQL, Snyk Code, Snyk SCA, Burp pentest en JaCoCo zijn aanwezig. Pentest bevestigt kritieke kwetsbaarheden. OWASP ZAP is niet uitgevoerd; hertest na fixes is nog nodig. | Testpiramide toepassen: unit tests, integratietests, SAST, SCA, DAST/pentest; exact hertesten na mitigatie. | `07-security-code-review.md`, `07-code-coverage.md`, `docs/pentest/03-bevindingen.md`, GitHub Actions artifacts | **Gedeeltelijk compliant.** Testbasis is aanwezig; validatie na fixes ontbreekt nog. |
| 13 | **Logging en monitoring voor detectie**: security events moeten detecteerbaar en herleidbaar zijn. | 8.15, 8.16, 8.17, 5.24 t/m 5.28 | Logging gap-analyse toont dat 21 van 23 kritieke eventcategorieën niet of onvoldoende auditwaardig worden gelogd. Auth-events staan op DEBUG en CRUD op patiëntdata wordt niet gelogd. | SEC-013 security logging auth; SEC-014 logging privilege escalations; SEC-020 audit trail care data; SEC-021 SIEM; SEC-022 anomaliedetectie. | `09-logging-gap-analyse.md`, `06-security-backlog.md`, `01-gap-analyse.md` | **Niet compliant voor auditdoel.** Zonder auditwaardige logging is detectie en reconstructie onvoldoende. |
| 14 | **Incident response en meldproces**: incidenten en kwetsbaarheden moeten volgens proces worden behandeld. | 5.5, 5.6, 5.24, 5.25, 5.26, 5.27, 5.28, 6.8 | Incidentrespons is als gap benoemd; er is geen volledig IRP of vulnerability disclosureproces in de module/repo aangetoond. | SEC-023 incident response plan; nieuw advies SEC-033 responsible disclosure/security policy; SEC-034 vulnerability intake en triageproces. | `02-pipeline-compliance.md`, `06-security-backlog.md`, `09-logging-gap-analyse.md` | **Niet volledig compliant.** Procedurele maatregelen ontbreken of zijn buiten modulescope belegd. |
| 15 | **Responsible disclosure / vulnerability contact**: kwetsbaarheden moeten gemeld en opgevolgd kunnen worden. | 5.5, 5.6, 5.24 t/m 5.28, 6.8 | Er is geen expliciete `SECURITY.md`, vulnerability contact of disclosureprocedure vastgesteld in de auditdocumentatie. | Voeg `SECURITY.md` toe met contactpunt, scope, responstijden, triageproces en verwijzing naar backlog/IRP. | Repo-root, `06-security-backlog.md`, toekomstige `SECURITY.md` | **Open aanbeveling.** Nodig voor CRA-vulnerability handling en auditbaarheid. |
| 16 | **Wijzigingsbeheer en integriteit van build/deployment**: wijzigingen aan code, dependencies en pipeline moeten gecontroleerd plaatsvinden. | 5.3, 8.4, 8.25, 8.29, 8.31, 8.32, 5.21 | CI/CD-risico's: C1 secret leak score 20, C2 supply-chain attack score 15, C3 pipelineconfigwijziging score 12. GitHub Actions en dependencies zijn onderdeel van de supply chain. | Branch protection, PR reviews, SAST/SCA/SBOM, dependency review, action pinning, deployment approvals en gescheiden OTAP-secrets. | `04b-cicd-risico.md`, `04b-cicd-bow-tie.md`, `02-pipeline-compliance.md`, GitHub Security tab | **Gedeeltelijk compliant.** Controls zijn beschreven; volledige hardening en bewijs per GitHub-instelling moeten actueel worden toegevoegd. |

---

## 5. Traceerbaarheid naar belangrijkste bevindingen

| Bevinding | CRA-thema | NEN 7510-2-koppeling | Backlog | Productie-impact |
|---|---|---|---|---|
| PT-003 — anoniem `POST /cleardbcache` geeft 204 | Unauthorized access, availability, secure default | 8.3, 8.5, 8.6, 8.20, 8.26 | SEC-019 | **Blokkerend** voor productie met echte patiëntdata |
| PT-004 — `settings.form` anoniem bereikbaar + stack trace | Secure default, confidentiality, configuration management | 8.9, 8.24, 8.26 | SEC-007, SEC-010 | **Blokkerend** voor productie |
| 106 unieke SCA-kwetsbaarheden | SBOM, vulnerability handling, security updates | 5.21, 8.8, 8.29, 8.32 | SEC-006 | Critical CVE's moeten getriageerd en geprioriteerd worden |
| Logging gap: 21/23 eventcategorieën onvoldoende | Monitoring, incident response, auditability | 8.15, 8.16, 5.24 t/m 5.28 | SEC-013, SEC-014, SEC-020, SEC-021 | Onvoldoende aantoonbaarheid bij incident/datalek |
| Geen rate limiting/brute-forcebescherming | Availability, abuse prevention, authentication | 8.5, 8.6, 8.20 | SEC-003, SEC-004, SEC-015 | Verhoogd risico op scraping, brute-force en DoS |
| CI/CD secret leak risico C1 score 20 | Supply-chain security, build integrity | 5.21, 8.4, 8.8, 8.32 | SEC-005, SEC-017 | Compromittering van build/deployment mogelijk |
| Geen expliciete responsible disclosureprocedure | Vulnerability reporting, incident intake | 5.5, 5.6, 5.24 t/m 5.28, 6.8 | Nieuw: SEC-033/SEC-034 | Onvoldoende voorbereid op externe kwetsbaarheidsmeldingen |

---

## 6. Productiegate vanuit CRA-perspectief

Op basis van deze CRA-mapping is productiegebruik met echte patiëntdata pas verdedigbaar wanneer minimaal aan de volgende voorwaarden is voldaan:

1. **PT-003 en PT-004 zijn opgelost en hertest is vastgelegd.**  
   De anonieme toegang tot `cleardbcache` en `settings.form` moet aantoonbaar 401/403 opleveren voor onbevoegde gebruikers.

2. **SEC-006 is operationeel als vulnerability-handlingproces.**  
   Elke Snyk/CodeQL/SCA-bevinding moet een besluit hebben: patchen, accepteren, supprimeren of mitigeren. Critical bevindingen mogen niet zonder risicoacceptatie openstaan.

3. **SBOM is actueel per release.**  
   `docs/sbom.cdx.json` moet reproduceerbaar uit de CI-pipeline komen en gekoppeld zijn aan de gebruikte release/build.

4. **Security logging is auditwaardig.**  
   Minimaal authenticatie, autorisatiefouten, toegang tot patiëntdata, wijzigingen/verwijderingen en privileged operations moeten met de 5 W's worden vastgelegd.

5. **Rate limiting en brute-forcebescherming zijn ingericht.**  
   Vooral op login/session endpoints en bulk-querypaden zoals `patient?limit=N`.

6. **Security updateproces is vastgelegd.**  
   Dependency-updates, platformafhankelijkheden en legacy/no-fix componenten moeten een eigenaar, deadline en restrisicoacceptatie hebben.

7. **Responsible disclosure is toegevoegd.**  
   Een `SECURITY.md` of vergelijkbaar proces moet beschrijven hoe kwetsbaarheden gemeld, beoordeeld en opgevolgd worden.

---

## 7. Aanbevolen aanvullende backlog-items

De bestaande backlog dekt veel technische risico's. Voor een sterkere CRA-mapping worden deze aanvullende items geadviseerd:

| ID | Requirement | Reden | Acceptatiecriterium | Prioriteit |
|---|---|---|---|---|
| SEC-033 | Responsible disclosure / `SECURITY.md` toevoegen | CRA-vulnerability handling en externe meldbaarheid | Repo bevat `SECURITY.md` met scope, contactpunt, SLA, triageproces en safe harbor-formulering | P2 |
| SEC-034 | Vulnerability intake en triageproces formaliseren | Structurele opvolging van externe meldingen, Snyk, CodeQL en pentestbevindingen | Elk issue heeft eigenaar, ernst, besluit, deadline, bewijs en status | P1/P2 |
| SEC-035 | Release security checklist toevoegen | CRA-lifecycle en secure updates | Per release: SBOM, SCA, SAST, changelog, known vulnerabilities en update-instructies vastgelegd | P2 |
| SEC-036 | Security updatebeleid documenteren | Onderhoud gedurende supportperiode | Beleid beschrijft patchtermijnen, dependency-eigenaren, risicoacceptatie en communicatie | P2 |
| SEC-037 | Build artifact integrity versterken | Supply-chain en update-integriteit | Artifacts zijn herleidbaar aan commit, workflow-run en optioneel checksum/signature | P2 |

---

## 8. Conclusie

De OpenMRS `webservices.rest` module heeft een sterke auditbasis: risicoanalyse, risicomatrix, bow-tie, threat model, attack surface analyse, SAST/SCA, SBOM, dependency-updateadvies, code review, pentest en security backlog zijn aanwezig. Daarmee is de **traceability** richting CRA grotendeels aantoonbaar.

Tegelijk is de module vanuit CRA-perspectief **nog niet productiegeschikt met echte patiëntdata**. De belangrijkste redenen zijn:

- kritieke speciale endpoints zijn of waren anoniem bereikbaar;
- secure-by-default is onvoldoende door stack traces en module-settings;
- vulnerability handling is aanwezig als scan- en adviesproces, maar nog niet als harde quality gate;
- logging en monitoring zijn onvoldoende voor auditbaarheid en incidentrespons;
- responsible disclosure en release/updatebeleid ontbreken nog als expliciet proces.

De aanbevolen aanpak is daarom: eerst PT-003/PT-004 oplossen en hertesten, daarna SEC-006/SBOM/updateproces formaliseren, logging versterken en responsible disclosure toevoegen. Na deze maatregelen daalt het restrisico en wordt de CRA-koppeling verdedigbaar in het auditrapport.

---

## 9. Referenties

- Regulation (EU) 2024/2847 — Cyber Resilience Act.
- European Commission, Cyber Resilience Act — Shaping Europe's digital future.
- European Commission, CRA summary of the legislative text.
- NEN 7510-2:2024+A1:2026 — Medische informatica — Informatiebeveiliging in de zorg — Deel 2: Beheersmaatregelen.
- Projectdocumentatie in `docs/auditrapport/` en `docs/pentest/`.
