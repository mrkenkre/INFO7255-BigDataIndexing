package com.restapi.model;
import lombok.Data;
import java.io.Serializable;

@Data
public class MedPlans implements Serializable {


  /*  @NotBlank(message = "_org field cannot be blank")
    private String _org;

    @NotBlank(message = "objectId field cannot be blank")
    private String objectId;

    @NotBlank(message = "objectType field cannot be blank")
    private String objectType;

    @NotBlank(message = "planType field cannot be blank")
    private String planType;

    @NotBlank(message = "creationDate field cannot be blank")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-(0[1-9]|1\\d|2\\d|3[01])-(\\d{4})$", message = "creationDate must be in MM-dd-yyyy format")
    private String creationDate;*/
}
