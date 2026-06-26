package br.com.srm.receivable.repository;

import br.com.srm.receivable.domain.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Repositório de tipos de recebível — só leitura, pois os produtos são cadastrados via seed. */
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, UUID> { }
