package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.entidades.Email;

@Component
public class AdicionadorLinkEmail implements AdicionadorLink<Email> {

	@Override
	public void adicionarLink(Collection<Email> lista) {
		for (Email email : lista) {
			adicionarLinkProprio(email);
		}
	}

	@Override
	public void adicionarLink(Email objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmailControle.class)
						.obterEmails())
				.withRel("emails");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Email email) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (email.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmailControle.class)
						.obterEmail(email.getId()))
				.withSelfRel();
		email.add(linkProprio);
	}
}
