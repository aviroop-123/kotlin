package witcher;

/** Java class that depends on Kotlin [Sign]. Direction: Java -> Kotlin */
public class Potion {
    private final String name;
    private final Sign enhancedSign;

    public Potion(String name, Sign enhancedSign) {
        this.name = name;
        this.enhancedSign = enhancedSign;
    }

    public String getName() { return name; }
    public Sign getEnhancedSign() { return enhancedSign; }

    public String describe() {
        return name + " enhances " + enhancedSign.getElement() + " power";
    }
}
