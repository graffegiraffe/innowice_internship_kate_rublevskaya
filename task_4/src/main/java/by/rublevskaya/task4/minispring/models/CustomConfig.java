package by.rublevskaya.task4.minispring.models;

import by.rublevskaya.task4.minispring.annotations.Bean;
import by.rublevskaya.task4.minispring.annotations.Configuration;

@Configuration
public class CustomConfig {
    @Bean
    public DependencyComponent dependencyComponent() {
        return new DependencyComponent();
    }
}
