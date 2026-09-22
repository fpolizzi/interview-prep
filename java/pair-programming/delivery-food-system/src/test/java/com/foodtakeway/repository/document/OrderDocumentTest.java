package com.foodtakeway.repository.document;

import com.foodtakeway.model.Order;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDocumentTest {

    private MappingMongoConverter converter;

    @BeforeEach
    void setUp() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.afterPropertiesSet();
        converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, mappingContext);
        converter.afterPropertiesSet();
    }

    @Test
    @DisplayName("Should convert domain Order to OrderDocument")
    void shouldConvertDomainToDocument() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .orderId(orderId)
                .amount(150.0)
                .userEmail("user@example.com")
                .isProcessed(true)
                .build();

        OrderDocument document = OrderDocument.fromDomain(order);

        assertThat(document).isNotNull();
        assertThat(document.getOrderId()).isEqualTo(orderId);
        assertThat(document.getAmount()).isEqualTo(150.0);
        assertThat(document.getUserEmail()).isEqualTo("user@example.com");
        assertThat(document.isProcessed()).isTrue();
    }

    @Test
    @DisplayName("Should return null document when domain Order is null")
    void shouldReturnNullWhenDomainIsNull() {
        assertThat(OrderDocument.fromDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should convert OrderDocument to domain Order")
    void shouldConvertDocumentToDomain() {
        UUID orderId = UUID.randomUUID();
        OrderDocument document = OrderDocument.builder()
                .orderId(orderId)
                .amount(250.0)
                .userEmail("customer@test.com")
                .isProcessed(false)
                .build();

        Order order = document.toDomain();

        assertThat(order).isNotNull();
        assertThat(order.getOrderId()).isEqualTo(orderId);
        assertThat(order.getAmount()).isEqualTo(250.0);
        assertThat(order.getUserEmail()).isEqualTo("customer@test.com");
        assertThat(order.isProcessed()).isFalse();
    }

    @Test
    @DisplayName("Should correctly serialize OrderDocument to MongoDB BSON Document with _id mapped from UUID")
    void shouldMapToBsonDocumentWithStandardUuidId() {
        UUID orderId = UUID.randomUUID();
        OrderDocument document = OrderDocument.builder()
                .orderId(orderId)
                .amount(99.5)
                .userEmail("test@domain.com")
                .isProcessed(true)
                .build();

        Document bsonDoc = new Document();
        converter.write(document, bsonDoc);

        // Verify Spring Data MongoDB @Id maps to BSON _id field
        assertThat(bsonDoc.get("_id")).isEqualTo(orderId);
        assertThat(bsonDoc.getDouble("amount")).isEqualTo(99.5);
        assertThat(bsonDoc.getString("userEmail")).isEqualTo("test@domain.com");
        assertThat(bsonDoc.getBoolean("isProcessed")).isTrue();

        // Verify deserialization back to OrderDocument
        OrderDocument readBack = converter.read(OrderDocument.class, bsonDoc);
        assertThat(readBack).isNotNull();
        assertThat(readBack.getOrderId()).isEqualTo(orderId);
        assertThat(readBack.getAmount()).isEqualTo(99.5);
        assertThat(readBack.getUserEmail()).isEqualTo("test@domain.com");
        assertThat(readBack.isProcessed()).isTrue();
    }
}
