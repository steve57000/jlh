package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service("userService")
@Primary
public class UserService implements UserDetailsService {
    private final AdministrateurRepository adminRepo;
    private final ClientRepository clientRepo;

    public UserService(AdministrateurRepository a, ClientRepository c){
        this.adminRepo  = a;
        this.clientRepo = c;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return adminRepo.findByEmail(email)
                .map(admin -> {
                    var level = admin.getNiveauAcces();
                    if (level == com.jlh.jlhautopambackend.modeles.NiveauAccesAdministrateur.GESTIONNAIRE) {
                        return User.withUsername(admin.getEmail())
                                .password(admin.getMotDePasse())
                                .roles("MANAGER")
                                .build();
                    }
                    if (level == com.jlh.jlhautopambackend.modeles.NiveauAccesAdministrateur.PRINCIPAL) {
                        return User.withUsername(admin.getEmail())
                                .password(admin.getMotDePasse())
                                .roles("ADMIN", "ADMIN_PRINCIPAL")
                                .build();
                    }
                    return User.withUsername(admin.getEmail())
                            .password(admin.getMotDePasse())
                            .roles("ADMIN")
                            .build();
                })
                .or(() -> clientRepo.findByEmailIgnoreCase(email)
                        .map(cli -> User.withUsername(cli.getEmail())
                                .password(cli.getMotDePasse())
                                .roles("CLIENT")
                                .build()))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur inconnu : " + email));
    }

    public String getFirstnameFromEmail(String email) {
        if (email == null) return null;

        return adminRepo.findByEmail(email)
                .map(a -> a.getPrenom())
                .or(() -> clientRepo.findByEmailIgnoreCase(email).map(c -> c.getPrenom()))
                .orElse(email); // fallback : on renvoie l’email si on trouve personne
    }

}
