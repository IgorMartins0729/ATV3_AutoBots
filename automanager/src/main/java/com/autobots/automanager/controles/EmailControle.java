package com.autobots.automanager.controles;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkEmail;
import com.autobots.automanager.modelo.EmailAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioEmail;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
@RequestMapping("/email")
public class EmailControle {
	@Autowired
	private RepositorioEmail repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private EmailAtualizador atualizador;
	@Autowired
	private AdicionadorLinkEmail adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Email> obterEmail(@PathVariable long id) {
		Optional<Email> email = repositorio.findById(id);
		if (email.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(email.get());
		return new ResponseEntity<>(email.get(), HttpStatus.OK);
	}

	@GetMapping("/emails")
	public ResponseEntity<List<Email>> obterEmails() {
		List<Email> emails = repositorio.findAll();
		adicionadorLink.adicionarLink(emails);
		return new ResponseEntity<>(emails, HttpStatus.OK);
	}

	@PostMapping("/cadastro/{usuarioId}")
	@Transactional
	public ResponseEntity<Email> cadastrarEmail(@PathVariable long usuarioId, @RequestBody Email email) {
		Optional<Usuario> encontrado = repositorioUsuario.findById(usuarioId);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (verificador.verificar(email.getEndereco())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		email.setId(null);
		Email salvo = repositorio.save(email);
		Usuario usuario = encontrado.get();
		usuario.getEmails().add(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Email> atualizarEmail(@PathVariable long id, @RequestBody Email atualizacao) {
		Optional<Email> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Email email = encontrado.get();
		atualizador.atualizar(email, atualizacao);
		repositorio.save(email);
		adicionadorLink.adicionarLink(email);
		return new ResponseEntity<>(email, HttpStatus.OK);
	}

	// remover do dono basta: orphanRemoval apaga o email
	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirEmail(@PathVariable long id) {
		Optional<Usuario> dono = repositorioUsuario.findByEmailsId(id);
		if (dono.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = dono.get();
		usuario.getEmails().removeIf(email -> email.getId().equals(id));
		repositorioUsuario.save(usuario);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
