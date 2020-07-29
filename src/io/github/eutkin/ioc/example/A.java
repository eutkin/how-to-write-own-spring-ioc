package io.github.eutkin.ioc.example;

public class A {

    private final B delegate;

    public A(B b1) {
        this.delegate = b1;
    }

    public void print() {
        delegate.print();
    }
}
