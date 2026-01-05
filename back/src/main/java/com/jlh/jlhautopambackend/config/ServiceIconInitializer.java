package com.jlh.jlhautopambackend.config;

import com.jlh.jlhautopambackend.modeles.ServiceIcon;
import com.jlh.jlhautopambackend.repository.ServiceIconRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceIconInitializer implements ApplicationRunner {
    private static final List<ServiceIcon> DEFAULT_ICONS = List.of(
            ServiceIcon.builder().url("assets/icons/picto-metier-pneu.png").label("Pneumatiques").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-hybride.png").label("Véhicules hybrides").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-geometrie.png").label("Géométrie").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-freinage.png").label("Freinage").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-embrayage.png").label("Embrayage").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-echappement.png").label("Échappement").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-distribution.png").label("Distribution").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-climatisation.png").label("Climatisation").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-amortisseur.png").label("Amortisseurs").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-pre_controle.png").label("Pré-contrôle technique").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-revision_constructeur.png").label("Révision constructeur").build(),
            ServiceIcon.builder().url("assets/icons/picto-metier-vidange.png").label("Vidange").build()
    );

    private final ServiceIconRepository repo;

    public ServiceIconInitializer(ServiceIconRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (ServiceIcon icon : DEFAULT_ICONS) {
            repo.findByUrl(icon.getUrl()).orElseGet(() -> repo.save(icon));
        }
    }
}
