# 2.3 Bow-tie analyse — OpenMRS REST Web Services Module

## Context

Deze bow-tie analyse is opgesteld voor de gekozen OpenMRS module `webservices.rest`. Deze module vormt een REST API-laag bovenop OpenMRS en kan daardoor toegang geven tot medische gegevens zoals patiëntgegevens, encounters en observaties.

De bow-tie methode wordt gebruikt om één concreet risicoscenario uit te werken: links staan de dreigingen en preventieve barrières, in het midden het top event, en rechts de gevolgen en herstelbarrières. De methode helpt om zowel preventieve als correctieve maatregelen inzichtelijk te maken.

## Gekozen hoogste risico

Het hoogste risico is:

> Ongeautoriseerde toegang tot of bulk-extractie van patiëntgegevens via de OpenMRS REST API.

Dit risico is gekozen omdat patiëntgegevens kroonjuwelen zijn binnen een healthcare-omgeving. De impact is hoog op vertrouwelijkheid, juridische verplichtingen, reputatie en auditbaarheid.

## Hazard

**Aanwezigheid van herleidbare patiëntgegevens via REST endpoints.**

De hazard is niet de aanval zelf, maar de gevaarlijke situatie: de module verwerkt of ontsluit gevoelige medische gegevens via API-endpoints.

## Top event

**Ongeautoriseerde toegang tot of bulk-extractie van patiëntgegevens via een REST endpoint.**

Dit betekent dat een onbevoegde of onvoldoende bevoegde gebruiker via de REST API patiëntgegevens kan ophalen, bijvoorbeeld door misbruik van credentials, onvoldoende autorisatie of massale API-calls.

## Risico-inschatting

| Onderdeel | Waarde |
|---|---|
| Kans | 4 — Waarschijnlijk |
| Impact | 5 — Catastrofaal |
| Risicoscore | 20 |
| Risiconiveau | Rood — onacceptabel risico |
| CIA/BIV-impact | Vertrouwelijkheid hoog, integriteit midden, beschikbaarheid laag/midden |

De score is gebaseerd op de risicomatrix uit WS03: risico = kans × impact. Een score van 15 of hoger valt in rood en vereist directe mitigatie.

---

## Bow-tie diagram

```mermaid
flowchart LR
    H["Hazard: herleidbare patiëntgegevens beschikbaar via REST API"]
    TE(("Top event: ongeautoriseerde toegang of bulk-extractie van patiëntdata"))

    T1["Threat: BOLA/IDOR - aanvaller wijzigt patient-id of UUID"]
    T2["Threat: gestolen credentials - Basic Auth account misbruikt"]
    T3["Threat: te brede privileges - geen behandelrelatiecontrole"]
    T4["Threat: brute-force / scraping - veel API-calls zonder rate limiting"]
    T5["Threat: onveilige codewijziging - PR introduceert access-control bug"]

    PB1["Preventieve barriere: fine-grained authorization - A.8.3"]
    PB2["Preventieve barriere: MFA/SSO via gateway - A.8.5"]
    PB3["Preventieve barriere: least privilege en autorisatiematrix - A.8.3 / A.5.18"]
    PB4["Preventieve barriere: rate limiting en IP-allowlist - A.8.20 / A.8.26"]
    PB5["Preventieve barriere: CodeQL/SAST en PR-review - A.8.29 / A.8.32"]

    C1["Gevolg: grootschalig datalek patiëntgegevens"]
    C2["Gevolg: niet kunnen aantonen wie wat heeft ingezien"]
    C3["Gevolg: juridische sancties of meldplicht"]
    C4["Gevolg: reputatieschade en verlies van vertrouwen"]

    RB1["Herstelbarriere: anomaliedetectie en SIEM-alerting - A.8.15 / A.8.16"]
    RB2["Herstelbarriere: immutable audit logging - A.8.15"]
    RB3["Herstelbarriere: incident response procedure - A.5.24 / A.5.25 / A.5.26"]
    RB4["Herstelbarriere: communicatie- en herstelplan - A.5.27"]

    H --> TE

    T1 --> PB1 --> TE
    T2 --> PB2 --> TE
    T3 --> PB3 --> TE
    T4 --> PB4 --> TE
    T5 --> PB5 --> TE

    TE --> RB1 --> C1
    TE --> RB2 --> C2
    TE --> RB3 --> C3
    TE --> RB4 --> C4

    classDef hazard fill:#f4b183,stroke:#a65e00,color:#000,stroke-width:2px;
    classDef threat fill:#f8cbad,stroke:#c00000,color:#000,stroke-width:2px;
    classDef preventive fill:#ffe699,stroke:#bf9000,color:#000,stroke-width:2px;
    classDef top fill:#c00000,stroke:#7f0000,color:#fff,stroke-width:3px;
    classDef recovery fill:#9dc3e6,stroke:#2f75b5,color:#000,stroke-width:2px;
    classDef consequence fill:#c6e0b4,stroke:#548235,color:#000,stroke-width:2px;

    class H hazard;
    class T1,T2,T3,T4,T5 threat;
    class PB1,PB2,PB3,PB4,PB5 preventive;
    class TE top;
    class RB1,RB2,RB3,RB4 recovery;
    class C1,C2,C3,C4 consequence;
```