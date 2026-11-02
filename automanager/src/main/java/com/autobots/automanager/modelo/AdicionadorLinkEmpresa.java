package com.autobots.automanager.modelo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.entidades.Empresa;

@Component
public class AdicionadorLinkEmpresa implements AdicionadorLink<Empresa> {
	@Autowired
	private AdicionadorLinkTelefone adicionadorLinkTelefone;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLinkEndereco;
	@Autowired
	private AdicionadorLinkUsuario adicionadorLinkUsuario;
	@Autowired
	private AdicionadorLinkMercadoria adicionadorLinkMercadoria;
	@Autowired
	private AdicionadorLinkServico adicionadorLinkServico;
	@Autowired
	private AdicionadorLinkVenda adicionadorLinkVenda;

	@Override
	public void adicionarLink(Collection<Empresa> lista) {
		for (Empresa empresa : lista) {
			adicionarLinkProprio(empresa);
			adicionarLinkRelacionados(empresa);
		}
	}

	@Override
	public void adicionarLink(Empresa objeto) {
		adicionarLinkProprio(objeto);
		adicionarLinkRelacionados(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmpresaControle.class)
						.obterEmpresas())
				.withRel("empresas");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Empresa empresa) {
		if (empresa.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmpresaControle.class)
						.obterEmpresa(empresa.getId()))
				.withSelfRel();
		empresa.add(linkProprio);
	}

	private void adicionarLinkRelacionados(Empresa empresa) {
		adicionadorLinkTelefone.adicionarLink(empresa.getTelefones());
		adicionadorLinkUsuario.adicionarLink(empresa.getUsuarios());
		adicionadorLinkMercadoria.adicionarLink(empresa.getMercadorias());
		adicionadorLinkServico.adicionarLink(empresa.getServicos());
		adicionadorLinkVenda.adicionarLink(empresa.getVendas());
		if (empresa.getEndereco() != null) {
			adicionadorLinkEndereco.adicionarLink(List.of(empresa.getEndereco()));
		}
	}
}
