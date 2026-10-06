package it.maggioli.academy.classe.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import it.maggioli.academy.classe.entities.Anagrafica;

@Repository 
public interface AnagraficaRepository extends JpaRepository<Anagrafica, Long>{

    List<Anagrafica> findAllByOrderByCognomeAscNomeAsc();

    boolean existsByCodiceFiscale(String codiceFiscale);

    boolean existsByCodiceFiscaleAndIdNot(String codiceFiscale, Long id);
} 