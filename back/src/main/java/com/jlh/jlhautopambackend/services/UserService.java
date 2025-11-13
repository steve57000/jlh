package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
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
                .map(admin -> User.withUsername(admin.getEmail())
                        .password(admin.getMotDePasse())
                        .roles("ADMIN")
                        .build())
                .or(() -> clientRepo.findByEmail(email)
                        .map(cli -> User.withUsername(cli.getEmail())
                                .password(cli.getMotDePasse())
                                .roles("CLIENT")
                                .build()))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur inconnu : " + email));
    }
}
