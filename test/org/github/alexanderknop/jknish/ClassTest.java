package org.github.alexanderknop.jknish;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassTest {
    @Test
    void testConstruction() {
        Class.RawClass integer = Class.defineClass("Integer");
        Class.RawClass stack = Class.defineClass("Stack")
                .defineGetter("size", integer)
                .defineMethod("pop", Collections.emptyList(), integer)
                .defineMethod("push", List.of(integer), integer);
        assertEquals("Stack{pop : () -> Integer, size : Integer, push : (Integer) -> Integer}",
                stack.finish().toString());
    }
}