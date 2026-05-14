package com.tp.donatrack;

public class ImportacionResponseDTO {
    private Boolean success;
    private String message;

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
