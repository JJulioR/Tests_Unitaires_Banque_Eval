package fr.esgi.banque.service.impl;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
}