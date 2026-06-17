# 5.6 Mini-complianceverslag - Pipeline (compliant by design)

**Onderwerp:** geplande CI/CD-pipeline voor de OpenMRS `webservices.rest`-module
**Norm:** NEN-7510:2024-2 (Beheersmaatregelen, hoofdstuk 5 en 8)
**Datum:** 3 juni 2026

## 1. Inleiding

Dit mini-complianceverslag is opgesteld **voordat** de pipeline werd gebouwd, volgens het principe *compliant by design*: regelgeving is een ontwerpeis, geen afvinklijstje achteraf. Het hoort bij workshop WS02 (beveiligde pipeline) en beschrijft per relevante NEN-7510:2024-2 control wat de control vereist, hoe we de CI/CD-pipeline gaan inrichten om eraan te voldoen, en welk restrisico er op het moment van schrijven nog is.

Het team werkt **trunk-based**: er is één langlevende branch (`main`) en geen `test`/`develop`-branches. Wijzigingen komen uitsluitend via een pull request naar `main`, met minimaal één verplichte review en verplichte CI-checks vóór de merge. Omgevingsscheiding (OTAP) wordt niet via branches geregeld maar via GitHub Environments en aparte `docker-compose`-configuraties.

De pipeline wordt apart beoordeeld van de applicatiecode; die laatste komt aan bod in de [gap-analyse van de applicatie](01-gap-analyse.md). Samen vormen ze de technische onderbouwing voor het eindauditrapport (WS06). De controlkeuze volgt de relevante controls uit WS02.

## 2. Plan per control

Per control: de eis (wat de norm vraagt), de geplande pipeline-maatregel, en het restrisico (wat nog niet is ingericht en waarom).

| Control (NEN-7510-2) | Eis (kort) | Geplande pipeline-maatregel | Restrisico |
|---|---|---|---|
| **8.4 / 8.32** Toegang broncode & wijzigingsbeheer | Toegang tot broncode is beperkt; elke wijziging is traceerbaar naar een persoon. | Trunk-based op `main` met branch protection/ruleset: alleen via PR, minimaal 1 review, alle CI-checks moeten slagen, force-push en directe deletes geblokkeerd (ook voor admins). MFA verplicht voor alle GitHub-accounts; RBAC op de repo; GitHub Audit Log. | Signed commits (PGP) nog niet ingericht; ruleset moet nog worden vastgelegd en aantoonbaar gemaakt (export/screenshot). |
| **8.8** Beheer technische kwetsbaarheden | Kwetsbaarheden tijdig identificeren, beoordelen en verhelpen. | SAST (CodeQL) in CI; SCA op dependencies (Dependabot en/of Snyk); SBOM in CycloneDX-formaat. Patchbeleid op CVSS: kritiek (9-10) <= 24 uur, hoog (7-8.9) <= 1 week, midden (4-6.9) volgende sprint, laag (<4) geplande release. | Patchbeleid moet formeel worden vastgelegd; afweging rond "dependency cooldown" bij supply-chain-risico nog te maken. |
| **8.9** Configuratiebeheer | Configuraties beheerd, gedocumenteerd en beschermd tegen ongeautoriseerde wijziging. | Pipeline-as-code: workflows in `.github/workflows/`, versiebeheerd en reviewable. Quality gates blokkeren bij falen. Secrets via GitHub (Environment) Secrets, nooit hardcoded. Immutable artifacts (een fix is een nieuwe build). Externe actions op vaste commit-hash (SHA-pin). | Alerting op wijzigingen in workflow-bestanden nog in te richten (zie 8.16). |
| **8.16** Monitoring van de pipeline | Activiteiten worden gelogd en gecontroleerd op afwijkingen. | GitHub Security tab + Dependabot alerts; GitHub Audit Log (exporteerbaar naar SIEM); webhooks/notificaties bij kritieke events (mislukte runs, secret-gebruik, wijziging van workflows, deployment-events). | SIEM-koppeling en alerting nog niet ingericht; grotendeels op organisatieniveau te beleggen. |
| **8.25** Beveiligen tijdens ontwikkelen | Security by design: secure coding, threat modeling, gescheiden omgevingen, authenticatie/autorisatie als ontwerpeis, safe defaults. | Security-stappen ingebed in de pipeline vanaf de eerste commit; threat model in de designfase (bijv. STRIDE); omgevingsscheiding (zie 8.31); veilige standaardconfiguratie. | Threat model moet nog worden opgesteld en gedocumenteerd. |
| **8.28** Veilig programmeren | Vastgestelde richtlijnen voor veilig coderen, die worden gecontroleerd. | Coding standards (OWASP Secure Coding Practices); linters die bij violations falen in CI (SpotBugs, PMD, Checkstyle); GitHub Secret Scanning + pre-commit hooks (detect-secrets); dependency pinning (exacte versies/hashes, geen `latest`). | Linters en secret scanning nog te activeren in de CI. |
| **8.29** Beveiligingstests | Beveiligingstests in de ontwikkelcyclus; resultaten als bewijs bewaren. | SAST bij elke commit; Dependency Review bij elke PR (blokkeer nieuwe kwetsbare deps); DAST (OWASP ZAP) op de acceptatieomgeving; testresultaten als pipeline-artifact; pentest bij releases. | DAST en pentest nog niet ingericht; vereisen een acceptatieomgeving (zie 8.31). |
| **8.31** Scheiding van omgevingen (OTAP) | Gescheiden ontwikkel-, test-, acceptatie- en productieomgeving; productiedata nooit naar een lagere omgeving; gescheiden secrets. | GitHub Environments (minimaal test + productie) met protection rules en approval-gates; gescheiden secrets per omgeving; productie achter goedkeuring (2 approvers + tijdvenster); aparte `docker-compose`-bestanden per omgeving. | Acceptatieomgeving nog niet apart ingericht; volledige OTAP-inrichting volgt in opdracht 1. |
| **8.33** Testgegevens | Realistische tests met realistische, maar niet echte data. | Synthetische/gegenereerde testdata (bijv. Synthea); productiedata gaat nooit naar lagere omgevingen; gevoelige data zo snel mogelijk verwijderen na gebruik. | Pseudonimisering is technisch lastig; testdata-generatie nog in te richten. |
| **5.23** Beveiliging clouddiensten (GitHub) | Vastleggen welke clouddiensten worden gebruikt en met welk doel; beheerde toegang; retentiebeleid. | GitHub via organisatie-accounts (geen persoonlijke accounts); optioneel SSO/SAML met de organisatie-IdP; retentiebeleid voor pipeline-logs en artifacts vastleggen. | SSO is optioneel en (nog) niet ingericht; retentiebeleid nog vast te leggen. |
| **5.30** Bedrijfscontinuïteit (BCM) | Continuïteit en herstel na incident. | Beperkt relevant voor dit project: repository-mirror/backup, een rollback-procedure en RTO/RPO benoemen; de pipeline zelf is onderdeel van het continuïteitsplan. | Door WS02 als minder relevant aangemerkt; alleen op hoofdlijnen uitgewerkt. |

## 3. Checklist (minimale eisen aantoonbaar compliant pipeline)

Deze checklist (uit WS02) gebruiken we als planningsdoel; afvinken gebeurt naarmate opdracht 1 wordt uitgevoerd.

- [ ] Branch protection actief op `main` - alleen via PR, reviews verplicht
- [ ] Alle CI-checks slagen vóór merge (build, test, SAST)
- [ ] CodeQL of gelijkwaardige SAST actief (bijv. Snyk)
- [ ] Secret Scanning actief
- [ ] Dependabot alerts + security updates actief
- [ ] Dependency Review Action gekoppeld aan PR's
- [ ] SBOM gegenereerd (CycloneDX of SPDX) en geanalyseerd (SCA)
- [ ] GitHub Environments gedefinieerd met protection rules
- [ ] Secrets gescheiden per environment
- [ ] Pipeline-artifacts (rapporten, SBOM) worden bewaard
- [ ] README.md beschrijft beleid en procedure (mini-ISMS)

## 4. Conclusie

Dit plan legt vast hoe de CI/CD-pipeline vanaf het ontwerp aan NEN-7510:2024-2 gaat voldoen: toegang en wijzigingsbeheer via trunk-based PR's met review- en CI-gates (8.4/8.32), kwetsbaarhedenbeheer met SAST, SCA, SBOM en een CVSS-patchbeleid (8.8), configuratiebeheer via pipeline-as-code en secrets-beheer (8.9), en omgevingsscheiding via GitHub Environments (8.31). De belangrijkste restrisico's op dit moment zijn de nog vast te leggen branch-protection-ruleset, het formele patchbeleid, en de acceptatieomgeving voor DAST/pentest. Deze worden in opdracht 1 ingericht en daarna in het eindauditrapport (WS06) met concreet bewijs onderbouwd.
