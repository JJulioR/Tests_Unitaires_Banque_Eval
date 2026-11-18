package fr.esgi.banque.service.impl;
import fr.esgi.banque.business.Virement;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import fr.esgi.banque.business.Compte;

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

        Compte compteSource = compteSourceOpt.get();
        Compte compteDest = compteDestOpt.get();

        compteSource.debiter(montant);
        compteDest.crediter(montant);

        // V5 : sauvegarde 2 comptes
        repository.sauvegarder(compteSource);
        repository.sauvegarder(compteDest);

        // V5: création du record Virement avec l'horloge 
        Virement virement = new Virement(numeroSource, numeroDestination, montant, LocalDate.now(horloge));

        // Notif
        if (montant.compareTo(new BigDecimal("10000")) > 0) {
            notifications.alerter(compteSource.getTitulaire(),
                    "Virement important de " + montant + " € vers " + numeroDestination);
        }

        return virement;
    }
}