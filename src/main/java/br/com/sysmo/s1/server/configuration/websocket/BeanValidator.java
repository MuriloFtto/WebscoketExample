package br.com.sysmo.s1.server.configuration.websocket;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.config.AbstractMessageBrokerConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class BeanValidator {

    private static final String MVC_VALIDATOR_NAME = "mvcValidator";

    @Bean
    public Validator simpValidator(@Autowired ApplicationContext applicationContext) {
        Validator validator = null;
        if (applicationContext != null && applicationContext.containsBean(MVC_VALIDATOR_NAME)) {
            validator = applicationContext.getBean(MVC_VALIDATOR_NAME, Validator.class);
        } else if (ClassUtils.isPresent("javax.validation.Validator", getClass().getClassLoader())) {
            Class<?> clazz;
            try {
                String className = "org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean";
                clazz = ClassUtils.forName(className, AbstractMessageBrokerConfiguration.class.getClassLoader());
            } catch (Throwable ex) {
                throw new BeanInitializationException("Could not find default validator class", ex);
            }
            validator = (Validator) BeanUtils.instantiateClass(clazz);
        } else {
            validator = new Validator() {
                @Override
                public boolean supports(Class<?> clazz) {
                    return false;
                }

                @Override
                public void validate(@Nullable Object target, Errors errors) {
                }
            };
        }
        return validator;
    }
}
