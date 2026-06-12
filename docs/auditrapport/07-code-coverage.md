# 5.7 Code coverage — onderbouwing en inrichting

**Document:** `docs/auditrapport/07-code-coverage.md`
**Module:** OpenMRS `webservices.rest` v3.2.0
**Tool:** JaCoCo 0.8.13 (Maven)
**Norm:** NEN-7510:2024-2 **8.29** (beveiligingstesten) en **8.25/8.28** (veilige ontwikkeling) — testresultaten moeten *aantoonbaar* en *reproduceerbaar* zijn.

---

## 1. Doel

"Alles groen" zegt weinig als maar een klein deel van de code wordt getest. Code coverage maakt
meetbaar **hoeveel** van de broncode daadwerkelijk door de geautomatiseerde tests wordt uitgevoerd.
Dit document legt vast:

1. hoe coverage is geactiveerd en als CI-artefact beschikbaar komt;
2. wat de gemeten baseline is;
3. welk coverage-percentage als ondergrens (gate) is gekozen en **waarom**.

---

## 2. Inrichting

JaCoCo stond in de `pom.xml` wél gedefinieerd (alleen `prepare-agent` in `pluginManagement`), maar
werd nooit uitgevoerd: er was geen `report`, geen gate en geen artefact. Dat is nu rechtgezet.

| Onderdeel | Waar | Effect |
|-----------|------|--------|
| `prepare-agent` | root `pom.xml` (pluginManagement) | instrumenteert de JVM tijdens de tests |
| `report` | root `pom.xml`, fase `test` | genereert per module `target/site/jacoco/` (HTML + `jacoco.xml` + `jacoco.csv`) |
| `check` (gate) | `omod/pom.xml`, fase `verify` | **faalt de build** als de instruction-coverage van `omod` < drempel |
| Drempel | property `jacoco.coverage.minimum` (root) | standaard **0.80**; centraal aanpasbaar |
| CI-artefact | `.github/workflows/build.yml` | upload `**/target/site/jacoco/**` als `jacoco-coverage-<run_id>` |

De gate draait automatisch mee in `mvn clean verify`, dat de CI al uitvoert. Coverage wordt dus bij
elke PR en elke merge naar `main` gemeten en als artefact bewaard.

---

## 3. Gemeten baseline

Gemeten met de unit tests van de code-modules (`omod-common` + `omod`); de `integration-tests`-module
draait alleen tegen een live server (apart profiel) en telt niet mee voor unit-coverage.

| Metric | Gecombineerd | `omod` (REST-resources) | `omod-common` |
|--------|:------------:|:-----------------------:|:-------------:|
| **Instructions** | 67,6 % | **86,6 %** | 16,2 % |
| Lines | 68,5 % | — | — |
| Branches | 55,7 % | — | — |
| Methods | 55,3 % | — | — |
| Complexity | 65,2 % | — | — |

**Observatie:** vrijwel alle security-relevante code (de REST-resources, controllers en
search/handler-logica die patiëntdata ontsluiten) zit in de `omod`-module, en die is met **86,6 %**
goed gedekt. `omod-common` (gedeelde DTO-/hulpklassen, weinig eigen logica) trekt het gecombineerde
gemiddelde omlaag.

> **Belangrijke nuance (eerlijk over de herkomst).** Deze 86,6 % komt vrijwel volledig uit de
> **bestaande unit tests die OpenMRS zelf met de module meelevert** — niet uit tests die dit team
> heeft toegevoegd. Dit cijfer is dus een **geërfde baseline / nulmeting**, geen prestatie van het
> project. De waarde van deze taak zit in het *meetbaar en bewaakt maken* van coverage (activeren,
> rapport, gate, artefact), niet in het cijfer zelf. Zodra het team eigen code/tests toevoegt
> (bv. audit-logging), laat ditzelfde rapport zien of dáár voldoende dekking op zit.

---

## 4. Gekozen drempel en onderbouwing

> **Gate: minimaal 80 % instruction-coverage op de `omod`-module.**

De drempel is bewust gekozen op basis van de projectcontext (zorg-module die patiëntdata ontsluit) en
de gemeten nulmeting. Conform de opdracht onderbouwen we expliciet zowel de boven- als ondergrens.

**Waarom 80 % en niet lager?**

- **Het moet de huidige stand bewaken.** De baseline is 86,6 %. Zou de gate op bv. 50 % staan, dan
  zou de dekking flink kunnen wegzakken zonder dat de build het merkt — dan is de gate zinloos.
  80 % houdt het bewezen niveau ongeveer vast.
- **Risicogericht (NEN-7510 8.29).** De gate staat op de module die patiëntdata ontsluit; daar is een
  stevige ondergrens gerechtvaardigd.

**Waarom 80 % en niet hoger?**

- **Marge tegen vals alarm.** De baseline is 86,6 %. Direct op 86 % of 90 % gaan zou de build laten
  falen bij een normale, legitieme wijziging die de dekking een paar procent laat dalen. De ~6 %
  marge voorkomt dat de gate vals alarm geeft bij niet-risicovolle veranderingen.
- **Diminishing returns.** 90 %+ forceren leidt in de praktijk tot triviale tests die het cijfer
  opdrijven zonder echte zekerheid toe te voegen.

**Waarom instruction-coverage (en niet line/branch)?** Instruction-coverage is de meest stabiele
JaCoCo-counter en weinig gevoelig voor opmaak. Branch-coverage (55,7 %) is bewust *niet* als harde
gate gekozen, omdat de geërfde baseline daarvoor te laag is; dit is een expliciet verbeterpunt (§6).

### Eerlijke beperking van deze gate
De gate kijkt naar het **gemiddelde over de hele `omod`-bundle** (228 klassen). Dat betekent: als er
later één klein, slecht getest bestand bijkomt, beweegt het gemiddelde nauwelijks en blijft de build
groen. De gate vangt dus een *brede* terugval wel, maar een *lokaal* gat (één ongeteste nieuwe klasse)
niet. Wie dat ook wil afdekken, kan later een aanvullende regel op PACKAGE-/CLASS-niveau toevoegen —
zie §6.

### Waarom geen gate op `omod-common`
`omod-common` zit met 16,2 % ruim onder elke zinvolle drempel. Een harde gate hierop zou de build
direct breken zonder securitywinst. De module wordt wél gerapporteerd (artefact), maar valt buiten de
gate. Het verhogen van deze dekking is een vastgelegd verbeterpunt, geen blocker.

---

## 5. Resultaat en verificatie

De inrichting is lokaal aangetoond met exact het commando dat de CI draait (`mvn clean verify` over
de volledige reactor):

| Wat | Resultaat | Bewijs |
|-----|-----------|--------|
| Volledige build | **BUILD SUCCESS** | reactor `mvn clean verify` |
| Testsuite | **1783 tests, 0 failures, 0 errors** (14 skipped) | surefire-output |
| Rapport gegenereerd | `jacoco:report` draait op `omod` (228 klassen) | `omod/target/site/jacoco/` |
| Gate slaagt bij baseline | drempel 0.80 → *"All coverage checks have been met"* | `jacoco:check` |
| Gate faalt bij te lage dekking | drempel 0.95 → *"Rule violated … ratio is 0.86, but expected minimum is 0.95"* → BUILD FAILURE | bewust geforceerde testrun |

De laatste regel is belangrijk: de gate is in **beide richtingen** getoetst, dus hij blokkeert
daadwerkelijk een te lage dekking en is geen loze configuratie.

**Gemeten coverage-percentage (nulmeting):** `omod` = **86,6 % instructions** (drempel 80 %).

---

## 6. Hoe lees je het rapport (CI-artefact)

1. GitHub Actions → run van workflow **CI** → sectie **Artifacts** → `jacoco-coverage-<run_id>`.
2. Uitpakken en `omod/target/site/jacoco/index.html` openen voor het doorklikbare HTML-overzicht.
3. `jacoco.csv` / `jacoco.xml` zijn machine-leesbaar (bv. voor verdere analyse of een badge).

Lokaal reproduceren:

```bash
mvn -B clean verify -pl omod-common,omod -am
# rapport: omod/target/site/jacoco/index.html
```

---

## 7. Verbeterpunten

| Punt | Actie | Prioriteit |
|------|-------|-----------|
| Branch-coverage laag (55,7 %) | Tests toevoegen op conditionele autorisatie-/validatiepaden; later als gate | P2 |
| `omod-common` 16,2 % | Dekking verhogen of expliciet uitsluiten van scope met motivatie | P3 |
| Drempel verhogen | Na verbetering `jacoco.coverage.minimum` stapsgewijs ophogen | P3 |
| Aggregatierapport | Optioneel `report-aggregate`-module voor één gecombineerd cijfer | P3 |

---

## 8. Verwijzingen

| Onderwerp | Pad |
|-----------|-----|
| Coverage-configuratie | `pom.xml` (jacoco-maven-plugin), `omod/pom.xml` (gate) |
| CI-artefact | `.github/workflows/build.yml` |
| Pipeline-strategie | `docs/pipeline-strategie.md` |
| Risk Assessment Report | `docs/auditrapport/00-risk-assessment.md` |

*Versie 1.1 — 2026-06-11. Baseline gemeten en inrichting geverifieerd met volledige
`mvn clean verify` (1783 tests, gate getoetst op slagen én falen). JaCoCo 0.8.13.*
