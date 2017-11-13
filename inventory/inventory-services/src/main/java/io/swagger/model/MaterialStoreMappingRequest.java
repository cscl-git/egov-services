package io.swagger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contract class for web request. Array of Material items  are used in case of create or update
 */
@ApiModel(description = "Contract class for web request. Array of Material items  are used in case of create or update")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-11-02T16:27:56.269+05:30")
@Builder
public class MaterialStoreMappingRequest {
    @JsonProperty("RequestInfo")
    private org.egov.common.contract.request.RequestInfo requestInfo = null;

    @JsonProperty("materialStoreMappings")
    @Valid
    private List<MaterialStoreMapping> materialStoreMappings = null;

    public MaterialStoreMappingRequest requestInfo(org.egov.common.contract.request.RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    /**
     * Get requestInfo
     *
     * @return requestInfo
     **/
    @ApiModelProperty(value = "")

    @Valid

    public org.egov.common.contract.request.RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(org.egov.common.contract.request.RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public MaterialStoreMappingRequest materialStoreMappings(List<MaterialStoreMapping> materialStoreMappings) {
        this.materialStoreMappings = materialStoreMappings;
        return this;
    }

    public MaterialStoreMappingRequest addMaterialStoreMappingsItem(MaterialStoreMapping materialStoreMappingsItem) {
        if (this.materialStoreMappings == null) {
            this.materialStoreMappings = new ArrayList<MaterialStoreMapping>();
        }
        this.materialStoreMappings.add(materialStoreMappingsItem);
        return this;
    }

    /**
     * Used for search result and create only
     *
     * @return materialStoreMappings
     **/
    @ApiModelProperty(value = "Used for search result and create only")

    @Valid

    public List<MaterialStoreMapping> getMaterialStoreMappings() {
        return materialStoreMappings;
    }

    public void setMaterialStoreMappings(List<MaterialStoreMapping> materialStoreMappings) {
        this.materialStoreMappings = materialStoreMappings;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaterialStoreMappingRequest materialStoreMappingRequest = (MaterialStoreMappingRequest) o;
        return Objects.equals(this.requestInfo, materialStoreMappingRequest.requestInfo) &&
                Objects.equals(this.materialStoreMappings, materialStoreMappingRequest.materialStoreMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestInfo, materialStoreMappings);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MaterialStoreMappingRequest {\n");

        sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
        sb.append("    materialStoreMappings: ").append(toIndentedString(materialStoreMappings)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

