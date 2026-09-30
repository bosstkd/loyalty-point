# Domain — Modèle métier (DDD)

Cœur métier du programme de fidélité, modélisé selon les principes du **Domain-Driven Design**. Ce module est en **Java pur** : aucune dépendance framework (ni Spring, ni JPA).

## Démarche : partir des invariants

L'invariant central du métier est le **FIFO sur les lots de points** : les points gagnés en premier sont dépensés en premier et expirent en premier. Un invariant qui traverse plusieurs lots doit être protégé par **un seul agrégat** — les lots ne peuvent donc pas être des agrégats indépendants : ce sont des entités internes.

## Agrégat principal : `LoyaltyAccount`

Le compte de fidélité d'un client. Racine d'agrégat, il garantit toute la cohérence du solde et du FIFO :

```
LoyaltyAccount (Aggregate Root)
├── CustomerId          (identité — référence au client, pas d'objet Customer)
└── List<PointsLot>     (Entities internes, jamais exposées en mutation)
     ├── LotId
     ├── Points remaining
     └── earnedAt / expiresAt
```

**Comportements (l'API métier)** :

| Méthode | Rôle |
|---|---|
| `earn(points, expiryDate)` | Crée un nouveau lot de points |
| `spend(points, spendingType)` | Consomme en FIFO, échoue si solde insuffisant |
| `expireLots(now)` | Purge les lots périmés |
| `balance()` | Solde total des lots non expirés |
| `lotsExpiringBefore(date)` | Lots bientôt périmés (pour la notification) |

Le FIFO est un **détail interne** de l'agrégat : aucun code extérieur ne manipule un lot directement.

## Deuxième agrégat : `Voucher`

Le bon d'achat a son **propre cycle de vie** (émis → utilisé / expiré), indépendant du compte une fois créé → agrégat séparé, référencé par identifiant.

Le lien entre les deux se fait au niveau du use case (ou via l'événement `PointsSpent` → création du voucher), jamais par une référence objet directe entre agrégats.

Le **don** et le **paiement**, en revanche, n'ont pas de cycle de vie propre : une simple trace (transaction d'historique ou domain event) suffit — pas d'agrégat.

## Value Objects

Immuables, auto-validés, comparés par valeur :

- `Points` — quantité de points, jamais négative, avec arithmétique (`add`, `subtract`)
- `CustomerId`, `LotId`, `VoucherId` — identités typées (pas de `String`/`UUID` nus)
- `SpendingType` — `PAYMENT` / `VOUCHER` / `DONATION`

## Domain Events

Le domaine **constate** les faits métier, il ne déclenche pas d'effets techniques :

- `PointsEarned`, `PointsSpent`, `PointsExpired`
- `PointsExpiringSoon` — c'est cet événement qui déclenchera la notification côté infrastructure (le domaine ne notifie pas, il constate)

## Repositories

Interfaces définies **dans le domaine**, implémentées dans `infrastructure` (inversion de dépendance). Une par racine d'agrégat :

- `LoyaltyAccountRepository`
- `VoucherRepository`

## Arborescence

```
domain/src/main/java/com/carrefour/kata/domain/
├── shared/            # Points, DomainEvent, exceptions de base
├── account/           # LoyaltyAccount, PointsLot, CustomerId, LotId,
│                      #   SpendingType, LoyaltyAccountRepository, events
└── voucher/           # Voucher, VoucherId, VoucherRepository
```

## Règles de conception

- **Aucune dépendance framework** : le module ne compile qu'avec le JDK ;
- **Encapsulation** : pas de setters ; l'état ne change que via les comportements métier ;
- **Un agrégat = une transaction** : on ne modifie jamais deux agrégats dans la même transaction ;
- **Références entre agrégats par identifiant**, jamais par objet ;
- **Testé unitairement** (JUnit 5 + AssertJ), sans mock : le domaine pur se teste directement.
