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
				.hasMessageContaining("Le titulaire est obligatoire");
	}

	@Test 
	@DisplayName("C2: Solde initial et le découvert autorisé sont obligatoires")
	void testsoldeInitialNullDecouvertAutoriseNull(){
		//arrange
		String numero = "FR001";
		String titulaire = "Jean";
		BigDecimal solde = null;
		BigDecimal decouvert = null;

		Throwable erreurSoldNull = catchThrowable(() -> {
			new Compte(numero, titulaire, solde, decouvert);
		});

		assertThat(erreurSoldNull).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le solde initial et le découvert autorisé sont obligatoires");

		Throwable erreurDecouvertNull = catchThrowable(() -> {
			new Compte(numero, titulaire, new BigDecimal("100.00"), decouvert);
		});

		assertThat(erreurDecouvertNull).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le solde initial et le découvert autorisé sont obligatoires");
	}

	@Test 
	@DisplayName("C2: Le découvert autorisé doit être positif ou nul")
	void testDecouvertAutoriseNegatif(){
		//arrange
		String numero = "FR001";
		String titulaire = "Jean";
		BigDecimal solde = new BigDecimal("100.00");
		BigDecimal decouvert = new BigDecimal("-10.00");

		Throwable erreur = catchThrowable(() -> {
			new Compte(numero, titulaire, solde, decouvert);
		});

		assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le découvert autorisé doit être positif ou nul");
	}

	
	@Test 
	@DisplayName("C3 : créditer n'accepter que des montants positifs")
	void testCrediterMontantZero(){
		//arrange
		Compte compte = new Compte("FR001", "Jean", new BigDecimal("100.00"), new BigDecimal("0"));
		BigDecimal montantZero = new BigDecimal("0");

		Throwable erreur = catchThrowable(() -> {
			compte.crediter(montantZero);
		});

		assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le montant à créditer doit être strictement positif");
	}

	@Test 
	@DisplayName ("C4 : débiter 0 est refusé")
	void testDebiterMontantZero(){
		//arrange
		Compte compte = new Compte("FR001", "Jean", new BigDecimal("100.00"), new BigDecimal("0"));
		BigDecimal montantZero = new BigDecimal("0");

		Throwable erreur = catchThrowable(() -> {
			compte.debiter(montantZero);
		});

		assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le montant à débiter doit être strictement positif");
	}

	@Test 
	@DisplayName ("C4 : débiter un montant négatif est refusé")
	void testDebiterMontantNegatif(){
		//arrange
		Compte compte = new Compte("FR001", "Jean", new BigDecimal("100.00"), new BigDecimal("0"));
		BigDecimal montantNegatif = new BigDecimal("-10.00");

		Throwable erreur = catchThrowable(() -> {
			compte.debiter(montantNegatif);
		});

		assertThat(erreur).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Le montant à débiter doit être strictement positif");
	}

	@Test 
	@DisplayName ("C5 : Débiter un montant supérieur au solde + découvert autorisé est refusé")
	void testDebiterMontantSuperieurSoldeDecouvertAutorise(){
		//arrange
		Compte compte = new Compte("FR001", "Jean", new BigDecimal("100.00"), new BigDecimal("50.00"));
		BigDecimal montantSuperieur = new BigDecimal("200.00");

		Throwable erreur = catchThrowable(() -> {
			compte.debiter(montantSuperieur);
		});

		assertThat(erreur).isInstanceOf(SoldeInsuffisantException.class);
	}

	@Test 
	@DisplayName("C5 : débite jusqu'à la limite du découvert autorisé est accepté")
	void testDebiterMontantEgaleSoldeDecouvertAutorise(){
		//arrange
		Compte compte = new Compte("FR001", "Jean", new BigDecimal("100.00"), new BigDecimal("50.00"));
		BigDecimal montantEgale = new BigDecimal("150.00");

		//act
		compte.debiter(montantEgale);

		//assert
		assertThat(compte.getSolde()).isEqualByComparingTo("-50.00");
	}



}