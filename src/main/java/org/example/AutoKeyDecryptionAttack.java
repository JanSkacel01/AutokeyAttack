package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Scanner;

public class AutoKeyDecryptionAttack {

    private static final String CZECH_ALPHABET = "aábcčdďeéěfghiíjklmnňoópqrřsštťuúůvwxyýzž";
    private static final String DICTIONARY_FILE = "./src/main/java/org/example/czechWords.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        //Zadání zašifrovaného textu a výpočet indexu koincidence
        String encryptedText = promptForEncryptedText(scanner);
        double ic = calculateIC(encryptedText);
        System.out.println("Index koincidence: " + ic);

        if (ic < 0.05) {
            //Polyalfabetická šifra
            System.out.println("Index koincidence odpovídá polyalfabetické šifře. Zahajuji útok...");
            executeAttack(encryptedText, scanner, encryptedText.contains(" "));
        } else {
            //Monoalfabetická šifra
            System.out.println("Index koincidence odpovídá monoalfabetické šifře, pro kterou není implementován útok.");
            if (promptYesNo(scanner, "Chcete zkusit útok aspoň na autoklíč z šifrovaného textu? (y/n): ")) {
                executeAttack(encryptedText, scanner, encryptedText.contains(" "));
            }
        }
    }

    //Spustí útok na základě toho jestli je text s mezerami
    private static void executeAttack(String encryptedText, Scanner scanner, boolean withSpaces) {
        System.out.println(withSpaces ? "Text obsahuje mezery, používám slovníkový útok." : "Text neobsahuje mezery, používám útok na základě IC.");
        if (withSpaces) {
            dictionaryAttackWithSpaces(encryptedText, scanner);
        } else {
            dictionaryAttackNoSpaces(encryptedText, scanner);
        }
    }

    //Načtení a validace šifrovaného textu
    private static String promptForEncryptedText(Scanner scanner) {
        while (true) {
            System.out.print("Zadejte šifrovaný text: ");
            String input = scanner.nextLine();
            if (isValidEncryptedText(input)) {
                return input;
            }
            System.out.println("Neplatný vstup! Text může obsahovat pouze znaky abecedy a mezery.");
        }
    }

    // Validace. Text musí patřit do abecedy a může obsahovat mezery.
    private static boolean isValidEncryptedText(String text) {
        for (char c : text.toCharArray()) {
            if (!CZECH_ALPHABET.contains(String.valueOf(c)) && c != ' ') {
                return false;
            }
        }
        return true;
    }

    /* Slovníkový útok pro text bet mezer. Útok zkouší všechny klíče ze slovníku. Pokud dešifrovaný text
     * má index koincidence větší než uživatele nastavený threshold, pak je vybráno jako kandidát na
     * úspěšný útok. Poté je ještě kandidát testován, zda obsahuje více nebo rovno slov ze slovníku.
     * Ze slovníku se vybírají pouze slova delší než 4 znaky.
     */
    private static void dictionaryAttackNoSpaces(String encryptedText, Scanner scanner) {
        //Input od uživatele (počet slov ze slovníku a prah indexu koincidence)
        double icThreshold = promptForDouble(scanner, "Zadejte práh indexu koincidence (doporučeno 0.05): ", 0, 1);
        int minMatchingSubstrings = promptForInt(scanner, "Zadejte minimální počet podřetězců, které se musí shodovat se slovníkem: ", 0);

        //Načtení slovníku
        List<String> dictionary = loadDictionary();
        if (dictionary == null) return;

        List<String> possibleDecryptedTexts = new ArrayList<>();
        List<String> possibleKeys = new ArrayList<>();

        //Zkoušení klíčů ze slovníku
        for (String possibleKey : dictionary) {
            System.out.println("Zkouším klíč: " + possibleKey);
            String decryptedText = decryptAutokey(encryptedText, possibleKey);

            //Vyhodnocení zda je vhodný kandidát na základě IC a zda obsahuje daný počet slov
            if (isPotentialDecryption(decryptedText, icThreshold, minMatchingSubstrings, dictionary)) {
                if (possibleDecryptedTexts.isEmpty()) {
                    //Zobrazení prvniho "úspěšného" vysledku
                    System.out.println("První vysledek: ");
                    System.out.println("Klíč: " + possibleKey);
                    System.out.println("Dešifrovaný text: " + decryptedText);
                    boolean answer = promptYesNo(scanner, "Chcete projít všechny možné výsledky a vyhodnotit nejlepší? (y/n): ");
                    //Ukončení pokud uživatel je spokojen
                    if (!answer) return;
                }
                possibleDecryptedTexts.add(decryptedText);
                possibleKeys.add(possibleKey);
            }
        }
        //Vypsání nejlepších výsledků (výsledky které začínají reálným slovem)
        displayResults(possibleDecryptedTexts, possibleKeys, dictionary, scanner);
    }

    /*
     * Slovníkový útok pro text s mezerami. Zde díky mezerám přesně víme dané slova v textu,
     * které se porovnávají se slovníkem. Díky tomuto mnohem snažšímu porovnávání umožňuji
     * uživateli také nastavit počet slov, které jsou v dešifrovaném textu, ale ne ve slovníku
     * Dále také jak moc se může slovo lišit od slova ve slovníku (kvůli skloňování)
     */
    private static void dictionaryAttackWithSpaces(String encryptedText, Scanner scanner) {
        double maxDifferenceRatio = promptForDouble(scanner, "Zadejte maximální odchylku od slova ve slovníku [0;1] (doporučuje se 0.1): ", 0, 1);
        int allowedDifferentWords = promptForInt(scanner, "Zadejte počet slov, které se nemusí shodovat se slovníkem: ", 0);

        List<String> dictionary = loadDictionary();
        if (dictionary == null) return;

        List<String> possibleDecryptedTexts = new ArrayList<>();
        List<String> bestKeys = new ArrayList<>();

        for (String possibleKey : dictionary) {
            System.out.println("Zkouším klíč: " + possibleKey);
            String decryptedText = decryptAutokey(encryptedText, possibleKey);
            if (isMeaningfulText(decryptedText, dictionary, maxDifferenceRatio, allowedDifferentWords)) {
                if (possibleDecryptedTexts.isEmpty()) {
                    System.out.println("První vysledek: ");
                    System.out.println("Klíč: " + possibleKey);
                    System.out.println("Dešifrovaný text: " + decryptedText);
                    boolean answer = promptYesNo(scanner, "Chcete projít všechny možné výsledky a vyhodnotit nejlepší? (y/n): ");
                    if (!answer) return;
                }
                possibleDecryptedTexts.add(decryptedText);
                bestKeys.add(possibleKey);
            }
        }
        displayResults(possibleDecryptedTexts, bestKeys, dictionary, scanner);
    }
    //Smyčka pro input (double) dokud není validní. Lze nastavit i rozmezí
    private static double promptForDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Hodnota musí být mezi " + min + " a " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Neplatný vstup! Zadejte desetinné číslo.");
            }
        }
    }

    //Smyčka pro input (int) dokud není validní. Lze nastavit i minimální hodnotu
    private static int promptForInt(Scanner scanner, String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value >= min) {
                    return value;
                }
                System.out.println("Hodnota musí být větší nebo rovna " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("Neplatný vstup! Zadejte celé číslo.");
            }
        }
    }

    //Smyčka pro input (boolean) dokud není validní.
    private static boolean promptYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y")) return true;
            if (response.equals("n")) return false;
            System.out.println("Neplatný vstup! Zadejte 'y' pro ano nebo 'n' pro ne.");
        }
    }

    //Náčítání slovníku. Vrací seznam slov
    private static List<String> loadDictionary() {
        try {
            return Files.readAllLines(Path.of(DICTIONARY_FILE)).stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Chyba při načítání slovníku.");
            return null;
        }
    }

    //Vyhodnocení zda je vhodný kandidát na základě IC a zda obsahuje daný počet slov
    private static boolean isPotentialDecryption(String decryptedText, double icThreshold, int minMatchingSubstrings, List<String> dictionary) {
        return calculateIC(decryptedText) > icThreshold && countMatchingSubstrings(decryptedText, dictionary) >= minMatchingSubstrings;
    }

    //Zobrazá nejlepší výsledky. Pokud uživatel chce tak se zobrazí i veškeré výsledky.

    private static void displayResults(List<String> possibleDecryptedTexts, List<String> bestKeys, List<String> dictionary, Scanner scanner) {
        List<String> bestResults = selectStartingWithDictionaryWord(possibleDecryptedTexts, dictionary);
        if (bestResults.isEmpty()) {
            System.out.println("Nebyla nalezena žádná dešifrovaná zpráva mezi nejlepšími výsledky.");
        } else {
            System.out.println("Nejlepší výsledky:");
            for (String result : bestResults) {
                String key = bestKeys.get(possibleDecryptedTexts.indexOf(result));
                System.out.println("Klíč: " + key + ", Dešifrovaný text: " + result);
            }
            if (promptYesNo(scanner, "Chcete zobrazit všechny nalezené výsledky? (y/n): ")) {
                for (int i = 0; i < possibleDecryptedTexts.size(); i++) {
                    System.out.println("Klíč: " + bestKeys.get(i) + ", Dešifrovaný text: " + possibleDecryptedTexts.get(i));
                }
            }
        }
    }

    /*
    Vrátí dešifrované zprávy, které mají na začátku validní slovo ze slovníku.
    Takto jsou vyhodnocovány nejlepší výsledky. Validní slovo ze slovníku musí být ale minimální délky 4
    a více. Pokud obsahuje dešifrované slovo mezery, porovnává se se slovníkem první slovo, které
    má délku větši rovno 4
     */
    private static List<String> selectStartingWithDictionaryWord(List<String> decryptedTexts, List<String> dictionary) {
        List<String> bestResults = new ArrayList<>();

        for (String text : decryptedTexts) {
            // Rozdělení textu na slova
            String[] words = text.split(" ");

            // Pokud text obsahuje pouze jedno slovo.
            if (words.length == 1) {
                for (String word : dictionary) {
                    //Pokud začíná na slovo ze slovníku o minimální délce 4
                    if (word.length() >= 4 && text.startsWith(word)) {
                        bestResults.add(text);
                        break;
                    }
                }
            }
            // Pokud text obsahuje více slov
            else {
                // Najdeme první slovo, které má délku alespoň 4 znaky
                String firstValidWord = null;
                for (String word : words) {
                    if (word.length() >= 4) {
                        firstValidWord = word;
                        break;
                    }
                }

                // Pokud bylo nalezeno slovo, které splňuje podmínku, porovnáme s slovníkem
                if (firstValidWord != null) {
                    for (String word : dictionary) {
                        if (firstValidWord.equals(word)) {
                            bestResults.add(text);
                            break; // Pokud text začíná na toto slovo, přidáme ho mezi nejlepší výsledky
                        }
                    }
                }
            }
        }

        return bestResults;
    }

    // Vrátí pocet všech výsledků, které mají někde v textu validní slovo ze slovníku
    private static int countMatchingSubstrings(String text, List<String> dictionary) {
        return (int) dictionary.stream()
                .filter(word -> word.length() >= 4) // Pouze delší slova než 4 znaky
                .filter(text::contains) // Kontrola, zda je některé slovo ve slovníku podřetězcem textu
                .count();
    }

    //Dešifruje autoklíč z šifrovaného textu pomocí klíče
    public static String decryptAutokey(String msg, String key) {
        String currentKey = key + msg.replaceAll(" ", "");
        String decryptMsg = "";
        int numOfSpaces = 0;

        for (int x = 0; x < msg.length(); x++) {
            char msgChar = msg.charAt(x);
            if (msgChar == ' ') {
                numOfSpaces++;
                decryptMsg += ' ';
                continue;
            }

            int first = CZECH_ALPHABET.indexOf(msgChar);
            int second = CZECH_ALPHABET.indexOf(currentKey.charAt(x - numOfSpaces));
            int total = (first - second + CZECH_ALPHABET.length()) % CZECH_ALPHABET.length();
            char decryptedChar = CZECH_ALPHABET.charAt(total);
            decryptMsg += decryptedChar;
        }
        return decryptMsg;
    }

    /*
    Vyhodnocuje zda je text s mezerami smysluplný.
    Porovnává slova ze slovníkem.
    Vyhodnocuje na základě zadané odchylky a počtu možných "chyb"
     */
    private static boolean isMeaningfulText(String text, List<String> dictionary, double maxDifferenceRatio, int allowedDifferentWords) {
        String[] words = text.split(" ");
        int meaningfulWordCount = 0;

        for (String word : words) {
            if (isSimilarToDictionaryWord(word, dictionary, maxDifferenceRatio)) {
                meaningfulWordCount++;
            }
        }

        return words.length - meaningfulWordCount <= allowedDifferentWords;
    }

    /* Prochází seznam slovníku a kontroluje, zda se dané slovo podobá nějakému slovu ve slovníku
       na základě maximální povolené odchylky mezi slovy. Pouze pro texty obsahující mezery.
     */
    private static boolean isSimilarToDictionaryWord(String word, List<String> dictionary, double maxDifferenceRatio) {
        for (String dictWord : dictionary) { // Iterace přes všechny slova ve slovníku
            if (isSimilar(word, dictWord, maxDifferenceRatio)) { // Pokud je slovo podobné slovu ve slovníku
                return true; // Pokud najdeme shodu, vracíme true
            }
        }
        return false; // Pokud nenajdeme žádné podobné slovo, vracíme false
    }

    /* Vyhodnocuje, zda je slovo (word) podobné slovu ve slovníku (dictWord) podle maximální povolené odchylky.
        Odchylky jsou posuzovány na základě délky slov a počtu rozdílů mezi odpovídajícími znaky.
        Pouze pro texty obsahující mezery.
     */
    private static boolean isSimilar(String word, String dictWord, double maxDifferenceRatio) {
        int lengthDifference = Math.abs(word.length() - dictWord.length()); // Výpočet rozdílu v délkách slov
        int maxAllowedDifferences = (int) Math.round(Math.min(word.length(), dictWord.length()) * maxDifferenceRatio); // Maximální povolená odchylka na základě poměru

        if (lengthDifference > maxAllowedDifferences) { // Pokud je rozdíl v délkách větší než povolená odchylka, slova nejsou podobná
            return false;
        }

        int differences = 0;
        for (int i = 0; i < Math.min(word.length(), dictWord.length()); i++) { // Porovnání jednotlivých znaků
            if (word.charAt(i) != dictWord.charAt(i)) { // Pokud se znaky liší
                differences++; // Zvyšujeme počet rozdílů
                if (differences > maxAllowedDifferences) { // Pokud počet rozdílů přesáhne povolený limit
                    return false; // Slova nejsou dostatečně podobná
                }
            }
        }

        differences += lengthDifference; // Započítáme rozdíl v délce slov

        return differences <= maxAllowedDifferences; // Pokud je celkový počet rozdílů menší než povolený limit, slova jsou podobná
    }

    //Výpočet indexu koincidence
    public static double calculateIC(String text) {
        text = text.replaceAll("[^A-Za-z]", "").toUpperCase();
        int n = text.length();

        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }

        double ic = 0.0;
        for (int freq : frequencies.values()) {
            ic += freq * (freq - 1);
        }
        ic /= n * (n - 1);

        return ic;
    }

}