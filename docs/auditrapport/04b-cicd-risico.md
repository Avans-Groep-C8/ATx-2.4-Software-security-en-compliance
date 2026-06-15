# 4b. CI/CD Risicoanalyse

## Doel

Deze risicoanalyse richt zich uitsluitend op het CI/CD-proces van de OpenMRS REST Web Services Module. De scope omvat de GitHub-repository, GitHub Actions-workflows, deploymentprocessen, secretsbeheer, SAST, secret scanning, SCA en SBOM-generatie.

Applicatierisico's zoals ongeautoriseerde API-toegang, patiëntdatalekken en RBAC-fouten vallen buiten deze analyse.

---

## Scoremethode

**Risicoscore = Kans × Impact** (schaal 1–5)

| Score | Niveau     |
| ----- | ---------- |
| 15–25 | 🔴 Kritiek |
| 10–14 | 🟠 Hoog    |
| 5–9   | 🟡 Midden  |
| 1–4   | 🟢 Laag    |

---

## CI/CD Dreigingen

| ID | Dreiging                                                              | Kans | Impact |  Score | Niveau     |
| -- | --------------------------------------------------------------------- | :--: | :----: | :----: | ---------- |
| C1 | Secret leak in repository of CI/CD-pipeline                           |   4  |    5   | **20** | 🔴 Kritiek |
| C2 | Supply-chain aanval via GitHub Action of dependency                   |   3  |    5   | **15** | 🔴 Kritiek |
| C3 | Ongeautoriseerde wijziging van pipeline-configuratie                  |   3  |    4   | **12** | 🟠 Hoog    |
| C4 | Ontbrekende deployment approval naar productie                        |   3  |    4   | **12** | 🟠 Hoog    |
| C5 | False negative bij SAST/SCA waardoor kwetsbaarheid onopgemerkt blijft |   3  |    3   |  **9** | 🟡 Midden  |
| C6 | False positives blokkeren deploymentproces                            |   3  |    2   |  **6** | 🟡 Midden  |
| C7 | Ontbrekende of verouderde SBOM                                        |   2  |    3   |  **6** | 🟡 Midden  |
| C8 | Uitval van GitHub Actions of CI/CD-platform                           |   2  |    4   |  **8** | 🟡 Midden  |

---

## Relatie met CI/CD Beveiligingscontroles

| Controle              | Primair doel                                                                                  |
| --------------------- | --------------------------------------------------------------------------------------------- |
| Secret Scanning       | Detecteren van hardcoded secrets, API-keys, tokens, certificaten en wachtwoorden              |
| SAST                  | Detecteren van kwetsbaarheden in broncode zoals injection-, authenticatie- en validatiefouten |
| SCA                   | Detecteren van kwetsbare externe dependencies en bekende CVE's                                |
| SBOM                  | Inventariseren van gebruikte softwarecomponenten en afhankelijkheden                          |
| MFA                   | Beschermen van accounts tegen ongeautoriseerde toegang                                        |
| Branch Protection     | Voorkomen van ongeautoriseerde wijzigingen aan kritieke code en workflows                     |
| Deployment Protection | Verplichte goedkeuringen voor deployments naar productie                                      |

De verschillende controles mitigeren specifieke risico's:

| Risico                               | Belangrijkste controles                                                         |
| ------------------------------------ | ------------------------------------------------------------------------------- |
| C1 – Secret Leak                     | Secret Scanning, MFA, Least Privilege, Branch Protection, Deployment Protection |
| C2 – Supply-Chain Aanval             | SCA, SBOM, Dependency Review, Change Management                                 |
| C3 – Wijziging Pipeline Configuratie | Branch Protection, Pull Request Reviews, Change Management                      |
| C4 – Ontbrekende Deployment Approval | Deployment Protection, Change Management                                        |
| C5 – False Negative SAST/SCA         | SAST, SCA, periodieke validatie van scanresultaten                              |
| C6 – False Positives                 | Afstemming van scanregels en reviewprocessen                                    |
| C7 – Ontbrekende SBOM                | SBOM-generatie en dependencybeheer                                              |
| C8 – Uitval CI/CD Platform           | Beschikbaarheid- en continuïteitsmaatregelen                                    |

---

## Koppeling met NEN 7510

De geïdentificeerde risico's zijn gekoppeld aan relevante beheersmaatregelen uit NEN 7510.

| Risico                               | Relevante NEN 7510-control                    | Toelichting                                                                                                    |
| ------------------------------------ | --------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| C1 – Secret Leak                     | A.8.5 Secure Authentication                   | MFA voor GitHub-accounts en beheerders vermindert het risico op misbruik van gecompromitteerde accounts.       |
| C1 – Secret Leak                     | A.8.15 Logging                                | Logging en monitoring van repositories, workflows en secrets ondersteunen detectie en incidentonderzoek.       |
| C2 – Supply-Chain Aanval             | A.8.8 Management of Technical Vulnerabilities | SCA-scans en SBOM's helpen kwetsbare afhankelijkheden tijdig te identificeren.                                 |
| C2 – Supply-Chain Aanval             | A.8.32 Change Management                      | Wijzigingen aan dependencies, GitHub Actions en buildprocessen moeten gecontroleerd en beoordeeld worden.      |
| C3 – Wijziging Pipeline Configuratie | A.8.32 Change Management                      | Branch protection, pull-request reviews en workflow-goedkeuringen beperken ongeautoriseerde wijzigingen.       |
| C4 – Ontbrekende Deployment Approval | A.8.32 Change Management                      | Verplichte goedkeuringen voor productie-deployments verminderen het risico op ongecontroleerde wijzigingen.    |
| C5 – False Negative SAST/SCA         | A.8.8 Management of Technical Vulnerabilities | Regelmatige updates van scanregels, dependency databases en tooling verbeteren de detectie van kwetsbaarheden. |
| C7 – Ontbrekende SBOM                | A.8.8 Management of Technical Vulnerabilities | Een actuele SBOM ondersteunt impactanalyses bij nieuwe CVE's en supply-chain incidenten.                       |
| C8 – Uitval CI/CD Platform           | A.8.31 Separation of Environments             | Gescheiden build-, test- en productieomgevingen beperken operationele impact van verstoringen.                 |

---

## Toelichting per dreiging

### C1 — Secret Leak

Secrets zoals GitHub Tokens, deployment keys of registry credentials worden openbaar via source code, logs of workflowconfiguratie.

**Belangrijkste beheersmaatregelen**

* Secret Scanning
* MFA
* Least Privilege
* Branch Protection
* Deployment Protection

**Impact**

* Ongeautoriseerde toegang tot systemen
* Kwaadaardige deployments
* Mogelijke datalekken

---

### C2 — Supply-Chain Aanval

Een gecompromitteerde dependency of GitHub Action introduceert kwaadaardige code in de build.

**Belangrijkste beheersmaatregelen**

* Software Composition Analysis (SCA)
* SBOM-generatie
* Dependency Review
* Change Management

**Impact**

* Manipulatie van build-artifacten
* Backdoors in productie

---

### C3 — Wijziging Pipeline Configuratie

Een aanvaller wijzigt GitHub Actions-workflows of deploymentscripts.

**Belangrijkste beheersmaatregelen**

* Branch Protection
* Pull Request Reviews
* Workflow-goedkeuringen
* Change Management

**Impact**

* Omzeilen van beveiligingscontroles
* Deployen van ongeautoriseerde code

---

### C4 — Ontbrekende Deployment Approval

Code wordt zonder handmatige goedkeuring naar productie gedeployed.

**Belangrijkste beheersmaatregelen**

* Deployment Protection Rules
* Handmatige approvals
* Change Management

**Impact**

* Verhoogde kans op fouten of kwetsbaarheden in productie

---

### C5 — False Negative SAST/SCA

Een kwetsbaarheid wordt niet gedetecteerd door automatische beveiligingsscans.

**Belangrijkste beheersmaatregelen**

* Actuele SAST-regels
* Actuele vulnerability databases
* Regelmatige validatie van scanresultaten

**Impact**

* Kwetsbare software bereikt productie

---

### C6 — False Positives

Onterechte meldingen blokkeren builds of deployments.

**Belangrijkste beheersmaatregelen**

* Afstemming van scanregels
* Security reviewproces
* Periodieke tuning van tooling

**Impact**

* Vertraging van ontwikkel- en releaseprocessen

---

### C7 — Ontbrekende SBOM

Geen actueel overzicht van gebruikte softwarecomponenten.

**Belangrijkste beheersmaatregelen**

* Geautomatiseerde SBOM-generatie
* Dependencybeheer
* Versiebeheer van build-artifacten

**Impact**

* Moeilijke impactanalyse bij nieuwe CVE's
* Verminderde zichtbaarheid op afhankelijkheden

---

### C8 — Uitval CI/CD Platform

GitHub Actions of andere buildinfrastructuur is tijdelijk niet beschikbaar.

**Belangrijkste beheersmaatregelen**

* Monitoring
* Back-upprocedures
* Scheiding van omgevingen
* Continuïteitsmaatregelen

**Impact**

* Geen builds of deployments mogelijk

---

## Risicomatrix

| Kans \ Impact | 1 | 2  | 3  | 4      | 5  |
| ------------- | - | -- | -- | ------ | -- |
| **5**         |   |    |    |        |    |
| **4**         |   |    |    |        | C1 |
| **3**         |   | C6 | C5 | C3, C4 | C2 |
| **2**         |   |    | C7 | C8     |    |
| **1**         |   |    |    |        |    |

---

## Meest Kritieke Risico

Het risico met de hoogste score is:

**C1 – Secret Leak in de CI/CD-pipeline (Score: 20)**

Dit risico wordt verder uitgewerkt in de bow-tie analyse.

### Motivatie

Een secret leak kan direct leiden tot ongeautoriseerde toegang tot repositories, CI/CD-systemen en deploymentomgevingen. Omdat CI/CD-pipelines vaak beschikken over verhoogde rechten en toegang tot kritieke systemen, kan een gelekt geheim worden misbruikt voor kwaadaardige deployments, manipulatie van build-artifacten of verdere compromittering van de softwareleveringsketen.

Hoewel supply-chain aanvallen (C2) eveneens een kritieke impact hebben, wordt het risico op een secret leak als waarschijnlijker beschouwd vanwege menselijke fouten, onjuiste configuraties en het per ongeluk publiceren van credentials in broncode, configuratiebestanden of logs.
