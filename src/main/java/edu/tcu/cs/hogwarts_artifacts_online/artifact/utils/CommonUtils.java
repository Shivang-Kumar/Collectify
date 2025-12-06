package edu.tcu.cs.hogwarts_artifacts_online.artifact.utils;

import java.util.Collection;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class CommonUtils {
	
	
	public static double getScoreOfProperty(Object obj,String property)
	{
		BeanWrapper wrapper=new BeanWrapperImpl(obj);
		Object value=wrapper.getPropertyValue(property);
		

	    // Null check
	    if (value == null) {
	        throw new IllegalArgumentException(
	                "Property '" + property + "' is null and cannot be used for scoring."
	        );
	    }

		if(value instanceof Collection<?> collection)
		{
			return ((Collection) value).size();
		}
		
		if(value instanceof Number number)
			return number.doubleValue();
		
		throw new IllegalArgumentException("Property '" + property + "' is not numeric or a Collection.");
		
	}

}
