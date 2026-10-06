package it.maggioli.academy.classe.serviceImpl;

import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import it.maggioli.academy.classe.dtos.AnagraficaDTO;
import it.maggioli.academy.classe.dtos.AnagraficaRequest;
import it.maggioli.academy.classe.entities.Anagrafica;
import it.maggioli.academy.classe.exceptions.AnagraficaNotFoundException;
import it.maggioli.academy.classe.exceptions.CodiceFiscaleDuplicatoException;
import it.maggioli.academy.classe.mapper.AnagraficaMapper;
import it.maggioli.academy.classe.repositories.AnagraficaRepository;


@Service
@Transactional 
public class AnagraficaServiceImpl {

    private final AnagraficaRepository repository;
    private final AnagraficaMapper mapper;

    public AnagraficaServiceImpl(AnagraficaRepository repository, AnagraficaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AnagraficaDTO create (AnagraficaRequest req){
        Anagrafica anagrafica = mapper.toEntity(req);
        if (repository.existsByCodiceFiscale(anagrafica.getCodiceFiscale())) {
            throw new CodiceFiscaleDuplicatoException(anagrafica.getCodiceFiscale());
        }
        return mapper.toDto(repository.save(anagrafica));
    }
    
    public AnagraficaDTO update(Long id, AnagraficaRequest req){
        Anagrafica anagrafica = trova(id);
        String codiceFiscale = mapper.toEntity(req).getCodiceFiscale();
        if (repository.existsByCodiceFiscaleAndIdNot(codiceFiscale, id)) {
            throw new CodiceFiscaleDuplicatoException(codiceFiscale);
        }
        mapper.update(anagrafica, req);
        return mapper.toDto(repository.save(anagrafica));
    }

    @Transactional
    public List<AnagraficaDTO> findAll() {
        return repository.findAllByOrderByCognomeAscNomeAsc().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public AnagraficaDTO findById(Long id) {
        return mapper.toDto(trova(id));
    }

    public void delete(Long id) {
        repository.delete(trova(id));
    }

    private Anagrafica trova(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new AnagraficaNotFoundException(id));
    }
    
    
}
