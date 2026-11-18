package fr.esgi.banque.service.impl;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import fr.esgi.banque.business.Compte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
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

    @Mock
    private Clock horloge;

    @InjectMocks
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


}