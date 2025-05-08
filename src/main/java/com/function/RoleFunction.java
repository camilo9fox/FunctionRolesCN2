package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.function.model.Rol;
import com.function.service.RoleService;
import com.function.exception.RolNotFoundException;
import com.function.exception.ValidationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.HashMap;

public class RoleFunction {
    private final RoleService roleService = new RoleService();
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) 
            (src, typeOfSrc, context) -> context.serialize(src.toString()))
        .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) 
            (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString()))
        .create();

    @FunctionName("GetRoles")
    public HttpResponseMessage getRoles(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET},
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("Procesando solicitud GET para roles");
        try {
            String id = request.getQueryParameters().get("id");
            if (id != null) {
                try {
                    Rol rol = roleService.getRoleById(Long.parseLong(id)); 
                    if (rol == null) {
                        throw new RolNotFoundException("Rol no encontrado");
                    }
                    return request.createResponseBuilder(HttpStatus.OK)
                        .body(gson.toJson(rol))
                        .build();
                } catch (NumberFormatException e) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("ID inválido")
                        .build();
                }
            }
            return request.createResponseBuilder(HttpStatus.OK)
                .body(gson.toJson(roleService.getAllRoles())) 
                .build();
        } catch (RolNotFoundException e) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                .body(e.getMessage())
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage())
                .build();
        }
    }

    @FunctionName("CreateRole")
    public HttpResponseMessage createRole(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("Procesando solicitud POST para roles");
        try {
            Optional<String> requestBody = request.getBody();
            
            if (!requestBody.isPresent()) { 
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El cuerpo de la solicitud no puede estar vacío")
                    .build();
            }
        
            Rol nuevoRol = gson.fromJson(requestBody.get(), Rol.class);
            Rol creado = roleService.createRole(nuevoRol);
        
            return request.createResponseBuilder(HttpStatus.CREATED)
                .body(gson.toJson(creado))
                .build();
        } catch (JsonSyntaxException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Error en el formato del JSON")
                .build();
        } catch (ValidationException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error de validación");
            response.put("detalles", e.getViolations());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body(response)
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage())
                .build();
        }
    }

    @FunctionName("UpdateRole")
    public HttpResponseMessage updateRole(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.PUT},
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("Procesando solicitud PUT para roles");
        try {
            Optional<String> requestBody = request.getBody();
            if (!requestBody.isPresent()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El cuerpo de la solicitud no puede estar vacío")
                    .build();
            }
            
            Rol rolActualizado = gson.fromJson(requestBody.get(), Rol.class);
            Rol updated = roleService.updateRole(rolActualizado);
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(gson.toJson(updated))
                .build();
        } catch (JsonSyntaxException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Error en el formato del JSON")
                .build();
        } catch (ValidationException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error de validación");
            response.put("detalles", e.getViolations());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body(response)
                .build();
        } catch (RolNotFoundException e) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                .body(e.getMessage())
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage())
                .build();
        }
    }

    @FunctionName("DeleteRole")
    public HttpResponseMessage deleteRole(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.DELETE},
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("Procesando solicitud DELETE para roles");
        try {
            String idParam = request.getQueryParameters().get("id");
            if (idParam == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Se requiere el parámetro 'id'")
                    .build();
            }

            long id;
            try {
                id = Long.parseLong(idParam);
            } catch (NumberFormatException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("ID inválido")
                    .build();
            }

            // Verificar si se está intentando eliminar el rol con ID 66
            if (id == 66) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("No se puede eliminar el rol 'Sin rol' (ID: 66)")
                    .build();
            }

            // Verificar si el rol existe
            Rol rolExistente = roleService.getRoleById(id);
            if (rolExistente == null) {
                throw new RolNotFoundException("Rol no encontrado");
            }

            // Verificar y actualizar usuarios que tengan este rol
            boolean usuariosActualizados = roleService.updateUsersRoleToDefault(id);
            
            // Eliminar el rol
            roleService.deleteRole(id);

            String mensaje = usuariosActualizados 
                ? "Rol eliminado exitosamente. Los usuarios con este rol han sido actualizados al rol 'Sin rol'."
                : "Rol eliminado exitosamente.";

            return request.createResponseBuilder(HttpStatus.OK)
                .body(mensaje)
                .build();

        } catch (RolNotFoundException e) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                .body(e.getMessage())
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error al eliminar el rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage())
                .build();
        }
    }
}
