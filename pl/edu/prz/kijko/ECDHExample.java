package pl.edu.prz.kijko;

import java.util.concurrent.ThreadLocalRandom;

public class ECDHExample {

	private final PrivateKeyGenerator alicePrivKeyGen;
	private final PrivateKeyGenerator bobPrivKeyGen;

	public static class DomainParameters {
		// secp256k1
		public static final long a = 0;
		public static final long b = 7;
		public static final long p = 17;
		public static final Point G = new Point(6, 11);
		public static final long orderG = 18;
		public static final long countPoints = 18;
		public static final int cofactor = (int) (countPoints/orderG);
	}

	public static class PrivateKeyGenerator {
		public long get(long fromInclusive, long toInclusive) {
			return ThreadLocalRandom.current().nextLong(fromInclusive, toInclusive + 1);
		}
	}

	public ECDHExample(PrivateKeyGenerator alicePrivKeyGen, PrivateKeyGenerator bobPrivKeyGen) {
		this.alicePrivKeyGen = alicePrivKeyGen;
		this.bobPrivKeyGen = bobPrivKeyGen;
	}

	public ECDHExample() {
		this.alicePrivKeyGen = new PrivateKeyGenerator();
		this.bobPrivKeyGen = new PrivateKeyGenerator();
	}

	public void run() {
		// Tutaj powinno nastąpić sprawdzenie poprawności parametrów domenowych

		// Losowanie klucza prywatnego dA Alicji. Liczba całkowita z przedziału <1, n - 1>
		long alicePrivateKey = alicePrivKeyGen.get(1L, DomainParameters.orderG - 1);

		// Obliczenie klucza publicznego Qa Alicji dA x G
		Point alicePublicKey = multiplyPoint(alicePrivateKey, DomainParameters.G);

		// Losowanie klucza prywatnego dB Boba. Liczba całkowita z przedziału <1, n - 1>
		long bobPrivateKey = bobPrivKeyGen.get(1, DomainParameters.orderG - 1);

		// Obliczenie klucza publicznego Qb Boba dB x G
		Point bobPublicKey = multiplyPoint(bobPrivateKey, DomainParameters.G);

		// Alicja otrzymuje klucz publiczny Qb od Boba
		// Przed użyciem powinna go sprawdzić (np. czy punkt ten w ogóle leży na krzywej)
		// Oblicza sekretny punkt S = dA x Qb
		Point aliceSecretPoint = multiplyPoint(alicePrivateKey, bobPublicKey);

		if (aliceSecretPoint.isInfinity()) { // Możemy otrzymać punkt w nieskończoności. Powinniśmy wtedy rozpocząć komunikację na nowo
			System.out.println("Niepomyślne parametry. " + 
				"Sekretny punkt obliczony przez Alice jest punktem w nieskończoności. " + 
				"Spróbuj ponownie");

			return;
		}

		// Otrzymany sekret nie spełnia warunków dobrego klucza szyfrującego
		// Używamy więc KDF - Key Derivation Function
		String aliceSecret = kdf(aliceSecretPoint.x);


		// Bob otrzymuje klucz publiczny Qa od Alicji
		// Przed użyciem powinien go sprawdzić (np. czy punkt ten leży na krzywej)
		// Oblicza sekretny punkt S = dB x Qa
		Point bobSecretPoint = multiplyPoint(bobPrivateKey, alicePublicKey);

		if (bobSecretPoint.isInfinity()) {  // Możemy otrzymać punkt w nieskończoności. Powinniśmy wtedy rozpocząć komunikację na nowo
			System.out.println("Niepomyślne parametry. " + 
				"Sekretny punkt obliczony przez Boba jest punktem w nieskończoności. " + 
				"Spróbuj ponownie");
			return;
		}

		// Otrzymany sekret nie spełnia warunków dobrego klucza szyfrującego
		// Używamy więc KDF - Key Derivation Function
		String bobSecret = kdf(bobSecretPoint.x);

		if (!aliceSecret.equals(bobSecret))  {
			throw new RuntimeException("Wymiana kluczy nie powiodła się. " + 
				"Sekret Alicji=" + aliceSecret + " != Sekret Boba=" + bobSecret + "\n" + 
				"Najprawdopodobniej jest to błąd w implementacji lub niepoprawne parametry domenowe");
		} else {
			System.out.println("Sekret Alicji=" + aliceSecret + " == " + "Sekret Boba=" + bobSecret);
			System.out.println("OK!");
		}
	}

	private Point multiplyPoint(long multiplier, Point point) {
		if (point.isInfinity()) {
			return Point.INFINITY;
		}

		Point result = addPoints(point, point);

		for (long i = 1; i <= (multiplier - 2); i++) {
			result = addPoints(result, point);
		}

		return result;
	}

	private Point addPoints(Point P, Point Q) {
		if (P.isInfinity()) {
			return Q;
		}

		if (Q.isInfinity()) {
			return P;
		}

		long mod = DomainParameters.p;
		if (P.equals(Q)) { // Operacja podwojenia punktu

			Point negP = new Point(P);
			negP.y = adjustToMod(negP.y * (-1));

			if (negP.equals(P)) { // Rząd podwajanego punktu = 2, więc P + P = INFINITY
				return Point.INFINITY;
			}

			long mNumerator = adjustToMod(3 * (P.x * P.x) + DomainParameters.a);
			long mDenominator = adjustToMod(2 * P.y);

			if (mDenominator == 0) {
				throw new RuntimeException("Mianownik lambdy = 0. Nie powinno się to zdarzyć. Sprawdź kod");
			}

			long mDenominatorInverse = calcModInverse(mDenominator);
			long lambda = adjustToMod(mNumerator * mDenominatorInverse);

			return calcPointsSum(lambda, P, Q);
		} else { // Dodawanie punktów
			if (P.x == Q.x) { // punkty na tej samej prostej pionowej
				return Point.INFINITY;
			}

			long mNumerator = adjustToMod(Q.y - P.y);
			long mDenominator = adjustToMod(Q.x - P.x);

			long mDenominatorInverse = calcModInverse(mDenominator);
			long lambda = adjustToMod(mNumerator * mDenominatorInverse);

			return calcPointsSum(lambda, P, Q);
		}
	}

	// Metoda zakłada, że "num" i "DomainParameters.p" są względnie pierwsze
	private long calcModInverse(long num) {
		long totient = totientForPrime(DomainParameters.p);

		long inverse = adjustToMod(num * num);
		for (long i = 1; i <= (totient - 1) - 2; i++) {
			inverse = adjustToMod(inverse * num);
		}

		return inverse;
	}

	// Metoda zakłada "num" jako liczbę pierwszą
	private long totientForPrime(long num) {
		return num - 1;
	}

	private long adjustToMod(long num) {
		long result = num % DomainParameters.p;

		if (result < 0) {
			result += DomainParameters.p;
		}

		return result;
	}

	private Point calcPointsSum(long lambda, Point P, Point Q) {
		long Rx = adjustToMod((long) (lambda * lambda - P.x - Q.x));
		long Ry = adjustToMod((long) (lambda * (P.x - Rx) - P.y));

		return new Point(Rx, Ry);
	}

	// Metoda imituje KDF (Key Derivation Function) do pozyskania klucza szyfrującego
	// z sekretnej liczby uzgodnionej przez obie strony poprzez ECDH
	// 
	// W rzeczywistej implementacji kryptosystemu możnaby się tutaj spodziewać np.
	// implementacji HKDF (HMAC-based KDF) rekomendowanej w RFC5869
	private String kdf(long sharedPointX) {
		return "kdf(" + sharedPointX + ")";
	}

}

