package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministrateurDetailsService implements UserDetailsService {

    private final AdministrateurRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Administrateur admin = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin introuvable"));
        var level = admin.getNiveauAcces();
        if (level == com.jlh.jlhautopambackend.modeles.NiveauAccesAdministrateur.GESTIONNAIRE) {
            return new User(
                    admin.getUsername(),
                    admin.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
            );
        }
        if (level == com.jlh.jlhautopambackend.modeles.NiveauAccesAdministrateur.PRINCIPAL) {
            return new User(
                    admin.getUsername(),
                    admin.getMotDePasse(),
                    List.of(
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_ADMIN_PRINCIPAL")
                    )
            );
        }
        return new User(
                admin.getUsername(),
                admin.getMotDePasse(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
