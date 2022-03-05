import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static String delemiter = "~";

    public static String hashWithMD5(String username)
    {
        try {
            BigInteger hashedNumber = new BigInteger(1, ((MessageDigest.getInstance("MD5")).digest(username.getBytes())));
            String finalValue = hashedNumber.toString(16);
            while (finalValue.length() < 32) {
                finalValue = "0" + finalValue;
            }
            return finalValue;
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println(e.getLocalizedMessage());;
        }
        return null;
    }
}
