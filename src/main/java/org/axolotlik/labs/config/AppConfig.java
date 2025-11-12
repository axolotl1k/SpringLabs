package org.axolotlik.labs.config;

import org.axolotlik.labs.util.IdGenerator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

// Демонстрація анотації @Configuration
@Configuration
public class AppConfig {

    // Демонстрація анотації @Bean
    // Демонстрація @Scope("prototype")
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IdGenerator idGenerator() {
        // "prototype" означає, що Spring буде створювати
        // НОВИЙ об'єкт 'IdGenerator' кожного разу,
        // коли хтось попросить цей бін.
        return new IdGenerator();
    }
}