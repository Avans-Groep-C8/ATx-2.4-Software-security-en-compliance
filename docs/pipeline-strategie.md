# Pipeline-strategie (Trunk-Based Development)

## Trunk

- Enige integratiebranch: **`main`**
- Feature branches: kortlevend, altijd via pull request naar `main`
- Branches `develop` en `test` zijn **niet** meer onderdeel van CI (geen omgeving-branches)

## CI op elke wijziging

| Trigger | Workflow | Wat gebeurt er |
|---------|----------|----------------|
| PR → `main` | CI, CodeQL | `mvn clean verify` (zoals `docker-compose.test.yml`) |
| Merge → `main` | CI, CodeQL, SBOM | Zelfde tests + dev-`.omod` artifact + SBOM |
| Handmatig | Promote to Production | Prod-`.omod` na goedkeuring |

## Omgevingen (OTAP)

De namen **dev / test / prod** in Docker Compose zijn **build-modi**, geen git-branches:

| Omgeving | Lokaal (Compose) | Pipeline |
|----------|------------------|----------|
| test | `docker-compose.test.yml` | PR + `main`: `mvn verify` |
| dev | `docker-compose.dev.yml` | Automatisch artifact na groene CI op `main` |
| prod | `docker-compose.prod.yml` | Workflow **Promote to Production** |

OpenMRS draait **buiten** deze repo. Installatie: download artifact → plaats `.omod` in modules-map van de OpenMRS-container → herstart/reload.

## Goedkeuringen

| Stap | Reviewers |
|------|-----------|
| PR → `main` | 1 (branch protection in GitHub) |
| Prod-artifact | 2 (GitHub Environment `production`) |

### GitHub-instellingen (eenmalig in de repo)

1. **Branch protection** op `main`: require PR, 1 approval, status checks `build and test` + CodeQL.
2. **Environment `production`**: Required reviewers = 2.
3. Optioneel environment **`development`** voor toekomstige handmatige dev-acties (nu niet nodig; dev-artifact gaat automatisch mee op `main`).

## Artifacts

- **Dev:** `omod-dev-<full-sha>` — gebouwd uit dezelfde CI-run als `mvn verify` op `main`.
- **Prod:** `omod-prod-<full-sha>` — aparte run via `promote-prod.yml`; gebruik dezelfde commit-SHA als de goedgekeurde dev-build.

## Lokaal bouwen (Docker)

Zie [README-docker.md](../README-docker.md).

```powershell
docker compose -f docker-compose.yml -f docker-compose.test.yml run --rm maven
docker compose -f docker-compose.yml -f docker-compose.dev.yml run --rm maven
docker compose -f docker-compose.yml -f docker-compose.prod.yml run --rm maven
```

## Audit / compliance

Bewijs in de repository:

- `.github/workflows/build.yml` — trunk CI + dev-artifact
- `.github/workflows/codeql.yml` — SAST
- `.github/workflows/sbom.yml` — SBOM op `main`
- `.github/workflows/promote-prod.yml` — gecontroleerde prod-build
