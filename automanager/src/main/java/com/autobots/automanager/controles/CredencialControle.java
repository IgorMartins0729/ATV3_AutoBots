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

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkCredencial;
import com.autobots.automanager.modelo.CredencialAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioCredencial;
import com.autobots.automanager.repositorios.RepositorioUsuario;

// o corpo precisa do campo "tipo": "usuarioSenha" ou "codigoBarra"
@RestController
@RequestMapping("/credencial")
public class CredencialControle {
	@Autowired
	private RepositorioCredencial repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private CredencialAtualizador atualizador;
	@Autowired
	private AdicionadorLinkCredencial adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Credencial> obterCredencial(@PathVariable long id) {
		Optional<Credencial> credencial = repositorio.findById(id);
		if (credencial.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(credencial.get());
		return new ResponseEntity<>(credencial.get(), HttpStatus.OK);
	}

	@GetMapping("/credenciais")
	public ResponseEntity<List<Credencial>> obterCredenciais() {
		List<Credencial> credenciais = repositorio.findAll();
		adicionadorLink.adicionarLink(credenciais);
		return new ResponseEntity<>(credenciais, HttpStatus.OK);
	}

	@PostMapping("/cadastro/{usuarioId}")
	@Transactional
	public ResponseEntity<Credencial> cadastrarCredencial(@PathVariable long usuarioId,
			@RequestBody Credencial credencial) {
		Optional<Usuario> encontrado = repositorioUsuario.findById(usuarioId);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (invalida(credencial)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		atualizador.prepararCadastro(credencial);
		Credencial salva = repositorio.save(credencial);
		Usuario usuario = encontrado.get();
		usuario.getCredenciais().add(salva);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Credencial> atualizarCredencial(@PathVariable long id,
			@RequestBody Credencial atualizacao) {
		Optional<Credencial> encontrada = repositorio.findById(id);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Credencial credencial = encontrada.get();
		atualizador.atualizar(credencial, atualizacao);
		repositorio.save(credencial);
		adicionadorLink.adicionarLink(credencial);
		return new ResponseEntity<>(credencial, HttpStatus.OK);
	}

	// remover do dono basta: orphanRemoval apaga a credencial
	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirCredencial(@PathVariable long id) {
		Optional<Usuario> dono = repositorioUsuario.findByCredenciaisId(id);
		if (dono.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = dono.get();
		usuario.getCredenciais().removeIf(credencial -> credencial.getId().equals(id));
		repositorioUsuario.save(usuario);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	private boolean invalida(Credencial credencial) {
		if (credencial instanceof CredencialUsuarioSenha usuarioSenha) {
			return verificador.verificar(usuarioSenha.getNomeUsuario())
					|| verificador.verificar(usuarioSenha.getSenha());
		}
		return ((CredencialCodigoBarra) credencial).getCodigo() == null;
	}
}
