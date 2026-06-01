# 5.1 Module-keuze

## Gekozen module

| Onderdeel | Waarde |
|---|---|
| **Naam** | OpenMRS Webservices REST Module (`webservices.rest`) |
| **Versie (gebruikt in audit)** | `3.2.0` — uit de root-`pom.xml` (`<version>`) van de aangeleverde module |
| **Officiële versie (referentie)** | `master` staat momenteel op `2.50.0-SNAPSHOT` |
| **Link broncode** | https://github.com/openmrs/openmrs-module-webservices.rest |
| **Taal / build** | Java 8, Maven |

> **Let op:** de aangeleverde module is bewust een oudere/aangepaste versie ("er is mee gerommeld" volgens de opdracht). Het versienummer `3.2.0` is overgenomen uit de root-`pom.xml`. De `config.xml` is hiervoor géén betrouwbare bron: die gebruikt het build-time token `@MODULE_VERSION@`, dat pas bij compilatie wordt ingevuld. De `pom.xml` is daarom de bron van waarheid voor het versienummer.

## Motivatie

### Complexiteit
`webservices.rest` is een van de zwaarste modules in de lijst en biedt daardoor veel aanknopingspunten voor bevindingen:

| Metric | Waarde |
|---|---|
| Bestanden | 743 |
| Code lines (totaal) | 63.740 |
| Java code lines | 54.402 |
| Cyclomatische complexiteit (totaal) | 4.003 |
| Java-complexiteit | 3.878 |
| Geschatte ontwikkelkosten (COCOMO) | $2.119.473 |
| Geschatte doorlooptijd | 18,30 maanden |

Het overgrote deel van de codebase is Java (54.402 van de 63.740 code lines). Dat is precies de code die met SAST-tools gescand wordt. Een hoge cyclomatische complexiteit betekent veel branches, loops en condities — oftewel veel uitvoeringspaden die geanalyseerd moeten worden. Dit maakt de module geschikt om een gevuld auditrapport te produceren, in lijn met de tip om juist *niet* de simpelste module te kiezen.

### Scope
De module bestaat uit meerdere submodules (`omod-common` plus versie-specifieke `omod-x.x`-modules) en raakt een breed deel van de OpenMRS-API. Het is geen geïsoleerd hulpstuk maar een infrastructuur-laag die nagenoeg alle domein-objecten (patiënten, encounters, observaties, concepten, etc.) als REST-resources ontsluit. De omvang en spreiding over veel resource-handlers geven een realistische, niet-triviale scope voor een compliance-scan.

### Kritieke functionaliteit
`webservices.rest` exposeert de OpenMRS-API als RESTful web services: externe applicaties kunnen er gegevens uit ophalen én in wegschrijven. Dat maakt de module om meerdere redenen security-kritiek:

- **Externe aanvalsvlak (attack surface):** de module is het primaire HTTP-toegangspunt naar de onderliggende database. Kwetsbaarheden hier zijn van buitenaf bereikbaar.
- **Authenticatie & autorisatie:** request-handling, sessiebeheer en toegangscontrole lopen via deze laag — klassieke locaties voor bevindingen (broken access control, authN/authZ-fouten).
- **Input-verwerking & serialisatie:** parsing van representaties en data-binding zijn gevoelig voor injectie-, deserialisatie- en validatieproblemen.
- **Verwerking van medische (PII/PHI) gegevens:** omdat patiëntdata via deze endpoints stroomt, hebben gevonden kwetsbaarheden een hoge potentiële impact (vertrouwelijkheid en integriteit).

De combinatie van hoge complexiteit, brede scope en een security-kritieke functie (de externe API-laag die gevoelige zorgdata ontsluit) maakt `webservices.rest` een sterke keuze voor een compliance-audit met aantoonbare, relevante bevindingen.
