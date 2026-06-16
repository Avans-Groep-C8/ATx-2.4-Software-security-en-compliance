# 10. Attack Surface Analyse – OpenMRS REST Module

## Doel

Deze analyse identificeert en documenteert alle relevante ingangen (attack surface) van de OpenMRS REST Web Services Module binnen de context van de repository **ATx-2.4-Software-security-en-compliance**. De focus ligt zowel op de applicatie zelf als op de CI/CD-, build- en testomgeving die onderdeel uitmaken van de software supply chain.

---

# 1. Scope

## In scope

* OpenMRS REST API
* Authenticatie en autorisatie
* GitHub Actions CI/CD workflows
* CodeQL security scanning
* Maven buildproces
* Docker configuraties
* Integration tests
* Build artifacts

## Out of scope

* Onderliggende cloud- of hostinginfrastructuur
* OpenMRS Core buiten deze module
* Externe systemen zonder directe koppeling met de REST-module

---

# 2. Onderzoeksbasis

Deze analyse is gebaseerd op de volgende repository-onderdelen.

| Onderdeel                     | Locatie                        |
| ----------------------------- | ------------------------------ |
| GitHub Actions Build Pipeline | `.github/workflows/build.yml`  |
| CodeQL Security Scan          | `.github/workflows/codeql.yml` |
| Maven configuratie            | `pom.xml`                      |
| Docker configuratie           | `docker-compose.yml`           |
| Development omgeving          | `docker-compose.dev.yml`       |
| Testomgeving                  | `docker-compose.test.yml`      |
| Productieomgeving             | `docker-compose.prod.yml`      |
| Integration tests             | `integration-tests/`           |
| OpenMRS REST module           | `omod/`, `omod-common/`        |

Daarnaast is gebruikgemaakt van de projectdocumentatie en de README van de OpenMRS REST Web Services Module.

---

# 3. Overzicht Attack Surface

## 3.1 REST API

De primaire ingang van de module bestaat uit de REST API:

```text
/ws/rest/v1/*
```

Belangrijke resources:

```text
/ws/rest/v1/patient
/ws/rest/v1/person
/ws/rest/v1/encounter
/ws/rest/v1/obs
/ws/rest/v1/order
/ws/rest/v1/allergy
/ws/rest/v1/concept
```

### Endpoint-overzicht

| Endpoint                  | Methode    | Gegevens            | Vereiste rechten     | Risico   |
| ------------------------- | ---------- | ------------------- | -------------------- | -------- |
| `/ws/rest/v1/patient`     | GET        | Patiëntgegevens     | Get Patients         | Hoog     |
| `/ws/rest/v1/patient`     | POST       | Nieuwe patiënt      | Add Patients         | Hoog     |
| `/ws/rest/v1/obs`         | GET / POST | Observaties         | View/Add Observations| Hoog     |
| `/ws/rest/v1/order`       | GET / POST | Medische orders     | Manage Orders        | Kritiek  |
| `/ws/rest/v1/allergy`     | GET / POST | Allergieën          | Manage Allergies     | Kritiek  |
| `/ws/rest/v1/encounter`   | GET / POST | Klinische ontmoeten | View/Add Encounters  | Hoog     |
| `/ws/rest/v1/concept`     | GET        | Medische concepten  | View Concepts        | Laag     |

### Dataflow

```text
Client (HTTPS)
     ↓
REST API /ws/rest/v1/*
     ↓
OpenMRS Core (service-laag)
     ↓
MySQL / MariaDB
```

### Mogelijke risico's

* Ongeautoriseerde toegang tot patiëntgegevens
* Onvoldoende autorisatiecontroles
* Excessive data exposure
* Manipulatie van medische gegevens
* Misbruik van queryparameters

---

## 3.2 Authenticatie en Autorisatie

De REST-module ondersteunt:

* Session Authentication
* Basic Authentication
* Role Based Access Control (RBAC)

### Mogelijke risico's

* Session hijacking
* Credential stuffing
* Privilege escalation
* Onjuiste RBAC-configuratie

---

## 3.3 GitHub Actions CI/CD Pipeline

De repository bevat een buildworkflow:

```yaml
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  workflow_dispatch:
```

Belangrijke buildstappen:

* Checkout repository
* Java 8 setup
* Maven verify
* Upload test reports
* Build OMOD artifact
* Upload artifact

### Attack Surface

De CI-pipeline verwerkt automatisch code die naar de repository wordt gepusht of via pull requests wordt aangeboden.

### Mogelijke risico's

* Build-manipulatie via pull requests
* Supply-chain aanvallen
* Kwaadaardige dependency introductie
* Artifact manipulatie
* Workflow misbruik

---

## 3.4 CodeQL Security Scanning

De repository bevat een geautomatiseerde CodeQL workflow.

De workflow analyseert:

* Java/Kotlin code
* GitHub Actions workflows

Triggers:

* Push naar main
* Pull requests naar main
* Wekelijkse geplande scan

### Attack Surface

Security tooling vormt zelf een vertrouwenscomponent binnen de ontwikkelstraat.

### Mogelijke risico's

* False negatives
* Onvolledige detectie van kwetsbaarheden
* Verkeerde interpretatie van scanresultaten

---

## 3.5 Docker Configuraties

Aanwezige configuraties:

```text
docker-compose.yml
docker-compose.dev.yml
docker-compose.test.yml
docker-compose.prod.yml
```

### Attack Surface

* Exposed netwerkpoorten
* Environment variables
* Containercommunicatie
* Volumes en configuratiebestanden

### Mogelijke risico's

* Onbedoelde blootstelling van services
* Hardcoded configuraties
* Verkeerd geconfigureerde productieomgevingen

Op basis van de beschikbare informatie is niet vastgesteld of deze risico's daadwerkelijk aanwezig zijn.

---

## 3.6 Maven Dependencies

De build maakt gebruik van Maven:

```text
mvn clean verify
```

### Attack Surface

* Directe dependencies
* Transitive dependencies
* Maven plugins

### Mogelijke risico's

* Supply-chain aanvallen
* Dependency confusion
* Verouderde libraries met bekende kwetsbaarheden

Deze risico's dienen verder onderzocht te worden via SCA en SBOM-analyse.

---

## 3.7 Integration Tests

De README bevat het volgende voorbeeld:

```text
mvn clean verify -Pintegration-tests -DtestUrl=http://admin:Admin123@localhost:8080/openmrs
```

### Vastgestelde bevinding

Binnen de documentatie wordt een testaccount gebruikt:

```text
admin:Admin123
```

### Risico

De credentials lijken bedoeld voor een lokale testomgeving. Het risico ontstaat wanneer deze waarden in logs, documentatie, CI-output of productieachtige omgevingen worden hergebruikt.

### Aanbevelingen

* Testaccounts gescheiden houden van productieaccounts
* Geen productiecredentials gebruiken in testomgevingen
* Secrets via GitHub Secrets of een secrets manager beheren
* Credentials niet loggen

---

## 3.8 Module Uploads (.omod)

OpenMRS ondersteunt de installatie van modules via `.omod`-bestanden. Via de beheersinterface kunnen nieuwe modules worden geüpload en geactiveerd, waarbij de module als executable code binnen de applicatie wordt uitgevoerd.

### Attack Surface

* Upload van nieuwe `.omod`-modules via de beheersinterface
* Installatie van externe of niet-geverifieerde modules
* Mogelijke privilege escalation via kwaadaardige extensies

### Mogelijke risico's

* Upload van kwaadaardige modules door een aanvaller met beheertoegang
* Supply-chain aanvallen via gecompromitteerde module-bronnen
* Remote code execution via malafide extensies

**Status: Niet onderzocht**

---

## 3.9 Configuratie en Secrets

OpenMRS maakt gebruik van meerdere configuratiebronnen voor het opslaan van gevoelige gegevens zoals databasewachtwoorden en API-sleutels.

### Bronnen

* `runtime.properties` – bevat databaseverbindingsgegevens en sleutels
* Environment variables – worden meegegeven aan Docker-containers
* GitHub Secrets – gebruikt binnen CI/CD-workflows

### Mogelijke risico's

* Hardcoded secrets in configuratiebestanden of Docker Compose
* Secret leakage via logbestanden of CI-output
* Onvoldoende secret rotation
* Ongeautoriseerde toegang tot `runtime.properties`

**Status: Niet onderzocht**

---

# 4. Attack Surface Diagram

```text
          Internet
              |
           HTTPS
              |
   +----------------------+
   |  OpenMRS REST API    |
   |  /ws/rest/v1/*       |
   +----------------------+
              |
           JDBC
              |
   +----------------------+
   |   MySQL / MariaDB    |
   +----------------------+


   Developer
       |
    Git Push / Pull Request
       |
   +----------------------+
   |   GitHub Repository  |
   +----------------------+
       |
    GitHub Actions
       |
   +----------------------+
   |   Build Pipeline     |
   +----------------------+
       |
    OMOD Artifact
       |
    Deployment
```

---

# 5. Vastgestelde Bevindingen en Risico's

| Onderdeel                  | Risico                                   | Status          | Bewijs                                          |
| -------------------------- | ---------------------------------------- | --------------- | ----------------------------------------------- |
| Integration tests          | Hardcoded testcredentials                | Vastgesteld     | README voorbeeld met admin:Admin123             |
| GitHub Actions build       | Automatische builds op push en PR        | Vastgesteld     | build.yml                                       |
| Artifact generatie         | Automatische OMOD artifact upload        | Vastgesteld     | build.yml                                       |
| CodeQL scanning            | Geautomatiseerde security scan aanwezig  | Vastgesteld     | codeql.yml                                      |
| Maven dependencies         | Kwetsbare transitive dependencies        | Mogelijk risico | Verdere SCA-analyse nodig                       |
| GitHub Actions permissions | Te ruime rechten                         | Niet onderzocht | Workflow permissions niet volledig beoordeeld   |
| Docker configuraties       | Blootgestelde services                   | Niet onderzocht | Docker configuraties niet volledig geanalyseerd |
| Supply-chain risico        | Kwaadaardige dependencies                | Mogelijk risico | Maven ecosystem                                 |
| CodeQL false negatives     | Niet alle kwetsbaarheden worden gevonden | Mogelijk risico | Bekende beperking van SAST-tools                |
| Module uploads (.omod)     | Remote code execution via extensies      | Niet onderzocht | OpenMRS modulebeheer                            |
| Secrets / runtime.properties | Hardcoded of gelekte credentials       | Niet onderzocht | Configuratiebronnen niet volledig beoordeeld    |
| REST API → Database        | SQL-injectie, onvoldoende DB-rechten     | Mogelijk risico | Dataflow niet volledig geanalyseerd             |

---

# 6. High-Risk Ingangen

## HR1 – REST API

Waarom kritisch:

* Toegang tot patiëntgegevens
* Toegang tot observaties
* Toegang tot medische orders

CIA:

* Vertrouwelijkheid
* Integriteit

---

## HR2 – Authenticatie

Waarom kritisch:

* Toegangspoort tot alle API-functionaliteit

CIA:

* Vertrouwelijkheid
* Integriteit
* Beschikbaarheid

---

## HR3 – GitHub Actions CI/CD

Waarom kritisch:

* Automatische verwerking van codewijzigingen
* Artifact generatie

CIA:

* Integriteit
* Beschikbaarheid

---

## HR4 – Maven Dependency Chain

Waarom kritisch:

* Vertrouwen op externe softwarecomponenten

CIA:

* Integriteit

---

## HR5 – Docker Deployment Configuraties

Waarom kritisch:

* Productie- en testomgevingen worden geconfigureerd via Docker

CIA:

* Vertrouwelijkheid
* Beschikbaarheid

---

## HR6 – Module Uploads (.omod)

Waarom kritisch:

* Directe uitvoering van externe code binnen de applicatie
* Vereist beheertoegang, maar misbruik leidt tot volledige systeemcompromittatie

CIA:

* Vertrouwelijkheid
* Integriteit
* Beschikbaarheid

---

# 7. Trust Boundaries

## TB1 – Externe Client → REST API

```text
Internet
 ↓
REST API
```

Alle input afkomstig van externe gebruikers wordt beschouwd als onbetrouwbaar.

---

## TB2 – Repository → CI Pipeline

```text
Developer
 ↓
GitHub Repository
 ↓
GitHub Actions
```

Code die naar de repository wordt gepusht wordt automatisch verwerkt door de CI-pipeline.

Niet onderzocht is of aanvullende goedkeuringsmechanismen zoals verplichte reviews of deployment approvals aanwezig zijn.

---

## TB3 – CI Pipeline → Build Artifacts

```text
GitHub Actions
 ↓
OMOD Artifact
```

Artifacts worden automatisch gegenereerd en vervolgens vertrouwd door volgende processtappen.

---

## TB4 – CodeQL → Security Besluitvorming

```text
CodeQL
 ↓
Security Resultaten
 ↓
Ontwikkelteam
```

Er wordt vertrouwd op de juistheid van scanresultaten.

---

## TB5 – Docker Host → Containers

```text
Docker Host
 ↓
OpenMRS Containers
```

Containerconfiguraties bepalen runtime gedrag en netwerktoegang.

---

## TB6 – REST API → Database

```text
REST API
 ↓
MySQL / MariaDB
```

REST-resources communiceren direct met de onderliggende database. Onjuiste queryopbouw of onvoldoende databaserechten vormen een risico.

### Mogelijke risico's

* SQL-injectie
* Onvoldoende database-rechten per gebruikersrol
* Directe blootstelling van patiëntgegevens bij databaselekken

---

# 8. Impliciet Vertrouwen

## IV1 – Pull Requests

Aangenomen wordt dat aangeleverde code geen kwaadaardige functionaliteit bevat.

---

## IV2 – GitHub Actions

Aangenomen wordt dat gebruikte GitHub Actions veilig zijn en niet gecompromitteerd zijn.

---

## IV3 – Maven Dependencies

Aangenomen wordt dat externe libraries betrouwbaar zijn.

---

## IV4 – CodeQL Resultaten

Aangenomen wordt dat kwetsbaarheden correct worden gedetecteerd.

---

## IV5 – Docker Configuraties

Aangenomen wordt dat configuraties veilig zijn voor productiegebruik.

---

## IV6 – Module-integriteit (.omod)

Aangenomen wordt dat geïnstalleerde modules afkomstig zijn van betrouwbare bronnen en geen kwaadaardige code bevatten.

---

## IV7 – Configuratie en Secrets

Aangenomen wordt dat `runtime.properties` en environment variables geen gevoelige gegevens bevatten die onbedoeld worden blootgesteld.

---

# 9. Update Threat Model

Op basis van deze analyse worden de volgende dreigingen toegevoegd aan het bestaande threat model.

| ID  | Dreiging                                   |
| --- | ------------------------------------------ |
| T12 | Manipulatie van GitHub Actions workflows   |
| T13 | Supply-chain aanval via Maven dependencies |
| T14 | Kwaadaardige code via pull requests        |
| T15 | Docker misconfiguratie                     |
| T16 | Onvoldoende detectie door security tooling |
| T17 | Remote code execution via .omod upload     |
| T18 | Secret leakage via runtime.properties      |
| T19 | SQL-injectie via REST API naar database    |

---

# 10. Relatie met NEN 7510 en ISO 27001

| Onderwerp                    | Relevantie                           |
| ---------------------------- | ------------------------------------ |
| Authenticatie en autorisatie | Toegangsbeheer                       |
| Logging en monitoring        | Detectie en auditing                 |
| Secure software development  | Veilige ontwikkelprocessen           |
| Vulnerability management     | Kwetsbaarhedenbeheer                 |
| Dependency management        | Supply-chain security                |
| Secrets management           | Bescherming van toegangsgegevens     |
| Security testing             | Continue verificatie van beveiliging |
| Module-integriteit           | Beheersing van softwarecomponenten   |

De geïdentificeerde attack surface raakt meerdere onderdelen van NEN 7510 en ISO 27001 die relevant zijn voor veilige verwerking van medische gegevens.

---

# 11. Conclusie

De belangrijkste attack surfaces binnen deze repository zijn de OpenMRS REST API, de GitHub Actions CI/CD-pipeline, de Maven dependency chain en de Docker-gebaseerde deploymentconfiguraties.

Naast vastgestelde bevindingen, zoals automatische builds, artifact generatie en het gebruik van testcredentials, zijn ook meerdere potentiële risico's geïdentificeerd. Aanvullend zijn drie onderdelen toegevoegd die eerder ontbraken: de dataflow van de REST API naar de onderliggende database (TB6), de mogelijkheid tot het uploaden van `.omod`-modules als attack surface (3.8), en het beheer van configuratie en secrets via `runtime.properties` en environment variables (3.9).

Deze aanvullingen vereisen aanvullende analyse, bijvoorbeeld via SCA, SBOM-generatie en een diepgaand onderzoek van Docker-, workflow- en secrets-configuraties.

Door onderscheid te maken tussen vastgestelde bevindingen, mogelijke risico's en niet-onderzochte onderdelen ontstaat een beter onderbouwde, reproduceerbare en auditwaardige attack-surface-analyse.