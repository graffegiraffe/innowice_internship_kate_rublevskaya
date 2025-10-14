package by.rublevskaya.task4.minispring.utils;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public class ReflectionUtils {
    public static Set<Class<?>> findClassesWithAnnotation(String basePackage, Class<? extends Annotation> annotationClass) {
        Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
        return reflections.getTypesAnnotatedWith(annotationClass, true);
    }
}
