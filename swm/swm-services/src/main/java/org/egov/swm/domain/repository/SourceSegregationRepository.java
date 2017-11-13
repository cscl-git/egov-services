package org.egov.swm.domain.repository;

import org.egov.swm.domain.model.Pagination;
import org.egov.swm.domain.model.SourceSegregation;
import org.egov.swm.domain.model.SourceSegregationSearch;
import org.egov.swm.persistence.queue.repository.SourceSegregationQueueRepository;
import org.egov.swm.persistence.repository.SourceSegregationJdbcRepository;
import org.egov.swm.web.requests.SourceSegregationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SourceSegregationRepository {

	@Autowired
	private SourceSegregationJdbcRepository sourceSegregationJdbcRepository;

	@Autowired
	private SourceSegregationQueueRepository sourceSegregationQueueRepository;

	public SourceSegregationRequest save(SourceSegregationRequest sourceSegregationRequest) {

		return sourceSegregationQueueRepository.save(sourceSegregationRequest);

	}

	public SourceSegregationRequest update(SourceSegregationRequest sourceSegregationRequest) {

		return sourceSegregationQueueRepository.update(sourceSegregationRequest);

	}

	public Pagination<SourceSegregation> search(SourceSegregationSearch sourceSegregationSearch) {
		return sourceSegregationJdbcRepository.search(sourceSegregationSearch);

	}

	public Boolean uniqueCheck(String tenantId, String fieldName, String fieldValue, String uniqueFieldName,
			String uniqueFieldValue) {

		return sourceSegregationJdbcRepository.uniqueCheck(tenantId, fieldName, fieldValue, uniqueFieldName,
				uniqueFieldValue);
	}

}