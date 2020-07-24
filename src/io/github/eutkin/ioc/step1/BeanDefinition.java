package io.github.eutkin.ioc.step1;

public class BeanDefinition {

    private String name;

    private Class<?> beanClass;

    private String factoryBeanName;

    private String factoryMethodName;

    public String getName() {
        return name;
    }

    public BeanDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public BeanDefinition setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        return this;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public BeanDefinition setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
        return this;
    }

    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    public BeanDefinition setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
        return this;
    }
}
