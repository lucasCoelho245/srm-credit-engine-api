package br.com.srm.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Tratamento global de exceções da API.
 *
 * @RestControllerAdvice intercepta exceções lançadas em qualquer controller e
 * transforma em respostas JSON padronizadas. Sem isso, o Spring devolveria páginas
 * HTML de erro ou JSON não estruturado, o que quebraria o front.
 *
 * Hierarquia de tratamento:
 *   - BusinessException      → 422 Unprocessable Entity (regra de negócio violada)
 *   - ResourceNotFoundException → 404 Not Found (recurso não existe no banco)
 *   - MethodArgumentNotValidException → 400 Bad Request (falha no @Valid do request)
 *   - Exception (genérico)  → 500 Internal Server Error (erro não previsto, loga stacktrace)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Formato padrão de resposta de erro — igual para todos os handlers. */
    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}

    /** Regra de negócio violada — ex: taxa de câmbio inválida, moeda origem = destino. */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleBusiness(BusinessException ex, HttpServletRequest req) {
        return new ErrorResponse(
                Instant.now().toString(), 422,
                "Unprocessable Entity", ex.getMessage(), req.getRequestURI()
        );
    }

    /** Entidade não encontrada no banco pelo ID fornecido. */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return new ErrorResponse(
                Instant.now().toString(), 404,
                "Not Found", ex.getMessage(), req.getRequestURI()
        );
    }

    /**
     * Falha de validação do @Valid no body do request.
     * Coleta todas as mensagens de campo inválido e junta em uma string única para
     * o front poder exibir todos os erros de uma vez.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return new ErrorResponse(
                Instant.now().toString(), 400,
                "Bad Request", message, req.getRequestURI()
        );
    }

    /**
     * Fallback para qualquer exceção não tratada pelos handlers acima.
     * Loga o stacktrace completo para debugging, mas devolve mensagem genérica
     * para o cliente — nunca expõe detalhes internos em produção.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado ao processar {} {}", req.getMethod(), req.getRequestURI(), ex);
        return new ErrorResponse(
                Instant.now().toString(), 500,
                "Internal Server Error", "Erro interno. Contate o suporte.", req.getRequestURI()
        );
    }
}
