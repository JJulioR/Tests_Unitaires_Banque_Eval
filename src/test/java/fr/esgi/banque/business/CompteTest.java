package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.DisplayName;


/**
 * Test d'exemple fourni pour vérifier que le projet est bien configuré.
 * À compléter (partie A).
 */
class CompteTest {

	@Test
	void shouldIncreaseBalanceWhenCrediting() {
		// Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		// Act
		compte.crediter(new BigDecimal("50.00"));

		// Assert
		assertThat(compte.getSolde()).isEqualByComparingTo("150.00");
	}

	@Test 
	@DisplayName("C1: Numéro est obligatoire sinon refusé")
	void testNumeroNull(){
		//arrange
		String numero = null;
		String titulaire = "Jean";
		BigDecimal solde = new BigDecimal("100.00");
		BigDecimal decouvert = new BigDecimal("0");

		Throwable erreur = catchThrowable(() -> {
			new Compte(numero, titulaire, solde, decouvert);
		});

		assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le numéro de compte est obligatoire");
	}

	@Test 
    @DisplayName("C1: Titulaire vide est refusé")
	void testTitulaireVide(){
		//arrange
		String numero = "FR001";
		String titulaire = "";
		BigDecimal solde = new BigDecimal("100.00");
		BigDecimal decouvert = new BigDecimal("0");

		Throwable erreur = catchThrowable(() -> {
			new Compte(numero, titulaire, solde, decouvert);
		});

		assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le titulaire du compte est obligatoire");
	}

}
