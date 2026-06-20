package net.kevineleven.undertale_healthbars.util;

public class Reference<T> {
    private T value;

    public Reference(T initialValue) {
        value = initialValue;
    }

    public void set(T newVal) {
        value = newVal;
    }

    public T get() {
        return value;
    }
}
