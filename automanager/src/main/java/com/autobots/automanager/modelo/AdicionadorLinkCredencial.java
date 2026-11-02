package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.entidades.Credencial;

@Component
public class AdicionadorLinkCredencial implements AdicionadorLink<Credencial> {

	@Override
	public void adicionarLink(Collection<Credencial> lista) {
		for (Credencial credencial : lista) {
			adicionarLinkProprio(credencial);
		}
	}

	@Override
	public void adicionarLink(Credencial objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(CredencialControle.class)
						.obterCredenciais())
				.withRel("credenciais");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Credencial credencial) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (credencial.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(CredencialControle.class)
						.obterCredencial(credencial.getId()))
				.withSelfRel();
		credencial.add(linkProprio);
	}
}
