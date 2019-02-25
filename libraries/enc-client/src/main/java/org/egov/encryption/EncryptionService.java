package org.egov.encryption;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.User;
import org.egov.encryption.accesscontrol.AbacFilter;
import org.egov.encryption.models.AccessType;
import org.egov.encryption.models.Attribute;
import org.egov.encryption.models.AttributeAccess;
import org.egov.encryption.util.JSONUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class EncryptionService {

    @Autowired
    private EncryptionServiceRestInterface encryptionServiceRestInterface;

    private AbacFilter abacFilter;

    @Value("#{${egov.enc.field.type.map}}")
    private Map<String, String> fieldsAndTheirType;

    private Map<String, List<String>> typesAndFieldsToEncrypt;

    private ObjectMapper mapper;


    public EncryptionService() {
        mapper = new ObjectMapper(new JsonFactory());
    }

    public void initializeTypesAndFieldsToEncrypt() {
        typesAndFieldsToEncrypt = new HashMap<String, List<String>>();
        for (String field : fieldsAndTheirType.keySet()) {
            String type = fieldsAndTheirType.get(field);
            if (!typesAndFieldsToEncrypt.containsKey(type)) {
                List<String> fieldsToEncrypt = new ArrayList<String>();
                typesAndFieldsToEncrypt.put(type, fieldsToEncrypt);
            }
            typesAndFieldsToEncrypt.get(type).add(field);
        }
    }

    public ObjectNode encryptJson(Object plaintextJson, String tenantId) throws IOException {

        JsonNode plaintextNode = createObjectNode(plaintextJson);
        JsonNode encryptedNode = plaintextNode.deepCopy();

        Iterator<String> iterator = typesAndFieldsToEncrypt.keySet().iterator();
        while (iterator.hasNext()) {
            String type = iterator.next();
            List<String> fields = typesAndFieldsToEncrypt.get(type);

            JsonNode jsonNode = JSONUtils.filterJsonNodeWithPaths(plaintextNode, fields);
            if(jsonNode == null)
                continue;
            JsonNode returnedEncryptedNode = mapper.valueToTree(encryptionServiceRestInterface.callEncrypt(tenantId, type,
                    jsonNode));

            encryptedNode = JSONUtils.mergeNodesForGivenPaths(returnedEncryptedNode, encryptedNode, fields);
        }

        return (ObjectNode) encryptedNode;
    }

    public ObjectNode decryptJson(Object ciphertextJson, List<String> paths) throws IOException {
        JsonNode ciphertextNode = createObjectNode(ciphertextJson);
        JsonNode decryptedNode = ciphertextNode.deepCopy();

        JsonNode jsonNode = JSONUtils.filterJsonNodeWithPaths(ciphertextNode, paths);
        if(jsonNode != null) {
            JsonNode returnedDecryptedNode = mapper.valueToTree(encryptionServiceRestInterface.callDecrypt(jsonNode));
            decryptedNode = JSONUtils.mergeNodesForGivenPaths(returnedDecryptedNode, decryptedNode, paths);
        }

        return (ObjectNode) decryptedNode;
    }


    public ObjectNode decryptJson(Object ciphertextJson, User user) throws IOException {

        Map<Attribute, AccessType> attributeAccessTypeMap = abacFilter.getAttributeAccessForRole(user.getRoles());
        List<String> paths = attributeAccessTypeMap.keySet().stream()
                .map(Attribute::getJsonPath).collect(Collectors.toList());

        ObjectNode decryptedNode = decryptJson(ciphertextJson, paths);



        return decryptedNode;
    }

    private ObjectNode createObjectNode(Object plaintextJson) {
        ObjectNode jsonNode = null;
        try {
            if(plaintextJson instanceof ObjectNode)
                jsonNode = (ObjectNode) plaintextJson;
            else if(plaintextJson instanceof String)
                jsonNode = (ObjectNode) mapper.readTree((String) plaintextJson);           //JsonNode from JSON String
            else
                jsonNode = mapper.valueToTree(plaintextJson);                               //JsonNode from POJO or Map
        } catch (Exception e) {
            throw new CustomException("Cannot convert to JsonNode : " + plaintextJson, "Cannot convert to JsonNode");
        }
        return jsonNode;
    }


    public String encryptValue(String plaintext, String tenantId, String type) {
        return encryptValue(new ArrayList<String>(Collections.singleton(plaintext)), tenantId, type).get(0);
    }

    public List<String> encryptValue(List<String> plaintext, String tenantId, String type) {
        Object encryptionResponse = encryptionServiceRestInterface.callEncrypt(tenantId, type, plaintext);
        return (List<String>) encryptionResponse;
    }

    public Object decryptValue(Object ciphertext) {
        return encryptionServiceRestInterface.callDecrypt(ciphertext);
    }


}
