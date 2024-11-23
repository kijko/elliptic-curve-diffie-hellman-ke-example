package pl.edu.prz.kijko;

import java.util.List;
import java.util.ArrayList;
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
//        print(addPoints(new Point(6,11), new Point(10,2)).toString());
//        print(addPoints(new Point(10,2), new Point(6,11)).toString());
        //print(addPoints(new Point(6,11), new Point(6,11))); // 0 (1,12)
//        print(addPoints(new Point(6,11), new Point(1,12))); // 1 (8, 3)
        //print(addPoints(new Point(6,11), new Point(8,3))); //  2 (2,7)
//        print(addPoints(new Point(6,11), new Point(2,7))); //  3 (10,2)
        //print(addPoints(new Point(6,11), new Point(10,2))); // 4 (5,8)
//        print(addPoints(new Point(6,11), new Point(5,8))); //  5 (15,13)
        //print(addPoints(new Point(6,11), new Point(15,13))); //6 (12,16)
//        print(addPoints(new Point(6,11), new Point(12,16))); //7 (3,0)

        //Point x = multiplyPoint(8, new Point(6,11)); // 6,11 + 6,11 fails
        //if (x.equals(new Point(12, 16))) {
        //    print("OK!");
        //} else {
        //    print("FAIL: 8 x (6, 11) = (12, 16) != " + x.toString());
       // }



//        print(new Point(1, 1).equals(new Point(1, 1)));
  //      print(new Point(1, 2).equals(new Point(1, 1)));
        // ecdhExample();
        //
//        int x = 4935;
  //      int y = 17;
//        double z = (double) x / y;
    //    int z1 = x % y;
      //  print(z1);
      
        List<List<Long>> pks = new ArrayList<>();
        for (long i = 1; i <= p; i++) {
            for (long j = 1; j <= p; j++) {
                List<Long> pair = new ArrayList<>();
                pair.add(i);
                pair.add(j);
                pks.add(pair);
            }
        }

        for (List<Long> pair : pks) {
            print("---- TEST Alice private key: " + pair.get(0) + ", Bob private key: " + pair.get(1));
            ecdhExample(pair.get(0), pair.get(1));
        }
    }



    private static void ecdhExample(Long givenAlicePK, Long givenBobPK) {
        // Alice 

        // validation...

        long alicePrivateKey = givenAlicePK != null ? givenAlicePK : (long) randomInt(1, orderG - 1);
//        long alicePrivateKey = 8L;
        print("Alice private key: "+ alicePrivateKey);
        Point alicePublicKey = multiplyPoint(alicePrivateKey, G);
////        print("DEBUG: alicePubkey" + alicePublicKey.toString());

        if (alicePublicKey == INFINITY) {
            return;
        }
        
        // Bob 
        // validation...
        
        long bobPrivateKey = givenBobPK != null ? givenBobPK : (long) randomInt(1, orderG - 1);
        //long bobPrivateKey = 10L;
        print("Bob private key: "+ bobPrivateKey);
        Point bobPublicKey = multiplyPoint(bobPrivateKey, G);
////        print("DEBUG: bobPubkey" + bobPublicKey.toString());
        
        if (bobPublicKey == INFINITY) {
            print("INFINITY, aborting");
            return;
        }

        // Exchange

        // Alice
        Point aliceSecretPoint = multiplyPoint(alicePrivateKey, bobPublicKey);
//        print("DEBUG: aliceSecretPoint: " + aliceSecretPoint.toString());
        if (aliceSecretPoint == INFINITY) {
            print("INFINITY, aborting");
            return;
        }
        String aliceSecret = kdf(aliceSecretPoint.x);

        // Bob
        Point bobSecretPoint = multiplyPoint(bobPrivateKey, alicePublicKey);
//        print("DEBUG: bobSecretPoint: " + bobSecretPoint.toString());
        if (bobSecretPoint == INFINITY) {
            print("INFINITY, aborting");
            return;
        }
        String bobSecret = kdf(bobSecretPoint.x);

        // check

        if (!aliceSecret.equals(bobSecret))  {
            throw new RuntimeException("Wymiana kluczy nie powiodła się. Alice(" + aliceSecret + ")!=Bob(" + bobSecret + ")");
        }

        print("Alice secret: " + aliceSecret);
        print("Bob secret: " + bobSecret);

    }


//    HKDF (HMAC-based KDF)
  //  Recommended in RFC 5869.
    private static String kdf(long sharedPointX) {
        // todo
        return "kdf-" + sharedPointX;
    }

    // fails for 8, (6,11)
    private static Point multiplyPoint(long multiplier, Point point) {
        if (point == INFINITY) {
            return INFINITY;
        }

        Point result = addPoints(point, point); // 2 x point

        for (long i = 1; i <= (multiplier - 2); i++) {
            Point tempResult = addPoints(result, point);

            result = tempResult;
            //print(result.toString());
        }

//        if (result == INFINITY) {
  //          result = addPoints(result, point);
    ///    }

        return result;
    }

    private static Point addPoints(Point P, Point Q) {
        if (P == INFINITY) {
            return Q;
        }

        if (Q == INFINITY) {
            return P;
        }

        // todo domainParams.p
        long mod = p;
        if (P.equals(Q)) {

            Point negP = new Point(P);
            negP.y = adjustToMod(negP.y * (-1));

            if (negP.equals(P)) { // point of order 2, INF
               return INFINITY;
            }

            // todo domainParams.a
            long m;
            long mNumerator = adjustToMod(3 * (P.x * P.x) + a);
//            print("DEBUG: mNumerator=" + mNumerator);
            long mDenominator = adjustToMod(2 * P.y);
//            print("DEBUG: mDenominator=" + mDenominator);

            if (mDenominator == 0) {
                throw new RuntimeException("DIVIDE BY 0");
            }

            boolean needInverse = (mNumerator % mDenominator) != 0;

            if (needInverse) {
                long mDenominatorInverse = calcModInverse(mDenominator);
//                print("DEBUG: mDenominatorInverse=" + mDenominatorInverse);
                m = adjustToMod(mNumerator * mDenominatorInverse);
//                print("DEBUG: m=" + m);
            } else {
                m = adjustToMod(mNumerator / mDenominator);
            }

            Point sum = calcPointsSum(m, P, Q);
//            print("DEBUG sum=" +  sum.toString());
            return sum;
        } else {
            // punkty na tej samej prostej pionowej
            if (P.x == Q.x) {
                return INFINITY;
            }

            long m;
            long mNumerator = adjustToMod(Q.y - P.y);
//            print("DEBUG: mNumerator=" + mNumerator);
            long mDenominator = adjustToMod(Q.x - P.x);
//            print("DEBUG: mDenominator=" + mDenominator);

            boolean needInverse = (mNumerator % mDenominator) != 0;

            if (needInverse) {
                long mDenominatorInverse = calcModInverse(mDenominator);
//                print("DEBUG: mDenominatorInverse=" + mDenominatorInverse);
                m = adjustToMod(mNumerator * mDenominatorInverse);
//                print("DEBUG: m=" + m);
            } else {
                m = adjustToMod(mNumerator / mDenominator);
//                print("DEBUG: m=" + m);
            }

            Point sum = calcPointsSum(m, P, Q);
//            print("DEBUG sum=" + sum);
            return sum;
        }
    }

    private static long calcModInverse(long num) {
        // num and p are coprime, p is always prime
        // assert isPrime(p)
        long totient = totientForPrime(p);

        long inverse = adjustToMod(num * num);
        for (long i = 1; i <= (totient - 1) - 2; i++) {
            inverse = adjustToMod(inverse * num);
        }

        return inverse;
    }

    private static long totientForPrime(long num) {
        return num - 1;
    }

    private static long adjustToMod(long num) {
        long result = num % p;

        if (result < 0) {
            result += p;
        }

        return result;
    }

    private static Point calcPointsSum(long m, Point P, Point Q) {
            Point R = new Point();
            R.x = adjustToMod((long) (m * m - P.x - Q.x));
            R.y = adjustToMod((long) (m * (P.x - R.x) - P.y));

            return R;
    }


    private static int randomInt(int fromInclusive, int toInclusive) {
        return ThreadLocalRandom.current().nextInt(fromInclusive, toInclusive + 1);
    }

    public static void print(Object o) {
        System.out.println(o.toString());
    }
}

