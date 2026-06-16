# 5.6 Mini-complianceverslag - Pipeline

**Onderwerp:** pipeline van de OpenMRS `webservices.rest`-module
**Norm:** NEN-7510:2024-2 (Beheersmaatregelen, hoofdstuk 8 - Technisch)
**Datum:** 9 juni 2026

## 1. Inleiding

Dit mini-complianceverslag beoordeelt de **pipeline** van de OpenMRS `webservices.rest`-module tegen de relevante technische beheersmaatregelen uit NEN-7510:2024-2 (hoofdstuk 8, gericht op ontwikkeling en beheer). Het verslag hoort bij workshop WS02 (beveiligde pipeline) en vormt samen met de [gap-analyse van de applicatie](01-gap-analyse.md) de technische onderbouwing voor het auditrapport (WS06).

De pipeline is bewust apart beoordeeld van de applicatiecode. Waar de gap-analyse kijkt naar wat de module zelf afdwingt, kijkt dit verslag naar de **geautomatiseerde processen** eromheen: hoe code wordt gebouwd, getest, gescand en gedocumenteerd. De beoordeling is bewijsgericht volgens de audit-mindset uit WS01: per control benoemen we de geïmplementeerde pipeline-maatregel en verwijzen we naar het concrete bewijs (`bestand:regel`) in `.github/workflows/`. Waar een maatregel ontbreekt, is dat als negatief bevinding vastgelegd in plaats van als aanname.

## 2. Pipeline-overzicht

De pipeline bestaat uit drie GitHub Actions-workflows. Samen dekken zij het bouwen, testen, statisch scannen en documenteren van de componenten af.

| Workflow | Doel | Triggers |
|---|---|---|
| `.github/workflows/build.yml` | Compile + unit/integration tests | push & PR op `main`/`test`/`develop`, `workflow_dispatch` |
| `.github/workflows/codeql.yml` | CodeQL SAST (statische codeanalyse) | push & PR op `main`/`test`/`develop`, wekelijkse cron |
| `.github/workflows/sbom.yml` | CycloneDX-SBOM genereren | push & PR op `main`, `workflow_dispatch` |

Opvallend is dat de SBOM-workflow alleen op `main` draait, terwijl build en CodeQL ook op `test` en `develop` actief zijn. Voor de feature-branches is er dus wel statische analyse en testdekking, maar geen actuele SBOM.

## 3. Beoordeling per control

Onderstaande tabel koppelt elke relevante NEN-7510-2 control aan de aanwezige pipeline-maatregel, het bewijs en een statusoordeel. De status drukt uit in hoeverre de control aantoonbaar volledig is afgedekt: **Aanwezig** (volledig), **Gedeeltelijk** (basis aanwezig maar onvolledig) of **Afwezig**.

| NEN-7510-2 control | Pipeline-maatregel | Bewijs (`bestand:regel`) | Status |
|---|---|---|---|
| **8.4** Toegang tot broncode | Workflow-token met least privilege (`permissions: contents: read`); CodeQL krijgt alleen `security-events: write`. Wijzigingen lopen via pull requests. | `build.yml:10-11`; `codeql.yml:31-40`; `sbom.yml:10-11`; PR-triggers `build.yml:6-7` | 🟡 Gedeeltelijk |
| **8.8** Beheer van technische kwetsbaarheden | CodeQL SAST op elke push/PR en wekelijks (cron). CycloneDX-SBOM voor componenttransparantie. | `codeql.yml:14-20` (triggers), `codeql.yml:98-101` (analyse); `sbom.yml:35-41` (SBOM naar `docs/sbom.cdx.json`) | 🟡 Gedeeltelijk |
| **8.15** Logging (pipeline-activiteiten) | GitHub Actions legt per run/job/stap uitvoeringslogs vast en registreert wie een run triggert; testrapporten worden als artefact bewaard. | `build.yml:50-56` (upload surefire-reports); run-logs per workflow (GitHub Actions) | 🟡 Gedeeltelijk |
| **8.25** Beveiligde ontwikkellevenscyclus | Beveiligingsstappen (SAST + SBOM + tests) zijn ingebed in de pipeline en draaien geautomatiseerd bij elke wijziging. | `codeql.yml:14-20`; `sbom.yml:3-8`; `build.yml:3-8` | 🟡 Gedeeltelijk |
| **8.28** Veilig coderen | CodeQL geeft geautomatiseerde feedback op onveilige codepatronen (`java-kotlin` + `actions`). | `codeql.yml:45-49` (taalmatrix), `codeql.yml:98-101` | 🟡 Gedeeltelijk |
| **8.29** Beveiligingstests in ontwikkeling en acceptatie | `mvn verify` draait de test-suite en CodeQL-analyse draait geautomatiseerd bij elke push/PR. | `build.yml:32-48` (test-job); `codeql.yml:17-18` (PR-trigger) | ✅ Aanwezig |
| **8.31** Scheiding van ontwikkel-, test- en productieomgeving | Aparte branches `main`/`test`/`develop`; de pipeline draait gescheiden per branch. | `build.yml:4-7`; `codeql.yml:15-18` | 🟡 Gedeeltelijk |
| **8.32** Wijzigingsbeheer | Elke wijziging loopt via een pull request; build, test en SAST draaien daarbij als controles. Acties zijn op versie vastgezet (SHA-pin). | PR-triggers `build.yml:6-7`, `codeql.yml:17-18`; SHA-pinned action `sbom.yml:36` | 🟡 Gedeeltelijk |

## 4. Bevindingen

**Sterke punten.** De pipeline laat een volwassen basis zien. Beveiliging is geen losse stap maar zit ingebed in de standaard pipeline: bij elke push of pull request draaien zowel de testsuite (`mvn verify`) als de CodeQL-analyse, en bij wijzigingen op `main` wordt automatisch een machineleesbare CycloneDX-SBOM gegenereerd. Daarmee is **8.29 (beveiligingstests)** aantoonbaar afgedekt: het testen en scannen gebeurt geautomatiseerd en is herhaalbaar. Verder zijn twee hardening-keuzes het noemen waard: de workflow-tokens draaien met minimale rechten (`contents: read`, en alleen CodeQL krijgt `security-events: write`), en de SBOM-workflow zet de externe action op een vaste commit-hash vast (SHA-pin), wat supply-chain-manipulatie via een verschoven tag voorkomt.

**Zwakke punten.** Tegenover die basis staan een paar concrete gaten. Bij **8.8 (kwetsbaarhedenbeheer)** is er wel statische analyse (CodeQL) en een SBOM, maar geen software composition analysis: er is geen Dependabot- of vergelijkbare SCA-configuratie aangetroffen (`.github/dependabot.yml` ontbreekt) en geen vastgelegd patchbeleid op basis van CVSS-ernst. Bekende kwetsbaarheden in afhankelijkheden worden dus niet automatisch gesignaleerd. Bij **8.28 (veilig coderen)** draait CodeQL met de standaard-queryset; de uitgebreidere `security-extended`-queries staan uitgeschakeld (`codeql.yml:79`, uitgecommentarieerd), waardoor een deel van de mogelijke bevindingen niet wordt opgepikt.

**Het belangrijkste aandachtspunt is afdwingbaarheid.** De build-, test- en scanstappen zijn waardevolle controles, maar of ze een onveilige merge daadwerkelijk **tegenhouden** hangt af van branch protection en verplichte status checks. Die instellingen staan op organisatie-/repositoryniveau en zijn niet uit de broncode aantoonbaar. Zolang dat niet is aangetoond, zijn de checks een signaal en geen harde poort, wat de status van **8.4** en **8.32** op "Gedeeltelijk" houdt.

**Logging in de juiste context.** Het is belangrijk om **8.15** hier zuiver te duiden: GitHub Actions houdt uitvoeringslogs bij en registreert wie een run start, wat bruikbaar is voor de traceerbaarheid van de pipeline zelf. Dit is echter iets anders dan de audit-logging van handelingen op patiëntdossiers die 8.15 in de zorgcontext vraagt; bovendien zijn Actions-logs standaard beperkt houdbaar en niet onveranderlijk (append-only). Daarom staat 8.15 hier op "Gedeeltelijk" en niet hoger. De eigenlijke 8.15-eis voor patiëntdata wordt in de [gap-analyse van de applicatie](01-gap-analyse.md) beoordeeld.

## 5. Restpunten en aanbevelingen

De volgende acties tillen de gedeeltelijk afgedekte controls naar een aantoonbaar volledig niveau.

| Control | Gap | Aanbevolen actie |
|---|---|---|
| **8.8** | Geen SCA-tool (Dependabot/Snyk) en geen patchbeleid op CVSS-ernst (`.github/dependabot.yml` ontbreekt). | Dependabot/SCA toevoegen en een patchbeleid met deadlines per CVSS-score vastleggen. |
| **8.28** | CodeQL draait met de default-queryset; `security-extended` staat uit (`codeql.yml:79`). | `queries: security-extended,security-and-quality` activeren. |
| **8.4 / 8.32** | Branch protection, verplichte review en "disable force push" niet uit de repo aantoonbaar. | Branch protection + verplichte PR-review + required status checks aanzetten en aantoonbaar maken (policy-export/screenshot). |
| **8.15** | Pipeline-logs zijn beperkt houdbaar en niet onveranderlijk. | Indien nodig pipeline-/repository-auditlogs exporteren naar een centrale, append-only logvoorziening met bewaartermijn. |
| **8.31** | Branch-scheiding aanwezig, maar geen aparte deploy-/acceptatieomgevingen in de repo. | Deploy-/environment-stappen met approvals toevoegen indien van toepassing. |

## 6. Conclusie

De pipeline dekt de kern van de NEN-7510-2 ontwikkel-controls goed af: beveiligingstests draaien aantoonbaar en geautomatiseerd (8.29), de ontwikkellevenscyclus bevat ingebedde security-stappen (8.25) en er zijn duidelijke hardening-keuzes gemaakt (least-privilege tokens, SHA-pinning). De resterende punten zijn overzichtelijk en concreet: voeg software composition analysis met patchbeleid toe (8.8), schakel de uitgebreide CodeQL-queryset in (8.28), en maak de afdwingbaarheid van de checks aantoonbaar via branch protection (8.4 en 8.32). Geen enkele beoordeelde control is volledig afwezig; de pipeline is daarmee op de goede weg, maar nog niet volledig auditbestendig.
