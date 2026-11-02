package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkVenda;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.modelo.VendaAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;

// venda pertence a uma empresa; cliente, funcionario, veiculo, mercadorias e servicos vao no corpo so com o id
@RestController
@RequestMapping("/venda")
public class VendaControle {
	@Autowired
	private RepositorioVenda repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVeiculo repositorioVeiculo;
	@Autowired
	private RepositorioMercadoria repositorioMercadoria;
	@Autowired
	private RepositorioServico repositorioServico;
	@Autowired
	private VendaAtualizador atualizador;
	@Autowired
	private AdicionadorLinkVenda adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Venda> obterVenda(@PathVariable long id) {
		Optional<Venda> venda = repositorio.findById(id);
		if (venda.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(venda.get());
		return new ResponseEntity<>(venda.get(), HttpStatus.OK);
	}

	@GetMapping("/vendas")
	public ResponseEntity<List<Venda>> obterVendas() {
		List<Venda> vendas = repositorio.findAll();
		adicionadorLink.adicionarLink(vendas);
		return new ResponseEntity<>(vendas, HttpStatus.OK);
	}

	@PostMapping("/cadastro/{empresaId}")
	@Transactional
	public ResponseEntity<Venda> cadastrarVenda(@PathVariable long empresaId, @RequestBody Venda dados) {
		Optional<Empresa> encontrada = repositorioEmpresa.findById(empresaId);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (verificador.verificar(dados.getIdentificacao()) || dados.getCliente() == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		// venda nova montada so com o que interessa do corpo
		Venda venda = new Venda();
		venda.setIdentificacao(dados.getIdentificacao());
		venda.setCadastro(dados.getCadastro() != null ? dados.getCadastro() : new Date());
		if (!vincular(venda, dados, encontrada.get())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Venda salva = repositorio.save(venda);
		// o historico de compras do cliente fica em usuario.vendas
		salva.getCliente().getVendas().add(salva);
		repositorioUsuario.save(salva.getCliente());
		if (salva.getVeiculo() != null) {
			salva.getVeiculo().getVendas().add(salva);
			repositorioVeiculo.save(salva.getVeiculo());
		}
		Empresa empresa = encontrada.get();
		empresa.getVendas().add(salva);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	@Transactional
	public ResponseEntity<Venda> atualizarVenda(@PathVariable long id, @RequestBody Venda atualizacao) {
		Optional<Venda> encontrada = repositorio.findById(id);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// toda venda pertence a uma empresa: e contra ela que as referencias sao conferidas
		Optional<Empresa> empresa = repositorioEmpresa.findByVendasId(id);
		if (empresa.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		Venda venda = encontrada.get();
		Veiculo veiculoAnterior = venda.getVeiculo();
		Usuario clienteAnterior = venda.getCliente();
		if (!vincular(venda, atualizacao, empresa.get())) {
			// vincular pode ter alterado parte da venda: desfaz tudo
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		atualizador.atualizar(venda, atualizacao);
		repositorio.save(venda);
		if (!Objects.equals(clienteAnterior == null ? null : clienteAnterior.getId(), venda.getCliente().getId())) {
			if (clienteAnterior != null) {
				clienteAnterior.getVendas().removeIf(item -> item.getId().equals(id));
				repositorioUsuario.saveAndFlush(clienteAnterior);
			}
			venda.getCliente().getVendas().add(venda);
			repositorioUsuario.save(venda.getCliente());
		}
		if (venda.getVeiculo() != null && !Objects.equals(veiculoAnterior == null ? null : veiculoAnterior.getId(),
				venda.getVeiculo().getId())) {
			if (veiculoAnterior != null) {
				veiculoAnterior.getVendas().removeIf(item -> item.getId().equals(id));
				repositorioVeiculo.save(veiculoAnterior);
			}
			venda.getVeiculo().getVendas().add(venda);
			repositorioVeiculo.save(venda.getVeiculo());
		}
		adicionadorLink.adicionarLink(venda);
		return new ResponseEntity<>(venda, HttpStatus.OK);
	}

	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirVenda(@PathVariable long id) {
		Optional<Venda> encontrada = repositorio.findById(id);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = encontrada.get();
		repositorioEmpresa.findByVendasId(id).ifPresent(empresa -> {
			empresa.getVendas().removeIf(item -> item.getId().equals(id));
			repositorioEmpresa.save(empresa);
		});
		repositorioUsuario.findByVendasId(id).ifPresent(usuario -> {
			usuario.getVendas().removeIf(item -> item.getId().equals(id));
			repositorioUsuario.save(usuario);
		});
		if (venda.getVeiculo() != null) {
			venda.getVeiculo().getVendas().removeIf(item -> item.getId().equals(id));
			repositorioVeiculo.save(venda.getVeiculo());
		}
		repositorio.delete(venda);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// troca as referencias que vieram so com id pelas do banco; false se alguma nao existir ou for invalida
	private boolean vincular(Venda venda, Venda dados, Empresa empresa) {
		if (dados.getCliente() != null) {
			Usuario cliente = buscarUsuario(dados.getCliente(), PerfilUsuario.CLIENTE);
			if (cliente == null) {
				return false;
			}
			venda.setCliente(cliente);
		}
		if (dados.getFuncionario() != null) {
			Usuario funcionario = buscarUsuario(dados.getFuncionario(), PerfilUsuario.FUNCIONARIO);
			if (funcionario == null) {
				return false;
			}
			venda.setFuncionario(funcionario);
		}
		if (dados.getVeiculo() != null) {
			Long veiculoId = dados.getVeiculo().getId();
			Optional<Veiculo> veiculo = veiculoId == null ? Optional.empty() : repositorioVeiculo.findById(veiculoId);
			if (veiculo.isEmpty()) {
				return false;
			}
			venda.setVeiculo(veiculo.get());
		}
		if (dados.getMercadorias() != null && !dados.getMercadorias().isEmpty()) {
			Set<Long> ids = dados.getMercadorias().stream().map(Mercadoria::getId).collect(Collectors.toSet());
			List<Mercadoria> mercadorias = ids.contains(null) ? List.of() : repositorioMercadoria.findAllById(ids);
			if (mercadorias.size() != ids.size()) {
				return false;
			}
			venda.getMercadorias().clear();
			venda.getMercadorias().addAll(mercadorias);
		}
		if (dados.getServicos() != null && !dados.getServicos().isEmpty()) {
			Set<Long> ids = dados.getServicos().stream().map(Servico::getId).collect(Collectors.toSet());
			List<Servico> servicos = ids.contains(null) ? List.of() : repositorioServico.findAllById(ids);
			if (servicos.size() != ids.size()) {
				return false;
			}
			venda.getServicos().clear();
			venda.getServicos().addAll(servicos);
		}
		// o veiculo atendido tem que ser do cliente da venda
		Veiculo veiculo = venda.getVeiculo();
		if (veiculo != null && venda.getCliente() != null && (veiculo.getProprietario() == null
				|| !veiculo.getProprietario().getId().equals(venda.getCliente().getId()))) {
			return false;
		}
		return daEmpresa(dados, empresa);
	}

	// cliente, funcionario, mercadorias e servicos enviados precisam ser da empresa da venda
	// (so o que veio no corpo: um cliente transferido de empresa nao trava a edicao das vendas antigas)
	private boolean daEmpresa(Venda dados, Empresa empresa) {
		Set<Long> usuarios = empresa.getUsuarios().stream().map(Usuario::getId).collect(Collectors.toSet());
		Set<Long> mercadorias = empresa.getMercadorias().stream().map(Mercadoria::getId).collect(Collectors.toSet());
		Set<Long> servicos = empresa.getServicos().stream().map(Servico::getId).collect(Collectors.toSet());
		return (dados.getCliente() == null || usuarios.contains(dados.getCliente().getId()))
				&& (dados.getFuncionario() == null || usuarios.contains(dados.getFuncionario().getId()))
				&& (dados.getMercadorias() == null
						|| dados.getMercadorias().stream().allMatch(item -> mercadorias.contains(item.getId())))
				&& (dados.getServicos() == null
						|| dados.getServicos().stream().allMatch(item -> servicos.contains(item.getId())));
	}

	private Usuario buscarUsuario(Usuario referencia, PerfilUsuario perfil) {
		if (referencia.getId() == null) {
			return null;
		}
		return repositorioUsuario.findById(referencia.getId())
				.filter(usuario -> usuario.getPerfis().contains(perfil))
				.orElse(null);
	}
}
