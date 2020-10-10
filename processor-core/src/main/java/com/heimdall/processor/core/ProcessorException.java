package com.heimdall.processor.core;

/**
 * @author crh
 * @since 2020-10-03
 */
public class ProcessorException extends RuntimeException {

    private static final long serialVersionUID = -4580810318408300251L;

    public ProcessorException(String message) {
        super(message);
    }

    public ProcessorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessorException(Throwable cause) {
        super(cause);
    }
}
