package org.egov.pt.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KakfaConfiguration {

        @Autowired
        private KafkaProperties kafkaProperties;

        public Map<String, Object> consumerConfigs() {
            Map<String, Object> props = new HashMap<>(
                    kafkaProperties.buildConsumerProperties()
            );
            return props;
        }

        public ConsumerFactory<String, Object> consumerFactory() {
            return new DefaultKafkaConsumerFactory<>(consumerConfigs());
        }

        public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory());
            return factory;
        }

    }



