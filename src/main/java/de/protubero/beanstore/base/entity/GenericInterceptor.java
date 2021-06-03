package de.protubero.beanstore.base.entity;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

public class GenericInterceptor {


    @RuntimeType
    public void invoke(@This AbstractEntity proxy, @Origin Method method, @AllArguments Object[] args, @SuperCall Callable<?> zuper) throws Throwable {
        if (args.length != 1) {
        	throw new RuntimeException();
        }
        
        intercept(proxy, method, args, zuper);
    }


    private void intercept(AbstractEntity proxy, Method setter, Object[] args, Callable<?> zuper) {
        final String setterName = setter.getName();
        final String fieldName = Character.toLowerCase(setterName.charAt(3)) + setterName.substring(4);

        proxy.onBeforeSetValue(fieldName, args[0]);

    	try {
			zuper.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
        
        proxy.onAfterValueSet(fieldName, args[0]);
    }


}