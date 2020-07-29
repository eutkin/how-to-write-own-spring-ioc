package io.github.eutkin.ioc;

import io.github.eutkin.ioc.example.A;
import io.github.eutkin.ioc.example.B;
import io.github.eutkin.ioc.example.C;
import io.github.eutkin.ioc.example.FactoryB;
import io.github.eutkin.ioc.step2.BeanDefinition;
import io.github.eutkin.ioc.step2.Container;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Container container = Container.from(List.of(
                new BeanDefinition("b1", B.class, "factoryB", "createB"),
                new BeanDefinition("b2", B.class),
                new BeanDefinition("c", C.class, "valueOf"),
                new BeanDefinition("a", A.class),
                new BeanDefinition("factoryB", FactoryB.class)
        ));

        container.start();
        final Map<String, Object> beans = container.getBeans();
        final A a = (A) beans.get("a");
        a.print();
    }
}
