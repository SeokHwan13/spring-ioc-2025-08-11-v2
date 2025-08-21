package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.standard.util.Ut;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class ApplicationContext {
    private final Map<String, Object> beans = new HashMap<>();
    private final Reflections reflections;

    public ApplicationContext(String basePackage) {
        reflections = new Reflections(basePackage);
    }

    public void init() {
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class)
                .stream()
                .filter(c -> !c.isAnnotation())
                .collect(Collectors.toSet());

        for (Class<?> clazz : classes) {
            createBean(clazz);
        }
    }

    private Object createBean(Class<?> clazz) {
        String name = Ut.str.lcfirst(clazz.getSimpleName());

        // 이미 만들어진 빈이면 반환
        if (beans.containsKey(name)) {
            return beans.get(name);
        }

        try {
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];

            // 생성자 파라미터들을 재귀적으로 해결
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(this::createBean) // 재귀 호출
                    .toArray();

            Object bean = constructor.newInstance(params);
            beans.put(name, bean);
            return bean;

        } catch (Exception e) {
            throw new RuntimeException("빈 생성 실패: " + clazz, e);
        }
    }

    public <T> T genBean(String beanName) {
        return  (T) beans.get(beanName);
    }
}