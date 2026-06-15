@ -0,0 +1,339 @@
# 10. Attack Surface Analyse – OpenMRS REST Module

## Doel

Deze analyse identificeert alle relevante ingangen (attack surface) van de OpenMRS REST Web Services Module binnen de context van de huidige repository. De focus ligt op zowel de applicatie (REST API) als het CI/CD- en buildproces.

---

# 1. Scope

## In scope

- REST API van OpenMRS
- GitHub Actions CI/CD workflows
- CodeQL security scanning pipeline
- Maven buildproces
- Docker build & deployment configuraties
- Integration tests
- Artifact generatie

## Out of scope

- Onderliggende infrastructuur (cloud/hosting)
- Externe systemen zonder directe integratie
- OpenMRS core buiten deze module

---

# 2. Overzicht van Attack Surface

## 2.1 REST API (primaire ingang)

De module exposeert endpoints via:

```
/ws/rest/v1/*
```

Voorbeelden:

- /patient
- /encounter
- /obs
- /order
- /allergy
- /concept

### Risico’s

- Ongeautoriseerde toegang tot medische data
- Manipulatie van patiëntinformatie
- Data-exfiltratie via query parameters
- Overexposure van gevoelige data

---

## 2.2 Authenticatie & Autorisatie

Mechanismen:

- Session-based authentication
- Basic Authentication
- Role-Based Access Control (RBAC)

### Risico’s

- Session hijacking
- Credential stuffing
- Onjuiste RBAC-configuratie
- Privilege escalation

---

## 2.3 CI/CD Pipeline (GitHub Actions)

### Build workflow

Triggers:

- push naar `main`
- pull_request naar `main`
- manual workflow_dispatch

Belangrijke stappen:

- Maven build (`mvn clean verify`)
- Unit + integration tests
- Artifact upload (OMOD module)
- Java 8 build environment

### High-risk aspecten

- Build draait automatisch op elke push
- Artifacts worden automatisch gegenereerd
- CI heeft toegang tot repository context
- Geen expliciete deployment approval gate

### Attack surface

- Injectie in build via pull request
- Manipulatie van build artifacts
- Misbruik van workflow permissions
- Dependency poisoning via Maven

---

## 2.4 CodeQL Security Pipeline

Workflow:

- Analyse van Java/Kotlin code
- Analyse van GitHub Actions workflows
- Scheduled scans (cron job)
- Security events write permissions

### High-risk aspecten

- Automatische security scanning zonder blocking gate
- False negatives in CodeQL
- Vertrouwen op default query sets

### Attack surface

- Onopgemerkte kwetsbaarheden in PR’s
- Supply chain vulnerabilities in dependencies
- Misconfiguratie van security scanning

---

## 2.5 Docker / Container Attack Surface

Bestanden:

- docker-compose.yml
- docker-compose.dev.yml
- docker-compose.test.yml
- docker-compose.prod.yml

### Ingangen

- Exposed ports (REST API)
- Environment variables (mogelijke secrets)
- Container-to-container communicatie

### Risico’s

- Onbedoelde public exposure van services
- Hardcoded credentials in env files
- Privilege escalation binnen containers
- Misconfiguratie van production environment

---

## 2.6 Maven Build & Dependencies

Build:

```
mvn clean verify
```

### Attack surface

- External Maven dependencies
- Transitive dependencies
- Plugin execution tijdens build

### Risico’s

- Dependency confusion attack
- Malicious transitive dependency
- Outdated libraries met CVE’s

---

## 2.7 Integration Tests

Uitvoering:

```
mvn verify -Pintegration-tests
```

Test endpoint:

```
http://admin:Admin123@localhost:8080/openmrs
```

### Attack surface

- Hardcoded test credentials
- Test environment exposure
- Automatische API calls met admin rechten

### Risico’s

- Credential leakage
- Herbruikbare testaccounts in productie-achtige context
- Onveilige testconfiguratie

---

# 3. High-Risk Ingangen

## HR1 – REST API (/ws/rest/v1/*)

- Directe toegang tot patiëntdata
- CRUD operaties op medische gegevens

---

## HR2 – CI/CD GitHub Actions

- Automatische build en test uitvoering
- Mogelijkheid tot supply chain injectie

---

## HR3 – CodeQL workflow

- Security scanning afhankelijk van configuratie
- Mogelijke false negatives

---

## HR4 – Docker Compose configuraties

- Mogelijke blootstelling van services
- Environment-based secrets

---

## HR5 – Maven dependency chain

- Externe library risico’s
- Transitive dependency attacks

---

## HR6 – Integration test credentials

- Hardcoded admin credentials
- Herbruikbare login context

---

# 4. Trust Boundaries

## TB1 – External Client → REST API

Internet input wordt volledig ontrusted beschouwd.

---

## TB2 – GitHub Repository → CI Pipeline

Code in repository wordt automatisch vertrouwd door CI.

---

## TB3 – CI Pipeline → Build Artifacts

Artifacts worden automatisch gegenereerd en vertrouwd.

---

## TB4 – CodeQL → Security Decision Layer

Security scan resultaten worden gebruikt als “vertrouwensindicator”.

---

## TB5 – Docker Host → Container Runtime

Container configuraties bepalen runtime gedrag.

---

# 5. Impliciet Vertrouwen

## IV1 – Vertrouwen in pull requests

Aangenomen dat PR’s geen kwaadaardige code bevatten.

---

## IV2 – Vertrouwen in GitHub Actions

Aangenomen dat actions (v4) veilig en niet gecompromitteerd zijn.

---

## IV3 – Vertrouwen in Maven dependencies

Aangenomen dat externe libraries betrouwbaar zijn.

---

## IV4 – Vertrouwen in CodeQL resultaten

Aangenomen dat security scanning volledig is.

---

## IV5 – Vertrouwen in Docker configuraties

Aangenomen dat production configs correct en veilig zijn.

---

# 6. Update Threat Model

Op basis van deze attack surface worden de volgende dreigingen expliciet toegevoegd:

| ID | Dreiging |
|----|-----------|
| T12 | CI/CD pipeline manipulation via GitHub Actions |
| T13 | Supply chain attack via Maven dependencies |
| T14 | Code injection via pull request builds |
| T15 | Docker misconfiguration leading to exposure |
| T16 | False sense of security from CodeQL scanning |

---

# 7. Conclusie

De attack surface van deze repository bestaat uit meerdere lagen:

1. REST API (hoogste risico)
2. CI/CD pipeline (GitHub Actions)
3. Code security scanning (CodeQL)
4. Docker deployment configuraties
5. Dependency management (Maven)
6. Testomgeving met elevated credentials

De combinatie van automatische builds, dependency management en REST exposure maakt dit systeem gevoelig voor supply chain en configuration-based attacks.

Het CI/CD-systeem vormt een kritieke uitbreiding van de attack surface naast de applicatie zelf.