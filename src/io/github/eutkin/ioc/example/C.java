package io.github.eutkin.ioc.example;

public class C {

    public static C valueOf() {
        return new C();
    }

    public void print() {
        System.out.println("Hello World");
    }
}
