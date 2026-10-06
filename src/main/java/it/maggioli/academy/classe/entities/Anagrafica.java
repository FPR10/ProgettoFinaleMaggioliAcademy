package it.maggioli.academy.classe.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Table(name= "anagrafica")
public class Anagrafica {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=50)
    private String nome;

    @Column(nullable=false, length=50)
    private String cognome;

    @Column (name="codice_fiscale", nullable=false, unique=true, length=16)
    private String codiceFiscale;

    @Column (name="email", nullable=false, length=254)
    private String email;

    @Column (name="telefono", length=30)
    private String telefono;


    @Column(name="data_nascita", nullable=false)
    private LocalDate dataNascita;
    
}
