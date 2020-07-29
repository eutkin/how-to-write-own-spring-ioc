package io.github.eutkin.ioc.example;

public class B {
    private final C delegate;

    public B(C delegate) {
        this.delegate = delegate;
    }

    public void print() {
        delegate.print();
    }
}
