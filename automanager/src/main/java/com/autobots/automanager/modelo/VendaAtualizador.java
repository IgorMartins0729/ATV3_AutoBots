package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Venda;

@Component
public class VendaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Venda venda, Venda atualizacao) {
		if (atualizacao != null) {
			if (atualizacao.getCadastro() != null) {
				venda.setCadastro(atualizacao.getCadastro());
			}
			if (!verificador.verificar(atualizacao.getIdentificacao())) {
				venda.setIdentificacao(atualizacao.getIdentificacao());
			}
		}
	}
}
