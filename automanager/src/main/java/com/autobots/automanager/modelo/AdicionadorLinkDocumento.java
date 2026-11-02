package com.autobots.automanager.modelo;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.entidades.Documento;

@Component
public class AdicionadorLinkDocumento implements AdicionadorLink<Documento> {

	@Override
	public void adicionarLink(Collection<Documento> lista) {
		for (Documento documento : lista) {
			adicionarLinkProprio(documento);
		}
	}

	@Override
	public void adicionarLink(Documento objeto) {
		adicionarLinkProprio(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(DocumentoControle.class)
						.obterDocumentos())
				.withRel("documentos");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Documento documento) {
		// o mesmo objeto pode aparecer mais de uma vez na resposta
		if (documento.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(DocumentoControle.class)
						.obterDocumento(documento.getId()))
				.withSelfRel();
		documento.add(linkProprio);
	}
}
