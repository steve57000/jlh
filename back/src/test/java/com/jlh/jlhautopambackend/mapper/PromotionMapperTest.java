package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Promotion;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromotionMapperTest {

    @Test
    void toResponse_shouldUseImagesDirectory_whenBaseUrlDoesNotContainIt() throws Exception {
        PromotionMapper mapper = mapperWithBaseUrl("http://localhost:80/promotions");
        Promotion entity = samplePromotion("/promotions/images/sample.pdf");

        PromotionResponse response = mapper.toResponse(entity);

        assertEquals("http://localhost:80/promotions/images/sample.pdf", response.getImageUrl());
    }

    @Test
    void toResponse_shouldKeepDirectory_whenBaseUrlAlreadyTargetsIt() throws Exception {
        PromotionMapper mapper = mapperWithBaseUrl("http://localhost:80/promotions/images/");
        Promotion entity = samplePromotion("promotions/images/sample.pdf");

        PromotionResponse response = mapper.toResponse(entity);

        assertEquals("http://localhost:80/promotions/images/sample.pdf", response.getImageUrl());
    }

    @Test
    void toResponse_shouldReturnExternalUrl_asIs() throws Exception {
        PromotionMapper mapper = mapperWithBaseUrl("http://localhost:80/promotions/images/");
        Promotion entity = samplePromotion("https://cdn.example.com/img.pdf");

        PromotionResponse response = mapper.toResponse(entity);

        assertEquals("https://cdn.example.com/img.pdf", response.getImageUrl());
    }

    private PromotionMapper mapperWithBaseUrl(String baseUrl) throws Exception {
        PromotionMapper mapper = Mappers.getMapper(PromotionMapper.class);
        Field field = PromotionMapper.class.getDeclaredField("imagesBaseUrl");
        field.setAccessible(true);
        field.set(mapper, baseUrl);
        return mapper;
    }

    private Promotion samplePromotion(String imageUrl) {
        return Promotion.builder()
                .idPromotion(42)
                .administrateur(Administrateur.builder().idAdmin(7).build())
                .imageUrl(imageUrl)
                .validFrom(Instant.parse("2025-01-01T00:00:00Z"))
                .validTo(Instant.parse("2025-12-31T23:59:59Z"))
                .description("Promo test")
                .build();
    }
}
