package it.maggioli.academy.classe.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import it.maggioli.academy.classe.dtos.AnagraficaDTO;
import it.maggioli.academy.classe.dtos.AnagraficaRequest;
import it.maggioli.academy.classe.serviceImpl.AnagraficaServiceImpl;
import jakarta.validation.Valid;

@RestController  
@RequestMapping("/api/anagrafiche")
public class AnagraficaController {

    private final AnagraficaServiceImpl service;

    public AnagraficaController(AnagraficaServiceImpl service) {
        this.service = service;
    }

    @GetMapping 
    public List<AnagraficaDTO> findAll(){
        return service.findAll();
    }

    @PostMapping 
    public ResponseEntity<AnagraficaDTO> create(@Valid @RequestBody AnagraficaRequest req){
        AnagraficaDTO created = service.create(req);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri()).body(created);
    }
    
    @GetMapping("/{id}")
    public AnagraficaDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public AnagraficaDTO update(@PathVariable Long id, @Valid @RequestBody AnagraficaRequest req) {
        return service.update(id, req);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        service.delete(id);
    }
    
}
