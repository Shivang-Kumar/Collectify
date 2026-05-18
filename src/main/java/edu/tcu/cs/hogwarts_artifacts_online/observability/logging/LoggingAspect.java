package edu.tcu.cs.hogwarts_artifacts_online.observability.logging;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(logged)")
    public Object log(ProceedingJoinPoint joinPoint, Logged logged) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Logger log = LoggerFactory.getLogger(signature.getDeclaringType());

        // Log method entry with arguments
        log.info("→ {}.{}() called with args: {}", 
                className, methodName, Arrays.toString(joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();

        try {
            // Run the actual method
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log method exit with result and execution time
            log.info("← {}.{}() completed in {}ms | result: {}", 
                    className, methodName, executionTime, result);
            
            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log exception
            log.error("✗ {}.{}() failed in {}ms | exception: {}: {}", 
                    className, methodName, executionTime,
                    throwable.getClass().getSimpleName(), throwable.getMessage());
            
            throw throwable;
        }
    }
}
