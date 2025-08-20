package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.standard.util.Ut;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationContext {
    private final Map<String, Object> beans = new HashMap<>();
    private final Reflections  reflections;

    public ApplicationContext(String basePackage) {
        reflections = new Reflections(basePackage);
    }

    public void init() {
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class)
                .stream()
                .filter(c -> !c.isAnnotation())
                .collect(Collectors.toSet());

        for (Class<?> clazz : classes) {
            try {
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                Constructor<?> constructor = constructors[0];

                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] params = Arrays.stream(paramTypes)
                        .map(paramType -> beans.get(Ut.str.lcfirst(paramType.getSimpleName())))
                        .toArray();

                Object instance = constructor.newInstance(params);

                String name = Ut.str.lcfirst(clazz.getSimpleName());

                beans.put(name, instance);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T> T genBean(String beanName) {
        return (T) beans.get(beanName);
    }
}
