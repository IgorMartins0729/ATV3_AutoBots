package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Empresa;

@Component
public class EmpresaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();
	private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();

	// telefones, usuarios, mercadorias, servicos e vendas sao atualizados pelos proprios endpoints
	public void atualizar(Empresa empresa, Empresa atualizacao) {
		if (!verificador.verificar(atualizacao.getRazaoSocial())) {
			empresa.setRazaoSocial(atualizacao.getRazaoSocial());
		}
		if (!verificador.verificar(atualizacao.getNomeFantasia())) {
			empresa.setNomeFantasia(atualizacao.getNomeFantasia());
		}
		if (empresa.getEndereco() == null) {
			if (atualizacao.getEndereco() != null) {
				atualizacao.getEndereco().setId(null);
			}
			empresa.setEndereco(atualizacao.getEndereco());
		} else {
			enderecoAtualizador.atualizar(empresa.getEndereco(), atualizacao.getEndereco());
		}
	}
}
