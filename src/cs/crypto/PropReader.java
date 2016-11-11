package cs.crypto;

import java.io.*;
import java.util.Properties;

/**
 * Created by edavis on 11/10/16.
 */
public class PropReader {
    public static Properties read(String file) {
        InputStream input = null;
        Properties props = new Properties();

        try {
            input = ClassLoader.getSystemResourceAsStream(file);

            if (input == null) {
                String workDir = System.getProperty("user.dir");
                file = String.format("%s/src/resources/%s", workDir, file);

                if (new File(file).exists()) {
                    input = new FileInputStream(file);
                } else {
                    throw new FileNotFoundException("Unable to read file '" + file + "'.");
                }
            }

            props.load(input);
            input.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return props;
    }

    public static void write(java.util.Properties props, String file) {
        OutputStream output = null;

        try {
            output = new FileOutputStream(file);
            props.store(output, null);
            output.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
