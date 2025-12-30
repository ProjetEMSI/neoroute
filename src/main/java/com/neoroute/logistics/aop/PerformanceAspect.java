package com.neoroute.logistics.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Aspect pour le monitoring des performances
 * 
 * <p>
 * Cet aspect mesure automatiquement le temps d'exécution des méthodes
 * dans les services et les contrôleurs pour détecter les goulots
 * d'étranglement.
 * </p>
 * 
 * <p>
 * Les mesures sont loggées avec un niveau WARNING si le temps dépasse
 * un seuil configurable (actuellement 1000ms).
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1. 0
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    /** Seuil en millisecondes pour logger un avertissement de performance */
    private static final long SLOW_METHOD_THRESHOLD_MS = 1000;

    /**
     * Point de coupe pour toutes les méthodes publiques des services
     */
    @Pointcut("execution(public * com.neoroute.logistics.service..*(..))")
    public void publicServiceMethods() {
    }

    /**
     * Point de coupe pour toutes les méthodes publiques des contrôleurs
     */
    @Pointcut("execution(public * com.neoroute.logistics.controller..*(..))")
    public void publicControllerMethods() {
    }

    /**
     * Mesure le temps d'exécution des méthodes de service
     * 
     * @param joinPoint Point d'exécution de la méthode
     * @return Résultat de la méthode interceptée
     * @throws Throwable Si la méthode lève une exception
     */
    @Around("publicServiceMethods()")
    public Object measureServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return measureExecutionTime(joinPoint, "Service");
    }

    /**
     * Mesure le temps d'exécution des méthodes de contrôleur
     * 
     * @param joinPoint Point d'exécution de la méthode
     * @return Résultat de la méthode interceptée
     * @throws Throwable Si la méthode lève une exception
     */
    @Around("publicControllerMethods()")
    public Object measureControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return measureExecutionTime(joinPoint, "Controller");
    }

    /**
     * Méthode utilitaire pour mesurer le temps d'exécution
     * 
     * @param joinPoint Point d'exécution
     * @param type      Type de composant (Service ou Controller)
     * @return Résultat de la méthode
     * @throws Throwable Si la méthode lève une exception
     */
    private Object measureExecutionTime(ProceedingJoinPoint joinPoint, String type) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            String methodName = joinPoint.getSignature().toShortString();

            if (executionTime > SLOW_METHOD_THRESHOLD_MS) {
                log.warn("⚠️ {} lent: {} - Temps: {}ms (> {}ms)",
                        type, methodName, executionTime, SLOW_METHOD_THRESHOLD_MS);
            } else {
                log.trace("⏱️ {} : {} - Temps: {}ms", type, methodName, executionTime);
            }
        }
    }
}
