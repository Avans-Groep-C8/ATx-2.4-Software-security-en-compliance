# 4b. CI/CD Risicoanalyse

## Doel

Deze risicoanalyse richt zich uitsluitend op het CI/CD-proces van de OpenMRS REST Web Services Module. De scope omvat de GitHub-repository, GitHub Actions-workflows, deploymentproces, secretsbeheer, SAST, SCA en SBOM-generatie.

Applicatierisico's zoals ongeautoriseerde API-toegang, patiëntdatalekken en RBAC-fouten vallen buiten deze analyse.

---

## Scoremethode

**Risicoscore = Kans × Impact** (schaal 1–5)

| Score | Niveau |
|---------|---------|
| 15–25 | 🔴 Kritiek |
| 10–14 | 🟠 Hoog |
| 5–9 | 🟡 Midden |
| 1–4 | 🟢 Laag |

---

## CI/CD Dreigingen

| ID | Dreiging | Kans | Impact | Score | Niveau |
|----|-----------|:----:|:------:|:-----:|---------|
| C1 | Secret leak in repository of CI/CD-pipeline | 4 | 5 | **20** | 🔴 Kritiek |
| C2 | Supply-chain aanval via GitHub Action of dependency | 3 | 5 | **15** | 🔴 Kritiek |
| C3 | Ongeautoriseerde wijziging van pipeline-configuratie | 3 | 4 | **12** | 🟠 Hoog |
| C4 | Ontbrekende deployment approval naar productie | 3 | 4 | **12** | 🟠 Hoog |
| C5 | False negative bij SAST/SCA waardoor kwetsbaarheid onopgemerkt blijft | 3 | 3 | **9** | 🟡 Midden |
| C6 | False positives blokkeren deploymentproces | 3 | 2 | **6** | 🟡 Midden |
| C7 | Ontbrekende of verouderde SBOM | 2 | 3 | **6** | 🟡 Midden |
| C8 | Uitval van GitHub Actions of CI/CD-platform | 2 | 4 | **8** | 🟡 Midden |

---

## Toelichting per dreiging

### C1 — Secret Leak
Secrets zoals GitHub Tokens, deployment keys of registry credentials worden openbaar via source code, logs of workflowconfiguratie.

**Impact**
- Ongeautoriseerde toegang tot systemen
- Kwaadaardige deployments
- Mogelijke datalekken

---

### C2 — Supply-Chain Aanval
Een gecompromitteerde dependency of GitHub Action introduceert kwaadaardige code in de build.

**Impact**
- Manipulatie van build-artifacten
- Backdoors in productie

---

### C3 — Wijziging Pipeline Configuratie
Een aanvaller wijzigt GitHub Actions-workflows of deploymentscripts.

**Impact**
- Omzeilen van beveiligingscontroles
- Deployen van ongeautoriseerde code

---

### C4 — Ontbrekende Deployment Approval
Code wordt zonder handmatige goedkeuring naar productie gedeployed.

**Impact**
- Verhoogde kans op fouten of kwetsbaarheden in productie

---

### C5 — False Negative SAST/SCA
Een kwetsbaarheid wordt niet gedetecteerd door automatische beveiligingsscans.

**Impact**
- Kwetsbare software bereikt productie

---

### C6 — False Positives
Onterechte meldingen blokkeren builds of deployments.

**Impact**
- Vertraging van ontwikkel- en releaseprocessen

---

### C7 — Ontbrekende SBOM
Geen actueel overzicht van gebruikte softwarecomponenten.

**Impact**
- Moeilijke impactanalyse bij nieuwe CVE's

---

### C8 — Uitval CI/CD Platform
GitHub Actions of andere buildinfrastructuur is tijdelijk niet beschikbaar.

**Impact**
- Geen builds of deployments mogelijk

---

## Risicomatrix

| Kans \ Impact | 1 | 2 | 3 | 4 | 5 |
|---------------|---|---|---|---|---|
| **5** | | | | | |
| **4** | | | | C1 | C1 |
| **3** | | C6 | C5 | C3, C4 | C2 |
| **2** | | | C7 | C8 | |
| **1** | | | | | |

---

## Meest Kritieke Risico

Het risico met de hoogste score is:

**C1 – Secret Leak in de CI/CD-pipeline (Score: 20)**

Dit risico wordt verder uitgewerkt in de bow-tie analyse.