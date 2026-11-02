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
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelo.AdicionadorLinkServico;
import com.autobots.automanager.modelo.ServicoAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioVenda;

// servico pertence ao catalogo de uma empresa
@RestController
@RequestMapping("/servico")
public class ServicoControle {
	@Autowired
	private RepositorioServico repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private ServicoAtualizador atualizador;
	@Autowired
	private AdicionadorLinkServico adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Servico> obterServico(@PathVariable long id) {
		Optional<Servico> servico = repositorio.findById(id);
		if (servico.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(servico.get());
		return new ResponseEntity<>(servico.get(), HttpStatus.OK);
	}

	@GetMapping("/servicos")
	public ResponseEntity<List<Servico>> obterServicos() {
		List<Servico> servicos = repositorio.findAll();
		adicionadorLink.adicionarLink(servicos);
		return new ResponseEntity<>(servicos, HttpStatus.OK);
	}

	@PostMapping("/cadastro/{empresaId}")
	@Transactional
	public ResponseEntity<Servico> cadastrarServico(@PathVariable long empresaId, @RequestBody Servico servico) {
		Optional<Empresa> encontrada = repositorioEmpresa.findById(empresaId);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (verificador.verificar(servico.getNome()) || servico.getValor() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		servico.setId(null);
		Servico salvo = repositorio.save(servico);
		Empresa empresa = encontrada.get();
		empresa.getServicos().add(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Servico> atualizarServico(@PathVariable long id, @RequestBody Servico atualizacao) {
		Optional<Servico> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Servico servico = encontrado.get();
		atualizador.atualizar(servico, atualizacao);
		repositorio.save(servico);
		adicionadorLink.adicionarLink(servico);
		return new ResponseEntity<>(servico, HttpStatus.OK);
	}

	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirServico(@PathVariable long id) {
		Optional<Servico> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// servico prestado faz parte do historico da venda
		if (repositorioVenda.existsByServicosId(id)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		repositorioEmpresa.findByServicosId(id).ifPresent(empresa -> {
			empresa.getServicos().removeIf(servico -> servico.getId().equals(id));
			repositorioEmpresa.save(empresa);
		});
		repositorio.delete(encontrado.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
