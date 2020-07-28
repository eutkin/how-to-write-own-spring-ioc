package io.github.eutkin.ioc.step1;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public interface Container {

    Map<String, Object> getBeans();

    static Container from(List<BeanDefinition> beanDefinitions) {
        return new ContainerImpl(beanDefinitions);
    }

    class ContainerImpl implements Container {

        private final Map<String, BeanDefinition> beanDefinitions;
        private final Map<String, Object> beans;

        public ContainerImpl(List<BeanDefinition> beanDefinitions) {
            this.beanDefinitions = beanDefinitions.stream().collect(toMap(BeanDefinition::getName, bd -> bd));
            this.beans = new HashMap<>(this.beanDefinitions.size());
        }

        @Override
        public Map<String, Object> getBeans() {
            return Collections.unmodifiableMap(this.beans);
        }

        private Object createBean(BeanDefinition beanDefinition) {
            final String beanName = beanDefinition.getName();
            final String factoryBeanName = beanDefinition.getFactoryBeanName();
            if (factoryBeanName != null) {
                final Object factoryBean = this.createBean(beanDefinitions.get(factoryBeanName));
                final String factoryMethodName = beanDefinition.getFactoryMethodName();
            }
            return null;
        }

    }
}
