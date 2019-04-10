package se.hannes.max.domain;

public class Utbildningsinstans {

    private short version;
    private String kod;
    private String benämningSv;
    private String benämningEn;

    public static Utbildningsinstans enUtbildningsinstans() {
        return new Utbildningsinstans();
    }

    public short version() {
        return version;
    }

    public Utbildningsinstans setVersion(short version) {
        this.version = version;
        return this;
    }

    public String kod() {
        return kod;
    }

    public Utbildningsinstans setKod(String kod) {
        this.kod = kod;
        return this;
    }

    public String benämningSv() {
        return benämningSv;
    }

    public Utbildningsinstans setBenämningSv(String benämningSv) {
        this.benämningSv = benämningSv;
        return this;
    }

    public String benämningEn() {
        return benämningEn;
    }

    public Utbildningsinstans setBenämningEn(String benämningEn) {
        this.benämningEn = benämningEn;
        return this;
    }

    @Override
    public String toString() {
        return "Versions=" + version + "|" +
                "Kod=" + kod + "|" +
                "BenämningSv=" + benämningSv + "|" +
                "BenämningEn=" + benämningEn;
    }
}
