package com.example.service;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CrudService {

    @PersistenceContext
    private EntityManager entityManager;

    public <T> T guardar(T entidad) {
        entityManager.persist(entidad);
        return entidad;
    }

    @Transactional(readOnly = true)
    public <T> T buscarPorId(Class<T> clase, Object id) {
        return entityManager.find(clase, id);
    }

    @Transactional(readOnly = true)
    public <T> List<T> listarTodos(Class<T> clase) {
        String hql = "SELECT e FROM " + clase.getSimpleName() + " e";
        return entityManager.createQuery(hql, clase).getResultList();
    }

    public <T> T actualizar(T entidad) {
        return entityManager.merge(entidad);
    }

    public <T> boolean eliminar(Class<T> clase, Object id) {
        T entidad = entityManager.find(clase, id);
        if (entidad != null) {
            entityManager.remove(entidad);
            return true;
        }
        return false;
    }
}