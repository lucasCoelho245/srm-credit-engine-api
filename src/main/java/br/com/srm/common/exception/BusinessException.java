package br.com.srm.common.exception;

/**
 * Exceção de negócio: lançada quando uma regra de domínio é violada durante o processamento.
 *
 * Exemplos: moedas de câmbio iguais, taxa de câmbio não cadastrada para o par e a data.
 * O GlobalExceptionHandler captura essa exceção e retorna HTTP 422 (Unprocessable Entity)
 * com a mensagem de erro no corpo da resposta JSON.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
