package com.aws.image.mongodb.util;

import java.util.Optional;
import java.util.function.Consumer;

public class OptionalConsumer<T> {
	private Optional<T> optional;

    private OptionalConsumer(Optional<T> optional) {
        this.optional = optional;
    }
    
    public static <T> OptionalConsumer<T> of(Optional<T> optional) {
        return new OptionalConsumer<>(optional);
    }
    
    public static <T> OptionalConsumer<T> ofNullable(Optional<T> optional) {
        if(optional.isPresent())
        	return new OptionalConsumer<>(optional);
        else
        	return new OptionalConsumer<T>(Optional.empty());
    }

    public OptionalConsumer<T> ifPresent(Consumer<T> c) {
        optional.ifPresent(c);
        return this;
    }

    public OptionalConsumer<T> ifNotPresent(Runnable r) {
        if (!optional.isPresent())
            r.run();
        return this;
    }
    
    /*USAGE:
     * Optional<Any> o = Optional.of(...);
     * Optional<Any> o = Optional.ofNullable(...);
     * ifPresent(opt, x -> System.out.println("found " + x))
    .orElse(() -> System.out.println("NOT FOUND"));*/
}