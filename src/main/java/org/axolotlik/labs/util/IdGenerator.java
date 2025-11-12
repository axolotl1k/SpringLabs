package org.axolotlik.labs.util;

// Це клас з внутрішнім станом (stateful),
// оскільки він зберігає 'counter'.
// Це робить його ідеальним кандидатом для @Scope("prototype").
public class IdGenerator {
    private long counter = 0;

    public long getNextId() {
        return ++counter;
    }
}