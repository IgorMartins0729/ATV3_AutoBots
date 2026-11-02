package com.autobots.automanager.controles;

import java.util.Date;
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
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.modelo.AdicionadorLinkMercadoria;
import com.autobots.automanager.modelo.MercadoriaAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

// mercadoria pertence ao estoque de uma empresa
@RestController
@RequestMapping("/mercadoria")
public class MercadoriaControle {
	@Autowired
	private RepositorioMercadoria repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private MercadoriaAtualizador atualizador;
	@Autowired
	private AdicionadorLinkMercadoria adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Mercadoria> obterMercadoria(@PathVariable long id) {
		Optional<Mercadoria> mercadoria = repositorio.findById(id);
		if (mercadoria.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(mercadoria.get());
		return new ResponseEntity<>(mercadoria.get(), HttpStatus.OK);
	}

	@GetMapping("/mercadorias")
	public ResponseEntity<List<Mercadoria>> obterMercadorias() {
		List<Mercadoria> mercadorias = repositorio.findAll();
		adicionadorLink.adicionarLink(mercadorias);
		return new ResponseEntity<>(mercadorias, HttpStatus.OK);
	}

	@PostMapping("/cadastro/{empresaId}")
	@Transactional
	public ResponseEntity<Mercadoria> cadastrarMercadoria(@PathVariable long empresaId,
			@RequestBody Mercadoria mercadoria) {
		Optional<Empresa> encontrada = repositorioEmpresa.findById(empresaId);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (verificador.verificar(mercadoria.getNome()) || mercadoria.getValidade() == null
				|| mercadoria.getFabricacao() == null || mercadoria.getQuantidade() == null
				|| mercadoria.getValor() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		mercadoria.setId(null);
		mercadoria.setCadastro(new Date());
		Mercadoria salva = repositorio.save(mercadoria);
		Empresa empresa = encontrada.get();
		empresa.getMercadorias().add(salva);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Mercadoria> atualizarMercadoria(@PathVariable long id,
			@RequestBody Mercadoria atualizacao) {
		Optional<Mercadoria> encontrada = repositorio.findById(id);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Mercadoria mercadoria = encontrada.get();
		atualizador.atualizar(mercadoria, atualizacao);
		repositorio.save(mercadoria);
		adicionadorLink.adicionarLink(mercadoria);
		return new ResponseEntity<>(mercadoria, HttpStatus.OK);
	}

	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirMercadoria(@PathVariable long id) {
		Optional<Mercadoria> encontrada = repositorio.findById(id);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// mercadoria vendida faz parte do historico da venda
		if (repositorioVenda.existsByMercadoriasId(id)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		repositorioEmpresa.findByMercadoriasId(id).ifPresent(empresa -> {
			empresa.getMercadorias().removeIf(mercadoria -> mercadoria.getId().equals(id));
			repositorioEmpresa.save(empresa);
		});
		repositorioUsuario.findByMercadoriasId(id).ifPresent(fornecedor -> {
			fornecedor.getMercadorias().removeIf(mercadoria -> mercadoria.getId().equals(id));
			repositorioUsuario.save(fornecedor);
		});
		repositorio.delete(encontrada.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
