package romatattoo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import romatattoo.entities.UserTienda;
import romatattoo.repositories.UserTiendaRepository;

import java.util.Optional;

@Service
public class UserTiendaService {

    private final UserTiendaRepository userTiendaRepository;

    @Autowired
    public UserTiendaService(UserTiendaRepository userRepository) {
        this.userTiendaRepository = userRepository;
    }

    public boolean existsByEmail(String email) {
        return userTiendaRepository.existsByEmail(email);
    }

    public Optional<UserTienda> obtenerUserTiendaByEmail(String email) {
        return userTiendaRepository.findUserByEmail(email);
    }

    public void save(UserTienda userTienda) {
        userTiendaRepository.save(userTienda);
    }
}
