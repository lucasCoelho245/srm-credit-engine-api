package br.com.srm.common.exception;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado no banco de dados.
 *
 * Exemplos: tipo de produto com UUID desconhecido, moeda não cadastrada.
 * O GlobalExceptionHandler captura essa exceção e retorna HTTP 404 (Not Found)
 * com a mensagem de erro no corpo da resposta JSON.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
