package by.rublevskaya.task4.minispring.context;

import by.rublevskaya.task4.minispring.annotations.*;
import by.rublevskaya.task4.minispring.exceptions.BeanCreatException;
import by.rublevskaya.task4.minispring.lifecycle.InitializingBean;
import by.rublevskaya.task4.minispring.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class MiniAppContext {
    private static final Logger logger = LoggerFactory.getLogger(MiniAppContext.class);

    private final Map<Class<?>, Object> singletonBeans = new HashMap<>();
    private final String basePackage;

    public MiniAppContext(String basePackage) {
        this.basePackage = basePackage;
        initializeContext();
    }

    private void initializeContext() {
        Set<Class<?>> componentClasses = ReflectionUtils.findClassesWithAnnotation(basePackage, Component.class);
        logger.info("Detected {} components in package: {}", componentClasses.size(), basePackage);

        for (Class<?> clazz : componentClasses) {
            if (isSingleton(clazz)) {
                logger.info("Registering singleton bean: {}", clazz.getName());
                singletonBeans.put(clazz, null);
            }
        }

        for (Class<?> clazz : componentClasses) {
            if (isSingleton(clazz)) {
                Object singleton = createBean(clazz);
                singletonBeans.put(clazz, singleton);
                logger.info("Created singleton bean: {}", clazz.getName());
            }
        }

        processConfigurationClasses();
    }

    public <T> T getBean(Class<T> type) {
        logger.info("Retrieving bean: {}", type.getName());
        if (singletonBeans.containsKey(type)) {
            Object bean = singletonBeans.get(type);
            if (bean != null) {
                return type.cast(bean);
            }
            Object singleton = createBean(type);
            singletonBeans.put(type, singleton);
            return type.cast(singleton);
        }

        if (isPrototype(type)) {
            logger.info("Creating prototype bean: {}", type.getName());
            return type.cast(createBean(type));
        }

        logger.error("Bean of type {} is not managed by the container", type.getName());
        throw new RuntimeException("Bean of type " + type.getName() + " is not managed by the container");
    }

    private Object createBean(Class<?> clazz) {
        try {
            logger.info("Creating bean: {}", clazz.getName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);

            if (instance instanceof InitializingBean initializingBean) {
                initializingBean.afterPropertiesSet();
                logger.info("afterPropertiesSet() called for: {}", clazz.getName());
            }
            return instance;
        } catch (Exception e) {
            logger.error("Failed to create bean: {}", clazz.getName(), e);
            throw new BeanCreatException("Failed to create bean: " + clazz.getName(), e);
        }
    }

    private void processConfigurationClasses() {
        Set<Class<?>> configClasses = ReflectionUtils.findClassesWithAnnotation(basePackage, Configuration.class);
        for (Class<?> configClass : configClasses) {
            try {
                Object configInstance = configClass.getDeclaredConstructor().newInstance();
                for (Method method : configClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Bean.class)) {
                        Object bean = method.invoke(configInstance);
                        singletonBeans.put(bean.getClass(), bean);
                        logger.info("Registered bean from @Configuration: {}", bean.getClass().getName());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to process @Configuration class: {}", configClass.getName(), e);
            }
        }
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                Object dependency = getBean(field.getType());
                field.set(instance, dependency);
                logger.info("Injected dependency {} into {}", field.getType().getName(), clazz.getName());
            }
        }
    }

    private boolean isSingleton(Class<?> clazz) {
        Scope scope = clazz.getAnnotation(Scope.class);
        return scope == null || scope.value() == ScopeType.SINGLETON;
    }

    private boolean isPrototype(Class<?> clazz) {
        Scope scope = clazz.getAnnotation(Scope.class);
        return scope != null && scope.value() == ScopeType.PROTOTYPE;
    }
}