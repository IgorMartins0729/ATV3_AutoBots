package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.entidades.Telefone;

@Component
public class AdicionadorLinkTelefone implements AdicionadorLink<Telefone> {

	@Override
	public void adicionarLink(Collection<Telefone> lista) {
		for (Telefone telefone : lista) {
			adicionarLinkProprio(telefone);
		}
	}

	@Override
	public void adicionarLink(Telefone objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(TelefoneControle.class)
						.obterTelefones())
				.withRel("telefones");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Telefone telefone) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (telefone.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(TelefoneControle.class)
						.obterTelefone(telefone.getId()))
				.withSelfRel();
		telefone.add(linkProprio);
	}
}
