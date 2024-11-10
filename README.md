# Autokey Attack

Tento projekt je implementace šifrování a dešifrování pomocí autoklíčové šifry a slovníkového útoku na tuto šifru. Kromě šifrování a dešifrování umožňuje projekt útok na zašifrovaný text s využitím indexu koincidence a českého slovníku pro detekci smysluplných výsledků.

## Struktura Projektu

- `src/main/java/org.example/AutoKey.java`: Třída pro šifrování a dešifrování pomocí autoklíčové šifry.
- `src/main/java/org.example/AutoKeyDecryptionAttack.java`: Třída pro útok na autoklíčovou šifru pomocí slovníkového útoku.
- `src/main/java/org.example/czechWords.txt`: Soubor obsahující seznam českých slov, který je využíván pro slovníkový útok.

V hlavní složce projektu je také složka `jar`, která obsahuje dvě spustitelné `.jar` soubory:
- `AutoKey.jar`: Pro šifrování a dešifrování pomocí autoklíčové šifry.
- `AutoKeyDecryptionAttack.jar`: Pro provedení slovníkového útoku na zašifrovaný text.

## Jak Projekt Spustit

K spuštění `.jar` souborů potřebujete mít nainstalovanou Javu (Java Runtime Environment, JRE). Můžete použít příkazovou řádku, a to následovně:

1. Pro šifrování a dešifrování:
   ```bash
   java -jar path/to/AutoKey.jar
   
2. Pro útok na šifru:
   ```bash
   java -jar path/to/AutoKeyDecryptionAttack.jar

# Poznámka k Diakritice na Windows

Při spuštění na příkazové řádce Windows mohou nastat problémy při zadávání vstupu s diakritikou, protože Windows používá odlišné formátování, které může být nekompatibilní. Pokud se setkáte s těmito problémy, zkuste zadávat text bez diakritiky nebo použít alternativní prostředí (například PowerShell nebo Git Bash).

# Závislosti

- Java Runtime Environment (JRE)
