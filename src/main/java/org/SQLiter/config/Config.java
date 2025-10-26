package org.SQLiter.config;

import org.SQLiter.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {private static Properties properties = new Properties();

    static {
        try(InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties")){
            if(input == null){
                IO.println("Unable to find application.properties in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getProperty(String name){
        return properties.getProperty(name);
    }
}
