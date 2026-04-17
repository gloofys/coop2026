# Coop Panga tarkvaraarendaja praktika kodutöö

See projekt on loodud Coop Panga tarkvaraarendaja praktika kodutöö lahendusena. Rakendus realiseerib laenutaotluse esitamise ja kinnitamise protsessi backend poole ning võimaldab luua laenutaotlusi, valideerida Eesti isikukoode, teha vanusekontrolli, piirata ühe aktiivse taotluse olemasolu kliendi kohta, genereerida annuiteetmaksegraafikut ning viia taotluse ülevaatuse, kinnitamise või tagasilükkamise etappi.

## Setup

### Eeldused

- Docker Desktop
- Java 21
- Git

### Käivitamine Docker Compose abil

Projekti root kaustas käivita:

```bash
docker compose up --build
```

Pärast edukat käivitust on backend kättesaadav aadressil:

`http://localhost:8080`

Swagger UI:

`http://localhost:8080/swagger-ui/index.html`

### Lokaalne käivitamine

Kui soovid backendi käivitada lokaalselt ja andmebaasi Dockeriga, siis:

1. Käivita PostgreSQL:

```bash
docker compose up -d postgres
```

2. Käivita backend:

```bash
cd backend
gradlew bootRun
```

Swagger UI:

`http://localhost:8080/swagger-ui/index.html`

### Testide käivitamine

```bash
cd backend
gradlew clean test
```

## Tehnoloogiad

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- SpringDoc OpenAPI / Swagger
- Docker
- Docker Compose
- JUnit 5
- Mockito

## Funktsionaalsus

### Laenutaotluse loomine

Rakendus võimaldab esitada laenutaotluse järgmiste andmetega:

- eesnimi
- perenimi
- isikukood
- laenu pikkus kuudes
- intressimarginaal
- baasintressimäär
- laenusumma

Taotlus valideeritakse ja salvestatakse andmebaasi.

### Isikukoodi valideerimine

Rakendus kontrollib, et isikukood vastaks Eesti isikukoodi formaadile ja kontrollnumbrile. Vigase isikukoodi korral tagastatakse veateade.

### Vanusekontroll

Rakendus arvutab kliendi vanuse isikukoodi põhjal. Kui kliendi vanus ületab lubatud piirmäära, lükatakse taotlus automaatselt tagasi:

- staatus: `REJECTED`
- põhjus: `CUSTOMER_TOO_OLD`

Vanuse piirmäär on seadistatav konfiguratsioonis.

### Ühe aktiivse taotluse reegel

Ühel kliendil saab korraga olla ainult üks aktiivne taotlus.

Aktiivseteks staatusteks loetakse:

- `SUBMITTED`
- `IN_REVIEW`

Kui kliendil on juba aktiivne taotlus, siis uue aktiivse taotluse loomine blokeeritakse.

### Maksegraafiku genereerimine

Rakendus genereerib igakuise annuiteetmaksegraafiku ning salvestab selle andmebaasi.

Iga maksegraafiku kirje sisaldab:

- makse numbrit
- makse kuupäeva
- kogumakset
- põhiosa makset
- intressiosa makset
- jääkväärtust

Pärast maksegraafiku edukat genereerimist liigub taotlus staatusesse `IN_REVIEW`.

### Taotluse ülevaatamine

Rakendus võimaldab:

- vaadata taotluse detaile
- vaadata genereeritud maksegraafikut
- kinnitada taotluse
- lükata taotluse tagasi põhjusega

## Peamised endpointid

### Loo uus laenutaotlus

`POST /api/loan-applications`

### Vaata taotluse detaile

`GET /api/loan-applications/{id}`

### Genereeri maksegraafik

`POST /api/loan-applications/{id}/schedule`

### Kinnita taotlus

`POST /api/loan-applications/{id}/approve`

### Lükka taotlus tagasi

`POST /api/loan-applications/{id}/reject`

## Näidisprotsess

1. Luuakse uus laenutaotlus.
2. Kontrollitakse isikukoodi korrektsust.
3. Tehakse vanusekontroll.
4. Sobiva kliendi puhul jääb taotlus staatusesse `SUBMITTED`.
5. Genereeritakse maksegraafik.
6. Taotlus liigub staatusesse `IN_REVIEW`.
7. Taotlus kinnitatakse või lükatakse tagasi.

## Veahaldus

Rakenduses on kasutatud `@RestControllerAdvice` lahendust, et eristada äriloogikast tulenevaid vigu tehnilistest vigadest.

Näited ärivigadest:

- vigane isikukood
- juba olemasolev aktiivne taotlus
- vale olek üleminekuks

## Projektistruktuur

```text
coop-loan-approval/
├── backend/
│   ├── src/
│   ├── build.gradle
│   ├── Dockerfile
│   └── ...
├── frontend/
├── docker-compose.yml
└── README.md
```

## Märkused

- Andmebaasi skeemi haldamiseks kasutatakse Flyway migratsioone.
- OpenAPI dokumentatsioon on kättesaadav Swagger UI kaudu.
- Äriloogika katmiseks on lisatud Mockito põhised ühikutestid.
- Lahendus keskendub backendi põhifunktsionaalsusele.

## Võimalikud edasiarendused

- lihtne kasutajaliides
- maksegraafiku regenereerimine ülevaatuse etapis
- täiendavad controller või integratsioonitestid
- dünaamiliste parameetrite hoidmine andmebaasis