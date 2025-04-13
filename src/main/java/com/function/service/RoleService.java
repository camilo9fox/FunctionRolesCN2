package com.function.service;

import com.function.model.Rol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.List;

public class RoleService {
    private final EntityManagerFactory emf;

    public RoleService() {
        this.emf = Persistence.createEntityManagerFactory("OracleDB");
    }

    public Rol getRoleById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Rol.class, id);
        } finally {
            em.close();
        }
    }

    public Rol getRoleByName(String nombreRol) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Rol r WHERE r.nombreRol = :nombreRol", Rol.class)
                    .setParameter("nombreRol", nombreRol)
                    .getSingleResult();
        } catch (Exception e) {
            return null; 
        } finally {
            em.close();
        }
    }

    public List<Rol> getAllRoles() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Rol r", Rol.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Rol createRole(Rol rol) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Rol rolPersistido = em.merge(rol); // ← Usa merge en lugar de persist
            tx.commit();
            return rolPersistido;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Rol updateRole(Rol rol) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Rol existingRol = em.find(Rol.class, rol.getId());
            if (existingRol == null) {
                throw new IllegalArgumentException("Rol no encontrado");
            }

            if (rol.getNombreRol() != null) {
                existingRol.setNombreRol(rol.getNombreRol());
            }
            if (rol.getDescripcion() != null) {
                existingRol.setDescripcion(rol.getDescripcion());
            }

            Rol updatedRol = em.merge(existingRol);
            tx.commit();
            return updatedRol;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteRole(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Rol rol = em.find(Rol.class, id);
            if (rol != null) {
                em.remove(rol);
            } else {
                throw new IllegalArgumentException("Rol no encontrado");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
