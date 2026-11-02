package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.entidades.Endereco;

@Component
public class AdicionadorLinkEndereco implements AdicionadorLink<Endereco> {

	@Override
	public void adicionarLink(Collection<Endereco> lista) {
		for (Endereco endereco : lista) {
			adicionarLinkProprio(endereco);
		}
	}

	@Override
	public void adicionarLink(Endereco objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EnderecoControle.class)
						.obterEnderecos())
				.withRel("enderecos");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Endereco endereco) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (endereco.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EnderecoControle.class)
						.obterEndereco(endereco.getId()))
				.withSelfRel();
		endereco.add(linkProprio);
	}
}
