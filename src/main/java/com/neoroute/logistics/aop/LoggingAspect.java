package com.neoroute.logistics.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Aspect pour la journalisation centralisée des opérations métier
 * 
 * <p>
 * Cet aspect intercepte automatiquement les appels aux services et contrôleurs
 * pour logger les entrées, sorties et exceptions de manière uniforme.
 * </p>
 * 
 * <p>
 * <strong>Points d'interception:</strong>
 * </p>
 * <ul>
 * <li>Tous les services (@Service)</li>
 * <li>Tous les contrôleurs REST (@RestController)</li>
 * </ul>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Point de coupe pour tous les services
     */
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {
    }

    /**
     * Point de coupe pour tous les contrôleurs REST
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
    }

    /**
     * Log avant l'exécution d'une méthode de service
     * 
     * @param joinPoint Point d'exécution de la méthode
     */
    @Before("serviceMethods()")
    public void logBeforeService(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.debug("→ Appel service: {}.{}()",
                className.substring(className.lastIndexOf('.') + 1), methodName);
    }

    /**
     * Log après le retour réussi d'une méthode de service
     * 
     * @param joinPoint Point d'exécution de la méthode
     * @param result    Résultat retourné par la méthode
     */
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterService(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.debug("← Fin service: {}.{}() - Succès",
                className.substring(className.lastIndexOf('.') + 1), methodName);
    }

    /**
     * Log lorsqu'une exception est levée dans un service ou contrôleur
     * 
     * @param joinPoint Point d'exécution de la méthode
     * @param exception Exception levée
     */
    @AfterThrowing(pointcut = "serviceMethods() || controllerMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.error("✗ Erreur dans {}.{}(): {}",
                className.substring(className.lastIndexOf('.') + 1),
                methodName,
                exception.getMessage());
    }

    /**
     * Log des appels aux endpoints REST
     * 
     * @param joinPoint Point d'exécution de la méthode
     */
    @Before("controllerMethods()")
    public void logBeforeController(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("➤ API: {} - Paramètres: {}", methodName, args.length);
    }
}
