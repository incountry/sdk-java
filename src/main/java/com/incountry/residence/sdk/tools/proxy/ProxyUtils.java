package com.incountry.residence.sdk.tools.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public class ProxyUtils {

    private ProxyUtils() {
    }

    public static <T, R> R createLoggingProxyForPublicMethods(T object) {
        Logger log = LoggerFactory.getLogger(object.getClass());
        InvocationHandler handler = (proxy, method, args) -> {
            long currentTime = 0;
            boolean isLogged = Modifier.isPublic(method.getModifiers());
            if (isLogged && log.isDebugEnabled()) {
                currentTime = System.currentTimeMillis();
                log.debug("{} start", method.getName());
            }
            Object result;
            try {
                result = method.invoke(object, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            } catch (Exception ex) {
                throw ex;
            } finally {
                if (isLogged && log.isDebugEnabled()) {
                    currentTime = System.currentTimeMillis() - currentTime;
                    log.debug("{} finish, latency in ms={}", method.getName(), currentTime);
                }
            }
            return result;
        };
        return (R) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                object.getClass().getInterfaces(), handler);
    }
}
