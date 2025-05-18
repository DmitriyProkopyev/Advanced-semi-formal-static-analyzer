package iu.sna.cli;

import io.micrometer.common.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.nio.file.Path;

@Configuration
public class PathConverter implements Converter<String, Path> {

    @Override
    public Path convert(@NonNull String source) {
        return Path.of(source);
    }
}
