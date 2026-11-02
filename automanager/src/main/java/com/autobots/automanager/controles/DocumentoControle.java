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

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkDocumento;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioDocumento;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {
	@Autowired
	private RepositorioDocumento repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private DocumentoAtualizador atualizador;
	@Autowired
	private AdicionadorLinkDocumento adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
		Optional<Documento> documento = repositorio.findById(id);
		if (documento.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(documento.get());
		return new ResponseEntity<>(documento.get(), HttpStatus.OK);
	}

	@GetMapping("/documentos")
	public ResponseEntity<List<Documento>> obterDocumentos() {
		List<Documento> documentos = repositorio.findAll();
		adicionadorLink.adicionarLink(documentos);
		return new ResponseEntity<>(documentos, HttpStatus.OK);
	}

	@PostMapping("/cadastro/{usuarioId}")
	@Transactional
	public ResponseEntity<Documento> cadastrarDocumento(@PathVariable long usuarioId,
			@RequestBody Documento documento) {
		Optional<Usuario> encontrado = repositorioUsuario.findById(usuarioId);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (documento.getTipo() == null || documento.getDataEmissao() == null
				|| verificador.verificar(documento.getNumero())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		documento.setId(null);
		Documento salvo = repositorio.save(documento);
		Usuario usuario = encontrado.get();
		usuario.getDocumentos().add(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Documento> atualizarDocumento(@PathVariable long id, @RequestBody Documento atualizacao) {
		Optional<Documento> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Documento documento = encontrado.get();
		atualizador.atualizar(documento, atualizacao);
		repositorio.save(documento);
		adicionadorLink.adicionarLink(documento);
		return new ResponseEntity<>(documento, HttpStatus.OK);
	}

	// remover do dono basta: orphanRemoval apaga o documento
	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirDocumento(@PathVariable long id) {
		Optional<Usuario> dono = repositorioUsuario.findByDocumentosId(id);
		if (dono.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = dono.get();
		usuario.getDocumentos().removeIf(documento -> documento.getId().equals(id));
		repositorioUsuario.save(usuario);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
