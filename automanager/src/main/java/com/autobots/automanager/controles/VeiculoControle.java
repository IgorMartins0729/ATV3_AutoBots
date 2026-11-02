package com.autobots.automanager.controles;

import java.util.HashSet;
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

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.modelo.AdicionadorLinkVeiculo;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.modelo.VeiculoAtualizador;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;

// veiculo pertence a um usuario (proprietario)
@RestController
@RequestMapping("/veiculo")
public class VeiculoControle {
	@Autowired
	private RepositorioVeiculo repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private VeiculoAtualizador atualizador;
	@Autowired
	private AdicionadorLinkVeiculo adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Veiculo> obterVeiculo(@PathVariable long id) {
		Optional<Veiculo> veiculo = repositorio.findById(id);
		if (veiculo.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(veiculo.get());
		return new ResponseEntity<>(veiculo.get(), HttpStatus.OK);
	}

	@GetMapping("/veiculos")
	public ResponseEntity<List<Veiculo>> obterVeiculos() {
		List<Veiculo> veiculos = repositorio.findAll();
		adicionadorLink.adicionarLink(veiculos);
		return new ResponseEntity<>(veiculos, HttpStatus.OK);
	}

	@PostMapping("/cadastro/{usuarioId}")
	@Transactional
	public ResponseEntity<Veiculo> cadastrarVeiculo(@PathVariable long usuarioId, @RequestBody Veiculo veiculo) {
		Optional<Usuario> encontrado = repositorioUsuario.findById(usuarioId);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (veiculo.getTipo() == null || verificador.verificar(veiculo.getModelo())
				|| verificador.verificar(veiculo.getPlaca())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Usuario proprietario = encontrado.get();
		veiculo.setId(null);
		veiculo.setProprietario(proprietario);
		// vendas sao registradas pelo cadastro de venda
		veiculo.setVendas(new HashSet<>());
		Veiculo salvo = repositorio.save(veiculo);
		proprietario.getVeiculos().add(salvo);
		repositorioUsuario.save(proprietario);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Veiculo> atualizarVeiculo(@PathVariable long id, @RequestBody Veiculo atualizacao) {
		Optional<Veiculo> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Veiculo veiculo = encontrado.get();
		atualizador.atualizar(veiculo, atualizacao);
		repositorio.save(veiculo);
		adicionadorLink.adicionarLink(veiculo);
		return new ResponseEntity<>(veiculo, HttpStatus.OK);
	}

	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirVeiculo(@PathVariable long id) {
		Optional<Veiculo> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// veiculo atendido faz parte do historico da venda
		if (repositorioVenda.existsByVeiculoId(id)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		Veiculo veiculo = encontrado.get();
		Usuario proprietario = veiculo.getProprietario();
		if (proprietario != null) {
			proprietario.getVeiculos().removeIf(item -> item.getId().equals(id));
			repositorioUsuario.save(proprietario);
		}
		repositorio.delete(veiculo);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
