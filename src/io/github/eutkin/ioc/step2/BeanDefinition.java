package io.github.eutkin.ioc.step2;

public class BeanDefinition {

    private final String name;

    private final Class<?> beanClass;

    private String factoryBeanName;

    private String factoryMethodName;

    public BeanDefinition(String name, Class<?> beanClass) {
        this.name = name;
        this.beanClass = beanClass;
    }

    public BeanDefinition(String name, Class<?> beanClass, String factoryMethodName) {
        this.name = name;
        this.beanClass = beanClass;
        this.factoryMethodName = factoryMethodName;
    }

    public BeanDefinition(String name, Class<?> beanClass, String factoryBeanName, String factoryMethodName) {
        this.name = name;
        this.beanClass = beanClass;
        this.factoryBeanName = factoryBeanName;
        this.factoryMethodName = factoryMethodName;
    }

    public String getName() {
        return name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public String getFactoryMethodName() {
        return factoryMethodName;
    }
}
