package org.example;

import java.lang.*;
import java.util.*;

public class AutoKey {

    private static final String alphabet = "aábcčdďeéěfghiíjklmnňoópqrřsštťuúůvwxyýzž";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String msg;
        String key;

        // Zadání otevřeného textu
        while (true) {
            System.out.print("Zadej text: ");
            msg = scanner.nextLine().toLowerCase();

            if (isValidInput(msg)) {
                break;
            } else {
                System.out.println("Nevhodný formát. Nepoužívejte prosím v textu speciální znaky.");
            }
        }

        // Zadání klíče
        while (true) {
            System.out.print("Zadej klíč (pouze jedno slovo): ");
            key = scanner.nextLine().toLowerCase();

            if (key.length() == 1 || isValidInput(key)) {
                break;
            } else {
                System.out.println("Nevhodný formát. Zadejte prosím jedno slovo bez speciálních znaků.");
            }
        }

        // Šifrování a dešifrování
        String enc = autoEncryption(msg, key);

        System.out.println("Otevřený text : " + msg);
        System.out.println("Zašifrovaný text : " + enc);
        System.out.println("Dešifrovaný text : " + autoDecryption(enc, key));

        scanner.close();
    }

    // Validace. Text musí patřit do abecedy a může obsahovat mezery.
    private static boolean isValidInput(String input) {
        for (char c : input.toCharArray()) {
            if (!alphabet.contains(String.valueOf(c)) && c != ' ') {
                return false;
            }
        }
        return true;
    }

    // Funkce pro šifrování textu pomocí autoklíče ze šifrovaného textu
    public static String autoEncryption(String msg, String key) {
        int len = msg.length();
        int numOfSpaces = 0;

        StringBuilder encryptMsg = new StringBuilder();

        for (int x = 0; x < len; x++) {
            char msgChar = msg.charAt(x);
            if (msgChar == ' ') {
                numOfSpaces++;
                encryptMsg.append(' ');  // Pokud narazí na mezeru pak jen předává, nešifruje
                continue;
            }

            int first = alphabet.indexOf(msgChar);
            int second = alphabet.indexOf(key.charAt(x - numOfSpaces % key.length()));  // Pouzici indexu klíče musíme snížit o počet mezer
            int total = (first + second) % alphabet.length();
            encryptMsg.append(alphabet.charAt(total));

            key = key.concat(String.valueOf(alphabet.charAt(total)));  // Rozšíření klíče o zašifrovaný znak
        }
        return encryptMsg.toString();
    }

    // Funkce pro dešifrování textu pomocí autoklíče z šifrovaného textu
    public static String autoDecryption(String msg, String key) {
        StringBuilder decryptMsg = new StringBuilder();
        int len = msg.length();
        int numOfSpaces = 0;

        // Skutečný klíč je složen z klíče + šifrovaného textu
        String currentKey = key + msg.replaceAll(" ", "");

        for (int x = 0; x < len; x++) {
            char msgChar = msg.charAt(x);
            if (msgChar == ' ') {
                numOfSpaces++;
                decryptMsg.append(' ');  // Přidání mezery bez dešifrování
                continue;
            }

            int first = alphabet.indexOf(msgChar);
            int second = alphabet.indexOf(currentKey.charAt(x - numOfSpaces)); // Pouzici indexu klíče musíme snížit o počet mezer
            int total = (first - second + alphabet.length()) % alphabet.length();
            decryptMsg.append(alphabet.charAt(total));
        }
        return decryptMsg.toString();
    }
}