package it.maggioli.academy.classe.dtos;

import java.time.LocalDate;

public record AnagraficaDTO (

    Long id,
    String nome, 
    String cognome,
    String codiceFiscale,
    String email,
    String telefono,
    LocalDate dataNascita
){ }



