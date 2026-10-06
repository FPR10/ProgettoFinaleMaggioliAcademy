package it.maggioli.academy.classe.mapper;

import org.springframework.stereotype.Component;

import it.maggioli.academy.classe.dtos.AnagraficaDTO;
import it.maggioli.academy.classe.dtos.AnagraficaRequest;
import it.maggioli.academy.classe.entities.Anagrafica;

@Component 
public class AnagraficaMapper {
    public AnagraficaDTO toDto (Anagrafica a){
        return new AnagraficaDTO(a.getId(), a.getNome(), a.getCognome(), a.getCodiceFiscale(), a.getEmail(),a.getTelefono(), a.getDataNascita());
    }

    public Anagrafica toEntity(AnagraficaRequest r){
        Anagrafica a = new Anagrafica();
        update(a, r);
        return a;
    }

    public void update(Anagrafica a, AnagraficaRequest r){
        a.setNome(r.nome().trim());
        a.setCognome(r.cognome().trim());
        a.setCodiceFiscale(r.codiceFiscale().toUpperCase());
        a.setEmail(r.email().trim());
        a.setTelefono(r.telefono() == null || r.telefono().isBlank() ? null : r.telefono().trim());
        a.setDataNascita(r.dataNascita());
    }
}
