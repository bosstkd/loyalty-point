package com.carrefour.kata.domain.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque un service de domaine sans état — une classe qui encapsule de la logique métier
 * n'appartenant pas à un seul agrégat.
 *
 * <p>Cette annotation est intentionnellement libre de toute dépendance framework : elle est
 * définie dans la couche domaine en utilisant uniquement le JDK. L'infrastructure applicative
 * (Spring) la détecte à l'exécution via le filtrage de component-scan.</p>
 *
 * <p>Règles pour les classes annotées avec {@code @DomainService} :
 * <ul>
 *   <li>Aucun import de framework (Spring, JPA, …)</li>
 *   <li>Sans état — pas de champs mutables, pas de dépendances de constructeur</li>
 *   <li>Toutes les dépendances passées en paramètres de méthode</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainService {
}
