# Loyalty Points — Backend

Backend Java 25 / Spring Boot 3.5 du programme de fidélité, organisé en **Maven multi-modules** selon les principes de la **Clean Architecture**.

## Vue d'ensemble

```
loyalty-points/ (pom parent)
├── domain/           # Cœur métier — entités, règles de gestion (FIFO, expiration)
├── application/      # Cas d'utilisation (use cases) et ports (interfaces)
├── infrastructure/   # Adapters sortants — persistance JPA/H2, notifications
└── presentation/     # Adapter entrant — API REST, point d'entrée Spring Boot
```

## Règle de dépendance

Les dépendances pointent **uniquement vers l'intérieur** : plus on s'approche du domaine, moins on dépend de la technique.

```
presentation ──┐
               ├──► application ──► domain
infrastructure ┘
```

| Module | Dépend de | Frameworks |
|---|---|---|
| `domain` | rien | aucun (Java pur) |
| `application` | `domain` | aucun (Java pur) |
| `infrastructure` | `application` | Spring Data JPA, H2 |
| `presentation` | `application`, `infrastructure` | Spring Web, Validation |

> `presentation` ne dépend d'`infrastructure` que pour le câblage Spring (composition root) : le code applicatif ne manipule que les ports.

## Rôle de chaque module

### `domain` — Cœur métier
Entités et logique métier **pures**, sans aucune dépendance framework :
- lots de points avec date d'expiration ;
- consommation et expiration en **FIFO** (les points gagnés en premier sont dépensés/expirés en premier) ;
- règles de dépense : paiement, conversion en bon d'achat, don.

Testé unitairement avec JUnit 5 + AssertJ, sans Spring.

### `application` — Cas d'utilisation
Orchestration du métier via des **use cases**, un par action utilisateur (gagner des points, dépenser, consulter le solde, notifier avant expiration…).

Définit les **ports** (interfaces) dont il a besoin — persistance, notification — sans connaître leur implémentation (inversion de dépendance).

Testé avec des mocks (Mockito) des ports.

### `infrastructure` — Adapters sortants
Implémente les ports de `application` :
- **Persistance** : entités JPA (distinctes des entités du domaine) + repositories Spring Data, base H2 ;
- **Notifications** : envoi des alertes avant expiration des points.

La conversion entités JPA ↔ entités du domaine se fait dans ce module : le domaine ne connaît jamais JPA.

### `presentation` — API REST
- Contrôleurs REST (DTOs de requête/réponse distincts des objets du domaine) ;
- Validation des entrées (`spring-boot-starter-validation`) ;
- Classe `@SpringBootApplication` : point d'entrée et composition root où Spring câble les adapters sur les ports.

## Lancement

```bash
mvn spring-boot:run -pl presentation
```

## Build & tests

```bash
mvn clean verify        # build complet + tests de tous les modules
mvn test -pl domain     # tests d'un seul module
```

## Pourquoi cette architecture ?

- **Testabilité** : le métier (`domain`, `application`) se teste sans Spring ni base de données ;
- **Indépendance du framework** : remplacer H2 par PostgreSQL ou REST par du messaging ne touche que les adapters ;
- **Protection par le build** : la séparation en modules Maven rend toute violation de la règle de dépendance impossible à compiler (le `domain` ne *peut pas* importer Spring).
