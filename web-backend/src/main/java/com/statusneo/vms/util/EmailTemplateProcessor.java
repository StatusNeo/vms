package com.statusneo.vms.util;

import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class EmailTemplateProcessor {
    public String loadTemplate(String templateName, Map<String, String> placeholders) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/" + templateName)) {
            if (inputStream == null) throw new FileNotFoundException("Template not found: " + templateName);
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return template;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load email template", e);
        }
    }
}
