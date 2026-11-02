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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkTelefone;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioTelefone;
import com.autobots.automanager.repositorios.RepositorioUsuario;

// telefone pertence a um usuario ou a uma empresa
@RestController
@RequestMapping("/telefone")
public class TelefoneControle {
	@Autowired
	private RepositorioTelefone repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private TelefoneAtualizador atualizador;
	@Autowired
	private AdicionadorLinkTelefone adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
		Optional<Telefone> telefone = repositorio.findById(id);
		if (telefone.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(telefone.get());
		return new ResponseEntity<>(telefone.get(), HttpStatus.OK);
	}

	@GetMapping("/telefones")
	public ResponseEntity<List<Telefone>> obterTelefones() {
		List<Telefone> telefones = repositorio.findAll();
		adicionadorLink.adicionarLink(telefones);
		return new ResponseEntity<>(telefones, HttpStatus.OK);
	}

	@PostMapping("/cadastro/usuario/{usuarioId}")
	@Transactional
	public ResponseEntity<Telefone> cadastrarTelefoneUsuario(@PathVariable long usuarioId,
			@RequestBody Telefone telefone) {
		Optional<Usuario> encontrado = repositorioUsuario.findById(usuarioId);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (invalido(telefone)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		telefone.setId(null);
		Telefone salvo = repositorio.save(telefone);
		Usuario usuario = encontrado.get();
		usuario.getTelefones().add(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PostMapping("/cadastro/empresa/{empresaId}")
	@Transactional
	public ResponseEntity<Telefone> cadastrarTelefoneEmpresa(@PathVariable long empresaId,
			@RequestBody Telefone telefone) {
		Optional<Empresa> encontrada = repositorioEmpresa.findById(empresaId);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (invalido(telefone)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		telefone.setId(null);
		Telefone salvo = repositorio.save(telefone);
		Empresa empresa = encontrada.get();
		empresa.getTelefones().add(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Telefone> atualizarTelefone(@PathVariable long id, @RequestBody Telefone atualizacao) {
		Optional<Telefone> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Telefone telefone = encontrado.get();
		atualizador.atualizar(telefone, atualizacao);
		repositorio.save(telefone);
		adicionadorLink.adicionarLink(telefone);
		return new ResponseEntity<>(telefone, HttpStatus.OK);
	}

	// remover do dono basta: orphanRemoval apaga o telefone
	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirTelefone(@PathVariable long id) {
		Optional<Usuario> usuario = repositorioUsuario.findByTelefonesId(id);
		if (usuario.isPresent()) {
			usuario.get().getTelefones().removeIf(telefone -> telefone.getId().equals(id));
			repositorioUsuario.save(usuario.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		Optional<Empresa> empresa = repositorioEmpresa.findByTelefonesId(id);
		if (empresa.isPresent()) {
			empresa.get().getTelefones().removeIf(telefone -> telefone.getId().equals(id));
			repositorioEmpresa.save(empresa.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	private boolean invalido(Telefone telefone) {
		return verificador.verificar(telefone.getDdd()) || verificador.verificar(telefone.getNumero());
	}
}
