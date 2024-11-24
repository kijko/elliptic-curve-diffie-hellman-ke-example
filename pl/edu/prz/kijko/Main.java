package pl.edu.prz.kijko;

import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        boolean runTest = false;
        if (args.length > 0) {
            runTest = args[0].equalsIgnoreCase("test");
        }

        if (runTest) {
            performTest();
        } else {
            new ECDHExample().run();
        }

    }

    public static void performTest() {
        List<List<Long>> privKeyPairs = new ArrayList<>();
        for (long i = 1; i <= ECDHExample.DomainParameters.p; i++) {
            for (long j = 1; j <= ECDHExample.DomainParameters.p; j++) {
                List<Long> pair = new ArrayList<>();
                pair.add(i);
                pair.add(j);
                privKeyPairs.add(pair);
            }
        }

        for (List<Long> pair : privKeyPairs) {
            System.out.println("---- TEST Alice private key: " + pair.get(0) + ", Bob private key: " + pair.get(1));

            ECDHExample example = new ECDHExample(
                    new ECDHExample.PrivateKeyGenerator() {
                        @Override public long get(long fromInclusive, long toInclusive) {
                            return pair.get(0);
                        }
                    },
                    new ECDHExample.PrivateKeyGenerator() {
                        @Override public long get(long fromInclusive, long toInclusive) {
                            return pair.get(1);
                        }
                    }
            );

            example.run();
        }
    }

}

