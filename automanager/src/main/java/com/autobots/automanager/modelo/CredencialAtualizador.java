package com.autobots.automanager.modelo;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

@Component
public class CredencialAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	// valores padrao de uma credencial nova (criacao e inativo sao obrigatorios no banco)
	public void prepararCadastro(Credencial credencial) {
		credencial.setId(null);
		credencial.setCriacao(new Date());
		if (credencial.getInativo() == null) {
			credencial.setInativo(false);
		}
	}

	public void atualizar(Credencial credencial, Credencial atualizacao) {
		if (atualizacao != null) {
			if (atualizacao.getUltimoAcesso() != null) {
				credencial.setUltimoAcesso(atualizacao.getUltimoAcesso());
			}
			if (atualizacao.getInativo() != null) {
				credencial.setInativo(atualizacao.getInativo());
			}
			// o tipo da credencial nao muda: so atualiza os campos se os dois forem do mesmo tipo
			if (credencial instanceof CredencialUsuarioSenha atual
					&& atualizacao instanceof CredencialUsuarioSenha nova) {
				if (!verificador.verificar(nova.getNomeUsuario())) {
					atual.setNomeUsuario(nova.getNomeUsuario());
				}
				if (!verificador.verificar(nova.getSenha())) {
					atual.setSenha(nova.getSenha());
				}
			}
			if (credencial instanceof CredencialCodigoBarra atual
					&& atualizacao instanceof CredencialCodigoBarra nova && nova.getCodigo() != null) {
				atual.setCodigo(nova.getCodigo());
			}
		}
	}
}
