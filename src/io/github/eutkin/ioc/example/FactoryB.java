package io.github.eutkin.ioc.example;

public class FactoryB {

    public static B createB(C delegate) {
        return new B(delegate);
    }
}
