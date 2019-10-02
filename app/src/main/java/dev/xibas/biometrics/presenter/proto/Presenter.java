package dev.xibas.biometrics.presenter.proto;

public interface Presenter<V> {

    void attachView(V view);

    void detachView(V view);

}
