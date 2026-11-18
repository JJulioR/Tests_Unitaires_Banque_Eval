package fr.esgi.banque.service.impl;
import fr.esgi.banque.service.impl.VirementsServiceImpl;
import fr.esgi.banque.business.Virement;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import java.math.BigDecimal;
import java.time.Clock;

public class VirementsServiceImpl{
   private final CompteRepository repository;
   private final NotificationService notifications;
   private final Clock horloge;

    public VirementsServiceImpl(CompteRepository repository, NotificationService notifications, Clock horloge) {
     this.repository = repository;
     this.notifications = notifications;
     this.horloge = horloge;
}

    public Virement virer(String numeroSource, String numeroDestination, BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Le montant du virement doit être strictement positif");
        }
        if (numeroSource != null && numeroSource.equals(numeroDestination)) {
             throw new IllegalArgumentException("Virement vers le même compte impossible");
        }

        var compteSourceOpt = repository.trouver(numeroSource);
        var compteDestOpt = repository.trouver(numeroDestination);

        if (compteSourceOpt.isEmpty() || compteDestOpt.isEmpty()) {
            throw new fr.esgi.banque.exception.CompteIntrouvableException("L'un des comptes est introuvable");
        }

        fr.esgi.banque.business.Compte compteSource = compteSourceOpt.get();
        fr.esgi.banque.business.Compte compteDest = compteDestOpt.get();

        if (compteSource.getSolde().compareTo(montant) < 0) {
            throw new fr.esgi.banque.business.SoldeInsuffisantException(numeroSource, montant);
        }

        return null;
    }
}