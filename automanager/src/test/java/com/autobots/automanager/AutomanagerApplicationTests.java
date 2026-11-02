package com.autobots.automanager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
class AutomanagerApplicationTests {
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private ObjectMapper mapper;

	@Test
	void contextLoads() {
	}

	// relacionamentos bidirecionais (usuario <-> veiculo <-> venda) nao podem gerar loop no JSON
	@Test
	void empresaSerializaSemLoop() throws Exception {
		Empresa empresa = repositorioEmpresa.findAll().get(0);
		mapper.writeValueAsString(empresa);
		// as duas vendas compartilham a mesma roda (ManyToMany)
		long vendasComRoda = empresa.getVendas().stream()
				.filter(v -> v.getMercadorias().stream().anyMatch(m -> m.getNome().startsWith("Roda"))).count();
		assertEquals(2, vendasComRoda);
	}
}
