package com.autobots.automanager.controles;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorExcecoes {

	// valor unico repetido (numero de documento, nome de usuario, identificacao da venda...) ou campo obrigatorio vazio
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Void> violacaoIntegridade() {
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}
}
