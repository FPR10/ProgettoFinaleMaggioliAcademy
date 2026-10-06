package it.maggioli.academy.classe.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AnagraficaRequest(

    @NotBlank @Size(max = 50) String nome,
    @NotBlank @Size(max = 50) String cognome,
    @NotBlank @Pattern(regexp="[A-Za-z0-9]{16}") String codiceFiscale,
    @NotBlank @Email String email,
    @NotBlank @Email String telefono,
    @NotNull @Past LocalDate dataNascita
) {}
