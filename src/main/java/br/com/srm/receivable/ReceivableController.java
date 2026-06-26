package br.com.srm.receivable;

import br.com.srm.receivable.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de recebíveis: expõe os endpoints de simulação e liquidação.
 *
 * Fica na camada de apresentação (Web/API). Não contém lógica — só recebe o
 * request HTTP, delega para o ReceivableService e devolve a resposta JSON.
 * O @Valid dispara automaticamente as validações definidas nos DTOs de request
 * antes de chamar o service.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Recebíveis", description = "Simulação e liquidação de recebíveis (FIDC)")
public class ReceivableController {

    private final ReceivableService receivableService;

    @GetMapping("/product-types")
    @Operation(summary = "Lista tipos de recebível com spread configurado")
    public List<ProductTypeResponse> listProductTypes() {
        return receivableService.listProductTypes();
    }

    // Simular não persiste — seguro chamar N vezes sem efeito colateral
    @PostMapping("/receivables/simulate")
    @Operation(summary = "Simula deságio SEM persistir — retorna VP e deságio para preview")
    public SimulateResponse simulate(@Valid @RequestBody SimulateRequest request) {
        return receivableService.simulate(request);
    }

    // Liquidar persiste no banco — retorna 201 Created conforme semântica REST
    @PostMapping("/receivables/liquidate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Liquida e persiste em ACID (@Transactional). Rollback em erro.")
    public LiquidateResponse liquidate(@Valid @RequestBody LiquidateRequest request) {
        return receivableService.liquidate(request);
    }
}
