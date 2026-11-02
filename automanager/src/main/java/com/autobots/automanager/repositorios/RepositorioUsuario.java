package com.autobots.automanager.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
	// buscas pelo "dono" de um item, usadas para desfazer o vinculo antes de excluir
	Optional<Usuario> findByTelefonesId(Long id);

	Optional<Usuario> findByEnderecoId(Long id);

	Optional<Usuario> findByDocumentosId(Long id);

	Optional<Usuario> findByEmailsId(Long id);

	Optional<Usuario> findByCredenciaisId(Long id);

	Optional<Usuario> findByMercadoriasId(Long id);

	Optional<Usuario> findByVendasId(Long id);
}
