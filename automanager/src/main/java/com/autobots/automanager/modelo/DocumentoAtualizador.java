package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Documento;

@Component
public class DocumentoAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Documento documento, Documento atualizacao) {
		if (atualizacao != null) {
			if (atualizacao.getTipo() != null) {
				documento.setTipo(atualizacao.getTipo());
			}
			if (atualizacao.getDataEmissao() != null) {
				documento.setDataEmissao(atualizacao.getDataEmissao());
			}
			if (!verificador.verificar(atualizacao.getNumero())) {
				documento.setNumero(atualizacao.getNumero());
			}
		}
	}
}
