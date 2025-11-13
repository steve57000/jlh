package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.modeles.Promotion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class PromotionMapperTest {

    @Autowired
    private PromotionMapper mapper;

    @Test
    void testUrlCompleteWithRelativePath() {
        Promotion promo = new Promotion();
        promo.setIdPromotion(1);
        promo.setImageUrl("promo123.jpg");

        PromotionResponse resp = mapper.toResponse(promo);
        assertThat(resp.getImageUrl())
                .isEqualTo("http://localhost:80/promotions/images/promo123.jpg"); // adapte au profil actif
    }

    @Test
    void testUrlAlreadyAbsolute() {
        Promotion promo = new Promotion();
        promo.setImageUrl("http://img.server.com/promo.jpg");
        PromotionResponse resp = mapper.toResponse(promo);

        assertThat(resp.getImageUrl()).isEqualTo("http://img.server.com/promo.jpg");
    }

    @Test
    void testUrlWithLeadingSlash() {
        Promotion promo = new Promotion();
        promo.setImageUrl("/promo-slash.jpg");
        PromotionResponse resp = mapper.toResponse(promo);

        assertThat(resp.getImageUrl())
                .isEqualTo("http://localhost:80/promotions/images/promo-slash.jpg"); // adapte à ta base-url
    }

    @Test
    void testUrlNull() {
        Promotion promo = new Promotion();
        promo.setImageUrl(null);
        PromotionResponse resp = mapper.toResponse(promo);

        assertThat(resp.getImageUrl()).isNull();
    }
}
