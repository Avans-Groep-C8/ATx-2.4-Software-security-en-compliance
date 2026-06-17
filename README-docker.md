# Docker Compose — OpenMRS module (OTAP)

Deze setup bouwt en test **alleen de module**. De OpenMRS Reference Application draait al apart in Docker (`http://localhost/openmrs/`). Er start hier geen database, backend of frontend.

**Git:** alleen branch `main` is trunk; CI en artifacts staan in [docs/pipeline-strategie.md](docs/pipeline-strategie.md).

## Omgevingen

| Omgeving | Doel | Artifact |
|----------|------|----------|
| **dev** | Snel bouwen + `.omod` klaarzetten | `deploy/modules/*.omod` |
| **test** | Tests draaien | Geen artifact (exit code = geslaagd/mislukt) |
| **prod** | Productie-build | `dist/*.omod` |

## Dev build

```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml run --rm maven
```

- Voert `mvn clean install -DskipTests` uit (artifact snel klaar; tests via test-compose)
- Kopieert `omod/target/*.omod` naar `deploy/modules/`

Mount `deploy/modules/` in je bestaande OpenMRS-container als modules-map, of kopieer het `.omod` handmatig naar die map.

## Tests draaien

```powershell
docker compose -f docker-compose.yml -f docker-compose.test.yml run --rm maven
```

- Voert `mvn clean verify` uit

## Prod artifact

```powershell
docker compose -f docker-compose.yml -f docker-compose.prod.yml run --rm maven
```

- Voert `mvn clean package -DskipTests` uit (alleen artifact)
- Kopieert `omod/target/*.omod` naar `dist/`

## Waar komt het `.omod`?

| Omgeving | Pad |
|----------|-----|
| dev | `deploy/modules/` |
| prod | `dist/` |
| Maven default | `omod/target/` (direct na build) |

## OpenMRS apart

Voor pentest/hertest van `webservices.rest`: gebruik de aparte stack in [`../openmrs-webservices-test/`](../openmrs-webservices-test/).

De Reference Application container laadt modules uit zijn eigen modules-directory. Na een dev- of prod-build:

1. Neem het `.omod` uit `deploy/modules/` of `dist/`
2. Plaats het in de modules-map van je OpenMRS-container
3. Herstart OpenMRS of laat auto-reload het module laden (afhankelijk van je setup)

Geen MySQL, geen OpenMRS backend en geen frontend worden door deze compose gestart.

## Bekende test-failure

De upstream module heeft 1783 unit tests. Soms faalt `ClearDbCacheController2_0Test` (Hibernate cache-assertie). Dat is geen Docker-fout.

- **dev/prod** slaan tests over (`-DskipTests`) — bedoeld voor `.omod` artifacts
- **test** draait `mvn clean verify` — daar zie je test-resultaten
