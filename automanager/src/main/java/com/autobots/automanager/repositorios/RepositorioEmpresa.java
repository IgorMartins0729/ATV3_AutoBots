package com.autobots.automanager.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Empresa;

public interface RepositorioEmpresa extends JpaRepository<Empresa, Long> {
	// buscas pelo "dono" de um item, usadas para desfazer o vinculo antes de excluir
	Optional<Empresa> findByTelefonesId(Long id);

	Optional<Empresa> findByEnderecoId(Long id);

	Optional<Empresa> findByUsuariosId(Long id);

	Optional<Empresa> findByMercadoriasId(Long id);

	Optional<Empresa> findByServicosId(Long id);

	Optional<Empresa> findByVendasId(Long id);
}
