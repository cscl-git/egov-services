package org.egov.pt.calculator.web.models.property;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = "code")
public class Role {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long createdBy;
    private Date createdDate;
    private Long lastModifiedBy;
    private Date lastModifiedDate;
    private String tenantId;

    public Role(){}

    public Role(Role role){
        this.setCode(role.getCode());
        this.setName(role.getName());
        this.setId(role.getId());
    }

}