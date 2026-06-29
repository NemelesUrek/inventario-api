package com.nemeles.inventario.web;

import com.nemeles.inventario.service.ConflictException;
import com.nemeles.inventario.service.NotFoundException;
import com.nemeles.inventario.service.PinInvalidoException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Traduce las excepciones a respuestas JSON consistentes (RFC-7807 ProblemDetail).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail noEncontrado(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflicto(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PinInvalidoException.class)
    public ProblemDetail pinInvalido(PinInvalidoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos");
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fe.getField(), fe.getDefaultMessage() == null ? "inválido" : fe.getDefaultMessage());
        }
        pd.setProperty("errores", errores);
        return pd;
    }

    /** JSON malformado, tipo de parámetro inválido o propiedad de orden inexistente -> 400 genérico. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            PropertyReferenceException.class})
    public ProblemDetail malaPeticion(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Petición mal formada.");
    }

    /** Subida que excede el límite multipart -> 413 (en vez de un 500 genérico). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail demasiadoGrande(MaxUploadSizeExceededException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo supera el tamaño permitido.");
    }
}
