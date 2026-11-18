package fr.esgi.banque.service.impl;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import fr.esgi.banque.business.Compte;
import fr.esgi.banque.business.Virement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Clock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(MockitoExtension.class)
class VirementsServiceImplTest {

    @Mock
    private CompteRepository repository;

    @Mock
    private NotificationService notifications;


   private final java.time.Clock horloge = java.time.Clock.fixed(
            java.time.Instant.parse("2026-11-18T10:00:00Z"),
            java.time.ZoneId.of("UTC"));

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        virementsService = new VirementsServiceImpl(repository, notifications, horloge);
    }

    private VirementsServiceImpl virementsService;

    @Test
    @DisplayName("V1 : Un virement avec un montant null est refusé")
    void testV1MontantNull() {
        // act
        Throwable erreur = catchThrowable(() -> {
            virementsService.virer("FR1", "FR2", null);
        });

        // assert
        assertThat(erreur).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-10.00"})
    @DisplayName("V1 : Un virement avec un montant nul ou négatif est refusé")
    void testV1MontantNulOuNegatif(String montantInvalide) {
        // act
        Throwable erreur = catchThrowable(() -> {
            virementsService.virer("FR1", "FR2", new BigDecimal(montantInvalide));
        });

        // assert
        assertThat(erreur).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("V2 : Un virement vers le même compte est refusé")
    void testV2MemeCompteRefuse() {
        // ct
        Throwable erreur = catchThrowable(() -> {
            virementsService.virer("FR1", "FR1", new BigDecimal("100.00"));
        });

        // assert
        assertThat(erreur).isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    @DisplayName ("V3: Un virement est refusé si le compte source n'existe pas")
    void CompteSourceInexistant() {
        // arrange

        String numeroSource = "FR_INCONNU";
        String numeroDestination = "FR2";
        BigDecimal montant = new BigDecimal("100.00");

        when(repository.trouver(numeroSource)).thenReturn(java.util.Optional.empty());

        // act
        Throwable erreur = catchThrowable(() -> {
            virementsService.virer(numeroSource, numeroDestination, montant);
        });

        // assert
        assertThat(erreur).isInstanceOf(fr.esgi.banque.exception.CompteIntrouvableException.class)
                 .hasMessageContaining("L'un des comptes est introuvable");
    }

    @Test 
    @DisplayName ("V4 Un virement est refusé si le solde du compte source est insuffisant")
    void CompteSourceSoldeInsuffisant() {
        // arrange
        String numeroSource = "FR1";
        String numeroDestination = "FR2";
        BigDecimal montant = new BigDecimal("100.00");

        fr.esgi.banque.business.Compte compteSource = new fr.esgi.banque.business.Compte(
                numeroSource, "Bob", new BigDecimal("50.00"), BigDecimal.ZERO);
        fr.esgi.banque.business.Compte compteDest = new fr.esgi.banque.business.Compte(
                numeroDestination, "Alice", new BigDecimal("100.00"), BigDecimal.ZERO);

        when(repository.trouver(numeroSource)).thenReturn(java.util.Optional.of(compteSource));
        when(repository.trouver(numeroDestination)).thenReturn(java.util.Optional.of(compteDest));

        // Act
        Throwable erreur = catchThrowable(() -> {
            virementsService.virer(numeroSource, numeroDestination, montant);
        });

        // Assert
        assertThat(erreur).isInstanceOf(fr.esgi.banque.business.SoldeInsuffisantException.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never())
                .sauvegarder(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("V5 : Un virement valide met à jour les soldes, sauvegarde les comptes et notifie")
    void VirementReussi() {
        // arrange
        String numeroSource = "FR1";
        String numeroDestination = "FR2";
        BigDecimal montant = new BigDecimal("30.00");

        Compte compteSource = new Compte(numeroSource, "Bob", new BigDecimal("100.00"), BigDecimal.ZERO);
        Compte compteDest = new Compte(numeroDestination, "Alice", new BigDecimal("50.00"), BigDecimal.ZERO);

        when(repository.trouver(numeroSource)).thenReturn(java.util.Optional.of(compteSource));
        when(repository.trouver(numeroDestination)).thenReturn(java.util.Optional.of(compteDest));

        // Act
        Virement resultat = virementsService.virer(numeroSource, numeroDestination, montant);

        // Assert (État)
        assertThat(resultat).isNotNull();
        assertThat(resultat.numeroSource()).isEqualTo(numeroSource);
        assertThat(resultat.numeroDestination()).isEqualTo(numeroDestination);
        assertThat(resultat.montant()).isEqualByComparingTo("30.00");
        assertThat(resultat.date()).isEqualTo(java.time.LocalDate.now(horloge));
        assertThat(compteSource.getSolde()).isEqualByComparingTo("70.00");
        assertThat(compteDest.getSolde()).isEqualByComparingTo("80.00");

        // Assert (Comportement)
        verify(repository).sauvegarder(compteSource);
        verify(repository).sauvegarder(compteDest);
    }

    @Test
    @DisplayName("V6 : Un virement strictement supérieur à 10 000 € déclenche une alerte au titulaire source")
    void AlerteSiMontantSuperieurA10000() {
        // Arrange
        String numeroSource = "FR1";
        String numeroDestination = "FR2";
        BigDecimal montant = new BigDecimal("10000.01");

        Compte compteSource = new Compte(numeroSource, "Bob", new BigDecimal("20000.00"), BigDecimal.ZERO);
        Compte compteDest = new Compte(numeroDestination, "Alice", new BigDecimal("100.00"), BigDecimal.ZERO);

        when(repository.trouver(numeroSource)).thenReturn(java.util.Optional.of(compteSource));
        when(repository.trouver(numeroDestination)).thenReturn(java.util.Optional.of(compteDest));

        // Act
        virementsService.virer(numeroSource, numeroDestination, montant);

        // Assert
        org.mockito.Mockito.verify(notifications).alerter(
                org.mockito.ArgumentMatchers.eq("Bob"),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("V6 : Un virement de 10000 € exactement ne déclenche pas d'alerte")
    void PasDAlerteSiMontantEgalA10000() {
        // aerange
        String numeroSource = "FR1";
        String numeroDestination = "FR2";
        BigDecimal montant = new BigDecimal("10000.00");

        Compte compteSource = new Compte(numeroSource, "Bob", new BigDecimal("20000.00"), BigDecimal.ZERO);
        Compte compteDest = new Compte(numeroDestination, "Alice", new BigDecimal("100.00"), BigDecimal.ZERO);

        when(repository.trouver(numeroSource)).thenReturn(java.util.Optional.of(compteSource));
        when(repository.trouver(numeroDestination)).thenReturn(java.util.Optional.of(compteDest));

        // act
        virementsService.virer(numeroSource, numeroDestination, montant);

        // assert
        org.mockito.Mockito.verify(notifications, org.mockito.Mockito.never())
                .alerter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("V7 : Aucun virement n'est accepté le dimanche et aucun compte n'est sauvegardé")
    void VirementRefuseLeDimanche() {
        // arrange
        java.time.Clock horlogeDimanche = java.time.Clock.fixed(
                java.time.Instant.parse("2026-11-15T10:00:00Z"), // 15 nov. 2026 = dimanche
                java.time.ZoneId.of("UTC"));
        VirementsServiceImpl serviceDimanche = new VirementsServiceImpl(repository, notifications, horlogeDimanche);

        String numeroSource = "FR1";
        String numeroDestination = "FR2";
        BigDecimal montant = new BigDecimal("50.00");

        // act
        Throwable erreur = catchThrowable(() -> {
            serviceDimanche.virer(numeroSource, numeroDestination, montant);
        });

        // assert
        assertThat(erreur).isInstanceOf(fr.esgi.banque.exception.VirementRefuseException.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never())
                .sauvegarder(org.mockito.ArgumentMatchers.any());
    }


}