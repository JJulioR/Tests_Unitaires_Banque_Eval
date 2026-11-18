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

        // act
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
        // arrange
        CalculFrais calcul = new CalculFrais();
        // act
        Throwable erreur = catchThrowable(() -> {
            calcul.fraisMensuels(28, null, false);
        });

        // Assert
        assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le solde moyen est obligatoire");
    }

    @Test 
    @DisplayName("F2: Les frais de tenue par défaut sont de 2.00 euros")
    void testFraisTenueParDefaut() {
        // arrange
        CalculFrais calcul = new CalculFrais();
        // Aact
        BigDecimal frais = calcul.fraisMensuels(28, BigDecimal.valueOf(1000.0), false);
        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @ParameterizedTest 
    @ValueSource (ints = {18, 25})
    @DisplayName ("F3 : Les moins de 26 ans ne payent pas de frais de tenue")
    void FraisOffertsMoins26ans(int ageJeune){
        // arrange

        CalculFrais calcul = new CalculFrais();
        // act

        BigDecimal frais = calcul.fraisMensuels(ageJeune, BigDecimal.valueOf(1000.0), false);
        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(0.0));
    }

    @Test 
    @DisplayName("F3: Un titulair de 26ans et  plus paie les frais de tenue")
    void FraisTenue26ansEtPlus(){
        // arrange
        CalculFrais calcul = new CalculFrais();
        // act
        BigDecimal frais = calcul.fraisMensuels(26, BigDecimal.valueOf(1000.0), false);
        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @ParameterizedTest 
    @ValueSource (strings = {"1000.00", "1500.00"})
    @DisplayName ("F4 : Les frais de tenue sont offerts si le solde moyen atteint 1000 euros ou plus")
    void FraisOffertsSoldeMoyenElevé(String solde) {
        // arrange
        CalculFrais calcul = new CalculFrais();
        // act
        BigDecimal frais = calcul.fraisMensuels(28, new BigDecimal(solde), false);

        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(0.0));
    }

    @Test
    @DisplayName ("F4 : Les frais de tenue sont dus si le solde moyen est inférieur à 1000 euros")
    void FraisTenueSoldeMoyenFaible(){
        // arrange
        CalculFrais calcul = new CalculFrais();
        // act
        BigDecimal frais = calcul.fraisMensuels(28, new BigDecimal("69.67"), false);

        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @Test 
    @DisplayName ("F5 : Supplément premium (5€) s'ajoute aux frais de tenue")
    void SupplementPremiumAvecFraisTenue(){
        // arrange
        CalculFrais calcul = new CalculFrais();

        // act
        BigDecimal frais = calcul.fraisMensuels(28, new BigDecimal("67.67"), true);

        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(7.0));
    }

    @Test 
    @DisplayName ("F5 : Supplément premium (5€) est dû même si les frais de tenue sont offerts (age)")
    void SupplementPremiumAvecFraisTenueOffertsAge(){
        // arrange
        CalculFrais calcul = new CalculFrais();

        // act
        BigDecimal frais = calcul.fraisMensuels(23, new BigDecimal("100.00"), true);

        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(5.0));
    }

    @Test
    @DisplayName ("F5 : Supplément premium (5€) est dû même si les frais de tenue sont offerts (solde moyen)")
    void SupplementPremiumAvecFraisTenueOffertsSoldeMoyen(){
        // arrange
        CalculFrais calcul = new CalculFrais();

        // act
        BigDecimal frais = calcul.fraisMensuels(98, new BigDecimal("1100.00"), true);

        // assert
        assertThat(frais).isEqualByComparingTo(BigDecimal.valueOf(5.0));
    }
}