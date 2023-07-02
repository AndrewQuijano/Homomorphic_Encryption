package security.socialistmillionaire;

import security.dgk.DGKOperations;
import security.misc.HomomorphicException;
import security.misc.NTL;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;

public class bob_joye extends bob_veugen {
    public bob_joye(KeyPair a, KeyPair b, KeyPair c) throws IllegalArgumentException {
        super(a, b, c);
    }

    public boolean Protocol1(BigInteger y) throws IOException, HomomorphicException, ClassNotFoundException {
        // Step 1 by Bob
        int delta_b;
        int delta_b_prime;
        int t = y.bitLength();
        BigInteger powT = TWO.pow(t);
        BigInteger nu = NTL.generateXBitRandom(t);
        BigInteger little_y_star = y.add(nu).mod(powT);
        BigInteger big_y_star = y.add(nu).divide(powT);
        toAlice.writeObject(little_y_star);
        toAlice.flush();

        // Step 2 by Alice
        Object o = fromAlice.readObject();
        BigInteger little_z_star;
        BigInteger y_prime;
        if (o instanceof BigInteger) {
            little_z_star = (BigInteger) o;
        }
        else {
            throw new HomomorphicException("Invalid Object in Step 2 Bob: " + o.getClass().getName());
        }
        y_prime = little_z_star.subtract(nu).mod(powT);
        BigInteger big_y_prime = little_z_star.subtract(nu).divide(powT);

        // Use Figure 1 comparison
        delta_b_prime = Protocol0(y_prime);

        // Step 3 Alice

        // Step 4 Bob
        if (big_y_star.add(big_y_prime).mod(TWO).equals(BigInteger.ZERO)) {
            delta_b = delta_b_prime;
        }
        else {
            delta_b = delta_b_prime ^ 1;
        }
        // Step 5, Extra: get delta_A XOR delta_B
        toAlice.writeInt(delta_b);
        toAlice.flush();

        o = fromAlice.readObject();
        if (o instanceof BigInteger) {
            return DGKOperations.decrypt((BigInteger) o, dgk_private) == 1;
        }
        else {
            throw new HomomorphicException("Invalid Object: " + o.getClass().getName());
        }
    }

    private int Protocol0(BigInteger y)
            throws IOException, ClassNotFoundException, IllegalArgumentException, HomomorphicException {
        // Constraint...
        if(y.bitLength() > dgk_public.getL()) {
            throw new IllegalArgumentException("Constraint violated: 0 <= x, y < 2^l, y is: " + y.bitLength() + " bits");
        }

        Object in;
        int deltaB = 0;
        BigInteger [] C;
        BigInteger temp;

        //Step 1: Bob sends encrypted bits to Alice
        BigInteger [] EncY = new BigInteger[y.bitLength()];
        for (int i = 0; i < y.bitLength(); i++) {
            EncY[i] = DGKOperations.encrypt(NTL.bit(y, i), dgk_public);
        }
        toAlice.writeObject(EncY);
        toAlice.flush();

        // Step 2: Alice...
        // Step 3: Alice...
        // Step 4: Alice...
        // Step 5: Alice...

        // Step 6: Check if one of the numbers in C_i is decrypted to 0.
        in = fromAlice.readObject();
        if(in instanceof BigInteger[]) {
            C = (BigInteger []) in;
        }
        else if (in instanceof BigInteger) {
            temp = (BigInteger) in;
            if (temp.equals(BigInteger.ONE)) {
                // x <= y is true, so I need delta_a XOR delta_b == 1
                return 1;
            }
            else if (temp.equals(BigInteger.ZERO)) {
                // x <= y is true, so I need delta_a XOR delta_b == 1
                return 0;
            }
            else {
                throw new IllegalArgumentException("This shouldn't be possible, value is + " + temp);
            }
        }
        else {
            throw new IllegalArgumentException("Protocol 1, Step 6: Invalid object: " + in.getClass().getName());
        }

        for (BigInteger C_i: C) {
            if (DGKOperations.decrypt(C_i, dgk_private) == 0) {
                deltaB = 1;
                break;
            }
        }
        return deltaB;
    }
}
