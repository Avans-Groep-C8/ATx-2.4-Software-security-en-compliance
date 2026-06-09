# Beleid: omgaan met scanbevindingen en false positives

Dit document beschrijft hoe het team omgaat met de bevindingen van de
geautomatiseerde security-scans in de CI/CD-pipeline (SAST, SCA, SBOM via Snyk en
CodeQL). Het is het bewijsstuk dat hoort bij **NEN-7510:2024-2 control 8.8**
(beheer van technische kwetsbaarheden): kwetsbaarheden worden tijdig
geïdentificeerd, beoordeeld én — onderbouwd — verholpen, geaccepteerd of
gesupprimeerd.

## 1. Waar komen de bevindingen vandaan?

| Bron | Techniek | Workflow | Output |
|------|----------|----------|--------|
| `snyk test --all-projects` | SCA (dependencies → CVE's) | [snyk.yml](../.github/workflows/snyk.yml) | `snyk-sca.json` |
| `snyk code test` | SAST (broncode → CWE's) | [snyk.yml](../.github/workflows/snyk.yml) | `snyk-sast.json` |
| `anchore/sbom-action` | SBOM (CycloneDX-inventaris) | [snyk.yml](../.github/workflows/snyk.yml) | `snyk-sbom.json` |
| CodeQL | SAST (tweede SAST-bron) | [codeql.yml](../.github/workflows/codeql.yml) | Security tab / SARIF |

De drie Snyk-outputs worden samengevoegd tot het artifact **`snyk-results.json`**
en bij elke run bewaard (audit trail, NEN-7510 8.29).

## 2. Waarom blokkeert de pipeline niet (bewuste keuze)

De Snyk-stappen draaien met `continue-on-error: true` en de exit-code wordt
opgevangen. De pipeline **faalt dus niet automatisch** op een gevonden CVE/CWE.
Dat is een bewuste keuze, met de volgende rationale:

- **De scan-output is bewijs, geen poortwachter.** Het doel van deze stap is een
  volledige, altijd-aanwezige audit trail (`snyk-results.json`). Een harde fail
  zou betekenen dat het artifact bij de eerste bevinding niet meer wordt
  gegenereerd.
- **Niet elke bevinding is een echt risico.** Zoals beschreven in WS04 meet CVSS
  technische ernst, niet het bedrijfsrisico. Veel bevindingen zijn false
  positives of niet bereikbaar (zie §4). Automatisch blokkeren zou het team
  dwingen tot ongefundeerde "snelle" suppressies.
- **De échte quality gate is de review + de security backlog.** Bevindingen
  worden niet genegeerd, maar contextueel beoordeeld en geprioriteerd (§3),
  niet automatisch weggedrukt.

> Restrisico: omdat de pipeline niet hard blokkeert, leunt de opvolging op het
> reviewproces. Dit restrisico is geaccepteerd en wordt afgedekt door de
> verplichte triage hieronder en door CodeQL, dat zijn bevindingen wél naar de
> GitHub Security tab schrijft.

## 3. Triage-proces per bevinding

Voor elke bevinding uit `snyk-results.json` doorloopt het team (conform WS04):

1. **CVSS Base Score** verifiëren op NVD (niet blind de Snyk-score overnemen).
2. **CWE-categorie** bepalen.
3. **Fix beschikbaar?** Directe of transitieve dependency?
4. **Bereikbaarheid** — is het code-pad/endpoint van buiten benaderbaar?
5. **Healthcare-impact** — raakt het patiëntdata of medische functionaliteit?
6. **Exploitability** — Snyk Exploit Maturity / EPSS / CISA KEV.

Op basis daarvan krijgt elke bevinding één van drie besluiten:

| Besluit | Wanneer | Vastlegging |
|---------|---------|-------------|
| **Patchen** | Echt risico, fix beschikbaar | Security backlog + deadline o.b.v. CVSS (zie §5) |
| **Accepteren** | Reëel maar (nog) niet op te lossen | Risicoacceptatie-document (eigenaar + reviewdatum) |
| **Supprimeren** | False positive (zie §4) | `.snyk`-bestand met `reason` + `expires` |

## 4. Wanneer is iets een false positive?

Signalen (WS04) dat een bevinding gesupprimeerd mag worden:

- **Unreachable code** — kwetsbare functie wordt nooit via een live pad aangeroepen.
- **Parameterized queries** — SAST meldt SQL-injectie, maar `PreparedStatement`
  wordt correct gebruikt.
- **Sanitization buiten scope** — input wordt upstream gesanitiseerd.
- **Component niet in runtime** — dependency is `test`-scope, zit niet in de
  productie-`.omod`/JAR.
- **Vendor heeft al gepatcht** — fix zit al (via backport) in de gebruikte versie.

### Suppressie is alleen toegestaan met documentatie

Conform NEN-7510 8.8 mag een bevinding **nooit stilzwijgend** worden genegeerd.
Suppressie gebeurt uitsluitend in een `.snyk`-bestand met:

- een **`reason`** (waarom is dit geen reëel risico),
- **wie** het heeft geverifieerd en **wanneer**,
- een **`expires`**-datum, zodat de suppressie periodiek opnieuw beoordeeld wordt.

Voorbeeldformaat:

```yaml
# .snyk
version: v1.25.0
ignore:
  SNYK-JAVA-COMSOMELIB-12345:
    - '*':
        reason: >
          jackson-databind wordt alleen in test-scope gebruikt en zit niet in
          de productie-.omod. Geverifieerd op 2026-06-09 door <naam>.
        expires: '2026-12-09T00:00:00.000Z'
```

## 5. Patch-prioriteit (richtlijn, WS02/WS04)

| CVSS | Bereikbaar + patiëntdata | Deadline |
|------|--------------------------|----------|
| 9.0–10 (Critical) | Ja | ≤ 24 uur |
| 7.0–8.9 (High) | Ja | ≤ 1 week / binnen sprint |
| 7.0–8.9 (High) | Nee / geen directe impact | Plannen + documenteren (≤ 3 mnd) |
| 4.0–6.9 (Medium) | — | Backlog met tijdlijn |
| < 4.0 (Low) | — | Accepteren of monitoren |

## 6. NEN-7510-koppeling

| Activiteit | Control |
|------------|---------|
| Scannen + prioriteren + besluiten | 8.8 — beheer technische kwetsbaarheden |
| Scan-output bewaren als artifact | 8.29 — beveiligingstesten (bewijs) |
| SBOM als versie-inventaris | 8.8 + 8.9 — configuratiebeheer |
| SAST-bevindingen (CWE's) opvolgen | 8.28 — veilig coderen |
| Gesupprimeerde/geaccepteerde risico's met vervaldatum | 8.8 — gedocumenteerde rationale |
