# 4b. Bow-Tie Analyse – Secret Leak in CI/CD Pipeline

## Geselecteerd risico

**C1 – Secret Leak in de CI/CD Pipeline**

Risicoscore: **20 (Kritiek)**

Dit top event is geselecteerd op basis van de CI/CD-risicoanalyse.  
Zie: **04b-cicd-risico.md**, waarin C1 als hoogste risico (score 20) is geïdentificeerd en als uitgangspunt dient voor deze bow-tie analyse.

Dit risico heeft de hoogste score binnen de CI/CD-risicoanalyse en kan leiden tot ongeautoriseerde toegang tot build-, deployment- en productieomgevingen.



## Hazard

**Aanwezigheid van gevoelige secrets en deployment-credentials binnen de CI/CD-pipeline.**

De hazard is niet het secret-lek zelf, maar de situatie waarin de CI/CD-pipeline toegang heeft tot gevoelige informatie zoals API-keys, access tokens, service accounts, deployment keys en cloudcredentials. Deze secrets zijn noodzakelijk voor build-, test- en deploymentprocessen en vormen daardoor een aantrekkelijk doelwit.

Wanneer beveiligingsmaatregelen rondom opslag, gebruik, logging of toegangsbeheer tekortschieten, kunnen deze secrets worden blootgesteld aan onbevoegden. Dit kan leiden tot ongeautoriseerde toegang tot systemen, manipulatie van softwaredeployments, misbruik van cloudresources en mogelijk een datalek.


## Bow-Tie Overzicht

```mermaid
flowchart LR
    H["Hazard: CI/CD pipeline verwerkt gevoelige secrets en deployment credentials"]
    TE(("T1: Secret Leak in CI/CD Pipeline"))

    T1["Threat: hardcoded secrets in repository"]
    T2["Threat: secrets zichtbaar in workflow logs"]
    T3["Threat: gecompromitteerd ontwikkelaarsaccount"]
    T4["Threat: onvoldoende toegangsbeheer tot repositories en secrets"]
    T5["Threat: foutieve configuratie van GitHub Actions"]

    PB1["Preventieve barriere: secret scanning en pre-commit controles - A.8.28 / A.8.29"]
    PB2["Preventieve barriere: log masking en secret redaction - A.8.15"]
    PB3["Preventieve barriere: MFA en account monitoring - A.8.5"]
    PB4["Preventieve barriere: least privilege en RBAC - A.5.15 / A.8.3"]
    PB5["Preventieve barriere: workflow reviews en branch protection - A.8.32"]

    C1["Gevolg: ongeautoriseerde toegang tot systemen"]
    C2["Gevolg: kwaadaardige deployment van software"]
    C3["Gevolg: misbruik van cloudresources"]
    C4["Gevolg: manipulatie van build-artifacten"]
    C5["Gevolg: mogelijk datalek"]

    RB1["Herstelbarriere: detectie van gelekte secrets en SIEM-alerting - A.8.15 / A.8.16"]
    RB2["Herstelbarriere: directe secret rotation en credential revocation - A.5.24"]
    RB3["Herstelbarriere: incident response procedure - A.5.24 / A.5.25 / A.5.26"]
    RB4["Herstelbarriere: deployment rollback en artifact validatie - A.8.32"]

    H --> TE

    T1 --> PB1 --> TE
    T2 --> PB2 --> TE
    T3 --> PB3 --> TE
    T4 --> PB4 --> TE
    T5 --> PB5 --> TE

    TE --> RB1 --> C1
    TE --> RB2 --> C2
    TE --> RB2 --> C3
    TE --> RB4 --> C4
    TE --> RB3 --> C5

    classDef hazard fill:#f4b183,stroke:#a65e00,color:#000,stroke-width:2px;
    classDef threat fill:#f8cbad,stroke:#c00000,color:#000,stroke-width:2px;
    classDef preventive fill:#ffe699,stroke:#bf9000,color:#000,stroke-width:2px;
    classDef top fill:#c00000,stroke:#7f0000,color:#fff,stroke-width:3px;
    classDef recovery fill:#9dc3e6,stroke:#2f75b5,color:#000,stroke-width:2px;
    classDef consequence fill:#c6e0b4,stroke:#548235,color:#000,stroke-width:2px;

    class H hazard;
    class T1,T2,T3,T4,T5 threat;
    class PB1,PB2,PB3,PB4,PB5 preventive;
    class TE top;
    class RB1,RB2,RB3,RB4 recovery;
    class C1,C2,C3,C4,C5 consequence;
```
---

## Preventieve Maatregelen

### Secrets Management

- Gebruik van GitHub Secrets
- Geen credentials in source code
- Secrets opslaan in beveiligde secret stores

### Secret Scanning

- GitHub Secret Scanning
- Gitleaks
- TruffleHog

### Toegangsbeheer

- Least Privilege principe
- Rolgebaseerde toegang
- Regelmatige review van toegangsrechten

### Authenticatie

- Verplichte Multi-Factor Authentication (MFA)
- Sterke wachtwoordvereisten

### Workflow Beveiliging

- Branch Protection Rules
- Pull Request Reviews
- Verplichte goedkeuring van workflowwijzigingen

---

## Correctieve Maatregelen

Wanneer een secret-lek wordt vastgesteld:

### Incident Response

- Incident registreren
- Omvang van het lek bepalen
- Onderzoeken welke systemen zijn geraakt

### Intrekken Toegang

- Tokens direct ongeldig maken
- Deployment keys vervangen
- Accounts tijdelijk blokkeren

### Containment

- CI/CD-pipeline tijdelijk stilleggen
- Verdachte deployments blokkeren

---

## Herstelmaatregelen

### Secret Rotatie

- Nieuwe API-keys genereren
- Nieuwe deployment credentials uitrollen

### Herstel Omgeving

- Buildomgeving controleren
- Deploymentomgeving opnieuw valideren

### Security Review

- Controle van repositoryhistorie
- Controle van workflowconfiguraties
- Controle van toegangsrechten

### Lessons Learned

- Evaluatie van oorzaak
- Verbetering van processen en controles

---

## Relatie met CI/CD Beveiligingscontroles

| Onderdeel | Relatie met risico |
|------------|-------------------|
| Pipeline | Secrets worden gebruikt tijdens build en deployment |
| Secrets | Direct doelwit van het risico |
| SAST | Secret Scanning |
| SCA | Beperkt risico op kwetsbare dependencies |
| SBOM | Ondersteunt impactanalyse bij incidenten |
| Deployment Approval | Verkleint kans op misbruik van gelekte credentials |

---

## Conclusie

Een secret leak vormt het grootste risico binnen het CI/CD-proces. Door toepassing van secret management, scanning, toegangscontrole en incidentresponsmaatregelen kan zowel de kans als de impact van dit risico aanzienlijk worden verminderd.
