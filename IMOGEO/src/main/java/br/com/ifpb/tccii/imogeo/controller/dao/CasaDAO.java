/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.ifpb.tccii.imogeo.controller.dao;

import br.com.ifpb.tccii.imogeo.model.Imovel;
import br.com.ifpb.tccii.imogeo.model.especializacao.Casa;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Germano
 */
@Stateless
public class CasaDAO {
    
    @PersistenceContext(unitName = "IMOGEOPU-JTA")
    EntityManager manager;
    
    public Casa retornarCasa(Long id) {
        return manager.find(Casa.class, id);
    }

    public List<Casa> listarCasasAnunciadas() {
        Query query = manager.createQuery("Select c from Casa c");
        List<Casa> casas = query.getResultList();
        List<Casa> result = new ArrayList<Casa>();
        for(int i = 0; i < casas.size();i++){
            if(casas.get(i).getAnuncio().getAnunciado()== true){
                result.add(casas.get(i)); 
            }
        }
        return result;
    }
    
    public List<Casa> listarCasas() {
        Query query = manager.createQuery("Select c from Casa c");
        return query.getResultList();
    }
    
    public List<Casa> listarCasas(String consulta) {
        Query query = manager.createQuery(consulta);
        return query.getResultList();
    }

    public void inserirCasa(Casa c) {
        manager.persist(c);
    }

    public void atualizarCasa(Casa c){
        manager.merge(c);
    }

    public void removerCasa(Casa c) {
        manager.remove(manager.getReference(Casa.class, c.getId()));
    }

}