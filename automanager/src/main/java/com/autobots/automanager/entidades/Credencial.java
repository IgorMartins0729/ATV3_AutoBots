package com.autobots.automanager.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
// classe abstrata: o JSON precisa do campo "tipo" para saber qual subclasse criar
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({ @JsonSubTypes.Type(value = CredencialUsuarioSenha.class, name = "usuarioSenha"),
		@JsonSubTypes.Type(value = CredencialCodigoBarra.class, name = "codigoBarra") })
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Credencial extends RepresentationModel<Credencial> {
	@Id()
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Date criacao;
	@Column()
	private Date ultimoAcesso;
	@Column(nullable = false)
	private Boolean inativo;
}