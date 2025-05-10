package iu.sna.cli;

import lombok.RequiredArgsConstructor;

import javax.lang.model.type.NullType;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class CommandStringBuilder {

    private final StringBuilder stringBuilder = new StringBuilder();

    public CommandStringBuilder addObject(Object object) {
        ObjectType objectType = Optional.ofNullable(object)
                .map(ObjectType::getByObject)
                .orElse(ObjectType.NULL);

        stringBuilder
                .append(objectType.clazz.getSimpleName())
                .append(" : ")
                .append(objectType.function.apply(object))
                .append('\n');
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    @RequiredArgsConstructor
    private enum ObjectType {
        STRING(String.class, Object::toString),
        ARRAY(String[].class, array -> Arrays.toString((String[]) array)),
        FILE(File.class, file -> ((File) file).getAbsolutePath()),
        ENUM(Enum.class, enumClass -> ((Enum<?>) enumClass).name()),
        NULL(NullType.class, nullObj -> "null");

        private final Class<?> clazz;
        private final Function<Object, String> function;

        public static ObjectType getByObject(Object object) {
            return Stream.of(values())
                    .filter(objectType ->
                            objectType.clazz.equals(object.getClass()) ||
                                    (objectType.clazz.equals(Enum.class) && object instanceof Enum<?>)
                    )
                    .findFirst()
                    .orElse(NULL);
        }
    }
}
