package com.incountry.residence.sdk.tools.proxy;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyUtils {
    private static final String MSG_ERR_UNEXPECTED = "Unexpected exception";

    private ProxyUtils() {
    }

    public static <T, R> R createLoggingProxyForPublicMethods(T object, boolean logParameters) {
        Logger log = LogManager.getLogger(object.getClass());
        InvocationHandler handler = (proxy, method, args) -> {
            long currentTime = 0;
            if (log.isDebugEnabled()) {
                currentTime = logBeforeInvoking(log, method, args, logParameters);
            }
            Object result = null;
            try {
                result = method.invoke(object, args);
            } catch (InvocationTargetException ex) {
                Throwable targetException = ex.getTargetException();
                if (StorageException.class.isAssignableFrom(targetException.getClass())) {
                    throw ex.getTargetException();
                }
                throw new StorageClientException(MSG_ERR_UNEXPECTED, targetException);
            } finally {
                if (log.isDebugEnabled()) {
                    currentTime = System.currentTimeMillis() - currentTime;
                    log.debug("{} finish, latency in ms={}", method.getName(), currentTime);
                    if (logParameters && log.isTraceEnabled()) {
                        log.trace("{} result={}", method.getName(), toString(result));
                    }
                }
            }
            return result;
        };
        return (R) Proxy.newProxyInstance(object.getClass().getClassLoader(),
                object.getClass().getInterfaces(), handler);
    }

    private static long logBeforeInvoking(Logger log, Method method, Object[] args, boolean logParameters) {
        long result = System.currentTimeMillis();
        log.debug("{} start", method.getName());
        if (logParameters && log.isTraceEnabled()) {
            StringBuilder builder = new StringBuilder(method.getName() + " params:");
            if (args == null) {
                builder.append("null");
            } else {
                Parameter[] params = method.getParameters();
                for (int i = 0; i < args.length; i++) {
                    builder.append(" ")
                            .append(params[i].getName())
                            .append("=")
                            .append(toString(args[i]))
                            .append(",");
                }
            }
            log.trace(builder.toString());
        }
        return result;
    }

    private static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            return ((List) obj).stream().map(Object::toString).collect(Collectors.joining(",")).toString();
        }
        return obj.toString();
    }
}
