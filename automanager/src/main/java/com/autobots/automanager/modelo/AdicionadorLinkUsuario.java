package com.autobots.automanager.modelo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Usuario;

@Component
public class AdicionadorLinkUsuario implements AdicionadorLink<Usuario> {
	@Autowired
	private AdicionadorLinkTelefone adicionadorLinkTelefone;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLinkEndereco;
	@Autowired
	private AdicionadorLinkDocumento adicionadorLinkDocumento;
	@Autowired
	private AdicionadorLinkEmail adicionadorLinkEmail;
	@Autowired
	private AdicionadorLinkCredencial adicionadorLinkCredencial;
	@Autowired
	private AdicionadorLinkMercadoria adicionadorLinkMercadoria;
	@Autowired
	private AdicionadorLinkVeiculo adicionadorLinkVeiculo;

	@Override
	public void adicionarLink(Collection<Usuario> lista) {
		for (Usuario usuario : lista) {
			adicionarLinkProprio(usuario);
			adicionarLinkRelacionados(usuario);
		}
	}

	@Override
	public void adicionarLink(Usuario objeto) {
		adicionarLinkProprio(objeto);
		adicionarLinkRelacionados(objeto);
		Link linkLista = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(UsuarioControle.class)
						.obterUsuarios())
				.withRel("usuarios");
		objeto.add(linkLista);
	}

	private void adicionarLinkProprio(Usuario usuario) {
		// o mesmo usuario pode aparecer mais de uma vez na resposta (ex.: cliente de varias vendas)
		if (usuario.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(UsuarioControle.class)
						.obterUsuario(usuario.getId()))
				.withSelfRel();
		usuario.add(linkProprio);
	}

	private void adicionarLinkRelacionados(Usuario usuario) {
		adicionadorLinkTelefone.adicionarLink(usuario.getTelefones());
		adicionadorLinkDocumento.adicionarLink(usuario.getDocumentos());
		adicionadorLinkEmail.adicionarLink(usuario.getEmails());
		adicionadorLinkCredencial.adicionarLink(usuario.getCredenciais());
		adicionadorLinkMercadoria.adicionarLink(usuario.getMercadorias());
		adicionadorLinkVeiculo.adicionarLink(usuario.getVeiculos());
		if (usuario.getEndereco() != null) {
			adicionadorLinkEndereco.adicionarLink(List.of(usuario.getEndereco()));
		}
	}
}
