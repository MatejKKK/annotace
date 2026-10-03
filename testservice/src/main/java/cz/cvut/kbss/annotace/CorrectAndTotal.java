package cz.cvut.kbss.annotace;

public record CorrectAndTotal(long correct, long total) {
    public double rate() {return (double) correct / total;}
    public long incorrect() {return total - correct;}
}