# 2.1 Asset-identificatie - OpenMRS REST Web Services Module

## Kroonjuwelen (hoog risico)

| ID   | Asset                                      | Categorie     | Risico | Waarom                                                                                                                                       |
| ------- | ------------------------------------------ | ------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------------------- |
| A-01 | Patiëntrecords                             | Patiëntdata   | Hoog   | Bevat volledige medische dossiers. Ongeautoriseerde toegang kan leiden tot privacy-schendingen, reputatieschade en overtreding van AVG/GDPR. |
| A-02 | Patiëntidentifiers (UUID / patiëntnummer)  | Patiëntdata   | Hoog   | Worden gebruikt om patiënten uniek te identificeren. Misbruik kan leiden tot koppeling van gevoelige medische gegevens aan personen.         |
| A-03 | Persoonsgegevens (naam, adres, attributen) | Patiëntdata   | Hoog   | Bevat direct identificeerbare persoonsgegevens (PII). Datalekken kunnen juridische en privacygevolgen hebben.                                |
| A-04 | Sessietokens (JSESSIONID / Basic Auth)     | Authenticatie | Hoog   | Kunnen worden gebruikt om actieve sessies over te nemen en ongeautoriseerde toegang tot patiëntgegevens te verkrijgen.                       |
| A-05 | Gebruikerscredentials & wachtwoorden       | Authenticatie | Hoog   | Compromittering geeft aanvallers directe toegang tot gebruikersaccounts en mogelijk administratieve functies.                                |
| A-06 | Rollen & privileges (RBAC)                 | Authenticatie | Hoog   | Bepaalt welke gebruikers toegang hebben tot gevoelige functies en data. Manipulatie kan leiden tot privilege-escalatie.                      |
| A-07 | Observaties (klinische meetwaarden)        | Medisch       | Hoog   | Medische meetwaarden zijn essentieel voor diagnose en behandeling. Onjuiste of gelekte gegevens kunnen patiëntveiligheid beïnvloeden.        |
| A-09 | Orders & medicatievoorschriften            | Medisch       | Hoog   | Wijzigingen of verlies van voorschriften kunnen direct gevolgen hebben voor de behandeling van patiënten.                                    |
| A-10 | Allergieregistraties                       | Medisch       | Hoog   | Onjuiste of ontbrekende allergiegegevens kunnen leiden tot gevaarlijke medische beslissingen en patiëntschade.                               |


## Overige assets (midden/laag risico)

| ID   | Asset                                      | Categorie      | Risico | Waarom                                                                                                                                                                             |
| ------- | ------------------------------------------ | -------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A-08 | Encounters (zorgcontactmomenten)           | Medisch        | Midden | Belangrijk voor de medische context en historie van patiënten, maar doorgaans minder kritisch dan directe behandelgegevens zoals medicatie of allergieën.                          |
| A-11 | Systeeminstellingen (global properties)    | Systeem        | Midden | Foutieve configuratie kan systeemfunctionaliteit verstoren of beveiligingsinstellingen verzwakken, maar bevat meestal geen directe patiëntdata.                                    |
| A-12 | Concept Dictionary (medische terminologie) | Systeem        | Midden | Essentieel voor correcte interpretatie van medische gegevens. Manipulatie kan leiden tot inconsistenties of foutieve registraties.                                                 |
| A-13 | Docker-omgeving & compose-bestanden        | Infrastructuur | Midden | Bevat configuratie van de applicatie-infrastructuur. Misconfiguratie of blootstelling kan leiden tot verstoring of indirecte compromittering van het systeem.                      |
| A-14 | CI/CD-pipeline (GitHub Actions / Bamboo)   | Infrastructuur | Laag   | Beïnvloedt het ontwikkel- en deploymentproces, maar bevat doorgaans geen operationele patiëntgegevens. Compromittering kan wel invloed hebben op softwarekwaliteit en integriteit. |


## Classificatie (CIA-triad)

- **Vertrouwelijkheid** — data mag alleen toegankelijk zijn voor geautoriseerde gebruikers
- **Integriteit** — data moet correct en ongemanipuleerd blijven
- **Beschikbaarheid** — systeem moet bereikbaar zijn wanneer nodig

