package com.banking.utils;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;

public class ThemeStyles {
    private static boolean isDarkMode = false;

    public static void applyTheme(boolean dark) {
        isDarkMode = dark;
        try {

            FlatLaf.registerCustomDefaultsSource("com.banking.themes");

            if (dark) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }


            FlatLaf.updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleTheme() {
        applyTheme(!isDarkMode);
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}
