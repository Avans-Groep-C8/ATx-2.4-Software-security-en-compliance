# Docker Compose — OpenMRS module (OTAP)

Deze setup bouwt en test **alleen de module**. De OpenMRS Reference Application draait al apart in Docker (`http://localhost/openmrs/`). Er start hier geen database, backend of frontend.

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

- Voert `mvn clean install` uit
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

- Voert `mvn clean package` uit
- Kopieert `omod/target/*.omod` naar `dist/`

## Waar komt het `.omod`?

| Omgeving | Pad |
|----------|-----|
| dev | `deploy/modules/` |
| prod | `dist/` |
| Maven default | `omod/target/` (direct na build) |

## OpenMRS apart

De Reference Application container laadt modules uit zijn eigen modules-directory. Na een dev- of prod-build:

1. Neem het `.omod` uit `deploy/modules/` of `dist/`
2. Plaats het in de modules-map van je OpenMRS-container
3. Herstart OpenMRS of laat auto-reload het module laden (afhankelijk van je setup)

Geen MySQL, geen OpenMRS backend en geen frontend worden door deze compose gestart.
