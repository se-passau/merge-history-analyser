package util;

public class Pair<S,T> {
    S fst;
    T scd;

    public Pair (S fst, T scd) {
        setFst(fst);
        setScd(scd);

    }

    public void setScd(T scd) {
        this.scd = scd;
    }

    public S getFst() {
        return fst;
    }

    public void setFst(S fst) {
        this.fst = fst;
    }
}