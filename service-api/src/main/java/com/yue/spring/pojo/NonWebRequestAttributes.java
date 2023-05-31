package com.yue.spring.pojo;

import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NonWebRequestAttributes implements RequestAttributes {
    private Map<String, Object> map = new ConcurrentHashMap();

    @Override
    public Object getAttribute(String s, int i) {
        return map.get(s);
    }

    @Override
    public void setAttribute(String s, Object o, int i) {
        map.put(s, o);
    }

    @Override
    public void removeAttribute(String s, int i) {
        map.remove(s);
    }

    @Override
    public String[] getAttributeNames(int i) {
        return new String[0];
    }

    @Override
    public void registerDestructionCallback(String s, Runnable runnable, int i) {

    }

    @Override
    public Object resolveReference(String s) {
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public Object getSessionMutex() {
        return null;
    }
}
