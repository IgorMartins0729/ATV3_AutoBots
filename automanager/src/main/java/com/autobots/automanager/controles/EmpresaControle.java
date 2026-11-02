package com.autobots.automanager.controles;

import java.util.Date;
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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkEmpresa;
import com.autobots.automanager.modelo.EmpresaAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
@RequestMapping("/empresa")
public class EmpresaControle {
	@Autowired
	private RepositorioEmpresa repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private EmpresaAtualizador atualizador;
	@Autowired
	private AdicionadorLinkEmpresa adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Empresa> obterEmpresa(@PathVariable long id) {
		Optional<Empresa> empresa = repositorio.findById(id);
		if (empresa.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(empresa.get());
		return new ResponseEntity<>(empresa.get(), HttpStatus.OK);
	}

	@GetMapping("/empresas")
	public ResponseEntity<List<Empresa>> obterEmpresas() {
		List<Empresa> empresas = repositorio.findAll();
		adicionadorLink.adicionarLink(empresas);
		return new ResponseEntity<>(empresas, HttpStatus.OK);
	}

	@PostMapping("/cadastro")
	public ResponseEntity<Empresa> cadastrarEmpresa(@RequestBody Empresa empresa) {
		if (verificador.verificar(empresa.getRazaoSocial())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		// ids vindos no corpo sao ignorados: cadastro sempre cria registros novos
		empresa.setId(null);
		empresa.setCadastro(new Date());
		if (empresa.getTelefones() == null) {
			empresa.setTelefones(new HashSet<>());
		}
		empresa.getTelefones().forEach(telefone -> telefone.setId(null));
		if (empresa.getEndereco() != null) {
			empresa.getEndereco().setId(null);
		}
		// usuarios, mercadorias, servicos e vendas tem cadastro proprio, vinculado a empresa
		empresa.setUsuarios(new HashSet<>());
		empresa.setMercadorias(new HashSet<>());
		empresa.setServicos(new HashSet<>());
		empresa.setVendas(new HashSet<>());
		Empresa salva = repositorio.save(empresa);
		adicionadorLink.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Empresa> atualizarEmpresa(@PathVariable long id, @RequestBody Empresa atualizacao) {
		Optional<Empresa> encontrada = repositorio.findById(id);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = encontrada.get();
		atualizador.atualizar(empresa, atualizacao);
		repositorio.save(empresa);
		adicionadorLink.adicionarLink(empresa);
		return new ResponseEntity<>(empresa, HttpStatus.OK);
	}

	// um usuario pertence a uma empresa so: se ja estiver em outra, e transferido
	@PutMapping("/associar/{empresaId}/usuario/{usuarioId}")
	@Transactional
	public ResponseEntity<Empresa> associarUsuario(@PathVariable long empresaId, @PathVariable long usuarioId) {
		Optional<Empresa> encontrada = repositorio.findById(empresaId);
		Optional<Usuario> usuario = repositorioUsuario.findById(usuarioId);
		if (encontrada.isEmpty() || usuario.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = encontrada.get();
		repositorio.findByUsuariosId(usuarioId).ifPresent(anterior -> {
			anterior.getUsuarios().removeIf(item -> item.getId().equals(usuarioId));
			repositorio.saveAndFlush(anterior);
		});
		empresa.getUsuarios().add(usuario.get());
		repositorio.save(empresa);
		adicionadorLink.adicionarLink(empresa);
		return new ResponseEntity<>(empresa, HttpStatus.OK);
	}

	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirEmpresa(@PathVariable long id) {
		Optional<Empresa> encontrada = repositorio.findById(id);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = encontrada.get();
		// usuarios e vendas carregam historico: precisam ser excluidos antes
		if (!empresa.getUsuarios().isEmpty() || !empresa.getVendas().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		repositorio.delete(empresa);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
