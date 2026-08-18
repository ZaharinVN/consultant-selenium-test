package utils;

import io.qameta.allure.Allure;

public final class AllureReporter {

    private AllureReporter() {
    }

    public static void attachText(String name, String text) {
        Allure.addAttachment(
                name,
                "text/plain",
                text,
                ".txt"
        );
    }
}