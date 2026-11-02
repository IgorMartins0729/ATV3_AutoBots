package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.entidades.Mercadoria;

@Component
public class AdicionadorLinkMercadoria implements AdicionadorLink<Mercadoria> {

	@Override
	public void adicionarLink(Collection<Mercadoria> lista) {
		for (Mercadoria mercadoria : lista) {
			adicionarLinkProprio(mercadoria);
		}
	}

	@Override
	public void adicionarLink(Mercadoria objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(MercadoriaControle.class)
						.obterMercadorias())
				.withRel("mercadorias");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Mercadoria mercadoria) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (mercadoria.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(MercadoriaControle.class)
						.obterMercadoria(mercadoria.getId()))
				.withSelfRel();
		mercadoria.add(linkProprio);
	}
}
