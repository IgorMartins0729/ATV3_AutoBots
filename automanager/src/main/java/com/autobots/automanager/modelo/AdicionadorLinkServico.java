package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.entidades.Servico;

@Component
public class AdicionadorLinkServico implements AdicionadorLink<Servico> {

	@Override
	public void adicionarLink(Collection<Servico> lista) {
		for (Servico servico : lista) {
			adicionarLinkProprio(servico);
		}
	}

	@Override
	public void adicionarLink(Servico objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ServicoControle.class)
						.obterServicos())
				.withRel("servicos");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Servico servico) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (servico.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(ServicoControle.class)
						.obterServico(servico.getId()))
				.withSelfRel();
		servico.add(linkProprio);
	}
}
