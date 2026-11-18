package fr.esgi.banque.business;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class CalculFraisTest {


       // j'combine @ParameterizedTest avec @ValueSource pour effectuer le meme test avec plusieurs valeurs d'entrée. (vu sur doc.junit.org)

    @ParameterizedTest
    @ValueSource(ints = {17, 5, 0})
    @DisplayName("F1: Age strictement inférieur à 18 ans est refusé")
    void testAgeMineurRefuse(int ageMineur) {
        // arrange
        CalculFrais calcul = new CalculFrais();

        // Act
        Throwable erreur = catchThrowable(() -> {
            calcul.fraisMensuels(ageMineur, BigDecimal.ZERO,false);
        });

        // assert
        assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le titulaire doit être majeur");
    }

    @Test
    @DisplayName("F1: Le solde moyen est obligatoire")
    void testSoldeMoyenNullRefuse() {
        // Arrange
        CalculFrais calcul = new CalculFrais();
        // Act
        Throwable erreur = catchThrowable(() -> {
            calcul.fraisMensuels(28, null, false);
        });

        // Assert
        assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le solde moyen est obligatoire");
    }
}