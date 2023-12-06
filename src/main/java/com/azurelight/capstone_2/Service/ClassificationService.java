package com.azurelight.capstone_2.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClassificationService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public String doClassification(final String path) {
        ProcessBuilder pb = new ProcessBuilder("python", "./src/main/resources/detector.py", path);
        pb.redirectErrorStream(true);
        Process p;
        String result = "";
        log.error("enterd service");
        try {
            p = pb.start();
            log.error("service start");
            try {
                int exitval = p.waitFor();
                log.error("exitvat : " + exitval);

            } catch (Exception e) {
                e.printStackTrace();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                result += line;
                log.error("in service" + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}