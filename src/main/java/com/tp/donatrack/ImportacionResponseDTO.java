package com.tp.donatrack;


import com.tp.donatrack.domain.entidad.Donante;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImportacionResponseDTO {
    private Boolean success;
    private String message;
    private List<Donante> data = null;
    public ImportacionResponseDTO(Boolean success, String message){
        this.success=success;
        this.message=message;
    }

    public Boolean getSuccess(){
        return this.success;
    }

    public String getMessage(){
        return this.message;
    }

}
