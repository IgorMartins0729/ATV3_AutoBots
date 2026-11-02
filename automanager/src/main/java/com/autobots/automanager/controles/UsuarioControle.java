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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkUsuario;
import com.autobots.automanager.modelo.CredencialAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.modelo.UsuarioAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
@RequestMapping("/usuario")
public class UsuarioControle {
	@Autowired
	private RepositorioUsuario repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private RepositorioMercadoria repositorioMercadoria;
	@Autowired
	private UsuarioAtualizador atualizador;
	@Autowired
	private CredencialAtualizador credencialAtualizador;
	@Autowired
	private AdicionadorLinkUsuario adicionadorLink;
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Usuario> obterUsuario(@PathVariable long id) {
		Optional<Usuario> usuario = repositorio.findById(id);
		if (usuario.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		adicionadorLink.adicionarLink(usuario.get());
		return new ResponseEntity<>(usuario.get(), HttpStatus.OK);
	}

	@GetMapping("/usuarios")
	public ResponseEntity<List<Usuario>> obterUsuarios() {
		List<Usuario> usuarios = repositorio.findAll();
		adicionadorLink.adicionarLink(usuarios);
		return new ResponseEntity<>(usuarios, HttpStatus.OK);
	}

	// todo usuario pertence a uma empresa
	@PostMapping("/cadastro/{empresaId}")
	@Transactional
	public ResponseEntity<Usuario> cadastrarUsuario(@PathVariable long empresaId, @RequestBody Usuario usuario) {
		Optional<Empresa> encontrada = repositorioEmpresa.findById(empresaId);
		if (encontrada.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// nome e ao menos um perfil (cliente, fornecedor ou funcionario) sao obrigatorios
		if (verificador.verificar(usuario.getNome()) || usuario.getPerfis() == null
				|| usuario.getPerfis().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		// ids vindos no corpo sao ignorados: cadastro sempre cria registros novos
		usuario.setId(null);
		if (usuario.getTelefones() == null) {
			usuario.setTelefones(new HashSet<>());
		}
		if (usuario.getDocumentos() == null) {
			usuario.setDocumentos(new HashSet<>());
		}
		if (usuario.getEmails() == null) {
			usuario.setEmails(new HashSet<>());
		}
		if (usuario.getCredenciais() == null) {
			usuario.setCredenciais(new HashSet<>());
		}
		usuario.getTelefones().forEach(telefone -> telefone.setId(null));
		usuario.getDocumentos().forEach(documento -> documento.setId(null));
		usuario.getEmails().forEach(email -> email.setId(null));
		usuario.getCredenciais().forEach(credencialAtualizador::prepararCadastro);
		if (usuario.getEndereco() != null) {
			usuario.getEndereco().setId(null);
		}
		// mercadorias, veiculos e vendas tem cadastro proprio
		usuario.setMercadorias(new HashSet<>());
		usuario.setVeiculos(new HashSet<>());
		usuario.setVendas(new HashSet<>());
		Usuario salvo = repositorio.save(usuario);
		Empresa empresa = encontrada.get();
		empresa.getUsuarios().add(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Usuario> atualizarUsuario(@PathVariable long id, @RequestBody Usuario atualizacao) {
		Optional<Usuario> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = encontrado.get();
		atualizador.atualizar(usuario, atualizacao);
		repositorio.save(usuario);
		adicionadorLink.adicionarLink(usuario);
		return new ResponseEntity<>(usuario, HttpStatus.OK);
	}

	// fornecedor da mercadoria: so usuario com perfil FORNECEDOR; se a mercadoria ja tinha outro, e transferida
	@PutMapping("/associar/{usuarioId}/mercadoria/{mercadoriaId}")
	@Transactional
	public ResponseEntity<Usuario> associarMercadoria(@PathVariable long usuarioId, @PathVariable long mercadoriaId) {
		Optional<Usuario> encontrado = repositorio.findById(usuarioId);
		Optional<Mercadoria> mercadoria = repositorioMercadoria.findById(mercadoriaId);
		if (encontrado.isEmpty() || mercadoria.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario fornecedor = encontrado.get();
		if (!fornecedor.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		// fornecedor e mercadoria precisam ser da mesma empresa
		Optional<Long> empresaFornecedor = repositorioEmpresa.findByUsuariosId(usuarioId).map(Empresa::getId);
		if (empresaFornecedor.isEmpty()
				|| !empresaFornecedor.equals(repositorioEmpresa.findByMercadoriasId(mercadoriaId).map(Empresa::getId))) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		repositorio.findByMercadoriasId(mercadoriaId).ifPresent(anterior -> {
			anterior.getMercadorias().removeIf(item -> item.getId().equals(mercadoriaId));
			repositorio.saveAndFlush(anterior);
		});
		fornecedor.getMercadorias().add(mercadoria.get());
		repositorio.save(fornecedor);
		adicionadorLink.adicionarLink(fornecedor);
		return new ResponseEntity<>(fornecedor, HttpStatus.OK);
	}

	@DeleteMapping("/excluir/{id}")
	@Transactional
	public ResponseEntity<Void> excluirUsuario(@PathVariable long id) {
		Optional<Usuario> encontrado = repositorio.findById(id);
		if (encontrado.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = encontrado.get();
		// usuario com veiculo ou que participou de venda nao pode sumir do historico
		if (!usuario.getVeiculos().isEmpty() || repositorioVenda.existsByClienteIdOrFuncionarioId(id, id)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		repositorioEmpresa.findByUsuariosId(id).ifPresent(empresa -> {
			empresa.getUsuarios().removeIf(item -> item.getId().equals(id));
			repositorioEmpresa.save(empresa);
		});
		repositorio.delete(usuario);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
