package com.incountry.residence.sdk.tools.proxy;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class ProxyUtils {

    private ProxyUtils() {
    }

    public static <T, R> R createLoggingProxyForPublicMethods(T object) {
        Logger log = LogManager.getLogger(object.getClass());
        InvocationHandler handler = (proxy, method, args) -> {
            long currentTime = 0;
            if (log.isDebugEnabled()) {
                currentTime = System.currentTimeMillis();
                log.debug("{} start", method.getName());
            }
            Object result;
            try {
                result = method.invoke(object, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            } finally {
                if (log.isDebugEnabled()) {
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
