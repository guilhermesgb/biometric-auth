package dev.xibas.biometrics.presenter;

public interface Presenter<V> {

    void attachView(V view);

    void detachView(V view);

}
