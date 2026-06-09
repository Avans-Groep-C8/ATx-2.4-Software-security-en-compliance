# 2.1 Asset-identificatie — OpenMRS REST Web Services Module

## Kroonjuwelen (hoog risico)

| ID | Asset | Categorie | Risico |
|----|-------|-----------|--------|
| A-01 | Patiëntrecords | Patiëntdata | Hoog |
| A-02 | Patiëntidentifiers (UUID / patiëntnummer) | Patiëntdata | Hoog |
| A-03 | Persoonsgegevens (naam, adres, attributen) | Patiëntdata | Hoog |
| A-04 | Sessietokens (JSESSIONID / Basic Auth) | Authenticatie | Hoog |
| A-05 | Gebruikerscredentials & wachtwoorden | Authenticatie | Hoog |
| A-06 | Rollen & privileges (RBAC) | Authenticatie | Hoog |
| A-07 | Observaties (klinische meetwaarden) | Medisch | Hoog |
| A-09 | Orders & medicatievoorschriften | Medisch | Hoog |
| A-10 | Allergieregistraties | Medisch | Hoog |

## Overige assets (midden/laag risico)

| ID | Asset | Categorie | Risico |
|----|-------|-----------|--------|
| A-08 | Encounters (zorgcontactmomenten) | Medisch | Midden |
| A-11 | Systeeminstellingen (global properties) | Systeem | Midden |
| A-12 | Concept Dictionary (medische terminologie) | Systeem | Midden |
| A-13 | Docker-omgeving & compose-bestanden | Infrastructuur | Midden |
| A-14 | CI/CD-pipeline (GitHub Actions / Bamboo) | Infrastructuur | Laag |

## Classificatie (CIA-triad)

- **Vertrouwelijkheid** — data mag alleen toegankelijk zijn voor geautoriseerde gebruikers
- **Integriteit** — data moet correct en ongemanipuleerd blijven
- **Beschikbaarheid** — systeem moet bereikbaar zijn wanneer nodig