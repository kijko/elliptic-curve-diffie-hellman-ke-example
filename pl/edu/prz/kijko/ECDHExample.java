package pl.edu.prz.kijko;

import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;

// todo komenty na PL
public class ECDHExample {
    private static final Point INFINITY = null;
    
    // parametry domenowe dla secp256k1
    private static final int a = 0;
    private static final int b = 7;
    private static final int p = 17;
    private static final Point G = new Point(6, 11);
    private static final int orderG = 18;
    private static final int countPoints = 18;
    private static final int cofactor = countPoints/orderG;


    public static void main(String[] args) {
        print(new Point(1, 1).equals(new Point(1, 1)));
        print(new Point(1, 2).equals(new Point(1, 1)));
        // ecdhExample();
    }



    private static void ecdhExample() {
        // Alice 

        // validation...

        int alicePrivateKey = randomInt(1, orderG);
        Point alicePublicKey = multiplyPoint(alicePrivateKey, G);
        
        // Bob 
        // validation...
        
        int bobPrivateKey = randomInt(1, orderG);
        Point bobPublicKey = multiplyPoint(bobPrivateKey, G);

        // Exchange

        // Alice
        Point aliceSecretPoint = multiplyPoint(alicePrivateKey, bobPublicKey);
        String aliceSecret = kdf(aliceSecretPoint.x);

        // Bob
        Point bobSecretPoint = multiplyPoint(bobPrivateKey, alicePublicKey);
        String bobSecret = kdf(bobSecretPoint.x);

        // check

        if (!aliceSecret.equals(bobSecret))  {
            throw new RuntimeException("Wymiana kluczy nie powiodła się. Alice(" + aliceSecret + ")!=Bob(" + bobSecret + ")");
        }

    }

    private static Point multiplyPoint(int multiplier, Point point) {
        if (point.equals(INFINITY)) {
            return INFINITY;
        }

        Point result = point;

        for (int i = 0; i < (multiplier - 1); i++) {
            result = addPoints(result, point);
        }

        if (result.equals(INFINITY)) {
            result = addPoints(result, point);
        }

        return result;
    }

    private static Point addPoint(Point P, Point Q) {
// todo
    }

    private static int randomInt(int fromInclusive, int toInclusive) {
        return ThreadLocalRandom.current().nextInt(fromInclusive, toInclusive + 1);
    }

    public static void print(Object o) {
        System.out.println(o.toString());
    }
}

