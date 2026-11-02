package com.autobots.automanager.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Venda;

public interface RepositorioVenda extends JpaRepository<Venda, Long> {
	// usados para bloquear a exclusao de quem ja faz parte de uma venda
	boolean existsByClienteIdOrFuncionarioId(Long clienteId, Long funcionarioId);

	boolean existsByVeiculoId(Long id);

	boolean existsByMercadoriasId(Long id);

	boolean existsByServicosId(Long id);
}
