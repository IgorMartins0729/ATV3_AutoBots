package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Usuario;

@Component
public class UsuarioAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();
	private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();

	// telefones, documentos, emails e credenciais sao atualizados pelos proprios endpoints
	public void atualizar(Usuario usuario, Usuario atualizacao) {
		if (!verificador.verificar(atualizacao.getNome())) {
			usuario.setNome(atualizacao.getNome());
		}
		if (!verificador.verificar(atualizacao.getNomeSocial())) {
			usuario.setNomeSocial(atualizacao.getNomeSocial());
		}
		if (atualizacao.getPerfis() != null && !atualizacao.getPerfis().isEmpty()) {
			usuario.getPerfis().clear();
			usuario.getPerfis().addAll(atualizacao.getPerfis());
		}
		if (usuario.getEndereco() == null) {
			if (atualizacao.getEndereco() != null) {
				atualizacao.getEndereco().setId(null);
			}
			usuario.setEndereco(atualizacao.getEndereco());
		} else {
			enderecoAtualizador.atualizar(usuario.getEndereco(), atualizacao.getEndereco());
		}
	}
}
