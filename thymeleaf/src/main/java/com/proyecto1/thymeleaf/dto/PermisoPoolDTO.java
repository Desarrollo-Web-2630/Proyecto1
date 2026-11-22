package com.proyecto1.thymeleaf.dto;

import com.proyecto1.thymeleaf.model.PermisoPool;
import com.proyecto1.thymeleaf.model.Usuario;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermisoPoolDTO {

    @NotBlank(message = "El rol de acceso es obligatorio")
    private String rolAcceso;

    private boolean puedeCrear;
    private boolean puedeEditar;
    private boolean puedeEliminar;

    public static PermisoPoolDTO desde(PermisoPool permiso) {
        return new PermisoPoolDTO(permiso.getRolAcceso(), permiso.isPuedeCrear(),
                permiso.isPuedeEditar(), permiso.isPuedeEliminar());
    }

    public boolean esRolValido() {
        return rolAcceso != null && (rolAcceso.equals(Usuario.RolAcceso.ADMIN.name())
                || rolAcceso.equals(Usuario.RolAcceso.EDITOR.name())
                || rolAcceso.equals(Usuario.RolAcceso.LECTURA.name()));
    }
}
