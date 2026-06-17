# Traceability Matrix

**OpenMRS webservices.rest module**
ICT-I2_4 Security — WS06 Audit Rapportage
Avans-Groep-C8/ATx-2.4-Software-security-en-compliance
Periode: 9 juni 2026 – 17 juni 2026

Bronnen: GitHub Issues #33, #34, #60, #61, #67 — Pull Requests #58, #65 — `docs/pentest/`

---

## Toelichting

Deze matrix koppelt elke aangetoonde NEN-7510:2024-2-control aan een concrete maatregel, de oorspronkelijke bevinding ("vóór"), de doorgevoerde aanpassing en het verifieerbare bewijs ("na"). Alle rijen zijn direct herleidbaar tot artefacten in de GitHub-repository Avans-Groep-C8/ATx-2.4-Software-security-en-compliance: pentest-bevindingen (issues #60, #61, #67), de bijbehorende fix-pull request #65, de audit-logging pull request #58, en het definitieve pentestrapport onder `docs/pentest/`.


---

## Matrix

| Norm | Maatregel | Vóór (bevinding) | Aanpassing | Na (bewijs) |
|---|---|---|---|---|
| NEN-7510 8.3, 8.26 Inperken van toegangsrechten / Veilig coderen | Authenticatie- en privilegecheck vóór uitvoeren van cleardbcache-actie | **PT-003 (Critical, CVSS 9.1)**. Pentest, BoyanKloosterman, 2026-06-09: `POST /ws/rest/v1/cleardbcache` anoniem (geen auth) → HTTP 204. Bestand: `ClearDbCacheController2_0.java` (geen `Context.requirePrivilege`) | `Context.requirePrivilege` toegevoegd (Manage RESTWS-privilege vereist). **Commit `2904d3e`** — "Require auth for cleardbcache; add docs/tests", PR #65, 2026-06-15 | Hertest 2026-06-15: anoniem `POST /cleardbcache` → **401 (was 204)**. Geauthenticeerde superuser: werkt nog (regressievrij). Unit test: `ClearDbCacheController2_0Test.java`. Bewijs: `docs/pentest/bevinding-PT-003-na.md` + Burp-screenshot burp-22 / burp-23 |
| NEN-7510 8.9, 8.26 Beveiligingsconfiguratie / Foutafhandeling | Privilegecheck + generieke 401/403 i.p.v. stack trace op settings.form | **PT-004 (High, CVSS 7.5)**. Pentest, 2026-06-09: `GET .../settings.form` anoniem bereikbaar (HTTP 200, stack trace). `search?prefix=` lekt global property-namen én -waarden. Bestand: `SettingsFormController.java` | `requireManageRestWsPrivilege()` toegevoegd + `@ExceptionHandler` die 401/403 teruggeeft i.p.v. stack trace. **Commit `6f85844`** — "Secure SettingsFormController and add pentest docs", PR #65 | Hertest 2026-06-15: anoniem `GET settings.form` → **401 (was 200)**. Geen stack trace meer in body. Bewijs: `docs/pentest/bevinding-PT-004-na.md` + Burp burp-24 / burp-25 |
| NEN-7510 8.3, 8.26 Inperken van toegangsrechten (RBAC) | Privilegecheck "Manage Global Properties" op SystemSetting-resource | **PT-006 (High, CVSS 6.5)**. Pentest, 2026-06-15: gebruiker `nurse_readonly` kan via REST `GET /systemsetting` (en `/{uuid}`) lezen → HTTP 200 + config-waarden. Verwacht voor least-privilege: 403 | RBAC afgedwongen op alle CRUD-operaties van `SystemSetting1_9` (retrieve, getAll, search, create, update, delete, purge). **Commit `75d7cd6`** — "Require Manage Global Properties on systemsetting", PR #65 | Hertest 2026-06-15: nurse `GET /systemsetting` → **403 (was 200)**. Admin blijft 200 (geen regressie). Unit tests: `getAll_shouldRejectUserWithoutManageGlobalPropertiesPrivilege`, `getByUuid_shouldReject...`. Bewijs: `bevinding-PT-006-na.md` + Burp burp-27 |
| NEN-7510 8.15 Logregistratie | Centrale `AuditLogService`: log van patiënt-write-acties (create/update/delete/purge) | Issue #33 "Ontbrekende logging implementeren": write-acties op `PatientResource1_8` (create, update, delete/void, purge) werden niet gelogd → geen audit trail (NEN-7510 8.15) | `AuditLogService` toegevoegd: logt event, outcome, userId, resourceType, resourceUuid, action, timestamp. Geen BSN, medische inhoud, wachtwoorden of tokens. **Commit `d5ac5f3`** — "Add audit logging for patient write actions", PR #58 | Unit tests `AuditLogServiceTest`: **PASSED** (`mvn -pl omod-common -Dtest=AuditLogServiceTest test`). Audit-events via aparte logger `OPENMRS_REST_AUDIT` (Log4j2). Sluit issue #33 en #34. Bewijs: screenshot testresultaat in PR #58 |
| NEN-7510 5.35 Onafhankelijke beoordeling | Periodieke onafhankelijke beoordeling van informatiebeveiliging | Vóór WS06: geen formeel, gestructureerd auditrapport aanwezig dat bevindingen, bewijs en besluiten van de pentest-cyclus (P1+P2) samenbrengt | Dit auditrapport (incl. deze traceability matrix) opgesteld conform NEN-7510:2024-2-scope, gebaseerd op `docs/pentest/pentestrapport-definitief.md`. **Commit `abe39ed`** — "Add final pentest report for webservices.rest", PR #65 | Dit document, inclusief verwijzingen naar PR #58, #65 en onderliggende commits. Periode pentest: 2026-06-09 t/m 2026-06-15 |
|  NEN-7510 8.8 Beheer van technische kwetsbaarheden  | SCA-scanning met Snyk en Grype, SBOM-generatie | Onvoldoende inzicht in afhankelijkheden en kwetsbare componenten | PR #23 "Enhance Snyk workflow with improved error handling and SBOM support", PR #53 "Add Grype SCA step and upload SARIF results", PR #9 "Add workflow to generate CycloneDX SBOM", PR #10 "Create sbom.cdx.json" | CycloneDX SBOM aanwezig (`sbom.cdx.json`), SCA-scans draaien automatisch in CI en leveren SARIF-resultaten op |
|  NEN-7510 8.25 Beveiliging in de ontwikkelcyclus  | Trunk-based development, verplichte PR-review, CI/CD-pipeline | Securitymaatregelen in ontwikkelcyclus waren beperkt aantoonbaar; OTAP- en releaseproces onvoldoende gedocumenteerd | PR #22 "Trunk-based CI; add promote-prod workflow", PR #18 "Add Docker Compose setup and CI workflows for OpenMRS OTAP module" | GitHub Actions workflows actief, OTAP-documentatie aanwezig, alle wijzigingen verlopen via pull requests en reviews |
|  NEN-7510 8.16 Monitoringactiviteiten  | Attack Surface Analysis en Threat Modelling | Aanvalsoppervlak was niet volledig gedocumenteerd | PR #57 "Attack Surface", PR #51 "Docs/threat model", PR #59 "Update threat model with attack surface findings" | Actuele attack-surface-analyse, threat model bijgewerkt met gevonden aanvalspaden en risico's |

---

## Brondocumenten

- [Issue #60 — PT-003: Anonieme POST cleardbcache zonder authenticatie](https://github.com/Avans-Groep-C8/ATx-2.4-Software-security-en-compliance/issues/60)
- [Issue #61 — PT-004: Module-instellingen (settings.form) anoniem bereikbaar](https://github.com/Avans-Groep-C8/ATx-2.4-Software-security-en-compliance/issues/61)
- [Issue #67 — PT-006: Nurse readonly kan /systemsetting lezen](https://github.com/Avans-Groep-C8/ATx-2.4-Software-security-en-compliance/issues/67)
- [Issue #33 — Ontbrekende logging implementeren](https://github.com/Avans-Groep-C8/ATx-2.4-Software-security-en-compliance/issues/33)
- [Issue #34 — Logging tests schrijven en CI-testresultaat opleveren](https://github.com/Avans-Groep-C8/ATx-2.4-Software-security-en-compliance/issues/34)
- [Pull Request #65 — Fix/mitigatie PT-003, PT-004, PT-006 (commits 2904d3e, 6f85844, 75d7cd6, abe39ed)](https://github.com/Avans-Groep-C8/ATx-2.4-Software-security-en-compliance/pull/65)
- [Pull Request #58 — Feature/auditlogging (commit d5ac5f3)](https://github.com/Avans-Groep-C8/ATx-2.4-Software-security-en-compliance/pull/58)
- `docs/pentest/pentestrapport-definitief.md` — definitief pentestrapport (periode 2026-06-09 t/m 2026-06-15)
