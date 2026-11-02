package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.entidades.Veiculo;

@Component
public class AdicionadorLinkVeiculo implements AdicionadorLink<Veiculo> {

	@Override
	public void adicionarLink(Collection<Veiculo> lista) {
		for (Veiculo veiculo : lista) {
			adicionarLinkProprio(veiculo);
		}
	}

	@Override
	public void adicionarLink(Veiculo objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VeiculoControle.class)
						.obterVeiculos())
				.withRel("veiculos");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Veiculo veiculo) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (veiculo.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VeiculoControle.class)
						.obterVeiculo(veiculo.getId()))
				.withSelfRel();
		veiculo.add(linkProprio);
	}
}
