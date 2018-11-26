package org.egov.infra.indexer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.infra.indexer.custom.pgr.models.PGRCustomDecorator;
import org.egov.infra.indexer.custom.pgr.models.PGRIndexObject;
import org.egov.infra.indexer.custom.pgr.models.ServiceResponse;
import org.egov.infra.indexer.service.IndexerService;
import org.egov.infra.indexer.util.IndexerUtils;
import org.egov.infra.indexer.web.contract.LegacyIndexRequest;
import org.egov.infra.indexer.web.contract.ReindexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class IndexerMessageListener implements MessageListener<String, String> {

	public static final Logger logger = LoggerFactory.getLogger(IndexerMessageListener.class);

	@Autowired
	private IndexerService indexerService;

	@Autowired
	private IndexerUtils indexerUtils;
	
	@Autowired
	private PGRCustomDecorator pgrCustomDecorator;

	@Value("${egov.core.reindex.topic.name}")
	private String reindexTopic;
	
	@Value("${egov.core.legacyindex.topic.name}")
	private String legacyIndexTopic;

	@Value("${egov.indexer.pgr.create.topic.name}")
	private String pgrCreateTopic;

	@Value("${egov.indexer.pgr.update.topic.name}")
	private String pgrUpdateTopic;
	
	@Value("${egov.indexer.pgr.legacyindex.topic.name}")
	private String pgrLegacyTopic;
	


	@Override
	public void onMessage(ConsumerRecord<String, String> data) {
		logger.info("Topic: " + data.topic());
		logger.debug("Value: " + data.value());
		
		ObjectMapper mapper = indexerUtils.getObjectMapper();
		if (data.topic().equals(reindexTopic)) {
			try {
				ReindexRequest reindexRequest = mapper.readValue(data.value(), ReindexRequest.class);
				indexerService.reindexInPages(reindexRequest);
			} catch (Exception e) {
				logger.error("Couldn't parse reindex request: ", e);
			}
		}else if(data.topic().equals(legacyIndexTopic)) {
			try {
				LegacyIndexRequest legacyIndexRequest = mapper.readValue(data.value(), LegacyIndexRequest.class);
				indexerService.legacyIndexInPages(legacyIndexRequest);
			}catch(Exception e) {
				logger.error("Couldn't parse legacyindex request: ", e);
			}
			
		}else if(data.topic().equals(pgrCreateTopic) || data.topic().equals(pgrUpdateTopic) || data.topic().equals(pgrLegacyTopic)) {
			try {
				ServiceResponse serviceResponse = mapper.readValue(data.value(), ServiceResponse.class);
				PGRIndexObject indexObject = pgrCustomDecorator.dataTransformationForPGR(serviceResponse);
				indexerService.elasticIndexer(data.topic(), mapper.writeValueAsString(indexObject));
			} catch (Exception e) {
				logger.error("Couldn't parse reindex request: ", e);
			}
		}else {
			try {
				indexerService.elasticIndexer(data.topic(), data.value());
			} catch (Exception e) {
				logger.error("error while indexing: ", e);
			}
		}
	}

}
