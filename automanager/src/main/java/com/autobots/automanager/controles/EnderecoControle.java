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
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkEndereco;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioEndereco;
import com.autobots.automanager.repositorios.RepositorioUsuario;

// endereco pertence a um usuario ou a uma empresa (um so por dono)
@RestController
@RequestMapping("/endereco")
public class EnderecoControle {
	@Autowired
	private RepositorioEndereco repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private EnderecoAtualizador atualizador;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
		Optional<Endereco> endereco = repositorio.findById(id);
		if (endereco.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(endereco.get());
		return new ResponseEntity<>(endereco.get(), HttpStatus.OK);
	}

	@GetMapping("/enderecos")
	public ResponseEntity<List<Endereco>> obterEnderecos() {
		List<Endereco> enderecos = repositorio.findAll();
		adicionadorLink.adicionarLink(enderecos);
		return new ResponseEntity<>(enderecos, HttpStatus.OK);
	}

	@PostMapping("/cadastro/usuario/{usuarioId}")
	@Transactional
	public ResponseEntity<Endereco> cadastrarEnderecoUsuario(@PathVariable long usuarioId,
			@RequestBody Endereco endereco) {
		Optional<Usuario> encontrado = repositorioUsuario.findById(usuarioId);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = encontrado.get();
		if (usuario.getEndereco() != null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		if (invalido(endereco)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		endereco.setId(null);
		Endereco salvo = repositorio.save(endereco);
		usuario.setEndereco(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PostMapping("/cadastro/empresa/{empresaId}")
	@Transactional
	public ResponseEntity<Endereco> cadastrarEnderecoEmpresa(@PathVariable long empresaId,
			@RequestBody Endereco endereco) {
		Optional<Empresa> encontrada = repositorioEmpresa.findById(empresaId);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = encontrada.get();
		if (empresa.getEndereco() != null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		if (invalido(endereco)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		endereco.setId(null);
		Endereco salvo = repositorio.save(endereco);
		empresa.setEndereco(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Endereco> atualizarEndereco(@PathVariable long id, @RequestBody Endereco atualizacao) {
		Optional<Endereco> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Endereco endereco = encontrado.get();
		atualizador.atualizar(endereco, atualizacao);
		repositorio.save(endereco);
		adicionadorLink.adicionarLink(endereco);
		return new ResponseEntity<>(endereco, HttpStatus.OK);
	}

	// tirar do dono basta: orphanRemoval apaga o endereco
	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirEndereco(@PathVariable long id) {
		Optional<Usuario> usuario = repositorioUsuario.findByEnderecoId(id);
		if (usuario.isPresent()) {
			usuario.get().setEndereco(null);
			repositorioUsuario.save(usuario.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		Optional<Empresa> empresa = repositorioEmpresa.findByEnderecoId(id);
		if (empresa.isPresent()) {
			empresa.get().setEndereco(null);
			repositorioEmpresa.save(empresa.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// todos os campos do endereco sao obrigatorios no banco, exceto informacoesAdicionais
	private boolean invalido(Endereco endereco) {
		return verificador.verificar(endereco.getEstado()) || verificador.verificar(endereco.getCidade())
				|| verificador.verificar(endereco.getBairro()) || verificador.verificar(endereco.getRua())
				|| verificador.verificar(endereco.getNumero()) || verificador.verificar(endereco.getCodigoPostal());
	}
}
