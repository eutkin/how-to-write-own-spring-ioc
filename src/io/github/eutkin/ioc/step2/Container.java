package io.github.eutkin.ioc.step2;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public interface Container {

    Map<String, Object> getBeans();

    void start();

    void stop();

    static Container from(List<BeanDefinition> beanDefinitions) {
        return new ContainerImpl(beanDefinitions);
    }

    class ContainerImpl implements Container {

        private final Map<String, BeanDefinition> beanDefinitions;
        private final Map<Class<?>, List<BeanDefinition>> indexByType;
        private final Map<String, Object> beans;

        public ContainerImpl(List<BeanDefinition> beanDefinitions) {
            this.beanDefinitions = beanDefinitions.stream().collect(toMap(BeanDefinition::getName, bd -> bd));
            this.indexByType = beanDefinitions.stream().collect(groupingBy(BeanDefinition::getBeanClass));
            this.beans = new HashMap<>(this.beanDefinitions.size());
        }

        @Override
        public Map<String, Object> getBeans() {
            return Collections.unmodifiableMap(this.beans);
        }

        @Override
        public void start() {
            this.beanDefinitions.values().forEach(this::createBean);
        }

        @Override
        public void stop() {
        }

        private Object createBean(BeanDefinition beanDefinition) {
            final String factoryBeanName = beanDefinition.getFactoryBeanName();
            final String factoryMethodName = beanDefinition.getFactoryMethodName();
            Creator creator = getExecutable(beanDefinition, factoryBeanName, factoryMethodName);
            final Map<String, Class<?>> beanDependencies = creator.getDependenciesTypes();
            final Object[] dependencies = beanDependencies
                    .entrySet()
                    .stream()
                    .map(dependency -> {
                        if (!this.indexByType.containsKey(dependency.getValue())) {
                            throw new RuntimeException("Don't have dependency for injection: " +
                                    dependency);
                        }
                        final List<BeanDefinition> beanDefinitions = this.indexByType.get(dependency.getValue());
                        if (beanDefinitions.size() > 1) {
                            for (BeanDefinition definition : beanDefinitions) {
                                if (definition.getName().equals(dependency.getKey())) {
                                    return definition;
                                }
                            }
                            throw new RuntimeException("Can't decide which dependency does selected");
                        }
                        return beanDefinitions.get(0);
                    })
                    .map(definition -> {
                        if (this.beans.containsKey(definition.getName())) {
                            return this.beans.get(definition.getName());
                        }
                        return this.createBean(definition);
                    })
                    .toArray();

            final Object bean = creator.create(dependencies);
            this.beans.put(beanDefinition.getName(), bean);
            return bean;
        }

        private Creator getExecutable(BeanDefinition beanDefinition, String factoryBeanName, String factoryMethodName) {
            if (factoryBeanName != null) {
                return getCreator(factoryBeanName, factoryMethodName);
            } else if (factoryMethodName != null) {
                return getCreator(beanDefinition, factoryMethodName);
            } else {
                return getCreator(beanDefinition);
            }
        }

        private Creator getCreator(BeanDefinition beanDefinition) {
            final Constructor<?>[] constructors = beanDefinition.getBeanClass().getConstructors();
            final Constructor<?> constructor = Stream.of(constructors)
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow();
            return new Creator() {
                @Override
                public Object create(Object[] dependencies) {
                    try {
                        constructor.setAccessible(true);
                        return constructor.newInstance(dependencies);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public Map<String, Class<?>> getDependenciesTypes() {
                    return Stream.of(constructor.getParameters()).collect(toMap(Parameter::getName, Parameter::getType));
                }
            };
        }

        private Creator getCreator(BeanDefinition beanDefinition, String factoryMethodName) {
            final Method method = Stream.of(beanDefinition.getBeanClass().getMethods())
                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                    .filter(m -> factoryMethodName.equals(m.getName()))
                    .max(Comparator.comparingInt(Method::getParameterCount))
                    .orElseThrow();
            return new Creator() {
                @Override
                public Object create(Object[] dependencies) {
                    try {
                        method.setAccessible(true);
                        return method.invoke(null, dependencies);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException();
                    }
                }

                @Override
                public Map<String, Class<?>> getDependenciesTypes() {
                    return Stream.of(method.getParameters()).collect(toMap(Parameter::getName, Parameter::getType));
                }
            };
        }

        private Creator getCreator(String factoryBeanName, String factoryMethodName) {
            Object factoryBean;
            if (this.beans.containsKey(factoryBeanName)) {
                factoryBean = this.beans.get(factoryBeanName);
            } else {
                if (!this.beanDefinitions.containsKey(factoryBeanName)) {
                    throw new RuntimeException(factoryBeanName + "not found");
                }
                final BeanDefinition beanDefinition = this.beanDefinitions.get(factoryBeanName);
                factoryBean = this.createBean(beanDefinition);

            }
            final Method method = Stream.of(factoryBean.getClass().getMethods())
                    .filter(m -> factoryMethodName.equals(m.getName()))
                    .max(Comparator.comparingInt(Method::getParameterCount))
                    .orElseThrow();
            return new Creator() {
                @Override
                public Object create(Object[] dependencies) {
                    try {
                        method.setAccessible(true);
                        return method.invoke(factoryBean, dependencies);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public Map<String, Class<?>> getDependenciesTypes() {
                    return Stream.of(method.getParameters()).collect(toMap(Parameter::getName, Parameter::getType));
                }
            };
        }

        private interface Creator {

            Object create(Object[] dependencies);

            Map<String, Class<?>> getDependenciesTypes();
        }

    }
}
