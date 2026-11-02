package com.autobots.automanager.modelo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.entidades.Venda;

@Component
public class AdicionadorLinkVenda implements AdicionadorLink<Venda> {
	@Autowired
	private AdicionadorLinkUsuario adicionadorLinkUsuario;
	@Autowired
	private AdicionadorLinkVeiculo adicionadorLinkVeiculo;
	@Autowired
	private AdicionadorLinkMercadoria adicionadorLinkMercadoria;
	@Autowired
	private AdicionadorLinkServico adicionadorLinkServico;

	@Override
	public void adicionarLink(Collection<Venda> lista) {
		for (Venda venda : lista) {
			adicionarLinkProprio(venda);
			adicionarLinkRelacionados(venda);
		}
	}

	@Override
	public void adicionarLink(Venda objeto) {
		adicionarLinkProprio(objeto);
		adicionarLinkRelacionados(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VendaControle.class)
						.obterVendas())
				.withRel("vendas");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Venda venda) {
		if (venda.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VendaControle.class)
						.obterVenda(venda.getId()))
				.withSelfRel();
		venda.add(linkProprio);
	}

	private void adicionarLinkRelacionados(Venda venda) {
		adicionadorLinkMercadoria.adicionarLink(venda.getMercadorias());
		adicionadorLinkServico.adicionarLink(venda.getServicos());
		if (venda.getCliente() != null) {
			adicionadorLinkUsuario.adicionarLink(List.of(venda.getCliente()));
		}
		if (venda.getFuncionario() != null) {
			adicionadorLinkUsuario.adicionarLink(List.of(venda.getFuncionario()));
		}
		if (venda.getVeiculo() != null) {
			adicionadorLinkVeiculo.adicionarLink(List.of(venda.getVeiculo()));
		}
	}
}
