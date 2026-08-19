package witcher;

/** Java class that depends on Kotlin [Witcher]. Direction: Java -> Kotlin */
public class WitcherSchool {
    public static Witcher createWhiteWolf() {
        return new Witcher("Geralt", Sign.AARD);
    }

    public static String prepareForHunt(Witcher witcher, Potion potion) {
        return witcher.drinkPotion(potion) + " — ready for the hunt!";
    }
}
