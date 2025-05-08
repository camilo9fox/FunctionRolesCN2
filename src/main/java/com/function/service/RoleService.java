package com.function.service;

import com.function.model.Rol;
import com.function.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.List;
import com.function.events.EventGridPublisher;

public class RoleService {
    private final EntityManagerFactory emf;
    private final EventGridPublisher eventPublisher;

    public RoleService() {
        this.emf = Persistence.createEntityManagerFactory("OracleDB");
        this.eventPublisher = new EventGridPublisher();
    }

    public Rol getRoleById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Rol rol = em.find(Rol.class, id);
            if (rol != null) {
                eventPublisher.publishRoleRetrieved(rol);
            }
            return rol;
        } finally {
            em.close();
        }
    }

    public Rol getRoleByName(String nombreRol) {
        EntityManager em = emf.createEntityManager();
        try {
            Rol rol = em.createQuery("SELECT r FROM Rol r WHERE r.nombreRol = :nombreRol", Rol.class)
                    .setParameter("nombreRol", nombreRol)
                    .getSingleResult();
            if (rol != null) {
                eventPublisher.publishRoleRetrieved(rol);
            }
            return rol;
        } catch (Exception e) {
            return null; 
        } finally {
            em.close();
        }
    }

    public List<Rol> getAllRoles() {
        EntityManager em = emf.createEntityManager();
        try {
            List<Rol> roles = em.createQuery("SELECT r FROM Rol r", Rol.class).getResultList();
            eventPublisher.publishRolesRetrieved(roles);
            return roles;
        } finally {
            em.close();
        }
    }

    public Rol createRole(Rol rol) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Rol rolPersistido = em.merge(rol);
            tx.commit();
            eventPublisher.publishRoleCreated(rolPersistido);
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
            eventPublisher.publishRoleUpdated(updatedRol);
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
                tx.commit();
                eventPublisher.publishRoleDeleted(id);
            } else {
                throw new IllegalArgumentException("Rol no encontrado");
            }
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza el rol de los usuarios que tienen el rol especificado al rol con ID 66 ("Sin rol").
     * @param roleId ID del rol que se está eliminando
     * @return true si se actualizó al menos un usuario, false en caso contrario
     */
    public boolean updateUsersRoleToDefault(long roleId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Obtener el rol por defecto (ID: 66)
            Rol rolPorDefecto = em.find(Rol.class, 66L);
            if (rolPorDefecto == null) {
                throw new IllegalStateException("No se encontró el rol por defecto (ID: 66)");
            }
            
            // Buscar usuarios con el rol que se va a eliminar
            List<Usuario> usuarios = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.rol.id = :roleId", Usuario.class)
                .setParameter("roleId", roleId)
                .getResultList();
            
            if (usuarios.isEmpty()) {
                return false; // No hay usuarios con este rol
            }
            
            // Actualizar el rol de los usuarios encontrados
            for (Usuario usuario : usuarios) {
                usuario.setRol(rolPorDefecto);
                em.merge(usuario);
            }
            
            tx.commit();
            return true;
            
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
