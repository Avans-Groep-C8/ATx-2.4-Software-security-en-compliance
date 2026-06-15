# 2.2 Risicomatrix — OpenMRS REST Web Services Module

## Scoremethode

**Risicoscore = Kans × Impact** (schaal 1–5)

| Score | Niveau |
|-------|--------|
| 15–25 | 🔴 Kritiek |
| 10–14 | 🟠 Hoog |
| 5–9   | 🟡 Midden |
| 1–4   | 🟢 Laag |

---

## Dreigingen

| ID | Dreiging | Kans | Impact | Score | Niveau | CIA |
|----|----------|:----:|:------:|:-----:|--------|-----|
| T1 | Ongeautoriseerde API-toegang (gestolen sessietoken / Basic Auth) | 4 | 5 | **20** | 🔴 Kritiek | V, I, B |
| T4 | Credential-lek in repository (docker-compose / GitHub Actions) | 4 | 4 | **16** | 🔴 Kritiek | V, I |
| T2 | Blootstelling patiëntdata (ontbrekende autorisatiecheck) | 3 | 5 | **15** | 🔴 Kritiek | V |
| T3 | Manipulatie medische orders / allergieën | 2 | 5 | **10** | 🟠 Hoog | I |
| T5 | Supply chain aanval via CI/CD-pipeline | 2 | 5 | **10** | 🟠 Hoog | I, B |
| T6 | Denial of Service op REST API | 3 | 3 | **9** | 🟡 Midden | B |
| T7 | Privilege escalatie via RBAC-fout | 2 | 4 | **8** | 🟡 Midden | V, I |
| T8 | Concept dictionary poisoning | 1 | 4 | **4** | 🟢 Laag | I |

---

## CIA-toelichting per dreiging

### T1 — Ongeautoriseerde API-toegang
- **Vertrouwelijkheid:** aanvaller leest patiëntdata via /ws/rest/v1/patient
- **Integriteit:** aanvaller kan records aanpassen
- **Beschikbaarheid:** account-lockout of sessie-flooding
- **Maatregel:** MFA, token-expiry, rate limiting op loginendpoint

### T2 — Blootstelling patiëntdata
- **Vertrouwelijkheid:** patiëntrecords, observaties en allergieën onbedoeld toegankelijk
- **Maatregel:** strikte privilege-checks per endpoint, HTTPS-only, audit logging

### T3 — Manipulatie medische orders / allergieën
- **Integriteit:** gewijzigde medicatievoorschriften of allergierecords vormen direct gevaar voor patiëntveiligheid
- **Maatregel:** integriteitsvalidatie, digitale signing van kritieke records, onweerlegbare audit trail

### T4 — Credential-lek in repository
- **Vertrouwelijkheid / Integriteit:** wachtwoorden of API-sleutels hardcoded in publieke repo
- **Maatregel:** GitHub secret scanning, .env + .gitignore, secrets manager (bijv. Vault)

### T5 — Supply chain aanval via CI/CD
- **Integriteit:** backdoor in productiebuild via gecompromitteerde GitHub Action of Bamboo-stap
- **Beschikbaarheid:** verstoring van deploymentproces
- **Maatregel:** action-versies pinnen op commit-hash, verplichte code review op workflow-wijzigingen

### T6 — Denial of Service op REST API
- **Beschikbaarheid:** flooding maakt OpenMRS onbereikbaar voor zorgverleners
- **Maatregel:** rate limiting, WAF, horizontale schaalbaarheid via Docker Compose

### T7 — Privilege escalatie via RBAC
- **Vertrouwelijkheid / Integriteit:** gebruiker met beperkte rol verkrijgt hogere rechten door ontbrekende check
- **Maatregel:** least-privilege principe, periodieke review van rollen, logging van rechtenwijzigingen

### T8 — Concept dictionary poisoning
- **Integriteit:** aanpassing van SNOMED/ICD-codes leidt tot foutieve diagnose-interpretatie
- **Maatregel:** toegang beperken, changemanagement-proces, checksums op terminologiebestanden

---

## Risicomatrix (5×5)

```
Kans
 5 | 🟡  🟠  🔴  🔴  🔴
 4 | 🟢  🟡  🟠  🔴  🔴
 3 | 🟢  🟡  🟡  🟠  🔴  ← T2(3×5) T6(3×3)
 2 | 🟢  🟢  🟡  🟡  🟠  ← T3(2×5) T5(2×5) T7(2×4)
 1 | 🟢  🟢  🟢  🟡  🟡  ← T8(1×4)
     1    2    3    4    5   Impact
              ↑         ↑
           T4(4×4)   T1(4×5) T2(3×5)
