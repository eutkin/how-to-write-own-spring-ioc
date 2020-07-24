package io.github.eutkin.ioc;

import io.github.eutkin.ioc.step1.Container;

import javax.sql.DataSource;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Container container = null; //...;
        final Map<String, Object> instances = container.getInstances(Map.of(
                "inputDatabase", DataSource.class,
                "outputDatabase", DataSource.class
        ));
    }
}
