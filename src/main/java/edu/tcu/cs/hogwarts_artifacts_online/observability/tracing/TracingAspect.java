package edu.tcu.cs.hogwarts_artifacts_online.observability.tracing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Aspect
@Component
public class TracingAspect {
	
	
	@Autowired
	private Tracer tracer;
	
	
	@Around("@annotation(traced)")
	public Object  trace(ProceedingJoinPoint joinPoint, Traced traced) throws Throwable
	{
		//Determine Span name - use custom value if provided else use method name
		MethodSignature  methodSignature=(MethodSignature) joinPoint.getSignature();
		
		String spanName=traced.value().isEmpty()? methodSignature.getDeclaringType().getSimpleName()+"."
				+ methodSignature.getName():traced.value();
		
		//Creatign a Span
		Span span=tracer.spanBuilder(spanName).startSpan();
		
		
		//Making span active in current context
		try(Scope scope=span.makeCurrent())
		{
			//Adding some important metadata  to span 
			span.setAttribute("class", methodSignature.getDeclaringType().getSimpleName());
			span.setAttribute("method",methodSignature.getMethod().getName());
			return joinPoint.proceed();
		}
		catch(Throwable throwable)
		{
			//Record exception in span if method throws some error
			span.setStatus(StatusCode.ERROR,throwable.getMessage());
			span.recordException(throwable);
			throw throwable;
		}
		
		finally{
			span.end();
		}
		
	}

}
