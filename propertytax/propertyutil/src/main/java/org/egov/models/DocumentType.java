package org.egov.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.enums.ApplicationEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This object holds type of documents to be uploaded during the transaction for
 * each application type. Author : Narendra
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentType {
	@JsonProperty("id")
	private Long id = null;
	
	@JsonProperty("tenantId")
	@Size(min=4,max=128)
	private String tenantId = null;

	@JsonProperty("name")
	private String name = null;
	
	@JsonProperty("code")
	@NotNull
	private String code = null;

	@JsonProperty("application")
	private ApplicationEnum application = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

}
