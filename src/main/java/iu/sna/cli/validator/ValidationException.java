package iu.sna.cli.validator;

import lombok.experimental.StandardException;

@StandardException
public class ValidationException extends RuntimeException {
    public ValidationException(String s) {
        super(s);
    }
}
