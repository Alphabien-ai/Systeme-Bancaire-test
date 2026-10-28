#!/usr/bin/env bash
# ============================================================
#  Lancer le Systeme Bancaire ASJA (Linux / macOS)
#  Prerequis : JDK 11 ou plus (javac et java dans le PATH)
#  Usage : ./lancer.sh            (ou : bash lancer.sh)
# ============================================================
cd "$(dirname "$0")" || exit 1
echo "Compilation du projet..."
rm -rf bin && mkdir -p bin
if ! javac -encoding UTF-8 -d bin -sourcepath src src/Lancer.java; then
    echo "ERREUR de compilation. Verifiez le JDK : javac -version"
    exit 1
fi
echo "Lancement de l'application..."
java -Dfile.encoding=UTF-8 -cp bin Lancer "$@"
