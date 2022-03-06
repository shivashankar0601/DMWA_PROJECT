package Utilities;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;


public class Utils {

    public static String delemiter = "~";

    public static String std_path = "./resources/";

    public static String hashWithMD5(String user_name) {
        try {
            BigInteger hashed_number = new BigInteger(1, ((MessageDigest.getInstance("MD5")).digest(user_name.getBytes())));
            String final_value = hashed_number.toString(16);
            while (final_value.length() < 32) {
                final_value = "0" + final_value;
            }
            return final_value;
        }
        catch (Exception e) {
            //System.out.println(e.getLocalizedMessage());;
            e.printStackTrace();
        }
        return null;
    }

    public static void createUserDirectory(String path, String directory_name){
        try {
            File f = new File(path + directory_name);
            f.mkdir();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}
