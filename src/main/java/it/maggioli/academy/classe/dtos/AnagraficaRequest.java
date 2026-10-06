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
    @NotBlank @Pattern(regexp="^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$") String codiceFiscale,
    @NotBlank @Email @Size(max = 254) String email,
    @Size(max = 30) String telefono,
    @NotNull @Past LocalDate dataNascita
) {}
